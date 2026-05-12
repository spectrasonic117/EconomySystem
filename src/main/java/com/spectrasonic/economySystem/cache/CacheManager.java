package com.spectrasonic.economySystem.cache;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.database.DatabaseManager;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class CacheManager {

    private final Main plugin;
    private final DatabaseManager databaseManager;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong flushCount = new AtomicLong(0);

    private final ConcurrentLinkedQueue<String> dirtyQueue = new ConcurrentLinkedQueue<>();
    private final Set<String> queuedKeys = ConcurrentHashMap.newKeySet();

    public CacheManager(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public Main getPlugin() {
        return plugin;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public double getBalance(String uuid) {
        CacheEntry entry = cache.get(uuid);
        if (entry != null) {
            hitCount.incrementAndGet();
            entry.updateAccessTime();
            return entry.getBalance();
        }

        missCount.incrementAndGet();
        return loadFromDatabase(uuid);
    }

    public void setBalance(String uuid, double balance) {
        CacheEntry entry = cache.get(uuid);
        if (entry != null) {
            entry.setBalance(balance);
        } else {
            entry = new CacheEntry(uuid, balance);
            cache.put(uuid, entry);
        }
        markDirty(uuid);
    }

    public void addBalance(String uuid, double amount) {
        CacheEntry entry = cache.get(uuid);
        if (entry != null) {
            entry.addBalance(amount);
        } else {
            double loadedBalance = databaseManager.getBalance(uuid);
            entry = new CacheEntry(uuid, loadedBalance);
            entry.addBalance(amount);
            cache.put(uuid, entry);
        }
        markDirty(uuid);
    }

    public void removeBalance(String uuid, double amount) {
        CacheEntry entry = cache.get(uuid);
        if (entry != null) {
            entry.removeBalance(amount);
        } else {
            double loadedBalance = databaseManager.getBalance(uuid);
            entry = new CacheEntry(uuid, loadedBalance);
            entry.removeBalance(amount);
            cache.put(uuid, entry);
        }
        markDirty(uuid);
    }

    public void ensureAccount(String uuid) {
        if (cache.containsKey(uuid)) return;
        databaseManager.createAccount(uuid);
        loadFromDatabase(uuid);
    }

    public void createAccount(String uuid) {
        if (cache.containsKey(uuid)) return;
        databaseManager.createAccount(uuid);
        loadFromDatabase(uuid);
    }

    public boolean accountExists(String uuid) {
        if (cache.containsKey(uuid)) {
            return true;
        }

        boolean exists = databaseManager.accountExists(uuid);
        if (exists) {
            loadFromDatabase(uuid);
        }

        return exists;
    }

    public void createTransaction(String uuidFrom, String uuidTo, double amount) {
        databaseManager.createTransaction(uuidFrom, uuidTo, amount);
    }

    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> topBalances = databaseManager.getTopBalances(limit);

        for (Map.Entry<String, Double> entry : topBalances.entrySet()) {
            CacheEntry cacheEntry = cache.get(entry.getKey());
            if (cacheEntry != null && cacheEntry.isDirty()) {
                entry.setValue(cacheEntry.getBalance());
            } else {
                CacheEntry newEntry = new CacheEntry(entry.getKey(), entry.getValue());
                newEntry.markClean();
                cache.put(entry.getKey(), newEntry);
            }
        }

        return topBalances;
    }

    public Map<String, CacheEntry> getDirtyEntries() {
        Map<String, CacheEntry> dirtyEntries = new LinkedHashMap<>();
        cache.forEach((uuid, entry) -> {
            if (entry.isDirty()) {
                dirtyEntries.put(uuid, entry);
            }
        });

        return dirtyEntries;
    }

    public CompletableFuture<Void> flushDirtyEntries() {
        flushAllSync();
        return CompletableFuture.completedFuture(null);
    }

    public int flushBatch(int maxBatchSize) {
        Map<String, Double> batch = pollDirtyBatch(maxBatchSize);
        if (batch.isEmpty()) return 0;

        databaseManager.batchUpdateBalance(batch);
        markBatchClean(batch.keySet());

        return batch.size();
    }

    public void flushAllSync() {
        Map<String, Double> batch = new LinkedHashMap<>();
        cache.forEach((uuid, entry) -> {
            if (entry.isDirty()) {
                batch.put(uuid, entry.getBalance());
            }
        });

        if (batch.isEmpty()) return;

        long startTime = System.currentTimeMillis();

        databaseManager.batchUpdateBalance(batch);

        batch.keySet().forEach(uuid -> {
            CacheEntry entry = cache.get(uuid);
            if (entry != null) entry.markClean();
        });

        dirtyQueue.clear();
        queuedKeys.clear();

        long duration = System.currentTimeMillis() - startTime;
        flushCount.incrementAndGet();

        plugin.getLogger().info(String.format(
            "Cache flush completed: %d entries in %dms (Total flushes: %d)",
            batch.size(),
            duration,
            flushCount.get()
        ));
    }

    public void evictEntry(String uuid) {
        CacheEntry entry = cache.remove(uuid);
        queuedKeys.remove(uuid);
        if (entry != null && entry.isDirty()) {
            databaseManager.setBalance(uuid, entry.getBalance());
        }
    }

    public void clearCache() {
        flushAllSync();
        cache.clear();
        dirtyQueue.clear();
        queuedKeys.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }

    public int getDirtyCount() {
        int count = 0;
        for (CacheEntry entry : cache.values()) {
            if (entry.isDirty()) {
                count++;
            }
        }

        return count;
    }

    public int getQueuedDirtyCount() {
        return dirtyQueue.size();
    }

    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        if (total == 0) {
            return 0.0;
        }

        return (double) hitCount.get() / total * 100;
    }

    private void markDirty(String uuid) {
        if (queuedKeys.add(uuid)) {
            dirtyQueue.add(uuid);
        }
    }

    private Map<String, Double> pollDirtyBatch(int maxBatchSize) {
        Map<String, Double> batch = new LinkedHashMap<>();
        String uuid;

        while ((uuid = dirtyQueue.poll()) != null) {
            queuedKeys.remove(uuid);
            CacheEntry entry = cache.get(uuid);
            if (entry != null && entry.isDirty()) {
                batch.put(uuid, entry.getBalance());
                if (batch.size() >= maxBatchSize) break;
            }
        }

        return batch;
    }

    private void markBatchClean(Set<String> uuids) {
        for (String uuid : uuids) {
            CacheEntry entry = cache.get(uuid);
            if (entry != null) {
                entry.markClean();
            }
        }
    }

    private double loadFromDatabase(String uuid) {
        double balance = databaseManager.getBalance(uuid);
        CacheEntry entry = new CacheEntry(uuid, balance);
        cache.put(uuid, entry);
        return balance;
    }
}

package com.spectrasonic.economySystem.cache;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.database.DatabaseManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CacheManager {

    private final Main plugin;
    private final DatabaseManager databaseManager;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong flushCount = new AtomicLong(0);

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
        CacheEntry entry = cache.computeIfAbsent(uuid, k -> {
            double loadedBalance = databaseManager.getBalance(uuid);
            return new CacheEntry(uuid, loadedBalance);
        });

        entry.setBalance(balance);
    }

    public void addBalance(String uuid, double amount) {
        CacheEntry entry = cache.computeIfAbsent(uuid, k -> {
            double loadedBalance = databaseManager.getBalance(uuid);
            return new CacheEntry(uuid, loadedBalance);
        });

        entry.addBalance(amount);
    }

    public void removeBalance(String uuid, double amount) {
        CacheEntry entry = cache.computeIfAbsent(uuid, k -> {
            double loadedBalance = databaseManager.getBalance(uuid);
            return new CacheEntry(uuid, loadedBalance);
        });

        entry.removeBalance(amount);
    }

    public void createAccount(String uuid) {
        if (!databaseManager.accountExists(uuid)) {
            databaseManager.createAccount(uuid);
        }

        CacheEntry entry = new CacheEntry(uuid, databaseManager.getBalance(uuid));
        cache.put(uuid, entry);
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
        flushDirtyEntries();

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
        Map<String, CacheEntry> dirtyEntries = getDirtyEntries();

        if (dirtyEntries.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            for (Map.Entry<String, CacheEntry> entry : dirtyEntries.entrySet()) {
                String uuid = entry.getKey();
                CacheEntry cacheEntry = entry.getValue();

                databaseManager.setBalance(uuid, cacheEntry.getBalance());
                cacheEntry.markClean();
            }

            long duration = System.currentTimeMillis() - startTime;
            flushCount.incrementAndGet();

            plugin.getLogger().info(String.format(
                "Cache flush completed: %d entries in %dms (Total flushes: %d)",
                dirtyEntries.size(),
                duration,
                flushCount.get()
            ));
        });
    }

    public void flushAllSync() {
        Map<String, CacheEntry> dirtyEntries = getDirtyEntries();

        if (dirtyEntries.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        for (Map.Entry<String, CacheEntry> entry : dirtyEntries.entrySet()) {
            String uuid = entry.getKey();
            CacheEntry cacheEntry = entry.getValue();

            databaseManager.setBalance(uuid, cacheEntry.getBalance());
            cacheEntry.markClean();
        }

        long duration = System.currentTimeMillis() - startTime;
        plugin.getLogger().info(String.format(
            "Synchronous cache flush completed: %d entries in %dms",
            dirtyEntries.size(),
            duration
        ));
    }

    public void evictEntry(String uuid) {
        CacheEntry entry = cache.remove(uuid);
        if (entry != null && entry.isDirty()) {
            databaseManager.setBalance(uuid, entry.getBalance());
        }
    }

    public void clearCache() {
        flushAllSync();
        cache.clear();
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

    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        if (total == 0) {
            return 0.0;
        }

        return (double) hitCount.get() / total * 100;
    }

    private double loadFromDatabase(String uuid) {
        double balance = databaseManager.getBalance(uuid);
        CacheEntry entry = new CacheEntry(uuid, balance);
        cache.put(uuid, entry);
        return balance;
    }
}

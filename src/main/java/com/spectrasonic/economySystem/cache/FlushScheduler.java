package com.spectrasonic.economySystem.cache;

import com.spectrasonic.economySystem.Main;

public class FlushScheduler {

    private final Main plugin;
    private final CacheManager cacheManager;
    private int taskId = -1;

    public FlushScheduler(Main plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    public Main getPlugin() {
        return plugin;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void start() {
        boolean cacheEnabled = plugin.getConfig().getBoolean("cache.enabled", true);

        if (!cacheEnabled) {
            plugin.getLogger().info("Cache is disabled. Flush scheduler will not start.");
            return;
        }

        long flushIntervalTicks = plugin.getConfig().getLong("cache.flush-interval", 5) * 20L;

        taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin,
            this::performFlush,
            flushIntervalTicks,
            flushIntervalTicks
        ).getTaskId();

        plugin.getLogger().info(String.format(
            "Adaptive flush scheduler started with interval: %d seconds",
            flushIntervalTicks / 20
        ));
    }

    public void stop() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
            plugin.getLogger().info("Flush scheduler stopped.");
        }
    }

    private void performFlush() {
        int maxBatchSize = plugin.getConfig().getInt("cache.max-batch-size", 50);
        int maxBurstsPerCycle = plugin.getConfig().getInt("cache.max-bursts-per-cycle", 10);
        int burstDelayMs = plugin.getConfig().getInt("cache.burst-delay-ms", 100);

        int totalFlushed = 0;
        long startTime = System.currentTimeMillis();

        for (int burst = 0; burst < maxBurstsPerCycle; burst++) {
            int flushed = cacheManager.flushBatch(maxBatchSize);
            totalFlushed += flushed;

            if (flushed == 0) break;
            if (flushed < maxBatchSize) break;

            if (burstDelayMs > 0 && burst < maxBurstsPerCycle - 1) {
                try {
                    Thread.sleep(burstDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (totalFlushed > 0) {
            long duration = System.currentTimeMillis() - startTime;
            plugin.getLogger().info(String.format(
                "Aggressive flush: %d entries in %dms (%d bursts, queue remaining: %d)",
                totalFlushed,
                duration,
                Math.min((totalFlushed + maxBatchSize - 1) / maxBatchSize, maxBurstsPerCycle),
                cacheManager.getQueuedDirtyCount()
            ));
        }
    }
}

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
            "Flush scheduler started with interval: %d seconds",
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
        int dirtyCount = cacheManager.getDirtyCount();

        if (dirtyCount == 0) {
            return;
        }

        if (dirtyCount <= maxBatchSize) {
            cacheManager.flushDirtyEntries().whenComplete((result, ex) -> {
                if (ex != null) {
                    plugin.getLogger().severe("Error during cache flush: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        } else {
            plugin.getLogger().info(String.format(
                "Dirty entries (%d) exceed max batch size (%d). Flushing in next cycle.",
                dirtyCount,
                maxBatchSize
            ));
        }
    }
}

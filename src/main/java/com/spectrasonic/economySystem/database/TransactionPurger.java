package com.spectrasonic.economySystem.database;

import com.spectrasonic.economySystem.Main;

public class TransactionPurger {

    private final Main plugin;
    private final DatabaseManager db;
    private final int retentionDays;
    private int taskId = -1;

    public TransactionPurger(Main plugin, DatabaseManager db, int retentionDays) {
        this.plugin = plugin;
        this.db = db;
        this.retentionDays = retentionDays;
    }

    public void start() {
        purgeNow();

        long ticks = 20L * 60 * 60 * 24;
        taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin, this::purgeNow, ticks, ticks
        ).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void purgeNow() {
        long cutoff = System.currentTimeMillis() - ((long) retentionDays * 86400000L);
        int deleted = db.purgeTransactions(cutoff);
        if (deleted > 0) {
            plugin.getLogger().info("Purged " + deleted + " transactions older than " + retentionDays + " days.");
        }
    }
}

package com.spectrasonic.economySystem.managers;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.cache.CacheManager;
import com.spectrasonic.economySystem.cache.FlushScheduler;
import com.spectrasonic.economySystem.database.DatabaseManager;
import com.spectrasonic.economySystem.database.LiteSQLManager;
import com.spectrasonic.economySystem.database.MariaDBManager;
import com.spectrasonic.economySystem.database.JDBCManager;
import com.spectrasonic.economySystem.database.TransactionPurger;
import com.spectrasonic.economySystem.utils.MessageManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final Main plugin;

    private MessageManager messages;
    private DatabaseManager databaseManager;
    private CacheManager cacheManager;
    private FlushScheduler flushScheduler;
    private Permission permissions;
    private TransactionPurger transactionPurger;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public Main getPlugin() {
        return plugin;
    }

    public MessageManager getMessages() {
        return messages;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public FlushScheduler getFlushScheduler() {
        return flushScheduler;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public TransactionPurger getTransactionPurger() {
        return transactionPurger;
    }

    public void loadMessages() {
        try {
            File file = new File(plugin.getDataFolder(), "messages.yml");
            if (!file.exists()) {
                plugin.saveResource("messages.yml", false);
            }
            messages = new MessageManager(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error loading the Messages file!");
            e.printStackTrace();
        }
    }

    public void setupDatabase() {
        String dbType = plugin.getConfig().getString("database.type", "litesql");
        switch (dbType.toLowerCase()) {
            case "mariadb" -> databaseManager = new MariaDBManager(plugin);
            case "litesql" -> databaseManager = new LiteSQLManager(plugin);
            case "url" -> databaseManager = new JDBCManager(plugin);
            default -> {
                plugin.getLogger().severe("Invalid Database type: " + dbType);
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return;
            }
        }

        String serverId = plugin.getConfig().getString("database.server-id", "unknown");
        databaseManager.setServerId(serverId);

        databaseManager.connect();

        setupTransactionPurger();
    }

    private void setupTransactionPurger() {
        int retentionDays = plugin.getConfig().getInt("database.transaction-retention", 30);
        if (retentionDays > 0) {
            transactionPurger = new TransactionPurger(plugin, databaseManager, retentionDays);
            transactionPurger.start();
            plugin.getLogger().info("Transaction purger started with " + retentionDays + " days retention.");
        }
    }

    public void setupCache() {
        boolean cacheEnabled = plugin.getConfig().getBoolean("cache.enabled", true);

        if (cacheEnabled) {
            cacheManager = new CacheManager(plugin, databaseManager);
            flushScheduler = new FlushScheduler(plugin, cacheManager);
            flushScheduler.start();

            plugin.getLogger().info("Cache system enabled and initialized.");
        } else {
            plugin.getLogger().info("Cache system disabled. Using direct database access.");
        }
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer()
                .getServicesManager()
                .getRegistration(Permission.class);
        permissions = rsp != null ? rsp.getProvider() : null;
        return permissions != null;
    }
}

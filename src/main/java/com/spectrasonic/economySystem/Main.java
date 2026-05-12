package com.spectrasonic.economySystem;

import com.spectrasonic.economySystem.cache.CacheManager;
import com.spectrasonic.economySystem.cache.FlushScheduler;
import com.spectrasonic.economySystem.managers.CommandManager;
import com.spectrasonic.economySystem.managers.ConfigManager;
import com.spectrasonic.economySystem.managers.LoadManager;
import com.spectrasonic.economySystem.database.DatabaseManager;
import com.spectrasonic.economySystem.utils.MessageManager;
import com.spectrasonic.economySystem.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private CommandManager commandManager;
    private ConfigManager configManager;
    private LoadManager loadManager;

    @Override
    public void onEnable() {

        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.loadMessages();
        configManager.setupPermissions();
        configManager.setupDatabase();
        configManager.setupCache();

        loadManager = new LoadManager(this, configManager);
        loadManager.registerPlaceholderAPI();
        loadManager.registerListeners();
        loadManager.hookVault();

        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        if (configManager.getFlushScheduler() != null) {
            configManager.getFlushScheduler().stop();
        }

        if (configManager.getCacheManager() != null) {
            getLogger().info("Flushing cache before shutdown...");
            configManager.getCacheManager().flushAllSync();
            getLogger().info("Cache flushed successfully.");
        }

        if (loadManager != null) {
            loadManager.shutdown();
        }

        MessageUtils.sendShutdownMessage(this);
    }

    public String formatBalance(double balance) {
        int decimalPlaces = getConfig().getInt("economy.decimal-places", 0);

        if (decimalPlaces == 0) {
            return String.format("%,.0f", balance);
        }
        return String.format("%,." + decimalPlaces + "f", balance);
    }

    public String formatBalanceDecimals(double balance) {
        return String.format("%,.2f", balance);
    }

    public String formatBalanceInteger(double balance) {
        return String.valueOf((int) balance);
    }

    public String formatBalanceRaw(double balance) {
        return String.valueOf(balance);
    }

    public String formatBalanceWithSymbol(double balance) {
        String symbol = getConfig().getString("economy.currency-symbol", "$");
        return symbol + " " + formatBalance(balance);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessages() {
        return configManager.getMessages();
    }

    public DatabaseManager getDatabaseManager() {
        return configManager.getDatabaseManager();
    }

    public CacheManager getCacheManager() {
        return configManager.getCacheManager();
    }

    public FlushScheduler getFlushScheduler() {
        return configManager.getFlushScheduler();
    }
}

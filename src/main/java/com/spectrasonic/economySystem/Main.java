package com.spectrasonic.economySystem;

import com.spectrasonic.economySystem.managers.CommandManager;
import com.spectrasonic.economySystem.managers.ConfigManager;
import com.spectrasonic.economySystem.managers.LoadManager;
import com.spectrasonic.economySystem.database.DatabaseManager;
import com.spectrasonic.economySystem.utils.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

public final class Main extends JavaPlugin {

    private CommandManager commandManager;
    private ConfigManager configManager;
    private LoadManager loadManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        CommandAPI.onEnable();
        configManager = new ConfigManager(this);
        configManager.loadMessages();
        configManager.setupPermissions();
        configManager.setupDatabase();

        loadManager = new LoadManager(this, configManager);
        loadManager.registerPlaceholderAPI();
        loadManager.registerListeners();
        loadManager.hookVault();

        commandManager = new CommandManager(this);
        commandManager.registerCommands();

        getServer().getConsoleSender().sendMessage(configManager.getMessages().get("plugin-enabled"));
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        if (loadManager != null) {
            loadManager.shutdown();
        }

        if (configManager != null && configManager.getMessages() != null) {
            getServer().getConsoleSender().sendMessage(configManager.getMessages().get("plugin-disabled"));
        }
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
}

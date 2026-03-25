package com.spectrasonic.economySystem.managers;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.database.DatabaseManager;
import com.spectrasonic.economySystem.listener.PlayerJoinListener;
import com.spectrasonic.economySystem.placeholderapi.MoneyTopExpansion;
import com.spectrasonic.economySystem.vault.Vault;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

/**
 * Manager responsible for loading plugin components during enable/disable.
 * Handles listeners, PlaceholderAPI, and Vault integration.
 */
public class LoadManager {

    private final Main plugin;
    private final ConfigManager configManager;

    public LoadManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void registerListeners() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(plugin), plugin);
    }

    public void registerPlaceholderAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MoneyTopExpansion(plugin).register();
            plugin.getLogger().info("MoneyTop PlaceholderAPI Expansion loaded!");
        }
    }

    public void hookVault() {
        new Vault(plugin);
    }

    public void shutdown() {
        DatabaseManager dbManager = configManager.getDatabaseManager();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}

package com.spectrasonic.economySystem.managers;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.commands.BalanceCommand;
import com.spectrasonic.economySystem.commands.BalanceTopCommand;
import com.spectrasonic.economySystem.commands.EconomyAdminCommand;
import com.spectrasonic.economySystem.commands.PayCommand;
import com.spectrasonic.economySystem.commands.EconomyAdminTabCompleter;
import org.bukkit.command.CommandExecutor;

/**
 * Manager responsible for registering all plugin commands.
 * Encapsulates command registration logic to keep the main class clean.
 */
public class CommandManager {

    private final Main plugin;

    public CommandManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        registerCommand("balance", new BalanceCommand(plugin));
        registerCommand("pay", new PayCommand(plugin));
        registerCommand("economyadmin", new EconomyAdminCommand(plugin));
        registerCommand("balancetop", new BalanceTopCommand(plugin));
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        var command = plugin.getCommand(commandName);
        if (command != null) {
            command.setExecutor(executor);
            // Register tab completer for economyadmin command
            if (executor instanceof EconomyAdminCommand) {
                command.setTabCompleter(new EconomyAdminTabCompleter());
            }
        } else {
            plugin.getLogger().warning("Command '" + commandName + "' not found in plugin.yml");
        }
    }
}

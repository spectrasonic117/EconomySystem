package com.spectrasonic.economySystem.managers;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.commands.BalanceCommand;
import com.spectrasonic.economySystem.commands.PayCommand;
import com.spectrasonic.economySystem.commands.BalancetopCommand;
import com.spectrasonic.economySystem.commands.EconomyadminCommand;

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
        new BalanceCommand(plugin).register();
        new PayCommand(plugin).register();
        new BalancetopCommand(plugin).register();
        new EconomyadminCommand(plugin).register();
    }
}

package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

public class BalancetopCommand {

    private final Main plugin;

    public BalancetopCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("balancetop")
            .withAliases("baltop", "topbalance", "topbal", "topgeld")
            .executes(this::handleBalanceTop)
            .register();
    }

    private void handleBalanceTop(CommandSender sender, CommandArguments args) {
        var topBalances = plugin.getDatabaseManager().getTopBalances(10);
        plugin.getLogger().info("BalanceTop: Found " + topBalances.size() + " players");

        sender.sendMessage(plugin.getMessages().get("top-list-title"));
        if (topBalances.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("no-players-found"));
        } else {
            int i = 1;
            for (var entry : topBalances.entrySet()) {
                String uuid = entry.getKey();
                double balance = entry.getValue();
                String playerName = plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(uuid)).getName();
                
                sender.sendMessage(plugin.getMessages().get("top-list-entry", 
                        "%position%", String.valueOf(i++), 
                        "%player%", playerName != null ? playerName : "Unknown", 
                        "%balance%", String.valueOf(balance)));
            }
        }
    }
}

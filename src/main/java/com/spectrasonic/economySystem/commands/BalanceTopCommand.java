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
            .executes((CommandSender sender, CommandArguments args) -> {
                var topBalances = plugin.getDatabaseManager().getTopBalances(10);
                plugin.getLogger().info("BalanceTop: Found " + topBalances.size() + " players");

                sender.sendMessage(plugin.getMessages().get("top-list-title"));
                if (topBalances.isEmpty()) {
                    sender.sendMessage(plugin.getMessages().get("no-players-found"));
                } else {
                    for (int i = 0; i < topBalances.size(); i++) {
                        String uuid = (String) topBalances.keySet().toArray()[i];
                        double balance = topBalances.get(uuid);
                        sender.sendMessage(plugin.getMessages().get("top-list-entry", "%position%", String.valueOf(i + 1), "%player%", plugin.getServer().getOfflinePlayer(java.util.UUID.fromString(uuid)).getName(), "%balance%", String.valueOf(balance)));
                    }
                }
            })
            .register();
    }
}

package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.utils.MessageUtils;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.UUID;

public class BalancetopCommand {

    private final Main plugin;

    public BalancetopCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandTree("balancetop")
                .withAliases("baltop", "topbalance", "topbal", "topgeld", "top2")
                .executes(this::handleBalanceTop)
                .then(new LiteralArgument("all")
                        .withPermission(CommandPermission.OP)
                        .executes(this::handleBalanceTopAll))
                .register();
    }

    private void handleBalanceTop(CommandSender sender, CommandArguments args) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            var topBalances = getTopBalances(10);
            plugin.getLogger().info("BalanceTop: Found " + topBalances.size() + " players");

            plugin.getServer().getScheduler().runTask(plugin, () ->
                sendTopList(sender, topBalances)
            );
        });
    }

    private void handleBalanceTopAll(CommandSender sender, CommandArguments args) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            var topBalances = getTopBalances(10);
            plugin.getLogger().info("BalanceTop: Found " + topBalances.size() + " players");

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    sendTopList(player, topBalances);
                }
            });
        });
    }

    private LinkedHashMap<String, Double> getTopBalances(int limit) {
        if (plugin.getCacheManager() != null) {
            return plugin.getCacheManager().getTopBalances(limit);
        }

        return plugin.getDatabaseManager().getTopBalances(limit);
    }

    private void sendTopList(CommandSender sender, java.util.Map<String, Double> topBalances) {
        MessageUtils.infoComponent(sender, plugin.getMessages().get("top-list-title"));
        if (topBalances.isEmpty()) {
            MessageUtils.warningComponent(sender, plugin.getMessages().get("no-players-found"));
        } else {
            int i = 1;
            for (var entry : topBalances.entrySet()) {
                String uuid = entry.getKey();
                double balance = entry.getValue();
                String playerName = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();

                MessageUtils.rawComponent(sender, plugin.getMessages().get("top-list-entry",
                        "%position%", String.valueOf(i++),
                        "%player%", playerName != null ? playerName : "Unknown",
                        "%balance%", String.valueOf(balance)));
            }
        }
    }
}

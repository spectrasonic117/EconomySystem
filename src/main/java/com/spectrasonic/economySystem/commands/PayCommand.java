package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.entity.Player;

public class PayCommand {

    private final Main plugin;

    public PayCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("pay")
            .executesPlayer((Player player, CommandArguments args) -> {
                player.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
            })
            .withArguments(new EntitySelectorArgument.OnePlayer("player"))
            .executesPlayer((Player player, CommandArguments args) -> {
                player.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
            })
            .withArguments(new IntegerArgument("amount"))
            .executesPlayer((Player player, CommandArguments args) -> {
                Object targetObj = args.get("player");
                if (targetObj == null) {
                    player.sendMessage(plugin.getMessages().get("player-not-found"));
                    return;
                }
                Player target = (Player) targetObj;

                Object amountObj = args.get("amount");
                if (amountObj == null) {
                    player.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
                    return;
                }
                int amount = (int) amountObj;

                if (amount <= 0) {
                    player.sendMessage(plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
                    return;
                }

                if (target.equals(player)) {
                    player.sendMessage(plugin.getMessages().get("pay-self"));
                    return;
                }

                String playerUuid = player.getUniqueId().toString();
                String targetUuid = target.getUniqueId().toString();

                if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                    plugin.getDatabaseManager().createAccount(playerUuid);
                }
                if (!plugin.getDatabaseManager().accountExists(targetUuid)) {
                    plugin.getDatabaseManager().createAccount(targetUuid);
                }

                double playerBalance = plugin.getDatabaseManager().getBalance(playerUuid);

                if (playerBalance < amount) {
                    player.sendMessage(plugin.getMessages().get("not-enough-money"));
                    return;
                }

                plugin.getDatabaseManager().removeBalance(playerUuid, amount);
                plugin.getDatabaseManager().addBalance(targetUuid, amount);
                plugin.getDatabaseManager().createTransaction(playerUuid, targetUuid, amount);

                player.sendMessage(plugin.getMessages().get("pay-success-sender", "%player%", target.getName(), "%amount%", String.valueOf(amount)));
                target.sendMessage(plugin.getMessages().get("pay-success-receiver", "%player%", player.getName(), "%amount%", String.valueOf(amount)));
            })
            .register();
    }
}

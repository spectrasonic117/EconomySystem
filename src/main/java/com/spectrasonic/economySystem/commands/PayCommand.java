package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.utils.MessageUtils;
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
            .executesPlayer(this::handlePayUsage)
            .withArguments(new EntitySelectorArgument.OnePlayer("player"))
            .executesPlayer(this::handlePayUsage)
            .withArguments(new IntegerArgument("amount"))
            .executesPlayer(this::handlePay)
            .register();
    }

    private void handlePayUsage(Player player, CommandArguments args) {
        MessageUtils.alertComponent(player, plugin.getMessages().get("usage", "%usage%", "/pay <player> <amount>"));
    }

    private void handlePay(Player player, CommandArguments args) {
        Player target = (Player) args.get("player");
        Integer amount = (Integer) args.get("amount");

        if (target == null) {
            MessageUtils.denyComponent(player, plugin.getMessages().get("player-not-found"));
            return;
        }

        if (amount == null || amount <= 0) {
            handlePayUsage(player, args);
            return;
        }

        if (target.equals(player)) {
            MessageUtils.denyComponent(player, plugin.getMessages().get("pay-self"));
            return;
        }

        String playerUuid = player.getUniqueId().toString();
        String targetUuid = target.getUniqueId().toString();

        ensureAccount(playerUuid);
        ensureAccount(targetUuid);

        double playerBalance = getBalance(playerUuid);

        if (playerBalance < amount) {
            MessageUtils.denyComponent(player, plugin.getMessages().get("not-enough-money"));
            return;
        }

        removeBalance(playerUuid, amount);
        addBalance(targetUuid, amount);

        recordTransactionAsync(playerUuid, targetUuid, amount);

        MessageUtils.successComponent(player, plugin.getMessages().get("pay-success-sender", "%player%", target.getName(), "%amount%", String.valueOf(amount)));
        MessageUtils.successComponent(target, plugin.getMessages().get("pay-success-receiver", "%player%", player.getName(), "%amount%", String.valueOf(amount)));
    }

    private void ensureAccount(String uuid) {
        if (plugin.getCacheManager() != null) {
            plugin.getCacheManager().ensureAccount(uuid);
        } else {
            if (!plugin.getDatabaseManager().accountExists(uuid)) {
                plugin.getDatabaseManager().createAccount(uuid);
            }
        }
    }

    private double getBalance(String uuid) {
        if (plugin.getCacheManager() != null) {
            return plugin.getCacheManager().getBalance(uuid);
        }

        return plugin.getDatabaseManager().getBalance(uuid);
    }

    private void addBalance(String uuid, double amount) {
        if (plugin.getCacheManager() != null) {
            plugin.getCacheManager().addBalance(uuid, amount);
        } else {
            plugin.getDatabaseManager().addBalance(uuid, amount);
        }
    }

    private void removeBalance(String uuid, double amount) {
        if (plugin.getCacheManager() != null) {
            plugin.getCacheManager().removeBalance(uuid, amount);
        } else {
            plugin.getDatabaseManager().removeBalance(uuid, amount);
        }
    }

    private void recordTransactionAsync(String from, String to, double amount) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getCacheManager() != null) {
                plugin.getCacheManager().createTransaction(from, to, amount);
            } else {
                plugin.getDatabaseManager().createTransaction(from, to, amount);
            }
        });
    }
}

package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class EconomyadminCommand {

    private final Main plugin;

    public EconomyadminCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("economyadmin")
                .withAliases("ecoa")
                .withPermission(CommandPermission.OP)
                .executes(this::sendUsage)
                .withSubcommand(new CommandAPICommand("reload")
                        .executes(this::handleReload))
                .withSubcommand(new CommandAPICommand("balance")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executes(this::handleBalance))
                .withSubcommand(new CommandAPICommand("set")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .withArguments(new IntegerArgument("amount"))
                        .executes(this::handleSet))
                .withSubcommand(new CommandAPICommand("add")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .withArguments(new IntegerArgument("amount"))
                        .executes(this::handleAdd))
                .withSubcommand(new CommandAPICommand("remove")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .withArguments(new IntegerArgument("amount"))
                        .executes(this::handleRemove))
                .withSubcommand(new CommandAPICommand("reset")
                        .withArguments(new EntitySelectorArgument.ManyPlayers("players"))
                        .executes(this::handleReset))
                .register();
    }

    private void sendUsage(CommandSender sender, CommandArguments args) {
        sender.sendMessage(plugin.getMessages().get("usage", "%usage%",
                "/economyadmin <set|add|remove|reset|balance|reload> <player> [amount]"));
    }

    private void handleReload(CommandSender sender, CommandArguments args) {
        plugin.reloadConfig();
        plugin.getConfigManager().loadMessages();
        sender.sendMessage("Plugin configuration and messages reloaded.");
    }

    private void handleBalance(CommandSender sender, CommandArguments args) {
        Collection<Player> players = (Collection<Player>) args.get("players");
        if (players == null || players.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("player-not-found"));
            return;
        }

        for (Player player : players) {
            String playerUuid = player.getUniqueId().toString();
            checkAccount(playerUuid);
            double balance = plugin.getDatabaseManager().getBalance(playerUuid);
            sender.sendMessage(plugin.getMessages().get("balance-message-other", "%player%",
                    player.getName(), "%balance%", String.valueOf(balance)));
        }
    }

    private void handleSet(CommandSender sender, CommandArguments args) {
        Collection<Player> players = (Collection<Player>) args.get("players");
        Integer amount = (Integer) args.get("amount");

        if (players == null || players.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("player-not-found"));
            return;
        }
        if (amount == null || amount < 0) {
            sender.sendMessage(plugin.getMessages().get("usage", "%usage%",
                    "/economyadmin set <player> <amount>"));
            return;
        }

        for (Player player : players) {
            String playerUuid = player.getUniqueId().toString();
            checkAccount(playerUuid);
            plugin.getDatabaseManager().setBalance(playerUuid, amount);
            sender.sendMessage(plugin.getMessages().get("balance-set-success", "%player%",
                    player.getName(), "%amount%", String.valueOf(amount)));
        }
    }

    private void handleAdd(CommandSender sender, CommandArguments args) {
        Collection<Player> players = (Collection<Player>) args.get("players");
        Integer amount = (Integer) args.get("amount");

        if (players == null || players.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("player-not-found"));
            return;
        }
        if (amount == null || amount <= 0) {
            sender.sendMessage(plugin.getMessages().get("usage", "%usage%",
                    "/economyadmin add <player> <amount>"));
            return;
        }

        for (Player player : players) {
            String playerUuid = player.getUniqueId().toString();
            checkAccount(playerUuid);
            plugin.getDatabaseManager().addBalance(playerUuid, amount);
            sender.sendMessage(plugin.getMessages().get("balance-add-success", "%player%",
                    player.getName(), "%amount%", String.valueOf(amount)));
        }
    }

    private void handleRemove(CommandSender sender, CommandArguments args) {
        Collection<Player> players = (Collection<Player>) args.get("players");
        Integer amount = (Integer) args.get("amount");

        if (players == null || players.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("player-not-found"));
            return;
        }
        if (amount == null || amount <= 0) {
            sender.sendMessage(plugin.getMessages().get("usage", "%usage%",
                    "/economyadmin remove <player> <amount>"));
            return;
        }

        for (Player player : players) {
            String playerUuid = player.getUniqueId().toString();
            checkAccount(playerUuid);
            plugin.getDatabaseManager().removeBalance(playerUuid, amount);
            sender.sendMessage(plugin.getMessages().get("remove-balance-success", "%player%",
                    player.getName(), "%amount%", String.valueOf(amount)));
        }
    }

    private void handleReset(CommandSender sender, CommandArguments args) {
        Collection<Player> players = (Collection<Player>) args.get("players");
        if (players == null || players.isEmpty()) {
            sender.sendMessage(plugin.getMessages().get("player-not-found"));
            return;
        }

        for (Player player : players) {
            String playerUuid = player.getUniqueId().toString();
            checkAccount(playerUuid);
            plugin.getDatabaseManager().setBalance(playerUuid, 0);
            sender.sendMessage(plugin.getMessages().get("balance-set-success", "%player%",
                    player.getName(), "%amount%", "0"));
        }
    }

    private void checkAccount(String playerUuid) {
        if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
            plugin.getDatabaseManager().createAccount(playerUuid);
        }
    }
}

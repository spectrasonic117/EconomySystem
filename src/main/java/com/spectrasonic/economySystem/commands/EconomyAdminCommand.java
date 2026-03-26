package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyadminCommand {

    private final Main plugin;

    public EconomyadminCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("economyadmin")
            .withAliases("ecoa")
            .withPermission(CommandPermission.OP)
            .executes((CommandSender sender, CommandArguments args) -> {
                sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin <set|add|remove|reset|balance|reload> <player> [amount]"));
            })
            .withSubcommand(new CommandAPICommand("reload")
                .executes((CommandSender sender, CommandArguments args) -> {
                    plugin.reloadConfig();
                    plugin.getConfigManager().loadMessages();
                    sender.sendMessage("Plugin configuration and messages reloaded.");
                })
            )
            .withSubcommand(new CommandAPICommand("balance")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .executes((CommandSender sender, CommandArguments args) -> {
                    Object playerObj = args.get("player");
                    if (playerObj == null) {
                        sender.sendMessage(plugin.getMessages().get("player-not-found"));
                        return;
                    }
                    Player player = (Player) playerObj;
                    String playerUuid = player.getUniqueId().toString();
                    if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                        plugin.getDatabaseManager().createAccount(playerUuid);
                    }
                    double balance = plugin.getDatabaseManager().getBalance(playerUuid);
                    sender.sendMessage(plugin.getMessages().get("balance-message-other", "%player%", player.getName(), "%balance%", String.valueOf(balance)));
                })
            )
            .withSubcommand(new CommandAPICommand("set")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(new IntegerArgument("amount"))
                .executes((CommandSender sender, CommandArguments args) -> {
                    Object playerObj = args.get("player");
                    if (playerObj == null) {
                        sender.sendMessage(plugin.getMessages().get("player-not-found"));
                        return;
                    }
                    Player player = (Player) playerObj;
                    Object amountObj = args.get("amount");
                    if (amountObj == null) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin set <player> <amount>"));
                        return;
                    }
                    int amount = (int) amountObj;

                    if (amount < 0) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin set <player> <amount>"));
                        return;
                    }

                    String playerUuid = player.getUniqueId().toString();
                    if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                        plugin.getDatabaseManager().createAccount(playerUuid);
                    }
                    plugin.getDatabaseManager().setBalance(playerUuid, amount);
                    sender.sendMessage(plugin.getMessages().get("balance-set-success", "%player%", player.getName(), "%amount%", String.valueOf(amount)));
                })
            )
            .withSubcommand(new CommandAPICommand("add")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(new IntegerArgument("amount"))
                .executes((CommandSender sender, CommandArguments args) -> {
                    Object playerObj = args.get("player");
                    if (playerObj == null) {
                        sender.sendMessage(plugin.getMessages().get("player-not-found"));
                        return;
                    }
                    Player player = (Player) playerObj;
                    Object amountObj = args.get("amount");
                    if (amountObj == null) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin add <player> <amount>"));
                        return;
                    }
                    int amount = (int) amountObj;

                    if (amount <= 0) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin add <player> <amount>"));
                        return;
                    }

                    String playerUuid = player.getUniqueId().toString();
                    if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                        plugin.getDatabaseManager().createAccount(playerUuid);
                    }
                    plugin.getDatabaseManager().addBalance(playerUuid, amount);
                    sender.sendMessage(plugin.getMessages().get("balance-add-success", "%player%", player.getName(), "%amount%", String.valueOf(amount)));
                })
            )
            .withSubcommand(new CommandAPICommand("remove")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .withArguments(new IntegerArgument("amount"))
                .executes((CommandSender sender, CommandArguments args) -> {
                    Object playerObj = args.get("player");
                    if (playerObj == null) {
                        sender.sendMessage(plugin.getMessages().get("player-not-found"));
                        return;
                    }
                    Player player = (Player) playerObj;
                    Object amountObj = args.get("amount");
                    if (amountObj == null) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin remove <player> <amount>"));
                        return;
                    }
                    int amount = (int) amountObj;

                    if (amount <= 0) {
                        sender.sendMessage(plugin.getMessages().get("usage", "%usage%", "/economyadmin remove <player> <amount>"));
                        return;
                    }

                    String playerUuid = player.getUniqueId().toString();
                    if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                        plugin.getDatabaseManager().createAccount(playerUuid);
                    }
                    plugin.getDatabaseManager().removeBalance(playerUuid, amount);
                    sender.sendMessage(plugin.getMessages().get("remove-balance-success", "%player%", player.getName(), "%amount%", String.valueOf(amount)));
                })
            )
            .withSubcommand(new CommandAPICommand("reset")
                .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                .executes((CommandSender sender, CommandArguments args) -> {
                    Object playerObj = args.get("player");
                    if (playerObj == null) {
                        sender.sendMessage(plugin.getMessages().get("player-not-found"));
                        return;
                    }
                    Player player = (Player) playerObj;
                    String playerUuid = player.getUniqueId().toString();
                    if (!plugin.getDatabaseManager().accountExists(playerUuid)) {
                        plugin.getDatabaseManager().createAccount(playerUuid);
                    }
                    plugin.getDatabaseManager().setBalance(playerUuid, 0);
                    sender.sendMessage(plugin.getMessages().get("balance-set-success", "%player%", player.getName(), "%amount%", "0"));
                })
            )
            .register();
    }
}

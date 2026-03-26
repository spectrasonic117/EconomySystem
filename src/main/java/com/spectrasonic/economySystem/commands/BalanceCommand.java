package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand {

    private final Main plugin;

    public BalanceCommand(Main plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("balance")
            .withAliases("bal", "money", "pfund", "coins", "geld")
            .executesPlayer((Player player, CommandArguments args) -> {
                String uuid = player.getUniqueId().toString();
                if (!plugin.getDatabaseManager().accountExists(uuid)) {
                    plugin.getDatabaseManager().createAccount(uuid);
                }
                double balance = plugin.getDatabaseManager().getBalance(uuid);
                player.sendMessage(plugin.getMessages().get("balance-message-own", "%balance%", String.valueOf(balance)));
            })
            .withArguments(new EntitySelectorArgument.OnePlayer("player"))
            .withPermission("economysystem.command.balance.other")
            .executes((CommandSender sender, CommandArguments args) -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getMessages().get("player-not-player"));
                    return;
                }
                Player player = (Player) sender;
                Object targetObj = args.get("player");
                if (targetObj == null) {
                    player.sendMessage(plugin.getMessages().get("player-not-found"));
                    return;
                }
                Player target = (Player) targetObj;
                String targetUuid = target.getUniqueId().toString();
                if (!plugin.getDatabaseManager().accountExists(targetUuid)) {
                    plugin.getDatabaseManager().createAccount(targetUuid);
                }
                double balance = plugin.getDatabaseManager().getBalance(targetUuid);
                player.sendMessage(plugin.getMessages().get("balance-message-other", "%player%", target.getName(), "%balance%", String.valueOf(balance)));
            })
            .register();
    }
}

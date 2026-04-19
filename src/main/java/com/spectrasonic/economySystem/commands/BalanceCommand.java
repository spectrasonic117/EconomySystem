package com.spectrasonic.economySystem.commands;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.utils.MessageUtils;
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
            .executesPlayer(this::handleBalanceSelf)
            .withArguments(new EntitySelectorArgument.OnePlayer("player"))
            .withPermission("economysystem.command.balance.other")
            .executes(this::handleBalanceOther)
            .register();
    }

    private void handleBalanceSelf(Player player, CommandArguments args) {
        String uuid = player.getUniqueId().toString();
        checkAccount(uuid);
        double balance = plugin.getDatabaseManager().getBalance(uuid);
        MessageUtils.infoComponent(player, plugin.getMessages().get("balance-message-own", "%balance%", String.valueOf(balance)));
    }

    private void handleBalanceOther(CommandSender sender, CommandArguments args) {
        if (!(sender instanceof Player)) {
            MessageUtils.denyComponent(sender, plugin.getMessages().get("player-not-player"));
            return;
        }

        Player player = (Player) sender;
        Player target = (Player) args.get("player");
        
        if (target == null) {
            MessageUtils.denyComponent(player, plugin.getMessages().get("player-not-found"));
            return;
        }

        String targetUuid = target.getUniqueId().toString();
        checkAccount(targetUuid);
        double balance = plugin.getDatabaseManager().getBalance(targetUuid);
        MessageUtils.infoComponent(player, plugin.getMessages().get("balance-message-other", "%player%", target.getName(), "%balance%", String.valueOf(balance)));
    }

    private void checkAccount(String uuid) {
        if (!plugin.getDatabaseManager().accountExists(uuid)) {
            plugin.getDatabaseManager().createAccount(uuid);
        }
    }
}

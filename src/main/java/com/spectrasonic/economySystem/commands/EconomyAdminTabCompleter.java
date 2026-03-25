package com.spectrasonic.economySystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EconomyAdminTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "set", "add", "remove", "balance", "reload");

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args) {
        // 1️⃣ First argument: sub‑command suggestions
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(sc -> sc.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 2️⃣ Second argument: player name (if not reload)
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            return java.util.Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 3️⃣ Third argument: amount (only for set/add/remove)
        if (args.length == 3 && (args[0].equalsIgnoreCase("set")
                || args[0].equalsIgnoreCase("add")
                || args[0].equalsIgnoreCase("remove"))) {
            // Provide a few example positive numbers; users can still type any value.
            List<String> examples = List.of("1", "10", "100");
            return examples.stream()
                    .filter(v -> v.startsWith(args[2]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}

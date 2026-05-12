package com.spectrasonic.economySystem.placeholderapi;

import com.spectrasonic.economySystem.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MoneyTopExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public MoneyTopExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "economysystem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ImDacro";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // ─── Placeholders de Balance del Jugador Actual ───

        // %economysystem_balance% - Balance según configuración
        if (params.equals("balance")) {
            return getFormattedBalance(player);
        }

        // %economysystem_balance_raw% - Balance sin formato
        if (params.equals("balance_raw")) {
            return getRawBalance(player);
        }

        // %economysystem_balance_int% - Balance como entero
        if (params.equals("balance_int")) {
            return getIntegerBalance(player);
        }

        // %economysystem_balance_decimals% - Balance con 2 decimales
        if (params.equals("balance_decimals")) {
            return getDecimalsBalance(player);
        }

        // %economysystem_balance_symbol% - Balance con símbolo de moneda
        if (params.equals("balance_symbol")) {
            return getBalanceWithSymbol(player);
        }

        // ─── Placeholders de Balance de Otro Jugador ───

        // Formato: balance_other_{nombre} o balance_other_{nombre}_{formato}
        if (params.startsWith("balance_other_")) {
            return handleOtherPlayerBalance(params);
        }

        // ─── Placeholders de Leaderboard ───

        // Formato: leaderboard_{pos}_{type} o leaderboard_{pos}_{type}_{formato}
        if (params.startsWith("leaderboard_")) {
            return handleLeaderboard(params);
        }

        // ─── Placeholders de Configuración ───

        // %economysystem_currency_symbol% - Símbolo de moneda
        if (params.equals("currency_symbol")) {
            return plugin.getConfig().getString("economy.currency-symbol", "$");
        }

        // %economysystem_currency_name% - Nombre de moneda
        if (params.equals("currency_name")) {
            return plugin.getConfig().getString("economy.currency-name", "Dollar");
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    // Métodos de Balance del Jugador Actual
    // ═══════════════════════════════════════════════════════════════

    private String getFormattedBalance(OfflinePlayer player) {
        double balance = getBalance(player.getUniqueId().toString());
        return plugin.formatBalance(balance);
    }

    private String getRawBalance(OfflinePlayer player) {
        double balance = getBalance(player.getUniqueId().toString());
        return plugin.formatBalanceRaw(balance);
    }

    private String getIntegerBalance(OfflinePlayer player) {
        double balance = getBalance(player.getUniqueId().toString());
        return plugin.formatBalanceInteger(balance);
    }

    private String getDecimalsBalance(OfflinePlayer player) {
        double balance = getBalance(player.getUniqueId().toString());
        return plugin.formatBalanceDecimals(balance);
    }

    private String getBalanceWithSymbol(OfflinePlayer player) {
        double balance = getBalance(player.getUniqueId().toString());
        return plugin.formatBalanceWithSymbol(balance);
    }

    // ═══════════════════════════════════════════════════════════════
    // Métodos de Balance de Otro Jugador
    // ═══════════════════════════════════════════════════════════════

    private String handleOtherPlayerBalance(String params) {
        // Formato esperado: balance_other_{nombre} o balance_other_{nombre}_{formato}
        String[] parts = params.split("_");

        if (parts.length < 3) {
            return null;
        }

        String playerName = parts[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            return "Jugador no encontrado";
        }

        String uuid = target.getUniqueId().toString();
        double balance = getBalance(uuid);

        // Verificar si hay un formato específico
        if (parts.length >= 4) {
            String format = parts[3];
            return switch (format.toLowerCase()) {
                case "raw" -> plugin.formatBalanceRaw(balance);
                case "int" -> plugin.formatBalanceInteger(balance);
                case "decimals" -> plugin.formatBalanceDecimals(balance);
                case "symbol" -> plugin.formatBalanceWithSymbol(balance);
                default -> plugin.formatBalance(balance);
            };
        }

        return plugin.formatBalance(balance);
    }

    // ═══════════════════════════════════════════════════════════════
    // Métodos de Leaderboard
    // ═══════════════════════════════════════════════════════════════

    private String handleLeaderboard(String params) {
        // Formato: leaderboard_{pos}_{type} o leaderboard_{pos}_{type}_{formato}
        String[] parts = params.split("_");

        if (parts.length < 3) {
            return null;
        }

        int pos;
        try {
            pos = Integer.parseInt(parts[1]) - 1; // 1 → index 0
        } catch (NumberFormatException e) {
            return null;
        }

        String type = parts[2]; // "name" o "value"

        // Obtener Top 10
        LinkedHashMap<String, Double> top = getTopBalances(10);
        List<Map.Entry<String, Double>> list = new ArrayList<>(top.entrySet());

        if (pos < 0 || pos >= list.size()) {
            return ""; // Fuera de rango
        }

        Map.Entry<String, Double> entry = list.get(pos);
        String uuid = entry.getKey();
        double balance = entry.getValue();

        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

        if (type.equalsIgnoreCase("name")) {
            return p.getName() != null ? p.getName() : "Unknown";
        }

        if (type.equalsIgnoreCase("value")) {
            // Verificar si hay un formato específico
            if (parts.length >= 4) {
                String format = parts[3];
                return switch (format.toLowerCase()) {
                    case "raw" -> plugin.formatBalanceRaw(balance);
                    case "int" -> plugin.formatBalanceInteger(balance);
                    case "decimals" -> plugin.formatBalanceDecimals(balance);
                    case "symbol" -> plugin.formatBalanceWithSymbol(balance);
                    default -> plugin.formatBalance(balance);
                };
            }
            return plugin.formatBalance(balance);
        }

        return null;
    }

    // ═══════════════════════════════════════════════════════════════
    // Utilidades — Cache-aware
    // ═══════════════════════════════════════════════════════════════

    private boolean hasCache() {
        return plugin.getCacheManager() != null;
    }

    private double getBalance(String uuid) {
        if (hasCache()) {
            return plugin.getCacheManager().getBalance(uuid);
        }
        return plugin.getDatabaseManager().getBalance(uuid);
    }

    private LinkedHashMap<String, Double> getTopBalances(int limit) {
        if (hasCache()) {
            return plugin.getCacheManager().getTopBalances(limit);
        }
        return plugin.getDatabaseManager().getTopBalances(limit);
    }
}

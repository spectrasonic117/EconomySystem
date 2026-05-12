package com.spectrasonic.economySystem.vault;

import java.util.List;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.cache.CacheManager;
import com.spectrasonic.economySystem.database.DatabaseManager;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;

public class EconomyProvider extends AbstractEconomy {

    private final Main plugin;

    public EconomyProvider(Main plugin) {
        this.plugin = plugin;
    }

    private boolean hasCache() {
        return plugin.getCacheManager() != null;
    }

    private CacheManager cache() {
        return plugin.getCacheManager();
    }

    private DatabaseManager db() {
        return plugin.getConfigManager().getDatabaseManager();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return plugin.getConfig().getString("economy.currency-name");
    }

    public boolean hasBankSupport() {
        return false;
    }

    public int fractionalDigits() {
        return plugin.getConfig().getInt("economy.decimal-places", 0);
    }

    public String format(double amount) {
        return plugin.formatBalance(amount);
    }

    public String currencyNamePlural() {
        return plugin.getConfig().getString("economy.currency-name");
    }

    public String currencyNameSingular() {
        return plugin.getConfig().getString("economy.currency-name");
    }

    public boolean hasAccount(String playerName) {
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        return hasCache() ? cache().accountExists(uuid) : db().accountExists(uuid);
    }

    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    public double getBalance(String playerName) {
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        return hasCache() ? cache().getBalance(uuid) : db().getBalance(uuid);
    }

    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        ensureAccount(uuid);

        if (hasCache()) {
            cache().removeBalance(uuid, amount);
            cache().createTransaction(uuid, "VAULT", amount);
        } else {
            db().removeBalance(uuid, amount);
            db().createTransaction(uuid, "VAULT", amount);
        }

        double newBalance = getBalance(playerName);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    public EconomyResponse depositPlayer(String playerName, double amount) {
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        ensureAccount(uuid);

        if (hasCache()) {
            cache().addBalance(uuid, amount);
            cache().createTransaction("VAULT", uuid, amount);
        } else {
            db().addBalance(uuid, amount);
            db().createTransaction("VAULT", uuid, amount);
        }

        double newBalance = getBalance(playerName);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
    }

    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    public EconomyResponse deleteBank(String name) {
        return null;
    }

    public EconomyResponse bankBalance(String name) {
        return null;
    }

    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    public List<String> getBanks() {
        return null;
    }

    public boolean createPlayerAccount(String playerName) {
        String uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        if (hasCache()) {
            if (!cache().accountExists(uuid)) {
                cache().createAccount(uuid);
            }
        } else {
            if (!db().accountExists(uuid)) {
                db().createAccount(uuid);
            }
        }
        return true;
    }

    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    private void ensureAccount(String uuid) {
        if (hasCache()) {
            if (!cache().accountExists(uuid)) {
                cache().createAccount(uuid);
            }
        } else {
            if (!db().accountExists(uuid)) {
                db().createAccount(uuid);
            }
        }
    }
}

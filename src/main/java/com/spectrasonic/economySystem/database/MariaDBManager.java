package com.spectrasonic.economySystem.database;

import com.spectrasonic.economySystem.Main;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class MariaDBManager extends AbstractHikariManager {

    public MariaDBManager(Main plugin) {
        super(plugin);
    }

    @Override
    protected String getJdbcUrl() {
        String host = plugin.getConfig().getString("database.mariadb.host", "localhost");
        int port = plugin.getConfig().getInt("database.mariadb.port", 3306);
        String database = plugin.getConfig().getString("database.mariadb.database", "economy");
        return "jdbc:mariadb://" + host + ":" + port + "/" + database;
    }

    @Override
    protected String getDriverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String getUsername() {
        return plugin.getConfig().getString("database.mariadb.username", "root");
    }

    @Override
    protected String getPassword() {
        return plugin.getConfig().getString("database.mariadb.password", "");
    }

    public CompletableFuture<Void> createAccountAsync(String uuid) {
        return CompletableFuture.runAsync(() -> createAccount(uuid), dbExecutor);
    }

    public CompletableFuture<Boolean> accountExistsAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> accountExists(uuid), dbExecutor);
    }

    public CompletableFuture<Double> getBalanceAsync(String uuid) {
        return CompletableFuture.supplyAsync(() -> getBalance(uuid), dbExecutor);
    }

    public CompletableFuture<Void> setBalanceAsync(String uuid, double balance) {
        return CompletableFuture.runAsync(() -> setBalance(uuid, balance), dbExecutor);
    }

    public CompletableFuture<Void> createTransactionAsync(String f, String t, double a, String transactionType) {
        return CompletableFuture.runAsync(() -> createTransaction(f, t, a, transactionType), dbExecutor);
    }

    public CompletableFuture<Boolean> transferBalanceAsync(String from, String to, double amount) {
        return CompletableFuture.supplyAsync(() -> transferBalance(from, to, amount), dbExecutor);
    }

    public CompletableFuture<HashMap<String, Double>> getTopBalancesAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> new HashMap<>(getTopBalances(limit)), dbExecutor);
    }

    public CompletableFuture<Integer> purgeTransactionsAsync(long beforeDate) {
        return CompletableFuture.supplyAsync(() -> purgeTransactions(beforeDate), dbExecutor);
    }
}

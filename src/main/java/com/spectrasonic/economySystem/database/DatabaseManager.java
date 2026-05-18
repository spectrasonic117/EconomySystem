package com.spectrasonic.economySystem.database;

import java.util.LinkedHashMap;
import java.util.Map;

public interface DatabaseManager {
    void connect();

    void close();

    boolean accountExists(String uuid);

    void createAccount(String uuid);

    double getBalance(String uuid);

    void setBalance(String uuid, double balance);

    void addBalance(String uuid, double amount);

    void removeBalance(String uuid, double amount);

    void createTransaction(String uuidFrom, String uuidTo, double amount, String transactionType);

    boolean transferBalance(String from, String to, double amount);

    LinkedHashMap<String, Double> getTopBalances(int limit);

    void batchUpdateBalance(Map<String, Double> entries);

    int purgeTransactions(long beforeDate);

    void setServerId(String serverId);
}

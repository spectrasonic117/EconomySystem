package com.spectrasonic.economySystem.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.events.BalanceChangeEvent;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractHikariManager implements DatabaseManager {

    protected final Main plugin;
    protected HikariDataSource dataSource;
    protected final ExecutorService dbExecutor = Executors.newFixedThreadPool(4);

    public AbstractHikariManager(Main plugin) {
        this.plugin = plugin;
    }

    protected abstract String getJdbcUrl();
    protected abstract String getDriverClassName();
    protected abstract String getUsername();
    protected abstract String getPassword();

    @Override
    public void connect() {
        String jdbcUrl = getJdbcUrl();
        String driverClass = getDriverClassName();
        String username = getUsername();
        String password = getPassword();

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            if (driverClass != null) config.setDriverClassName(driverClass);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);

            dataSource = new HikariDataSource(config);

            plugin.getLogger().info("Connected to database using " + this.getClass().getSimpleName());
            createTables();

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to database in " + this.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void createTables() {
        String economyTable = """
                CREATE TABLE IF NOT EXISTS economy (
                    uuid VARCHAR(36) PRIMARY KEY,
                    balance DOUBLE DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String transactionTable = """
                CREATE TABLE IF NOT EXISTS economy_transactions (
                    uuid_from VARCHAR(36),
                    uuid_to VARCHAR(36),
                    amount DOUBLE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(economyTable);
            stmt.executeUpdate(transactionTable);

            plugin.getLogger().info("Tables checked/created.");

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void createAccount(String uuid) {
        double startBalance = plugin.getConfig().getDouble("economy.start-balance");
        String sql = "INSERT IGNORE INTO economy (uuid, balance) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.setDouble(2, startBalance);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                createTransaction("SERVER", uuid, startBalance);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating account for " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public boolean accountExists(String uuid) {
        String sql = "SELECT 1 FROM economy WHERE uuid = ? LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double getBalance(String uuid) {
        String sql = "SELECT balance FROM economy WHERE uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getDouble("balance");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching balance for " + uuid);
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public void setBalance(String uuid, double balance) {
        if (balance < 0) balance = 0;

        String sql = "UPDATE economy SET balance = ? WHERE uuid = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, balance);
            ps.setString(2, uuid);
            ps.executeUpdate();

            BalanceChangeEvent evt = new BalanceChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)),
                    balance, !Bukkit.isPrimaryThread());

            Bukkit.getPluginManager().callEvent(evt);

        } catch (SQLException e) {
            plugin.getLogger().severe("Error setting balance for " + uuid);
            e.printStackTrace();
        }
    }

    @Override
    public void addBalance(String uuid, double amount) {
        double current = getBalance(uuid);
        setBalance(uuid, current + amount);
    }

    @Override
    public void removeBalance(String uuid, double amount) {
        double current = getBalance(uuid);
        amount = Math.min(amount, current);
        setBalance(uuid, current - amount);
    }

    @Override
    public void createTransaction(String from, String to, double amount) {
        String sql = "INSERT INTO economy_transactions (uuid_from, uuid_to, amount) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, from);
            ps.setString(2, to);
            ps.setDouble(3, amount);
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating transaction.");
            e.printStackTrace();
        }
    }

    @Override
    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT uuid, balance FROM economy ORDER BY balance DESC LIMIT " + limit;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("uuid"), rs.getDouble("balance"));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching top balances!");
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public void batchUpdateBalance(Map<String, Double> entries) {
        if (entries == null || entries.isEmpty()) return;

        String sql = "UPDATE economy SET balance = ? WHERE uuid = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<String, Double> entry : entries.entrySet()) {
                    double balance = Math.max(0, entry.getValue());
                    ps.setDouble(1, balance);
                    ps.setString(2, entry.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            for (Map.Entry<String, Double> entry : entries.entrySet()) {
                try {
                    BalanceChangeEvent evt = new BalanceChangeEvent(
                            Bukkit.getPlayer(UUID.fromString(entry.getKey())),
                            entry.getValue(),
                            !Bukkit.isPrimaryThread()
                    );
                    Bukkit.getPluginManager().callEvent(evt);
                } catch (IllegalArgumentException ignored) {
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error in batch balance update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info(this.getClass().getSimpleName() + " connection pool closed.");
        }
    }
}

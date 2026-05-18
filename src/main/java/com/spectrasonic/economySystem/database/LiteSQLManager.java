package com.spectrasonic.economySystem.database;

import com.spectrasonic.economySystem.Main;
import com.spectrasonic.economySystem.events.BalanceChangeEvent;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class LiteSQLManager implements DatabaseManager {

    private final Main plugin;
    private Connection connection;
    private final File databaseFile;
    private final ReentrantLock writeLock = new ReentrantLock();
    private String serverId = "unknown";

    public LiteSQLManager(Main plugin) {
        this.plugin = plugin;

        String fileName = plugin.getConfig().getString("database.litesql.file", "economy.db");
        this.databaseFile = new File(plugin.getDataFolder(), fileName);
    }

    @Override
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public void connect() {
        try {
            if (!databaseFile.exists()) {
                if (databaseFile.getParentFile().mkdirs()) {
                    plugin.getLogger().info("Created directories for LiteSQL database.");
                }
                if (databaseFile.createNewFile()) {
                    plugin.getLogger().info("LiteSQL database file created: " + databaseFile.getAbsolutePath());
                } else {
                    plugin.getLogger().warning("Failed to create LiteSQL database file.");
                }
            }

            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("Connected to LiteSQL database at " + databaseFile.getName());

            createTables();

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create LiteSQL database file: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error connecting to LiteSQL database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() {
        String createEconomyTable = "CREATE TABLE IF NOT EXISTS economy (" +
                "uuid TEXT PRIMARY KEY, " +
                "balance REAL DEFAULT 0, " +
                "created_at INTEGER DEFAULT (strftime('%s', 'now') * 1000), " +
                "updated_at INTEGER);";

        String balanceIndex = "CREATE INDEX IF NOT EXISTS idx_economy_balance ON economy (balance);";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createEconomyTable);
            stmt.executeUpdate(balanceIndex);
            plugin.getLogger().info("Tables checked/created with balance index.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error creating tables in LiteSQL!");
        }

        new MigrationManager(plugin, this).run();
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                plugin.getLogger().warning("SQLite connection lost, reconnecting...");
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Connection check failed, reconnecting...");
            connect();
        }
        return connection;
    }

    private void executeWrite(Runnable task) {
        writeLock.lock();
        try {
            task.run();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void createAccount(String uuid) {
        executeWrite(() -> {
            double startBalance = plugin.getConfig().getDouble("economy.start-balance");
            try (PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, ?)")) {
                ps.setString(1, uuid);
                ps.setDouble(2, startBalance);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    plugin.getLogger().info("Account created for " + uuid + " with balance " + startBalance);
                    createTransaction("SERVER", uuid, startBalance, "SERVER");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error creating account for " + uuid);
            }
        });
    }

    @Override
    public boolean accountExists(String uuid) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT 1 FROM economy WHERE uuid = ? LIMIT 1")) {
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
        double balance = 0;
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT balance FROM economy WHERE uuid = ?")) {
            statement.setString(1, uuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error fetching balance for UUID: " + uuid);
        }
        return balance;
    }

    @Override
    public void setBalance(String uuid, double balance) {
        final double finalBalance = Math.max(0, balance);
        executeWrite(() -> {
            try (PreparedStatement statement = getConnection().prepareStatement(
                    "UPDATE economy SET balance = ?, updated_at = ? WHERE uuid = ?")) {
                statement.setDouble(1, finalBalance);
                statement.setLong(2, System.currentTimeMillis());
                statement.setString(3, uuid);
                statement.executeUpdate();

                BalanceChangeEvent balanceChangeEvent = new BalanceChangeEvent(Bukkit.getPlayer(UUID.fromString(uuid)),
                        finalBalance, !Bukkit.isPrimaryThread());
                Bukkit.getPluginManager().callEvent(balanceChangeEvent);

            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error setting balance for UUID: " + uuid);
            }
        });
    }

    @Override
    public void addBalance(String uuid, double amount) {
        executeWrite(() -> {
            String sql = "UPDATE economy SET balance = balance + ?, updated_at = ? WHERE uuid = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setDouble(1, amount);
                ps.setLong(2, System.currentTimeMillis());
                ps.setString(3, uuid);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error adding balance for " + uuid);
            }
        });
    }

    @Override
    public void removeBalance(String uuid, double amount) {
        executeWrite(() -> {
            String sql = "UPDATE economy SET balance = MAX(0, balance - ?), updated_at = ? WHERE uuid = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setDouble(1, amount);
                ps.setLong(2, System.currentTimeMillis());
                ps.setString(3, uuid);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error removing balance for " + uuid);
            }
        });
    }

    @Override
    public void createTransaction(String uuidFrom, String uuidTo, double amount, String transactionType) {
        executeWrite(() -> {
            String sql = "INSERT INTO economy_transactions (uuid_from, uuid_to, amount, transaction_type, server_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, uuidFrom);
                ps.setString(2, uuidTo);
                ps.setDouble(3, amount);
                ps.setString(4, transactionType);
                ps.setString(5, serverId);
                ps.setLong(6, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getLogger().severe("Error creating transaction for " + uuidFrom + " to " + uuidTo);
            }
        });
    }

    @Override
    public boolean transferBalance(String from, String to, double amount) {
        writeLock.lock();
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);

            String deduct = "UPDATE economy SET balance = balance - ?, updated_at = ? WHERE uuid = ? AND balance >= ?";
            try (PreparedStatement ps = conn.prepareStatement(deduct)) {
                ps.setDouble(1, amount);
                ps.setLong(2, System.currentTimeMillis());
                ps.setString(3, from);
                ps.setDouble(4, amount);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String credit = "UPDATE economy SET balance = balance + ?, updated_at = ? WHERE uuid = ?";
            try (PreparedStatement ps = conn.prepareStatement(credit)) {
                ps.setDouble(1, amount);
                ps.setLong(2, System.currentTimeMillis());
                ps.setString(3, to);
                ps.executeUpdate();
            }

            String tx = "INSERT INTO economy_transactions (uuid_from, uuid_to, amount, transaction_type, server_id, created_at) VALUES (?, ?, ?, 'PAY', ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(tx)) {
                ps.setString(1, from);
                ps.setString(2, to);
                ps.setDouble(3, amount);
                ps.setString(4, serverId);
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            plugin.getLogger().severe("Error transferring balance: " + e.getMessage());
            return false;
        } finally {
            try {
                getConnection().setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            writeLock.unlock();
        }
    }

    @Override
    public LinkedHashMap<String, Double> getTopBalances(int limit) {
        LinkedHashMap<String, Double> topBalances = new LinkedHashMap<>();
        String sql = "SELECT uuid, balance FROM economy ORDER BY balance DESC LIMIT ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topBalances.put(rs.getString("uuid"), rs.getDouble("balance"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error fetching top balances.");
        }
        return topBalances;
    }

    @Override
    public void batchUpdateBalance(Map<String, Double> entries) {
        if (entries == null || entries.isEmpty()) return;

        writeLock.lock();
        try {
            String sql = "UPDATE economy SET balance = ?, updated_at = ? WHERE uuid = ?";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                for (Map.Entry<String, Double> entry : entries.entrySet()) {
                    double balance = Math.max(0, entry.getValue());
                    ps.setDouble(1, balance);
                    ps.setLong(2, System.currentTimeMillis());
                    ps.setString(3, entry.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            for (Map.Entry<String, Double> entry : entries.entrySet()) {
                try {
                    BalanceChangeEvent evt = new BalanceChangeEvent(
                            Bukkit.getPlayer(UUID.fromString(entry.getKey())),
                            entry.getValue(),
                            !Bukkit.isPrimaryThread());
                    Bukkit.getPluginManager().callEvent(evt);
                } catch (IllegalArgumentException ignored) {
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Error in SQLite batch balance update: " + e.getMessage());
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int purgeTransactions(long beforeDate) {
        String sql = "DELETE FROM economy_transactions WHERE created_at < ?";
        writeLock.lock();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, beforeDate);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Error purging transactions.");
            return 0;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("LiteSQL connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().severe("Error closing LiteSQL connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Connection getConnectionPublic() {
        return getConnection();
    }
}

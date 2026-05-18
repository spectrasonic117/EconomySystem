package com.spectrasonic.economySystem.database;

import com.spectrasonic.economySystem.Main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MigrationManager {

    private final Main plugin;
    private final DatabaseManager databaseManager;
    private final List<Migration> migrations = new ArrayList<>();

    public MigrationManager(Main plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;

        migrations.add(new Migration1_RecreateTransactions(databaseManager));
        migrations.add(new Migration2_AddUpdatedAt(databaseManager));
    }

    public void run() {
        try (Connection conn = getConnection()) {
            createSchemaVersionTable(conn);
            int currentVersion = getCurrentVersion(conn);

            List<Migration> pending = migrations.stream()
                    .filter(m -> m.version() > currentVersion)
                    .sorted((a, b) -> Integer.compare(a.version(), b.version()))
                    .toList();

            if (pending.isEmpty()) {
                plugin.getLogger().info("Database schema is up to date (version " + currentVersion + ").");
                return;
            }

            for (Migration migration : pending) {
                conn.setAutoCommit(false);
                try {
                    plugin.getLogger().info("Running migration v" + migration.version() + ": " + migration.description());
                    migration.up(conn);
                    registerVersion(conn, migration.version());
                    conn.commit();
                    plugin.getLogger().info("Migration v" + migration.version() + " applied successfully.");
                } catch (SQLException e) {
                    conn.rollback();
                    plugin.getLogger().severe("Migration v" + migration.version() + " failed: " + e.getMessage());
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Migration system error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql;
        if (databaseManager instanceof LiteSQLManager) {
            sql = "CREATE TABLE IF NOT EXISTS schema_version (version INTEGER PRIMARY KEY, applied_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000))";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS schema_version (version INT PRIMARY KEY, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private int getCurrentVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void registerVersion(Connection conn, int version) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO schema_version (version) VALUES (?)")) {
            ps.setInt(1, version);
            ps.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        if (databaseManager instanceof AbstractHikariManager hikari) {
            return hikari.dataSource.getConnection();
        } else if (databaseManager instanceof LiteSQLManager lite) {
            return lite.getConnectionPublic();
        }
        throw new SQLException("Unsupported DatabaseManager type");
    }

    private static class Migration1_RecreateTransactions implements Migration {
        private final DatabaseManager db;

        Migration1_RecreateTransactions(DatabaseManager db) {
            this.db = db;
        }

        @Override
        public int version() {
            return 1;
        }

        @Override
        public String description() {
            return "Recreate economy_transactions with new schema (PK, indexes, new columns)";
        }

        @Override
        public void up(Connection conn) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP TABLE IF EXISTS economy_transactions");
            }

            String createSql;
            if (db instanceof LiteSQLManager) {
                createSql = """
                        CREATE TABLE economy_transactions (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            uuid_from TEXT NOT NULL,
                            uuid_to TEXT NOT NULL,
                            amount REAL NOT NULL,
                            transaction_type TEXT NOT NULL DEFAULT 'PAY',
                            server_id TEXT,
                            created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now') * 1000)
                        )""";
            } else {
                createSql = """
                        CREATE TABLE economy_transactions (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            uuid_from VARCHAR(36) NOT NULL,
                            uuid_to VARCHAR(36) NOT NULL,
                            amount DOUBLE NOT NULL,
                            transaction_type VARCHAR(16) NOT NULL DEFAULT 'PAY',
                            server_id VARCHAR(36),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )""";
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createSql);
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tx_uuid_from ON economy_transactions (uuid_from)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tx_uuid_to ON economy_transactions (uuid_to)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tx_created_at ON economy_transactions (created_at)");
                stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_tx_type ON economy_transactions (transaction_type)");
            }
        }
    }

    private static class Migration2_AddUpdatedAt implements Migration {
        private final DatabaseManager db;

        Migration2_AddUpdatedAt(DatabaseManager db) {
            this.db = db;
        }

        @Override
        public int version() {
            return 2;
        }

        @Override
        public String description() {
            return "Add updated_at column to economy table";
        }

        @Override
        public void up(Connection conn) throws SQLException {
            String sql;
            if (db instanceof LiteSQLManager) {
                sql = "ALTER TABLE economy ADD COLUMN updated_at INTEGER";
            } else {
                sql = "ALTER TABLE economy ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP";
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate column")) {
                    // Column already exists, safe to ignore
                } else {
                    throw e;
                }
            }
        }
    }
}

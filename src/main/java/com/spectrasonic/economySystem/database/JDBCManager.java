package com.spectrasonic.economySystem.database;

import com.spectrasonic.economySystem.Main;

public class JDBCManager extends AbstractHikariManager {

    public JDBCManager(Main plugin) {
        super(plugin);
    }

    @Override
    protected String getJdbcUrl() {
        return plugin.getConfig().getString("database.url.jdbc", "");
    }

    @Override
    protected String getDriverClassName() {
        String url = getJdbcUrl();
        if (url.startsWith("jdbc:mariadb:")) {
            return "org.mariadb.jdbc.Driver";
        }
        // Let HikariCP try to resolve other drivers or return null
        return null;
    }

    @Override
    protected String getUsername() {
        // Assuming credentials are in the URL as per user example
        return null;
    }

    @Override
    protected String getPassword() {
        // Assuming credentials are in the URL as per user example
        return null;
    }
}

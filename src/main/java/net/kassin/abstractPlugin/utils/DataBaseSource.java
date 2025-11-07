package net.kassin.abstractPlugin.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.SQLException;

public class DataBaseSource {

    private String url;
    private String user;
    private String password;
    private HikariDataSource dataSource;

    private DataBaseSource() {
    }

    public static DataBaseSource create(String url, String user, String password) {
        DataBaseSource dataBaseSource = new DataBaseSource();
        dataBaseSource.url = url;
        dataBaseSource.user = user;
        dataBaseSource.password = password;
        dataBaseSource.dataSource = new HikariDataSource(dataBaseSource.getConfig());
        return dataBaseSource;
    }

    public static DataBaseSource create(ConfigurationSection section) {
        String url = section.getString("url");
        String user = section.getString("user");
        String password = section.getString("password");
        return create(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    private HikariConfig getConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(20);
        hikariConfig.setConnectionTimeout(3000);
        hikariConfig.setLeakDetectionThreshold(5000);
        hikariConfig.setPoolName("StatsPool");
        return hikariConfig;
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("[Database] HikariCP: Fonte de dados fechada com sucesso.");
        }
    }

}
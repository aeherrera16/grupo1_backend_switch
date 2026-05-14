package ec.edu.espe.banquito.switchpagos.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class LocalPostgresDatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String PROFILE_LOCAL = "local";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        boolean localProfileActive = Arrays.asList(env.getActiveProfiles()).contains(PROFILE_LOCAL);
        boolean autoCreateEnabled = env.getProperty("app.db.auto-create", Boolean.class, false);

        if (!localProfileActive && !autoCreateEnabled) {
            return;
        }

        String host = env.getProperty("DB_HOST", "localhost");
        String port = env.getProperty("DB_PORT", "5432");
        String databaseName = env.getProperty("DB_NAME", "switch_pagos");
        String username = env.getProperty("DB_USER", "postgres");
        String password = env.getProperty("DB_PASSWORD", "1234");
        String adminDatabase = env.getProperty("APP_DB_ADMIN_DB", "postgres");

        if (!databaseName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalStateException("DB_NAME contains invalid characters. Use only letters, numbers and underscore.");
        }

        String adminUrl = "jdbc:postgresql://" + host + ":" + port + "/" + adminDatabase;

        try (Connection connection = DriverManager.getConnection(adminUrl, username, password)) {
            if (!databaseExists(connection, databaseName)) {
                String createSql = "CREATE DATABASE \"" + databaseName + "\"";
                try (Statement statement = connection.createStatement()) {
                    statement.execute(createSql);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to auto-create PostgreSQL database '" + databaseName + "'", ex);
        }
    }

    private static boolean databaseExists(Connection connection, String databaseName) throws Exception {
        String sql = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, databaseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

}

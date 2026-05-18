package ec.edu.espe.banquito.switchpagos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(@Value("${spring.datasource.url}") String url,
                                 @Value("${spring.datasource.username}") String username,
                                 @Value("${spring.datasource.password}") String password,
                                 @Value("${app.db.auto-create:false}") boolean autoCreate) {

        if (autoCreate) {
            createDatabaseIfNotExists(url, username, password);
        }

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    private void createDatabaseIfNotExists(String url, String username, String password) {
        String dbName = extractDatabaseName(url);
        String postgresUrl = url.replace("/" + dbName, "/postgres");

        try {
            try (Connection conn = DriverManager.getConnection(postgresUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                var rs = stmt.executeQuery("SELECT COUNT(*) FROM pg_database WHERE datname = '" + dbName + "'");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count == 0) {
                        logger.info("Database '{}' does not exist. Creating it...", dbName);
                        stmt.execute("CREATE DATABASE " + dbName);
                        logger.info("Database '{}' created successfully", dbName);
                    } else {
                        logger.info("Database '{}' already exists", dbName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create database '{}': {}", dbName, e.getMessage());
            throw new RuntimeException("Failed to create database", e);
        }
    }

    private String extractDatabaseName(String url) {
        int lastSlash = url.lastIndexOf('/');
        return url.substring(lastSlash + 1);
    }
}

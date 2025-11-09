package com.fabric.messagemod.database;

import com.fabric.messagemod.database.entity.MessageEntity;
import com.fabric.messagemod.database.repository.MessageRepository;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;

    private EntityManagerFactory emf;
    private MessageRepository messageRepository;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize(Path configDir) {
        try {
            Properties props = loadDatabaseConfig(configDir);

            String url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/minecraft");
            String user = props.getProperty("db.username", "postgres");
            String pass = props.getProperty("db.password", "password");

            // Run Flyway migrations
            Flyway.configure()
                    .dataSource(url, user, pass)
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            // Hibernate configuration
            Configuration cfg = new Configuration()
                    .addAnnotatedClass(MessageEntity.class)
                    .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                    .setProperty("hibernate.connection.url", url)
                    .setProperty("hibernate.connection.username", user)
                    .setProperty("hibernate.connection.password", pass)
                    .setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
                    .setProperty("hibernate.hikari.minimumIdle", "5")
                    .setProperty("hibernate.hikari.maximumPoolSize", "10")
                    .setProperty("hibernate.hikari.idleTimeout", "30000")
                    .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                    .setProperty("hibernate.hbm2ddl.auto", "validate")
                    .setProperty("hibernate.show_sql", "false")
                    .setProperty("hibernate.format_sql", "true");

            emf = cfg.buildSessionFactory();
            messageRepository = new MessageRepository(emf);

            LOGGER.info("Database initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private Properties loadDatabaseConfig(Path configDir) throws IOException {
        Properties props = new Properties();
        Path configFile = configDir.resolve("database.properties");

        if (!Files.exists(configFile)) {
            // Create default config
            props.setProperty("db.url", "jdbc:postgresql://localhost:5432/minecraft");
            props.setProperty("db.username", "postgres");
            props.setProperty("db.password", "password");

            Files.createDirectories(configDir);
            try (var out = Files.newOutputStream(configFile)) {
                props.store(out, "Database configuration");
            }
            LOGGER.info("Created default database configuration at {}", configFile);
        } else {
            try (var in = Files.newInputStream(configFile)) {
                props.load(in);
            }
        }

        return props;
    }

    public MessageRepository getMessageRepository() {
        return messageRepository;
    }

    public void shutdown() {
        if (messageRepository != null) {
            messageRepository.close();
        }
        LOGGER.info("Database shut down");
    }
}

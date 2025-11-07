package com.fabric.messagemod.database;

import com.fabric.messagemod.database.entity.MessageEntity;
import com.fabric.messagemod.database.repository.MessageRepository;
import jakarta.persistence.EntityManagerFactory;
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
            Properties dbProperties = loadDatabaseConfig(configDir);

            Configuration configuration = new Configuration();
            configuration.addAnnotatedClass(MessageEntity.class);

            // Database connection settings
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            configuration.setProperty("hibernate.connection.url",
                    dbProperties.getProperty("db.url", "jdbc:postgresql://localhost:5432/minecraft"));
            configuration.setProperty("hibernate.connection.username",
                    dbProperties.getProperty("db.username", "postgres"));
            configuration.setProperty("hibernate.connection.password",
                    dbProperties.getProperty("db.password", "password"));

            // HikariCP connection pool settings
            configuration.setProperty("hibernate.connection.provider_class",
                    "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
            configuration.setProperty("hibernate.hikari.minimumIdle", "5");
            configuration.setProperty("hibernate.hikari.maximumPoolSize", "10");
            configuration.setProperty("hibernate.hikari.idleTimeout", "30000");

            // Hibernate settings
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "true");

            emf = configuration.buildSessionFactory();
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

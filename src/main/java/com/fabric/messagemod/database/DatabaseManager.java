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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private static final Path PROJECT_ROOT = Path.of(".").toAbsolutePath().normalize();
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
            String url = requireSetting("DB_URL");
            String user = requireSetting("DB_USERNAME");
            String password = requireSetting("DB_PASSWORD");

            Flyway.configure()
                    .dataSource(url, user, password)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();

            ensureMessageIdType(url, user, password);

            Configuration cfg = new Configuration()
                    .addAnnotatedClass(MessageEntity.class)
                    .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                    .setProperty("hibernate.connection.url", url)
                    .setProperty("hibernate.connection.username", user)
                    .setProperty("hibernate.connection.password", password)
                    .setProperty("hibernate.bytecode.provider", "bytebuddy")
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

    private static void ensureMessageIdType(String url, String user, String password) throws Exception {
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE IF EXISTS messages ALTER COLUMN id TYPE BIGINT USING id::BIGINT");
            LOGGER.info("Ensured messages.id is BIGINT");
        }
    }

    private static String requireSetting(String key) {
        String value = readSetting(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key + ". Add it to the environment or .env file.");
        }
        return value;
    }

    private static String readSetting(String key) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        for (Path envFile : findEnvFiles()) {
            if (!Files.exists(envFile)) {
                continue;
            }

            try {
                for (String line : Files.readAllLines(envFile)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    int idx = trimmed.indexOf('=');
                    if (idx <= 0) {
                        continue;
                    }

                    String name = trimmed.substring(0, idx).trim();
                    if (!key.equals(name)) {
                        continue;
                    }

                    String value = trimmed.substring(idx + 1).trim();
                    if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to read {} file", envFile, e);
            }
        }

        return null;
    }

    private static java.util.List<Path> findEnvFiles() {
        java.util.List<Path> candidates = new java.util.ArrayList<>();
        Path current = Path.of(".").toAbsolutePath().normalize();
        candidates.add(current.resolve(".env"));
        candidates.add(PROJECT_ROOT.resolve(".env"));

        Path parent = current;
        for (int i = 0; i < 8; i++) {
            parent = parent.getParent();
            if (parent == null) {
                break;
            }
            candidates.add(parent.resolve(".env"));
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            candidates.add(Path.of(userHome).resolve(".env"));
        }

        java.util.List<Path> unique = new java.util.ArrayList<>();
        for (Path candidate : candidates) {
            if (candidate != null && !unique.contains(candidate)) {
                unique.add(candidate);
            }
        }
        return unique;
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

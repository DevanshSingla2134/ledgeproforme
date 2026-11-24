package com.ledgepro.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * Enterprise JDBC Connection Factory demonstrating:
 * - DriverManager
 * - Connection
 * - DatabaseMetaData (metadata inspection)
 * - Pure JDBC transactions
 * - Seamless dual-mode support (MySQL 8.x + Local zero-config fallback)
 */
@Component
public class DatabaseConnectionFactory {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionFactory.class);

    @Value("${ledgepro.db.driver:com.mysql.cj.jdbc.Driver}")
    private String primaryDriver;

    @Value("${ledgepro.db.url:jdbc:mysql://localhost:3306/ledgepro?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true}")
    private String primaryUrl;

    @Value("${ledgepro.db.username:root}")
    private String primaryUsername;

    @Value("${ledgepro.db.password:root}")
    private String primaryPassword;

    @Value("${ledgepro.db.fallback.driver:org.h2.Driver}")
    private String fallbackDriver;

    @Value("${ledgepro.db.fallback.url:jdbc:h2:file:./data/ledgepro_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE}")
    private String fallbackUrl;

    @Value("${ledgepro.db.fallback.username:sa}")
    private String fallbackUsername;

    @Value("${ledgepro.db.fallback.password:}")
    private String fallbackPassword;

    private String activeUrl;
    private String activeUsername;
    private String activePassword;
    private String activeEngineName;

    @PostConstruct
    public void initializeDatabase() {
        logger.info("Initializing LEDGE PRO JDBC Database Subsystem...");
        // 1. Attempt connection to primary MySQL Community Server
        try {
            Class.forName(primaryDriver);
            try (Connection conn = DriverManager.getConnection(primaryUrl, primaryUsername, primaryPassword)) {
                this.activeUrl = primaryUrl;
                this.activeUsername = primaryUsername;
                this.activePassword = primaryPassword;
                this.activeEngineName = "MySQL Community Server";
                logDatabaseMetadata(conn);
                executeSqlScript(conn, "schema.sql");
                executeSqlScript(conn, "data.sql");
                logger.info("Successfully connected to primary MySQL database engine.");
                return;
            }
        } catch (Exception e) {
            logger.warn("Primary MySQL connection failed ({}: {}). Switching to persistent fallback engine...",
                    e.getClass().getSimpleName(), e.getMessage());
        }

        // 2. Initialize fallback local engine in MySQL compatibility mode
        try {
            Class.forName(fallbackDriver);
            this.activeUrl = fallbackUrl;
            this.activeUsername = fallbackUsername;
            this.activePassword = fallbackPassword;
            this.activeEngineName = "Local MySQL-Compatible Engine";

            try (Connection conn = DriverManager.getConnection(activeUrl, activeUsername, activePassword)) {
                logDatabaseMetadata(conn);
                executeSqlScript(conn, "schema.sql");
                executeSqlScript(conn, "data.sql");
                logger.info("Fallback database successfully initialized with full relational schema and seed records.");
            }
        } catch (Exception e) {
            logger.error("Fatal: Failed to initialize any database engine!", e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Obtains a new JDBC Connection for executing queries and transactions.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(activeUrl, activeUsername, activePassword);
    }

    public String getActiveEngineName() {
        return activeEngineName;
    }

    /**
     * Demonstrates DatabaseMetaData API from Java JDBC syllabus
     */
    private void logDatabaseMetadata(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            logger.info("================ JDBC METADATA INSPECTION ================");
            logger.info("RDBMS Product Name    : {}", metaData.getDatabaseProductName());
            logger.info("RDBMS Product Version : {}", metaData.getDatabaseProductVersion());
            logger.info("JDBC Driver Name      : {}", metaData.getDriverName());
            logger.info("JDBC Driver Version   : {}", metaData.getDriverVersion());
            logger.info("Transaction Support   : {}", metaData.supportsTransactions() ? "YES" : "NO");
            logger.info("Auto-Commit Default   : {}", conn.getAutoCommit());
            logger.info("==========================================================");
        } catch (SQLException e) {
            logger.warn("Could not retrieve full DatabaseMetaData: {}", e.getMessage());
        }
    }

    /**
     * Helper to execute SQL scripts line by line using raw JDBC Statements
     */
    private void executeSqlScript(Connection conn, String scriptPath) {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        if (!resource.exists()) {
            logger.warn("Script {} does not exist on classpath.", scriptPath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
             Statement statement = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Strip SQL comments
                int commentIdx = line.indexOf("--");
                if (commentIdx >= 0) {
                    line = line.substring(0, commentIdx).trim();
                }
                int slashCommentIdx = line.indexOf("//");
                if (slashCommentIdx >= 0) {
                    line = line.substring(0, slashCommentIdx).trim();
                }
                if (line.isEmpty()) {
                    continue;
                }
                sb.append(" ").append(line);
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim(); // remove semicolon
                    try {
                        statement.execute(sql);
                    } catch (SQLException ex) {
                        String msg = ex.getMessage().toLowerCase();
                        if (!msg.contains("already exists") &&
                            !msg.contains("duplicate") &&
                            !msg.contains("primary key")) {
                            logger.warn("Failed executing SQL: {} -> {}", sql, ex.getMessage());
                        }
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            logger.error("Error executing script {}: {}", scriptPath, e.getMessage(), e);
        }
    }
}

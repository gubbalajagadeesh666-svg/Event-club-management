package com.campusflow.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/campusflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";

    private static final List<String> PLACEHOLDER_PASSWORDS = List.of(
            "tharun",
            "tharun",
            "tharun"
    );

    private record LoadedConfig(String url, String user, String password, String sourceDescription) {
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new ExceptionInInitializerError("MySQL Connector/J driver not found on classpath: " + ex.getMessage());
        }
    }

    private DBConnection() {
    }

    private static LoadedConfig loadConfig() {
        Properties fileProps = new Properties();
        String source = "Default fallback (no db.properties or env vars found)";

        // 1. Check classpath (WEB-INF/classes/db.properties)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = DBConnection.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream("db.properties")) {
            if (in != null) {
                fileProps.load(in);
                source = "Classpath (db.properties)";
            }
        } catch (IOException ignored) {
        }

        // 2. Check deployed Tomcat WEB-INF/db.properties
        if (fileProps.isEmpty()) {
            try {
                if (DBConnection.class.getProtectionDomain().getCodeSource() != null) {
                    URI codeUri = DBConnection.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                    if ("file".equalsIgnoreCase(codeUri.getScheme())) {
                        Path classesPath = Paths.get(codeUri);
                        Path webInfProps = classesPath.resolve("../db.properties").normalize();
                        if (Files.isRegularFile(webInfProps)) {
                            try (InputStream in = Files.newInputStream(webInfProps)) {
                                fileProps.load(in);
                                source = webInfProps.toString();
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3. Check Eclipse project paths directly (handles Eclipse m2e resource exclusions)
        if (fileProps.isEmpty()) {
            List<Path> candidatePaths = List.of(
                    Paths.get("src/main/webapp/WEB-INF/db.properties"),
                    Paths.get("src/main/resources/db.properties"),
                    Paths.get("src/main/java/db.properties"),
                    Paths.get("db.properties")
            );
            for (Path candidate : candidatePaths) {
                if (Files.isRegularFile(candidate)) {
                    try (InputStream in = Files.newInputStream(candidate)) {
                        fileProps.load(in);
                        source = candidate.toAbsolutePath().toString();
                        break;
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        String envPass = System.getenv("CAMPUSFLOW_DB_PASSWORD");
        String sysPass = System.getProperty("db.password");
        if (envPass != null) {
            source = "Environment variable (CAMPUSFLOW_DB_PASSWORD)";
        } else if (sysPass != null) {
            source = "JVM System property (-Ddb.password)";
        }

        String url = firstNonBlank(
                System.getenv("CAMPUSFLOW_DB_URL"),
                System.getProperty("db.url"),
                fileProps.getProperty("db.url"),
                DEFAULT_URL
        );
        String user = firstNonBlank(
                System.getenv("CAMPUSFLOW_DB_USER"),
                System.getProperty("db.user"),
                fileProps.getProperty("db.user"),
                DEFAULT_USER
        );
        String password = firstDefined(
                envPass,
                sysPass,
                fileProps.getProperty("db.password"),
                ""
        );

        return new LoadedConfig(url, user, password, source);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    private static String firstDefined(String... values) {
        for (String v : values) {
            if (v != null) {
                return v.trim();
            }
        }
        return "";
    }

    public static Connection getConnection() throws SQLException {
        LoadedConfig cfg = loadConfig();
        return DriverManager.getConnection(cfg.url(), cfg.user(), cfg.password());
    }

    public static boolean isDatabaseReachable() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (SQLException ex) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CampusClubSystem JDBC Connectivity Check ===");
        LoadedConfig cfg = loadConfig();
        System.out.println("Working Directory : " + Paths.get("").toAbsolutePath());
        System.out.println("Config Source     : " + cfg.sourceDescription());
        System.out.println("Configured User   : " + cfg.user());

        if (cfg.password().isEmpty()) {
            System.out.println("Password Loaded   : NO (empty) -> Create src/main/webapp/WEB-INF/db.properties");
        } else if (PLACEHOLDER_PASSWORDS.contains(cfg.password())) {
            System.out.println("Password Loaded   : PLACEHOLDER DETECTED -> Replace placeholder in db.properties with your real MySQL password!");
        } else {
            System.out.println("Password Loaded   : YES (length: " + cfg.password().length() + " characters)");
        }

        try (Connection conn = DriverManager.getConnection(cfg.url(), cfg.user(), cfg.password())) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Connection Status : SUCCESS");
            System.out.println("Database Product  : " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("JDBC Driver       : " + meta.getDriverName() + " " + meta.getDriverVersion());
            System.out.println("Catalog / Schema  : " + conn.getCatalog());

            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM clubs");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Seeded Clubs Count: " + rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Connection Status : FAILED");
            System.err.println("SQLState          : " + ex.getSQLState() + " | ErrorCode: " + ex.getErrorCode());
            System.err.println("Message           : " + ex.getMessage());
            if (ex.getErrorCode() == 1049) {
                System.err.println("Hint              : Database 'campusflow_db' does not exist yet. Run database/schema.sql in MySQL Workbench!");
            }
            System.exit(1);
        }
    }
}
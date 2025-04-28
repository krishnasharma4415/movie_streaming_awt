package utils;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserDatabase {
    private static final String DB_URL = "jdbc:sqlite:movie_streaming.db";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Create users table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }

            // Check if default admin user exists
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(checkAdmin)) {
                pstmt.setString(1, DEFAULT_USERNAME);
                ResultSet rs = pstmt.executeQuery();
                if (rs.getInt(1) == 0) {
                    // Create default admin user
                    String insertAdmin = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertAdmin)) {
                        insertStmt.setString(1, DEFAULT_USERNAME);
                        insertStmt.setString(2, hashPassword(DEFAULT_PASSWORD));
                        insertStmt.setString(3, "admin@movieapp.com");
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    public static boolean validateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return storedHash.equals(hashPassword(password));
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Login validation error: " + e.getMessage());
            return false;
        }
    }

    public static boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public static boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Username check error: " + e.getMessage());
            return false;
        }
    }

    public static boolean isEmailTaken(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Email check error: " + e.getMessage());
            return false;
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Password hashing error: " + e.getMessage());
            return password; // Fallback to plain password (not recommended for production)
        }
    }
}
import java.sql.*;

public class MovieDatabase {
    private static final String DB_URL = "jdbc:sqlite:data/movies.db";
    
    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create movies table
            String sql = "CREATE TABLE IF NOT EXISTS movies (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "title TEXT NOT NULL," +
                         "genre TEXT," +
                         "year INTEGER," +
                         "file_path TEXT NOT NULL," +
                         "duration INTEGER," +
                         "description TEXT)";
            stmt.execute(sql);
            
            // Create users table
            sql = "CREATE TABLE IF NOT EXISTS users (" +
                  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                  "username TEXT UNIQUE NOT NULL," +
                  "password TEXT NOT NULL," +
                  "email TEXT)";
            stmt.execute(sql);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
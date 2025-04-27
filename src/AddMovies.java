import java.io.File;
import java.sql.*;

public class AddMovies {
    public static void main(String[] args) {
        System.out.println("Scanning media folder for MP4 files...");
        
        try (Connection conn = MovieDatabase.getConnection()) {
            // Clear existing movies (optional)
            Statement clearStmt = conn.createStatement();
            clearStmt.execute("DELETE FROM movies");
            
            // Scan media folder
            File mediaDir = new File("media");
            if (!mediaDir.exists()) {
                System.out.println("Error: 'media' folder not found");
                return;
            }
            
            File[] mp4Files = mediaDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".mp4"));
            
            if (mp4Files == null || mp4Files.length == 0) {
                System.out.println("No MP4 files found in media folder");
                return;
            }
            
            // Add each MP4 file to database
            for (File file : mp4Files) {
                String title = file.getName().replace(".mp4", "");
                String filePath = "media/" + file.getName();
                
                // Insert movie
                String sql = "INSERT INTO movies (title, file_path, genre, year, duration, description) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, title);
                    pstmt.setString(2, filePath);
                    pstmt.setString(3, "Unknown");
                    pstmt.setInt(4, 2023); // Default year
                    pstmt.setInt(5, 90);   // Default duration (minutes)
                    pstmt.setString(6, "No description available");
                    
                    pstmt.executeUpdate();
                    System.out.println("Added: " + title);
                }
            }
            
            System.out.println("\nSuccessfully added " + mp4Files.length + " movies to database");
            MovieDatabase.printAllMovies();
            
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
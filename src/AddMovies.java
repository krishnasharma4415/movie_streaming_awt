import java.io.File;
import java.sql.*;

public class AddMovies {
    public static void main(String[] args) {
        try (Connection conn = MovieDatabase.getConnection()) {
            // Clear existing movies (optional)
            Statement clearStmt = conn.createStatement();
            clearStmt.execute("DELETE FROM movies");

            // Scan the media folder for MP4 files
            File mediaFolder = new File("media");
            File[] mp4Files = mediaFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));

            if (mp4Files != null) {
                for (File file : mp4Files) {
                    String title = file.getName().replace(".mp4", ""); // Use file name as title
                    String filePath = file.getPath();
                    String genre = "Unknown"; // Default genre
                    int year = 2025; // Default year
                    int duration = 120; // Default duration (in minutes)

                    // Insert movie into the database
                    String sql = "INSERT INTO movies (title, genre, year, file_path, duration, description) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, title);
                    stmt.setString(2, genre);
                    stmt.setInt(3, year);
                    stmt.setString(4, filePath);
                    stmt.setInt(5, duration);
                    stmt.setString(6, "No description available");
                    stmt.executeUpdate();
                }
            }

            System.out.println("Movies added successfully from the media folder.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
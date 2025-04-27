import java.io.File;
import java.sql.*;

public class AddMovies {
    public static void main(String[] args) {
        System.out.println("Scanning media/movies folder for MP4 files...");

        File moviesFolder = new File("media/movies");
        File[] movieFiles = moviesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4"));

        if (movieFiles == null || movieFiles.length == 0) {
            System.out.println("No movies found in the media/movies folder.");
            return;
        }

        try (Connection conn = MovieDatabase.getConnection()) {
            // Clear existing movies (optional)
            Statement clearStmt = conn.createStatement();
            clearStmt.execute("DELETE FROM movies");

            // Insert movies into the database
            String sql = "INSERT INTO movies (title, file_path, thumbnail_path) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (File movieFile : movieFiles) {
                String title = movieFile.getName().replace(".mp4", ""); // Use file name as title
                String filePath = "media/movies/" + movieFile.getName();
                String thumbnailPath = "media/thumbnails/" + movieFile.getName().replace(".mp4", ".png");

                // Insert into database
                stmt.setString(1, title);
                stmt.setString(2, filePath);
                stmt.setString(3, thumbnailPath);
                stmt.executeUpdate();

                System.out.println("Added movie: " + title);
            }

            System.out.println("Movies added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
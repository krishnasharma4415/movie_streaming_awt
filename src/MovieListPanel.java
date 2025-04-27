import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieListPanel extends Panel {
    private MovieStreamingApp app;
    private List<String> movieTitles = new ArrayList<>();
    private List<String> moviePaths = new ArrayList<>();
    
    public MovieListPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        // Title
        Label titleLabel = new Label("Available Movies", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        // Load movies
        loadMovies();
        
        // Create a scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setSize(800, 600);
        
        // Movie grid
        Panel movieGrid = new Panel();
        movieGrid.setLayout(new GridLayout(0, 3, 15, 15));
        movieGrid.setBackground(new Color(240, 240, 240));
        
        if (movieTitles.isEmpty()) {
            movieGrid.add(new Label("No movies available", Label.CENTER));
        } else {
            for (int i = 0; i < movieTitles.size(); i++) {
                movieGrid.add(createMovieCard(i));
            }
        }
        
        // Add the movie grid to the scroll pane
        scrollPane.add(movieGrid);
        
        // Add the scroll pane to the center of the panel
        add(scrollPane, BorderLayout.CENTER);
        
        // Logout button
        Panel bottomPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button logoutButton = new Button("Logout");
        logoutButton.addActionListener(e -> app.showScreen("LOGIN"));
        bottomPanel.add(logoutButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadMovies() {
        try (Statement stmt = app.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title, file_path FROM movies")) {
            
            while (rs.next()) {
                movieTitles.add(rs.getString("title"));
                moviePaths.add(rs.getString("file_path"));
            }
        } catch (SQLException e) {
            app.showError("Error loading movies: " + e.getMessage());
        }
    }
    
    private Panel createMovieCard(int index) {
        Panel card = new Panel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 220));
        
        // Movie title
        Label titleLabel = new Label(movieTitles.get(index), Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Thumbnail placeholder
        Panel thumbnail = new Panel();
        thumbnail.setBackground(new Color(200, 200, 200));
        thumbnail.setPreferredSize(new Dimension(180, 120));
        thumbnail.add(new Label("🎬", Label.CENTER));
        card.add(thumbnail, BorderLayout.CENTER);
        
        // Play button
        Button playButton = new Button("Play Movie");
        playButton.addActionListener(e -> playMovie(index));
        card.add(playButton, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void playMovie(int index) {
        PlayerPanel player = (PlayerPanel) app.getCardPanel().getComponent(3);
        player.setMovie(moviePaths.get(index));
        app.showScreen("PLAYER");
    }
}
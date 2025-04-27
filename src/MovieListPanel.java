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
        loadMovies();
        add(new ListPanel(), BorderLayout.CENTER);
        
        Panel buttonPanel = new Panel();
        Button backButton = new Button("Logout");
        backButton.addActionListener(e -> app.showScreen("LOGIN"));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadMovies() {
        try {
            Statement stmt = app.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT title, file_path FROM movies");
            
            while (rs.next()) {
                movieTitles.add(rs.getString("title"));
                moviePaths.add(rs.getString("file_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private class ListPanel extends Panel {
        public ListPanel() {
            setLayout(new GridLayout(0, 1, 5, 5));
            
            for (int i = 0; i < movieTitles.size(); i++) {
                Panel movieCard = new Panel(new BorderLayout());
                movieCard.setBackground(Color.LIGHT_GRAY);
                
                Panel infoPanel = new Panel(new GridLayout(0, 1));
                Label titleLabel = new Label(movieTitles.get(i), Label.LEFT);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
                infoPanel.add(titleLabel);
                
                try {
                    PreparedStatement stmt = MovieListPanel.this.app.getConnection().prepareStatement(
                        "SELECT genre, year, duration FROM movies WHERE title = ?");
                    stmt.setString(1, movieTitles.get(i));
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        String info = String.format("%s • %d • %d mins", 
                            rs.getString("genre"),
                            rs.getInt("year"),
                            rs.getInt("duration"));
                        infoPanel.add(new Label(info));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                movieCard.add(infoPanel, BorderLayout.CENTER);
                
                Button playButton = new Button("Play");
                final int index = i;
                playButton.addActionListener(e -> {
                    PlayerPanel player = (PlayerPanel)MovieListPanel.this.app.getCardPanel().getComponent(3);
                    player.setMovie(moviePaths.get(index));
                    MovieListPanel.this.app.showScreen("PLAYER");
                });
                
                Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.add(playButton);
                movieCard.add(buttonPanel, BorderLayout.EAST);
                
                add(movieCard);
            }
        }
    }
}
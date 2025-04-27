import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class PlayerPanel extends Panel {
    private MovieStreamingApp app;
    private String moviePath;
    private Label statusLabel;
    
    public PlayerPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        // Status bar
        statusLabel = new Label("", Label.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);
        
        // Player area
        Panel playerArea = new Panel(new BorderLayout());
        playerArea.setBackground(Color.BLACK);
        playerArea.add(new Label("Movie Player", Label.CENTER) {{
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 24));
        }}, BorderLayout.CENTER);
        
        add(playerArea, BorderLayout.CENTER);
        
        // Controls
        Panel controlPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        Button playButton = new Button("Play");
        playButton.addActionListener(e -> playMovie());
        
        Button backButton = new Button("Back to Movies");
        backButton.addActionListener(e -> app.showScreen("MOVIES"));
        
        controlPanel.add(playButton);
        controlPanel.add(backButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    public void setMovie(String path) {
        this.moviePath = path;
        statusLabel.setText("Selected: " + new File(path).getName());
    }
    
    private void playMovie() {
        if (moviePath == null || moviePath.isEmpty()) {
            app.showError("No movie selected");
            return;
        }
        
        File movieFile = new File(moviePath);
        if (!movieFile.exists()) {
            app.showError("Movie file not found: " + moviePath);
            return;
        }
        
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(movieFile);
                statusLabel.setText("Playing: " + movieFile.getName());
            } else {
                app.showError("Desktop operations not supported on this platform");
            }
        } catch (IOException e) {
            app.showError("Error opening movie: " + e.getMessage());
            
            // Fallback: Try to open in browser
            try {
                Desktop.getDesktop().browse(movieFile.toURI());
            } catch (Exception ex) {
                app.showError("Failed to open in browser: " + ex.getMessage());
            }
        }
    }
}
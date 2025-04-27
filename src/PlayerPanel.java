import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PlayerPanel extends Panel {
    private MovieStreamingApp app;
    private String moviePath;
    private Label statusLabel;
    
    public PlayerPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        
        // Status label
        statusLabel = new Label("", Label.CENTER);
        add(statusLabel, BorderLayout.NORTH);
        
        // Control panel
        Panel controlPanel = new Panel();
        Button playButton = new Button("Play");
        Button backButton = new Button("Back to Movies");
        
        playButton.addActionListener(e -> playMovie());
        backButton.addActionListener(e -> app.showScreen("MOVIES"));
        
        controlPanel.add(playButton);
        controlPanel.add(backButton);
        
        add(controlPanel, BorderLayout.SOUTH);
        
        // Player area - just a placeholder in AWT
        add(new Label("Click Play to watch the movie", Label.CENTER), BorderLayout.CENTER);
    }
    
    public void setMovie(String path) {
        this.moviePath = path;
        statusLabel.setText("Selected: " + new File(path).getName());
    }
    
    private void playMovie() {
        if (moviePath == null || moviePath.isEmpty()) {
            statusLabel.setText("No movie selected");
            return;
        }

        File movieFile = new File(moviePath);
        if (!movieFile.exists()) {
            statusLabel.setText("Movie file not found");
            return;
        }

        try {
            // Try to open with default system player
            Desktop.getDesktop().open(movieFile);
            statusLabel.setText("Playing: " + movieFile.getName());
        } catch (IOException e) {
            statusLabel.setText("Error opening player: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Try to open in browser
            try {
                URI uri = movieFile.toURI();
                Desktop.getDesktop().browse(uri);
            } catch (IOException | UnsupportedOperationException ex) {
                statusLabel.setText("Couldn't open player or browser");
                ex.printStackTrace();
            }
        }
    }
}
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class PlayerPanel extends Panel {
    private MovieStreamingApp app;
    private String moviePath;
    private Label statusLabel;

    public PlayerPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30)); // Dark background

        // Status bar
        statusLabel = new Label("No movie selected", Label.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        add(statusLabel, BorderLayout.NORTH);

        // Video placeholder
        Panel videoArea = new Panel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.drawString("Video Player", getWidth() / 2 - 60, getHeight() / 2);
            }
        };
        videoArea.setPreferredSize(new Dimension(800, 450));
        add(videoArea, BorderLayout.CENTER);

        // Control panel
        Panel controlPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(40, 40, 40));

        // Play button
        Button playButton = new Button("Play");
        styleButton(playButton, new Color(65, 105, 225));
        playButton.addActionListener(e -> playMovie());
        controlPanel.add(playButton);

        // Pause button
        Button pauseButton = new Button("Pause");
        styleButton(pauseButton, new Color(200, 200, 50));
        pauseButton.addActionListener(e -> pauseMovie());
        controlPanel.add(pauseButton);

        // Back button
        Button backButton = new Button("Back to Movies");
        styleButton(backButton, new Color(200, 50, 50));
        backButton.addActionListener(e -> app.showScreen("MOVIES"));
        controlPanel.add(backButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    public void setMovie(String path) {
        this.moviePath = path;
        statusLabel.setText("Playing: " + new File(path).getName());
        repaint();
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
            Desktop.getDesktop().open(movieFile);
            statusLabel.setText("Playing: " + movieFile.getName());
        } catch (IOException e) {
            statusLabel.setText("Error opening player: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void pauseMovie() {
        // Placeholder for pause functionality
        statusLabel.setText("Pause functionality not implemented");
    }

    private void styleButton(Button button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(
                    Math.min(bgColor.getRed() + 20, 255),
                    Math.min(bgColor.getGreen() + 20, 255),
                    Math.min(bgColor.getBlue() + 20, 255)
                ));
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
}
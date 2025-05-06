import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Map;

public class PlayerPanel extends Panel {
    private MovieStreamingApp app;
    private Map<String, Object> movie;
    private Label statusLabel;
    private Label titleLabel;
    private Label infoLabel;
    private Panel videoArea;
    private MediaPanel mediaPanel;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);
    private boolean isPlaying = false;
    private boolean isBuffering = false;
    private Thread bufferingThread;

    public PlayerPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(darkGray);

        // Header with movie title and info
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        titleLabel = new Label("No movie selected", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(darkYellow);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        infoLabel = new Label("", Label.CENTER);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        infoLabel.setForeground(textColor);
        headerPanel.add(infoLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Status bar
        statusLabel = new Label("Ready", Label.LEFT);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(textColor);
        statusLabel.setBackground(new Color(25, 25, 25));
        add(statusLabel, BorderLayout.SOUTH);

        // Video area
        mediaPanel = new MediaPanel();

        // Control panel
        Panel controlPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(lightGray);
        controlPanel.setPreferredSize(new Dimension(getWidth(), 50));

        // Play button
        Button playButton = new Button("Play");
        styleButton(playButton, darkYellow);
        playButton.addActionListener(e -> playMovie());
        controlPanel.add(playButton);

        // Pause button
        Button pauseButton = new Button("Pause");
        styleButton(pauseButton, lightGray);
        pauseButton.addActionListener(e -> pauseMovie());
        controlPanel.add(pauseButton);

        // Stop button
        Button stopButton = new Button("Stop");
        styleButton(stopButton, lightGray);
        stopButton.addActionListener(e -> stopMovie());
        controlPanel.add(stopButton);

        // Back button
        Button backButton = new Button("Back to Movies");
        styleButton(backButton, darkGray);
        backButton.addActionListener(e -> {
            stopMovie();
            app.showScreen("MOVIES");
        });
        controlPanel.add(backButton);

        // Add control panel to the layout
        add(controlPanel, BorderLayout.SOUTH);

        // Initialize video area with placeholder
        videoArea = new Panel(new BorderLayout());
        videoArea.setBackground(Color.BLACK);
        videoArea.add(mediaPanel, BorderLayout.CENTER);
        add(videoArea, BorderLayout.CENTER);
    }

    public void setMovie(Map<String, Object> movie) {
        this.movie = movie;

        // Update UI with movie info
        String title = (String) movie.get("title");
        titleLabel.setText(title);

        // Rating and year
        Double rating = (Double) movie.get("vote_average");
        String year = (String) movie.get("year");
        infoLabel.setText(String.format("Rating: %.1f ★ | Year: %s", rating, year));

        // Reset player state
        isPlaying = false;
        isBuffering = false;

        // Update status
        statusLabel.setText("Ready to play: " + title);

        // Reset media panel
        mediaPanel.reset();
        mediaPanel.setMovieTitle(title);

        // Repaint
        repaint();
    }

    private void playMovie() {
        if (movie == null) {
            statusLabel.setText("No movie selected");
            return;
        }

        if (isPlaying) {
            statusLabel.setText("Movie is already playing");
            return;
        }

        // Start buffering animation
        isBuffering = true;
        statusLabel.setText("Buffering movie stream...");

        // Get movie ID for streaming
        Integer tmdbId = (Integer) movie.get("tmdb_id");

        // In a real implementation, this would get the actual stream URL
        // For this demo, we'll simulate streaming
        bufferingThread = new Thread(() -> {
            try {
                // Simulate network delay
                Thread.sleep(2000);

                // Get streaming URL
                String streamUrl = MovieDatabase.getStreamLink(tmdbId);

                // Update UI on the event dispatch thread
                EventQueue.invokeLater(() -> {
                    isBuffering = false;
                    isPlaying = true;
                    statusLabel.setText("Playing: " + movie.get("title"));
                    mediaPanel.startPlaying(streamUrl);
                    repaint();
                });
            } catch (Exception e) {
                EventQueue.invokeLater(() -> {
                    isBuffering = false;
                    statusLabel.setText("Error streaming movie: " + e.getMessage());
                });
            }
        });

        bufferingThread.start();
    }

    private void pauseMovie() {
        if (!isPlaying) {
            statusLabel.setText("No movie is playing");
            return;
        }

        if (isBuffering) {
            // Cancel buffering
            if (bufferingThread != null) {
                bufferingThread.interrupt();
                bufferingThread = null;
            }
            isBuffering = false;
            statusLabel.setText("Buffering cancelled");
            return;
        }

        // Pause playback
        statusLabel.setText("Paused: " + movie.get("title"));
        mediaPanel.pausePlaying();
        isPlaying = false;
    }

    private void stopMovie() {
        if (!isPlaying && !isBuffering) {
            statusLabel.setText("No movie is playing");
            return;
        }

        if (isBuffering) {
            // Cancel buffering
            if (bufferingThread != null) {
                bufferingThread.interrupt();
                bufferingThread = null;
            }
            isBuffering = false;
        }

        // Stop playback
        statusLabel.setText("Stopped: " + movie.get("title"));
        mediaPanel.stopPlaying();
        isPlaying = false;
    }

    private void styleButton(Button button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (bgColor.equals(darkYellow)) {
                    button.setBackground(new Color(255, 215, 50));
                } else {
                    button.setBackground(new Color(
                            Math.min(bgColor.getRed() + 20, 255),
                            Math.min(bgColor.getGreen() + 20, 255),
                            Math.min(bgColor.getBlue() + 20, 255)));
                }
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    // Media panel to display the video
    private class MediaPanel extends Panel {
        private String streamUrl;
        private String movieTitle;
        private Image posterImage;
        private boolean isPlaying = false;
        private boolean showControls = false;
        private long startTime;
        private int frameCount = 0;
        private Thread animationThread;

        public MediaPanel() {
            setBackground(Color.BLACK);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleControls();
                }
            });
        }

        public void setMovieTitle(String title) {
            this.movieTitle = title;
            repaint();
        }

        public void startPlaying(String streamUrl) {
            this.streamUrl = streamUrl;
            this.isPlaying = true;
            this.startTime = System.currentTimeMillis();

            // Start animation thread
            animationThread = new Thread(() -> {
                while (isPlaying) {
                    try {
                        Thread.sleep(100);
                        frameCount++;
                        repaint();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });

            animationThread.start();
        }

        public void pausePlaying() {
            isPlaying = false;
            if (animationThread != null) {
                animationThread.interrupt();
                animationThread = null;
            }
            repaint();
        }

        public void stopPlaying() {
            isPlaying = false;
            if (animationThread != null) {
                animationThread.interrupt();
                animationThread = null;
            }
            frameCount = 0;
            streamUrl = null;
            repaint();
        }

        public void reset() {
            stopPlaying();
            showControls = false;
        }

        private void toggleControls() {
            showControls = !showControls;
            repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Fill background
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, width, height);

            if (isPlaying) {
                // Simulate video playback - this would be real video rendering in a production
                // app

                // Draw simulated video frame
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 0, width, height);

                // Draw a simple animation to represent video
                int time = (int) ((System.currentTimeMillis() - startTime) / 1000);

                // Simple animation pattern
                g2.setColor(new Color(50, 50, 50));
                for (int i = 0; i < 10; i++) {
                    int x = (frameCount * 5 + i * 40) % width;
                    g2.fillOval(x - 20, height / 2 - 20, 40, 40);
                }

                // Draw movie title
                g2.setColor(darkYellow);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString(movieTitle + " - Playing", 20, 30);

                // Draw time counter
                String timeStr = String.format("Time: %02d:%02d", time / 60, time % 60);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                g2.drawString(timeStr, width - 100, 30);

                // Show controls if toggled
                if (showControls) {
                    drawControls(g2, width, height);
                }
            } else if (PlayerPanel.this.isBuffering) {
                // Draw buffering animation
                g2.setColor(new Color(30, 30, 30));
                g2.fillRect(0, 0, width, height);

                g2.setColor(darkYellow);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.drawString("Buffering...", width / 2 - 50, height / 2);

                // Draw loading dots
                int dotCount = (int) (System.currentTimeMillis() / 500) % 4;
                for (int i = 0; i < dotCount; i++) {
                    g2.fillOval(width / 2 + i * 15, height / 2 + 20, 10, 10);
                }
            } else {
                // Draw placeholder with movie info
                g2.setColor(new Color(20, 20, 20));
                g2.fillRect(0, 0, width, height);

                if (movieTitle != null) {
                    // Draw movie title
                    g2.setColor(darkYellow);
                    g2.setFont(new Font("Arial", Font.BOLD, 24));
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(movieTitle);
                    g2.drawString(movieTitle, (width - textWidth) / 2, height / 2 - 30);

                    // Draw play instructions
                    g2.setColor(textColor);
                    g2.setFont(new Font("Arial", Font.PLAIN, 16));
                    String message = "Click PLAY to start streaming";
                    textWidth = fm.stringWidth(message);
                    g2.drawString(message, (width - textWidth) / 2, height / 2 + 30);
                } else {
                    // No movie selected
                    g2.setColor(textColor);
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    g2.drawString("No Movie Selected", width / 2 - 100, height / 2);
                }
            }
        }

        private void drawControls(Graphics2D g2, int width, int height) {
            // Draw semi-transparent control bar
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, height - 50, width, 50);

            // Draw progress bar
            g2.setColor(darkGray);
            g2.fillRect(20, height - 40, width - 40, 10);

            // Simulate progress
            long elapsed = System.currentTimeMillis() - startTime;
            int progress = (int) (elapsed / 100) % (width - 40);
            g2.setColor(darkYellow);
            g2.fillRect(20, height - 40, progress, 10);

            // Draw control buttons
            String[] controls = { "<<", ">|", "[]", ">>" };
            int buttonWidth = 30;
            int startX = width / 2 - (controls.length * buttonWidth) / 2;

            g2.setFont(new Font("Arial", Font.BOLD, 16));
            for (int i = 0; i < controls.length; i++) {
                int x = startX + i * buttonWidth;
                g2.setColor(darkGray);
                g2.fillOval(x, height - 30, 25, 25);
                g2.setColor(textColor);
                g2.drawString(controls[i], x + 5, height - 13);
            }
        }
    }
}
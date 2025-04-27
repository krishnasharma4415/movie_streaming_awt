import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Map;

public class WebPlayerPanel extends Panel {
    private MovieStreamingApp app;
    private Map<String, Object> movie;
    private Label statusLabel;
    private Panel contentArea;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);
    private Label infoLabel;
    private Label titleLabel;

    public WebPlayerPanel(MovieStreamingApp app) {
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

        // Content area panel
        contentArea = new Panel(new BorderLayout());
        contentArea.setBackground(Color.BLACK);

        // Instructions label when no movie is playing
        Label instructionsLabel = new Label("Select a movie to play", Label.CENTER);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionsLabel.setForeground(textColor);
        contentArea.add(instructionsLabel, BorderLayout.CENTER);

        add(contentArea, BorderLayout.CENTER);

        // Button panel
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(lightGray);

        // Back button
        Button backButton = new Button("Back to Movies");
        styleButton(backButton, darkGray);
        backButton.addActionListener(e -> app.showScreen("MOVIES"));
        buttonPanel.add(backButton);

        // Open in Browser button
        Button browserButton = new Button("Open in Browser");
        styleButton(browserButton, darkYellow);
        browserButton.addActionListener(e -> openInBrowser());
        buttonPanel.add(browserButton);

        // Details button
        Button detailsButton = new Button("Movie Details");
        styleButton(detailsButton, new Color(0, 120, 215));
        detailsButton.addActionListener(e -> showMovieDetails());
        buttonPanel.add(detailsButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setMovie(Map<String, Object> movie) {
        this.movie = movie;

        if (movie == null) {
            resetPlayer();
            return;
        }

        // Update title and info
        String title = (String) movie.get("title");
        titleLabel.setText(title);

        // Display movie info
        StringBuilder infoBuilder = new StringBuilder();

        // Rating
        Double rating = (Double) movie.get("vote_average");
        infoBuilder.append(String.format("Rating: %.1f ★", rating));

        // Year
        String year = (String) movie.get("year");
        if (year != null && !year.isEmpty()) {
            infoBuilder.append(" | Year: ").append(year);
        }

        // Runtime
        if (movie.get("runtime") != null) {
            int runtime = (Integer) movie.get("runtime");
            infoBuilder.append(" | Runtime: ").append(runtime).append(" min");
        }

        infoLabel.setText(infoBuilder.toString());

        // Update status
        statusLabel.setText("Ready to play: " + title);

        // Load the streaming content
        loadStreamingContent();
    }

    private void resetPlayer() {
        titleLabel.setText("No movie selected");
        infoLabel.setText("");
        statusLabel.setText("Ready");

        contentArea.removeAll();
        Label instructionsLabel = new Label("Select a movie to play", Label.CENTER);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        instructionsLabel.setForeground(textColor);
        contentArea.add(instructionsLabel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void loadStreamingContent() {
        if (movie == null) {
            return;
        }

        contentArea.removeAll();

        try {
            // Get TMDb ID
            int tmdbId = ((Number) movie.get("tmdb_id")).intValue();

            // Get streaming URL
            String streamUrl = MovieDatabase.getStreamLink(tmdbId);

            // Create a panel with instructions since Java AWT doesn't support embedded web
            // browsers
            Panel instructionsPanel = new Panel(new BorderLayout(10, 10));
            instructionsPanel.setBackground(Color.BLACK);

            // Add movie poster on the left if available
            String posterPath = (String) movie.get("poster_path");
            if (posterPath != null && !posterPath.isEmpty()) {
                try {
                    URL imageUrl = new URL(posterPath);
                    Image posterImage = app.getToolkit().getImage(imageUrl);

                    Canvas posterCanvas = new Canvas() {
                        @Override
                        public void paint(Graphics g) {
                            g.drawImage(posterImage, 0, 0, 200, 300, this);
                        }
                    };
                    posterCanvas.setPreferredSize(new Dimension(200, 300));

                    Panel posterPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
                    posterPanel.setBackground(Color.BLACK);
                    posterPanel.add(posterCanvas);

                    instructionsPanel.add(posterPanel, BorderLayout.WEST);
                } catch (Exception e) {
                    // Ignore if poster can't be loaded
                }
            }

            // Create instructions panel
            Panel textPanel = new Panel(new GridLayout(5, 1, 10, 10));
            textPanel.setBackground(Color.BLACK);

            Label headingLabel = new Label("Stream " + movie.get("title"), Label.CENTER);
            headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
            headingLabel.setForeground(darkYellow);
            textPanel.add(headingLabel);

            Label instructionsLabel = new Label("This application does not support embedded web playback.",
                    Label.CENTER);
            instructionsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            instructionsLabel.setForeground(textColor);
            textPanel.add(instructionsLabel);

            Label instructionsLabel2 = new Label("Click 'Open in Browser' to watch this movie in your browser.",
                    Label.CENTER);
            instructionsLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
            instructionsLabel2.setForeground(textColor);
            textPanel.add(instructionsLabel2);

            Button openBrowserButton = new Button("Open in Browser");
            openBrowserButton.setFont(new Font("Arial", Font.BOLD, 16));
            openBrowserButton.setBackground(darkYellow);
            openBrowserButton.setForeground(Color.BLACK);
            openBrowserButton.addActionListener(e -> openInBrowser());

            Panel buttonWrapper = new Panel(new FlowLayout(FlowLayout.CENTER));
            buttonWrapper.setBackground(Color.BLACK);
            buttonWrapper.add(openBrowserButton);
            textPanel.add(buttonWrapper);

            instructionsPanel.add(textPanel, BorderLayout.CENTER);

            contentArea.add(instructionsPanel, BorderLayout.CENTER);

            statusLabel.setText("Ready to stream: " + movie.get("title"));
        } catch (Exception e) {
            Label errorLabel = new Label("Error preparing stream: " + e.getMessage(), Label.CENTER);
            errorLabel.setForeground(Color.RED);
            contentArea.add(errorLabel, BorderLayout.CENTER);

            statusLabel.setText("Error: Could not prepare streaming content");
        }

        contentArea.revalidate();
        contentArea.repaint();
    }

    private void openInBrowser() {
        if (movie == null) {
            statusLabel.setText("No movie selected");
            return;
        }

        try {
            // Get TMDb ID
            int tmdbId = ((Number) movie.get("tmdb_id")).intValue();

            // Get streaming URL
            String streamUrl = MovieDatabase.getStreamLink(tmdbId);

            // Try to open in browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(streamUrl));
                statusLabel.setText("Opened in browser: " + movie.get("title"));
            } else {
                // Fallback for systems that don't support Desktop
                Runtime rt = Runtime.getRuntime();

                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    rt.exec("rundll32 url.dll,FileProtocolHandler " + streamUrl);
                } else if (os.contains("mac")) {
                    rt.exec("open " + streamUrl);
                } else if (os.contains("nix") || os.contains("nux")) {
                    rt.exec("xdg-open " + streamUrl);
                } else {
                    throw new UnsupportedOperationException("Unsupported operating system");
                }
                statusLabel.setText("Opened in browser: " + movie.get("title"));
            }
        } catch (Exception e) {
            statusLabel.setText("Error opening browser: " + e.getMessage());
            app.showError("Could not open browser: " + e.getMessage());
        }
    }

    private void showMovieDetails() {
        if (movie == null) {
            statusLabel.setText("No movie selected");
            return;
        }

        // Create new detail panel
        MovieDetailPanel detailPanel = new MovieDetailPanel(app, movie);

        // Get the card panel from the app
        Panel cardPanel = app.getCardPanel();

        // Remove any existing DETAIL panel and add the new one
        try {
            cardPanel.remove(cardPanel.getComponent(4)); // Assuming DETAIL is at index 4
        } catch (Exception e) {
            // No existing panel, that's fine
        }

        cardPanel.add(detailPanel, "DETAIL");

        // Show the detail screen
        app.showScreen("DETAIL");
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
}
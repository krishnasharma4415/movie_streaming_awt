import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class MovieDetailPanel extends Panel {
    private MovieStreamingApp app;
    private Map<String, Object> movie;
    private Image posterImage;
    private boolean isLoading = true;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);
    private Label titleLabel;
    private TextArea overviewArea;
    private Label ratingLabel;
    private Label yearLabel;
    private Label runtimeLabel;
    private Label genreLabel;
    private Label castLabel;
    private Label directorLabel;
    private Button watchButton;
    private Button trailerButton;
    private Button backButton;
    private Panel similarMoviesPanel;
    private List<Map<String, Object>> similarMovies;
    private boolean loadingSimilar = true;

    public MovieDetailPanel(MovieStreamingApp app, Map<String, Object> movie) {
        this.app = app;

        // If this movie has basic info, try to get full details from API
        if (movie != null && movie.containsKey("tmdb_id")
                && (!movie.containsKey("cast") || !movie.containsKey("director"))) {
            int tmdbId = ((Number) movie.get("tmdb_id")).intValue();
            Map<String, Object> fullDetails = MovieDatabase.getMovieDetails(tmdbId);

            // Merge the maps, keeping existing data if the API call failed
            if (fullDetails != null && !fullDetails.isEmpty()) {
                // Preserve tmdb_id in case it wasn't returned
                fullDetails.putIfAbsent("tmdb_id", tmdbId);

                // Copy over any existing fields not in the new data
                for (Map.Entry<String, Object> entry : movie.entrySet()) {
                    fullDetails.putIfAbsent(entry.getKey(), entry.getValue());
                }

                this.movie = fullDetails;
            } else {
                this.movie = movie;
            }
        } else {
            this.movie = movie;
        }

        setLayout(new BorderLayout(20, 20));
        setBackground(darkGray);

        // Back button at the top
        Panel topPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(darkGray);
        backButton = new Button("← Back to Movies");
        backButton.setBackground(lightGray);
        backButton.setForeground(textColor);
        backButton.addActionListener(e -> app.showScreen("MOVIES"));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // Start loading the poster image in a separate thread
        loadPosterImage();

        // Create main content panel with movie details
        Panel contentPanel = new Panel(new BorderLayout(20, 20));
        contentPanel.setBackground(darkGray);

        // Poster panel (left side)
        Panel posterPanel = new Panel(new BorderLayout());
        posterPanel.setBackground(darkGray);
        posterPanel.setPreferredSize(new Dimension(300, 450));

        // Use a canvas for drawing the poster
        Canvas posterCanvas = new Canvas() {
            @Override
            public void paint(Graphics g) {
                if (isLoading) {
                    g.setColor(lightGray);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(textColor);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    FontMetrics fm = g.getFontMetrics();
                    String loadingText = "Loading...";
                    int textWidth = fm.stringWidth(loadingText);
                    g.drawString(loadingText, (getWidth() - textWidth) / 2, getHeight() / 2);
                } else if (posterImage != null) {
                    g.drawImage(posterImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(lightGray);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(textColor);
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    FontMetrics fm = g.getFontMetrics();
                    String noImageText = "No Image Available";
                    int textWidth = fm.stringWidth(noImageText);
                    g.drawString(noImageText, (getWidth() - textWidth) / 2, getHeight() / 2);
                }
            }
        };
        posterPanel.add(posterCanvas, BorderLayout.CENTER);

        // Details panel (right side)
        Panel detailsPanel = new Panel();
        detailsPanel.setLayout(new GridLayout(9, 1, 0, 10));
        detailsPanel.setBackground(darkGray);

        // Title
        String title = (String) movie.get("title");
        titleLabel = new Label(title, Label.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(textColor);

        // Info panel with various details
        Panel infoPanel = new Panel(new GridLayout(4, 1, 0, 5));
        infoPanel.setBackground(darkGray);

        // Year and Rating
        Panel yearRatingPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        yearRatingPanel.setBackground(darkGray);

        // Year
        String year = (String) movie.get("year");
        yearLabel = new Label("Year: " + year);
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        yearLabel.setForeground(textColor);
        yearRatingPanel.add(yearLabel);

        // Rating
        Double rating = (Double) movie.get("vote_average");
        String ratingStr = String.format("%.1f/10", rating);
        ratingLabel = new Label("Rating: " + ratingStr);
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ratingLabel.setForeground(darkYellow);
        yearRatingPanel.add(ratingLabel);

        // Runtime
        if (movie.get("runtime") != null) {
            int runtime = (Integer) movie.get("runtime");
            runtimeLabel = new Label("Runtime: " + runtime + " min");
            runtimeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            runtimeLabel.setForeground(textColor);
            yearRatingPanel.add(runtimeLabel);
        }

        infoPanel.add(yearRatingPanel);

        // Genre
        Panel genrePanel = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        genrePanel.setBackground(darkGray);

        Label genreTitle = new Label("Genre: ", Label.LEFT);
        genreTitle.setFont(new Font("Arial", Font.BOLD, 16));
        genreTitle.setForeground(textColor);
        genrePanel.add(genreTitle);

        String genreText = (String) movie.get("genre");
        if (genreText == null || genreText.isEmpty())
            genreText = "Unknown";
        genreLabel = new Label(genreText);
        genreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        genreLabel.setForeground(textColor);
        genrePanel.add(genreLabel);

        infoPanel.add(genrePanel);

        // Director
        Panel directorPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        directorPanel.setBackground(darkGray);

        Label directorTitle = new Label("Director: ", Label.LEFT);
        directorTitle.setFont(new Font("Arial", Font.BOLD, 16));
        directorTitle.setForeground(textColor);
        directorPanel.add(directorTitle);

        String directorText = (String) movie.get("director");
        if (directorText == null || directorText.isEmpty())
            directorText = "Unknown";
        directorLabel = new Label(directorText);
        directorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        directorLabel.setForeground(textColor);
        directorPanel.add(directorLabel);

        infoPanel.add(directorPanel);

        // Cast
        Panel castPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        castPanel.setBackground(darkGray);

        Label castTitle = new Label("Cast: ", Label.LEFT);
        castTitle.setFont(new Font("Arial", Font.BOLD, 16));
        castTitle.setForeground(textColor);
        castPanel.add(castTitle);

        String castText = (String) movie.get("cast");
        if (castText == null || castText.isEmpty())
            castText = "Unknown";
        castLabel = new Label(castText);
        castLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        castLabel.setForeground(textColor);
        castPanel.add(castLabel);

        infoPanel.add(castPanel);

        // Overview
        Panel overviewPanel = new Panel(new BorderLayout());
        overviewPanel.setBackground(darkGray);

        Label overviewTitle = new Label("Overview:", Label.LEFT);
        overviewTitle.setFont(new Font("Arial", Font.BOLD, 18));
        overviewTitle.setForeground(textColor);
        overviewPanel.add(overviewTitle, BorderLayout.NORTH);

        String overview = (String) movie.get("overview");
        if (overview == null || overview.isEmpty())
            overview = "No overview available.";
        overviewArea = new TextArea(overview, 30, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
        overviewArea.setEditable(false);
        overviewArea.setBackground(lightGray);
        overviewArea.setForeground(textColor);
        overviewArea.setFont(new Font("Arial", Font.PLAIN, 16));
        overviewPanel.add(overviewArea, BorderLayout.CENTER);

        // Action buttons panel
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(darkGray);

        // Watch button
        watchButton = new Button("Watch Now");
        watchButton.setBackground(new Color(0, 128, 0));
        watchButton.setForeground(Color.WHITE);
        watchButton.setFont(new Font("Arial", Font.BOLD, 18));
        watchButton.addActionListener(e -> watchMovie());
        buttonPanel.add(watchButton);

        // Trailer button (only shown if trailer available)
        if (movie.containsKey("trailer_key") && movie.get("trailer_key") != null) {
            trailerButton = new Button("Watch Trailer");
            trailerButton.setBackground(new Color(0, 0, 128));
            trailerButton.setForeground(Color.WHITE);
            trailerButton.setFont(new Font("Arial", Font.BOLD, 18));
            trailerButton.addActionListener(e -> watchTrailer());
            buttonPanel.add(trailerButton);
        }

        // Add all components to the details panel
        detailsPanel.add(titleLabel);
        detailsPanel.add(infoPanel);
        detailsPanel.add(new Label("")); // Spacer
        detailsPanel.add(overviewPanel);
        detailsPanel.add(new Label("")); // Spacer
        detailsPanel.add(buttonPanel);

        // Add the poster and details to the content panel
        contentPanel.add(posterPanel, BorderLayout.WEST);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);

        // Add padding around the content
        Panel paddingPanel = new Panel(new BorderLayout());
        paddingPanel.setBackground(darkGray);
        paddingPanel.add(contentPanel, BorderLayout.CENTER);
        paddingPanel.add(new Panel() {
            {
                setBackground(darkGray);
                setPreferredSize(new Dimension(20, 0));
            }
        }, BorderLayout.WEST);
        paddingPanel.add(new Panel() {
            {
                setBackground(darkGray);
                setPreferredSize(new Dimension(20, 0));
            }
        }, BorderLayout.EAST);
        paddingPanel.add(new Panel() {
            {
                setBackground(darkGray);
                setPreferredSize(new Dimension(0, 20));
            }
        }, BorderLayout.SOUTH);

        add(paddingPanel, BorderLayout.CENTER);

        // Add similar movies section at the bottom
        setupSimilarMoviesPanel();
        add(similarMoviesPanel, BorderLayout.SOUTH);

        // Load similar movies in a separate thread
        loadSimilarMovies();
    }

    private void setupSimilarMoviesPanel() {
        similarMoviesPanel = new Panel(new BorderLayout());
        similarMoviesPanel.setBackground(darkGray);
        similarMoviesPanel.setPreferredSize(new Dimension(getWidth(), 220));

        Label similarTitle = new Label("Similar Movies:", Label.LEFT);
        similarTitle.setFont(new Font("Arial", Font.BOLD, 18));
        similarTitle.setForeground(darkYellow);
        similarTitle.setBackground(darkGray);

        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        titlePanel.setBackground(darkGray);
        titlePanel.add(similarTitle);

        similarMoviesPanel.add(titlePanel, BorderLayout.NORTH);

        // Add loading indicator initially
        Panel loadingPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        loadingPanel.setBackground(darkGray);
        Label loadingLabel = new Label("Loading similar movies...");
        loadingLabel.setForeground(textColor);
        loadingPanel.add(loadingLabel);

        similarMoviesPanel.add(loadingPanel, BorderLayout.CENTER);
    }

    private void loadSimilarMovies() {
        new Thread(() -> {
            try {
                // Get TMDb ID from the movie
                int tmdbId = ((Number) movie.get("tmdb_id")).intValue();

                // Fetch similar movies
                similarMovies = MovieDatabase.getSimilarMovies(tmdbId);

                // Update UI on the event dispatch thread
                EventQueue.invokeLater(() -> {
                    loadingSimilar = false;
                    updateSimilarMoviesPanel();
                });
            } catch (Exception e) {
                System.err.println("Error loading similar movies: " + e.getMessage());

                // Handle error on the event dispatch thread
                EventQueue.invokeLater(() -> {
                    loadingSimilar = false;
                    Panel errorPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
                    errorPanel.setBackground(darkGray);
                    Label errorLabel = new Label("Error loading similar movies");
                    errorLabel.setForeground(Color.RED);
                    errorPanel.add(errorLabel);

                    similarMoviesPanel.removeAll();
                    similarMoviesPanel.add(errorPanel, BorderLayout.CENTER);
                    similarMoviesPanel.revalidate();
                    similarMoviesPanel.repaint();
                });
            }
        }).start();
    }

    private void updateSimilarMoviesPanel() {
        similarMoviesPanel.removeAll();

        // Add title
        Label similarTitle = new Label("Similar Movies:", Label.LEFT);
        similarTitle.setFont(new Font("Arial", Font.BOLD, 18));
        similarTitle.setForeground(darkYellow);

        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        titlePanel.setBackground(darkGray);
        titlePanel.add(similarTitle);

        similarMoviesPanel.add(titlePanel, BorderLayout.NORTH);

        if (similarMovies == null || similarMovies.isEmpty()) {
            Panel noMoviesPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
            noMoviesPanel.setBackground(darkGray);
            Label noMoviesLabel = new Label("No similar movies found");
            noMoviesLabel.setForeground(textColor);
            noMoviesPanel.add(noMoviesLabel);

            similarMoviesPanel.add(noMoviesPanel, BorderLayout.CENTER);
        } else {
            // Create horizontal scrollable panel for similar movies
            Panel scrollContent = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            scrollContent.setBackground(darkGray);

            // Add up to 5 similar movies
            int maxToShow = Math.min(similarMovies.size(), 5);
            for (int i = 0; i < maxToShow; i++) {
                scrollContent.add(createSimilarMovieCard(similarMovies.get(i)));
            }

            ScrollPane similarScroll = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
            similarScroll.setBackground(darkGray);
            similarScroll.add(scrollContent);

            similarMoviesPanel.add(similarScroll, BorderLayout.CENTER);
        }

        similarMoviesPanel.revalidate();
        similarMoviesPanel.repaint();
    }

    private Panel createSimilarMovieCard(Map<String, Object> movie) {
        Panel card = new Panel(new BorderLayout());
        card.setBackground(lightGray);
        card.setPreferredSize(new Dimension(150, 150));

        // Title at top
        String title = (String) movie.get("title");
        if (title.length() > 15) {
            title = title.substring(0, 12) + "...";
        }

        Label titleLabel = new Label(title, Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(textColor);
        card.add(titleLabel, BorderLayout.NORTH);

        // Mini poster in center
        MiniPosterPanel posterPanel = new MiniPosterPanel(movie);
        card.add(posterPanel, BorderLayout.CENTER);

        // Play button at bottom
        Button playButton = new Button("Play");
        playButton.setBackground(darkYellow);
        playButton.setForeground(Color.BLACK);
        playButton.addActionListener(e -> showMovie(movie));
        card.add(playButton, BorderLayout.SOUTH);

        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(80, 80, 80));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(lightGray);
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showMovie(movie);
            }
        });

        return card;
    }

    private void showMovie(Map<String, Object> movie) {
        // Create a new detail panel for the selected movie
        MovieDetailPanel newDetailPanel = new MovieDetailPanel(app, movie);

        // Replace the current panel in the card layout
        Panel cardPanel = app.getCardPanel();
        cardPanel.remove(this); // Remove the current panel
        cardPanel.add(newDetailPanel, "DETAIL");

        // Show the new detail panel
        app.showScreen("DETAIL");
    }

    private void loadPosterImage() {
        new Thread(() -> {
            try {
                String posterPath = (String) movie.get("poster_path");
                if (posterPath != null && !posterPath.isEmpty()) {
                    URL imageUrl = new URL(posterPath);
                    posterImage = app.getToolkit().getImage(imageUrl);

                    // Use MediaTracker to wait for the image to load
                    MediaTracker tracker = new MediaTracker(this);
                    tracker.addImage(posterImage, 0);
                    tracker.waitForID(0);
                }
            } catch (Exception e) {
                System.err.println("Error loading poster: " + e.getMessage());
            } finally {
                isLoading = false;
                repaint();
            }
        }).start();
    }

    private void watchMovie() {
        try {
            // Get the WebPlayerPanel from the app
            WebPlayerPanel playerPanel = (WebPlayerPanel) app.getCardPanel().getComponent(3);

            // Set the movie to play
            playerPanel.setMovie(movie);

            // Show the player screen
            app.showScreen("PLAYER");
        } catch (Exception e) {
            app.showError("Error starting player: " + e.getMessage());
        }
    }

    private void watchTrailer() {
        try {
            String trailerKey = (String) movie.get("trailer_key");
            if (trailerKey != null && !trailerKey.isEmpty()) {
                String trailerUrl = MovieDatabase.getTrailerUrl(trailerKey);

                // Open in browser
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(trailerUrl));
                } else {
                    // Fallback for systems that don't support Desktop
                    Runtime rt = Runtime.getRuntime();

                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        rt.exec("rundll32 url.dll,FileProtocolHandler " + trailerUrl);
                    } else if (os.contains("mac")) {
                        rt.exec("open " + trailerUrl);
                    } else if (os.contains("nix") || os.contains("nux")) {
                        rt.exec("xdg-open " + trailerUrl);
                    } else {
                        throw new UnsupportedOperationException("Unsupported operating system");
                    }
                }
            } else {
                app.showError("No trailer available for this movie");
            }
        } catch (Exception e) {
            app.showError("Error opening trailer: " + e.getMessage());
        }
    }

    // Panel to display mini posters for similar movies
    private class MiniPosterPanel extends Panel {
        private Map<String, Object> movie;
        private Image posterImage;
        private boolean isLoading = true;

        public MiniPosterPanel(Map<String, Object> movie) {
            this.movie = movie;
            setBackground(new Color(50, 50, 50));

            // Load poster image in a separate thread
            new Thread(() -> {
                try {
                    String posterPath = (String) movie.get("poster_path");
                    if (posterPath != null && !posterPath.isEmpty()) {
                        URL url = new URL(posterPath);
                        posterImage = Toolkit.getDefaultToolkit().getImage(url);
                        MediaTracker tracker = new MediaTracker(this);
                        tracker.addImage(posterImage, 0);
                        tracker.waitForAll();
                    }
                } catch (Exception e) {
                    System.out.println("Error loading poster: " + e.getMessage());
                } finally {
                    isLoading = false;
                    repaint();
                }
            }).start();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            int width = getWidth();
            int height = getHeight();

            if (isLoading) {
                g.setColor(new Color(60, 60, 60));
                g.fillRect(0, 0, width, height);
                g.setColor(textColor);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString("Loading...", width / 2 - 25, height / 2);
            } else if (posterImage != null) {
                g.drawImage(posterImage, 0, 0, width, height, this);
            } else {
                g.setColor(new Color(60, 60, 60));
                g.fillRect(0, 0, width, height);
                g.setColor(textColor);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                g.drawString("No Image", width / 2 - 25, height / 2);
            }
        }
    }
}
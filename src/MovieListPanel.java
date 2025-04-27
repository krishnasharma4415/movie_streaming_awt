import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class MovieListPanel extends Panel {
    private MovieStreamingApp app;
    private List<Map<String, Object>> movies = new ArrayList<>();
    private List<Map<String, Object>> filteredMovies = new ArrayList<>();
    private Panel movieGrid;
    private ScrollPane scrollPane;
    private TextField searchField;
    private Choice genreFilter;
    private Choice yearFilter;
    private Choice ratingFilter;
    private Choice sortByFilter;
    private Panel filterPanel;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);
    private Map<String, Integer> genreNameToId = new HashMap<>();
    private boolean isSearching = false;

    public MovieListPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(darkGray);

        // Header panel with app title and logout button
        setupHeader();

        // Search and filter panel
        setupSearchAndFilter();

        // Initialize movie grid inside a scroll pane
        setupMovieGrid();

        // Load popular movies from TMDb
        loadMovies();
    }

    private void setupHeader() {
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));

        Panel titlePanel = new Panel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        titlePanel.setBackground(new Color(25, 25, 25));

        Label appTitle = new Label("MOVIE STREAM", Label.LEFT);
        appTitle.setFont(new Font("Arial", Font.BOLD, 22));
        appTitle.setForeground(darkYellow);
        titlePanel.add(appTitle);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        Panel userPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        userPanel.setBackground(new Color(25, 25, 25));

        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, darkGray);
        logoutButton.addActionListener(e -> app.showScreen("LOGIN"));
        userPanel.add(logoutButton);

        headerPanel.add(userPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void setupSearchAndFilter() {
        filterPanel = new Panel(new BorderLayout());
        filterPanel.setBackground(lightGray);
        filterPanel.setPreferredSize(new Dimension(getWidth(), 130)); // Increased height for sort option

        // Search panel
        Panel searchPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        searchPanel.setBackground(lightGray);

        searchField = new TextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(new Color(55, 55, 55));
        searchField.setForeground(textColor);
        searchField.setText("Search movies...");

        // Clear placeholder text on focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search movies...")) {
                    searchField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search movies...");
                }
            }
        });

        // Add search functionality
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                if (query.equals("search movies...")) {
                    query = "";
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !query.isEmpty()) {
                    searchMovies(query);
                } else if (query.isEmpty() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadMoviesWithFilters(); // Load movies with current filters if search is cleared
                }
            }
        });

        Button searchButton = new Button("Search");
        styleButton(searchButton, darkYellow);
        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isEmpty() && !query.equals("Search movies...")) {
                searchMovies(query);
            } else {
                loadMoviesWithFilters(); // Load movies with current filters if search is cleared
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        filterPanel.add(searchPanel, BorderLayout.NORTH);

        // Filter panel
        Panel filtersPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        filtersPanel.setBackground(lightGray);

        // Genre filter
        Label genreLabel = new Label("Genre:");
        genreLabel.setForeground(textColor);
        filtersPanel.add(genreLabel);

        genreFilter = new Choice();
        genreFilter.setBackground(new Color(55, 55, 55));
        genreFilter.setForeground(textColor);
        genreFilter.add("All Genres");

        // Load genres from TMDb
        loadGenres();

        genreFilter.addItemListener(e -> applyFilters());
        filtersPanel.add(genreFilter);

        // Year filter
        Label yearLabel = new Label("Year:");
        yearLabel.setForeground(textColor);
        filtersPanel.add(yearLabel);

        yearFilter = new Choice();
        yearFilter.setBackground(new Color(55, 55, 55));
        yearFilter.setForeground(textColor);
        yearFilter.add("All Years");

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        for (int i = currentYear; i >= 1990; i--) {
            yearFilter.add(String.valueOf(i));
        }

        yearFilter.addItemListener(e -> applyFilters());
        filtersPanel.add(yearFilter);

        // Rating filter
        Label ratingLabel = new Label("Rating:");
        ratingLabel.setForeground(textColor);
        filtersPanel.add(ratingLabel);

        ratingFilter = new Choice();
        ratingFilter.setBackground(new Color(55, 55, 55));
        ratingFilter.setForeground(textColor);
        ratingFilter.add("All Ratings");
        ratingFilter.add("8+ ★");
        ratingFilter.add("7+ ★");
        ratingFilter.add("6+ ★");
        ratingFilter.add("5+ ★");

        ratingFilter.addItemListener(e -> applyFilters());
        filtersPanel.add(ratingFilter);

        // Add a second row for sort options
        Panel sortPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        sortPanel.setBackground(lightGray);

        // Sort By filter
        Label sortByLabel = new Label("Sort By:");
        sortByLabel.setForeground(textColor);
        sortPanel.add(sortByLabel);

        sortByFilter = new Choice();
        sortByFilter.setBackground(new Color(55, 55, 55));
        sortByFilter.setForeground(textColor);
        sortByFilter.add("Popular");
        sortByFilter.add("Top Rated");
        sortByFilter.add("Latest");
        sortByFilter.add("A-Z");

        sortByFilter.addItemListener(e -> applyFilters());
        sortPanel.add(sortByFilter);

        // Reset filters button
        Button resetButton = new Button("Reset All");
        styleButton(resetButton, darkGray);
        resetButton.addActionListener(e -> {
            genreFilter.select(0);
            yearFilter.select(0);
            ratingFilter.select(0);
            sortByFilter.select(0);
            searchField.setText("Search movies...");
            isSearching = false;
            loadMovies(); // Reload movies with no filters
        });
        sortPanel.add(resetButton);

        // Discover button
        Button discoverButton = new Button("Discover Movies");
        styleButton(discoverButton, new Color(0, 128, 128));
        discoverButton.setPreferredSize(new Dimension(150, 30));
        discoverButton.addActionListener(e -> {
            isSearching = false;
            loadMoviesWithFilters(); // Load movies with current filters using discover API
        });
        sortPanel.add(discoverButton);

        filterPanel.add(filtersPanel, BorderLayout.CENTER);
        filterPanel.add(sortPanel, BorderLayout.SOUTH);
        add(filterPanel, BorderLayout.NORTH);
    }

    private void loadGenres() {
        try {
            List<Map<String, Object>> genres = MovieDatabase.getGenres();

            for (Map<String, Object> genre : genres) {
                String name = (String) genre.get("name");
                Integer id = (Integer) genre.get("id");
                genreFilter.add(name);
                genreNameToId.put(name, id);
            }
        } catch (Exception e) {
            System.err.println("Error loading genres: " + e.getMessage());
        }
    }

    private void setupMovieGrid() {
        // Create a scroll pane
        scrollPane = new ScrollPane();
        scrollPane.setBackground(darkGray);

        // Movie grid
        movieGrid = new Panel(new GridLayout(0, 5, 15, 25)); // 5 columns with spacing
        movieGrid.setBackground(darkGray);

        // Add the movie grid to the scroll pane
        scrollPane.add(movieGrid);

        // Add the scroll pane to the center of the panel
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadMovies() {
        showLoading();

        // Load popular movies from TMDb with no filters
        movies = MovieDatabase.getPopularMovies();

        if (movies.isEmpty()) {
            showError("No movies found. Check your internet connection.");
        }

        // Copy all movies to filtered list initially
        filteredMovies.clear();
        filteredMovies.addAll(movies);

        // Populate the grid
        populateMovieGrid();
    }

    private void loadMoviesWithFilters() {
        showLoading();

        // Get filter values
        Integer genreId = getSelectedGenreId();
        String year = getSelectedYear();
        Double minRating = getSelectedRating();
        String sortBy = getSelectedSortBy();

        if (isSearching && !searchField.getText().equals("Search movies...")) {
            // If we're searching, use search with filters
            String query = searchField.getText();
            movies = MovieDatabase.searchMovies(query, genreId, year, minRating, sortBy);
        } else {
            // Otherwise use discover API
            movies = MovieDatabase.discoverMovies(genreId, year, minRating, sortBy);
        }

        if (movies.isEmpty()) {
            showError("No movies found with the selected filters.");
        }

        // Copy all movies to filtered list
        filteredMovies.clear();
        filteredMovies.addAll(movies);

        // Populate the grid
        populateMovieGrid();
    }

    private void searchMovies(String query) {
        showLoading();
        isSearching = true;

        // Get filter values to apply to the search
        Integer genreId = getSelectedGenreId();
        String year = getSelectedYear();
        Double minRating = getSelectedRating();
        String sortBy = getSelectedSortBy();

        // Fetch movies from TMDb API with filters
        List<Map<String, Object>> searchResults = MovieDatabase.searchMovies(query, genreId, year, minRating, sortBy);

        if (!searchResults.isEmpty()) {
            movies.clear();
            movies.addAll(searchResults);

            // Update filtered list
            filteredMovies.clear();
            filteredMovies.addAll(movies);

            // Populate grid with results
            populateMovieGrid();
        } else {
            showError("No movies found for: " + query);
        }
    }

    private Integer getSelectedGenreId() {
        String genre = genreFilter.getSelectedItem();
        if (genre.equals("All Genres")) {
            return null;
        }
        return genreNameToId.get(genre);
    }

    private String getSelectedYear() {
        String year = yearFilter.getSelectedItem();
        if (year.equals("All Years")) {
            return null;
        }
        return year;
    }

    private Double getSelectedRating() {
        String rating = ratingFilter.getSelectedItem();
        if (rating.equals("All Ratings")) {
            return null;
        }
        return Double.parseDouble(rating.split("\\+")[0]);
    }

    private String getSelectedSortBy() {
        String sortBy = sortByFilter.getSelectedItem();
        switch (sortBy) {
            case "Popular":
                return "popularity.desc";
            case "Top Rated":
                return "vote_average.desc";
            case "Latest":
                return "release_date.desc";
            case "A-Z":
                return "original_title.asc";
            default:
                return "popularity.desc";
        }
    }

    private void applyFilters() {
        // If we're already viewing results, apply filters by reloading with current
        // parameters
        loadMoviesWithFilters();
    }

    private void populateMovieGrid() {
        movieGrid.removeAll();

        if (filteredMovies.isEmpty()) {
            Label noMoviesLabel = new Label("No movies found", Label.CENTER);
            noMoviesLabel.setFont(new Font("Arial", Font.BOLD, 18));
            noMoviesLabel.setForeground(textColor);
            movieGrid.add(noMoviesLabel);
        } else {
            for (int i = 0; i < filteredMovies.size(); i++) {
                movieGrid.add(createMovieCard(filteredMovies.get(i)));
            }
        }

        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private Panel createMovieCard(Map<String, Object> movie) {
        Panel card = new Panel(new BorderLayout(0, 0));
        card.setBackground(lightGray);

        // Movie poster panel (will be painted in the paint method)
        PosterPanel posterPanel = new PosterPanel(movie);
        posterPanel.setPreferredSize(new Dimension(200, 300));
        card.add(posterPanel, BorderLayout.CENTER);

        // Info panel at the bottom of the card
        Panel infoPanel = new Panel(new BorderLayout());
        infoPanel.setBackground(lightGray);

        // Movie title
        String title = (String) movie.get("title");
        if (title.length() > 20) {
            title = title.substring(0, 17) + "...";
        }
        Label titleLabel = new Label(title, Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(textColor);
        infoPanel.add(titleLabel, BorderLayout.NORTH);

        // Rating & year in a single row
        Panel detailsPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        detailsPanel.setBackground(lightGray);

        // Rating
        Double rating = (Double) movie.get("vote_average");
        Label ratingLabel = new Label(String.format("%.1f ★", rating));
        ratingLabel.setForeground(darkYellow);
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 12));
        detailsPanel.add(ratingLabel);

        // Year - Fix the ClassCastException by handling both String and Integer types
        Object yearObj = movie.get("year");
        String yearStr;
        if (yearObj instanceof Integer) {
            yearStr = String.valueOf(yearObj);
        } else {
            yearStr = (String) yearObj;
        }
        Label yearLabel = new Label(yearStr);
        yearLabel.setForeground(textColor);
        yearLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsPanel.add(yearLabel);

        infoPanel.add(detailsPanel, BorderLayout.CENTER);

        // Action buttons
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(lightGray);

        Button detailsButton = new Button("Details");
        styleButton(detailsButton, new Color(0, 120, 215));
        detailsButton.setPreferredSize(new Dimension(70, 25));
        detailsButton.addActionListener(e -> showMovieDetails(movie));

        Button playButton = new Button("Play");
        styleButton(playButton, darkYellow);
        playButton.setPreferredSize(new Dimension(60, 25));
        playButton.addActionListener(e -> playMovie(movie));

        buttonPanel.add(detailsButton);
        buttonPanel.add(playButton);
        infoPanel.add(buttonPanel, BorderLayout.SOUTH);

        card.add(infoPanel, BorderLayout.SOUTH);

        // Add hover effect to the card
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(80, 80, 80));
                infoPanel.setBackground(new Color(80, 80, 80));
                detailsPanel.setBackground(new Color(80, 80, 80));
                buttonPanel.setBackground(new Color(80, 80, 80));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(lightGray);
                infoPanel.setBackground(lightGray);
                detailsPanel.setBackground(lightGray);
                buttonPanel.setBackground(lightGray);
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showMovieDetails(movie);
            }
        });

        return card;
    }

    private void showMovieDetails(Map<String, Object> movie) {
        // Create MovieDetailPanel instance
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

    private void playMovie(Map<String, Object> movie) {
        // Get the WebPlayerPanel from the app
        WebPlayerPanel playerPanel = (WebPlayerPanel) app.getCardPanel().getComponent(3);

        // Set the movie to play
        playerPanel.setMovie(movie);

        // Show the player screen
        app.showScreen("PLAYER");
    }

    private void showLoading() {
        movieGrid.removeAll();

        Label loadingLabel = new Label("Loading movies...", Label.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        loadingLabel.setForeground(textColor);
        movieGrid.add(loadingLabel);

        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private void styleButton(Button button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(80, 30));

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

    private void showError(String message) {
        movieGrid.removeAll();

        Label errorLabel = new Label(message, Label.CENTER);
        errorLabel.setFont(new Font("Arial", Font.BOLD, 18));
        errorLabel.setForeground(Color.RED);
        movieGrid.add(errorLabel);

        movieGrid.revalidate();
        movieGrid.repaint();
    }

    // Panel to display movie posters
    private class PosterPanel extends Panel {
        private Map<String, Object> movie;
        private Image posterImage;
        private boolean isLoading = true;

        public PosterPanel(Map<String, Object> movie) {
            this.movie = movie;
            setBackground(new Color(40, 40, 40));

            // Load poster image in a separate thread
            new Thread(() -> {
                try {
                    String posterUrl = (String) movie.get("poster_path");
                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        URL url = new URL(posterUrl);
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
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (isLoading) {
                // Draw loading placeholder
                g2.setColor(new Color(60, 60, 60));
                g2.fillRect(0, 0, width, height);
                g2.setColor(new Color(100, 100, 100));
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                g2.drawString("Loading...", width / 2 - 30, height / 2);
            } else if (posterImage != null) {
                // Draw poster image
                g2.drawImage(posterImage, 0, 0, width, height, this);

                // Add a subtle gradient overlay at the bottom for better text readability
                GradientPaint gradient = new GradientPaint(
                        0, height - 50, new Color(0, 0, 0, 0),
                        0, height, new Color(0, 0, 0, 100));
                g2.setPaint(gradient);
                g2.fillRect(0, height - 50, width, 50);
            } else {
                // Draw placeholder if no image
                g2.setColor(new Color(60, 60, 60));
                g2.fillRect(0, 0, width, height);
                g2.setColor(new Color(100, 100, 100));
                g2.setFont(new Font("Arial", Font.PLAIN, 14));

                String title = (String) movie.get("title");
                if (title.length() > 20) {
                    title = title.substring(0, 17) + "...";
                }

                // Center the title
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(title);
                g2.drawString(title, (width - textWidth) / 2, height / 2);
            }
        }
    }
}
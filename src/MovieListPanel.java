import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MovieListPanel extends Panel {
    private MovieStreamingApp app;
    private List<String> movieTitles = new ArrayList<>();
    private List<String> moviePaths = new ArrayList<>();
    private List<String> thumbnailPaths = new ArrayList<>();
    private List<String> filteredTitles = new ArrayList<>();
    private List<String> filteredPaths = new ArrayList<>();
    private List<String> filteredThumbnails = new ArrayList<>();
    private Panel movieGrid;
    private ScrollPane scrollPane;

    public MovieListPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 245)); // Light background

        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(new Color(50, 50, 50));

        Label appTitle = new Label("Movie Streaming App", Label.LEFT);
        appTitle.setFont(new Font("Arial", Font.BOLD, 20));
        appTitle.setForeground(Color.WHITE);
        headerPanel.add(appTitle, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Title
        Label titleLabel = new Label("Available Movies", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        add(titleLabel, BorderLayout.NORTH);

        // Search bar
        Panel searchPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        TextField searchField = new TextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setText("Search movies...");
        searchField.setForeground(Color.GRAY);

        // Clear placeholder text on focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search movies...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search movies...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        // Add search functionality
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                filterMovies(query);
            }
        });

        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Load movies
        loadMovies();

        // Create a scroll pane
        scrollPane = new ScrollPane();
        scrollPane.setSize(800, 600);

        // Movie grid
        movieGrid = new Panel();
        movieGrid.setLayout(new GridLayout(0, 3, 20, 20)); // 3 columns with 20px spacing
        movieGrid.setBackground(new Color(240, 240, 245));

        // Populate the movie grid
        populateMovieGrid();

        // Add the movie grid to the scroll pane
        scrollPane.add(movieGrid);

        // Add the scroll pane to the center of the panel
        add(scrollPane, BorderLayout.CENTER);

        // Logout button
        Panel bottomPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button logoutButton = new Button("Logout");
        styleButton(logoutButton, new Color(200, 50, 50));
        logoutButton.addActionListener(e -> app.showScreen("LOGIN"));
        bottomPanel.add(logoutButton);

        add(bottomPanel, BorderLayout.SOUTH);

        Panel footerPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(50, 50, 50));
        
        Label footerLabel = new Label("© 2025 Movie Streaming App");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadMovies() {
        try (Statement stmt = app.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title, file_path, thumbnail_path FROM movies")) {

            while (rs.next()) {
                movieTitles.add(rs.getString("title"));
                moviePaths.add(rs.getString("file_path"));
                thumbnailPaths.add(rs.getString("thumbnail_path"));
            }

            // Initially, all movies are shown
            filteredTitles.addAll(movieTitles);
            filteredPaths.addAll(moviePaths);
            filteredThumbnails.addAll(thumbnailPaths);
        } catch (SQLException e) {
            app.showError("Error loading movies: " + e.getMessage());
        }
    }

    private void filterMovies(String query) {
        // Filter movies based on the query
        filteredTitles = movieTitles.stream()
                .filter(title -> title.toLowerCase().contains(query))
                .collect(Collectors.toList());

        filteredPaths = moviePaths.stream()
                .filter(path -> movieTitles.get(moviePaths.indexOf(path)).toLowerCase().contains(query))
                .collect(Collectors.toList());

        filteredThumbnails = thumbnailPaths.stream()
                .filter(thumbnail -> movieTitles.get(thumbnailPaths.indexOf(thumbnail)).toLowerCase().contains(query))
                .collect(Collectors.toList());

        // Update the movie grid
        populateMovieGrid();
    }

    private void populateMovieGrid() {
        movieGrid.removeAll();

        if (filteredTitles.isEmpty()) {
            Label noMoviesLabel = new Label("No movies found", Label.CENTER);
            noMoviesLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noMoviesLabel.setForeground(Color.GRAY);
            movieGrid.add(noMoviesLabel);
        } else {
            for (int i = 0; i < filteredTitles.size(); i++) {
                movieGrid.add(createMovieCard(i));
            }
        }

        movieGrid.revalidate();
        movieGrid.repaint();
    }

    private Panel createMovieCard(int index) {
        Panel card = new Panel(new BorderLayout(5, 5)) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                // Add shadow effect
                g2.setColor(new Color(200, 200, 200, 100));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
            }
        };
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(220, 250)); // Increased card size

        // Movie title
        Label titleLabel = new Label(filteredTitles.get(index), Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(new Color(50, 50, 50));
        card.add(titleLabel, BorderLayout.NORTH);

        // Thumbnail
        Panel thumbnailPanel = new Panel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;

                String thumbnailPath = filteredThumbnails.get(index);
                File thumbnailFile = new File(thumbnailPath);

                if (thumbnailPath != null && thumbnailFile.exists()) {
                    Image thumbnail = Toolkit.getDefaultToolkit().getImage(thumbnailPath);
                    MediaTracker tracker = new MediaTracker(this);
                    tracker.addImage(thumbnail, 0);
                    try {
                        tracker.waitForAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Get original dimensions
                    int originalWidth = thumbnail.getWidth(this);
                    int originalHeight = thumbnail.getHeight(this);

                    // Target dimensions
                    int targetWidth = 200;
                    int targetHeight = 150;

                    // Calculate scaling while maintaining aspect ratio
                    double widthRatio = (double) targetWidth / originalWidth;
                    double heightRatio = (double) targetHeight / originalHeight;
                    double scale = Math.min(widthRatio, heightRatio);

                    int scaledWidth = (int) (originalWidth * scale);
                    int scaledHeight = (int) (originalHeight * scale);

                    // Calculate position to center the image
                    int x = (targetWidth - scaledWidth) / 2;
                    int y = (targetHeight - scaledHeight) / 2;

                    // Draw the scaled image
                    g2.drawImage(thumbnail, x, y, scaledWidth, scaledHeight, this);
                } else {
                    // Draw placeholder if thumbnail is missing
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(0, 0, 200, 150);
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("Arial", Font.PLAIN, 12));
                    g2.drawString("No Thumbnail", 70, 75);
                }
            }
        };
        thumbnailPanel.setPreferredSize(new Dimension(200, 150)); // Updated thumbnail size
        card.add(thumbnailPanel, BorderLayout.CENTER);

        // Play button
        Button playButton = new Button("Play Movie");
        styleButton(playButton, new Color(65, 105, 225));
        playButton.addActionListener(e -> playMovie(index));
        card.add(playButton, BorderLayout.SOUTH);

        return card;
    }

    private void playMovie(int index) {
        PlayerPanel player = (PlayerPanel) app.getCardPanel().getComponent(3);
        player.setMovie(filteredPaths.get(index));
        app.showScreen("PLAYER");
    }

    private void styleButton(Button button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(140, 35)); // Increased button size

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
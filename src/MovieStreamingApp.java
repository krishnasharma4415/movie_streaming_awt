import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MovieStreamingApp extends Frame {
    private CardLayout cardLayout;
    private Panel cardPanel;
    
    // Database connection
    private Connection conn;
    
    public MovieStreamingApp() {
        super("Movie Streaming App");
        setSize(800, 600);
        
        // Initialize database
        MovieDatabase.initialize();
        try {
            conn = MovieDatabase.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Setup UI
        setupUI();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dispose();
                System.exit(0);
            }
        });
    }
    
    private void setupUI() {
        cardLayout = new CardLayout();
        cardPanel = new Panel(cardLayout);
        
        // Create different screens
        LoginPanel loginPanel = new LoginPanel(this);
        RegistrationPanel registrationPanel = new RegistrationPanel(this);
        MovieListPanel movieListPanel = new MovieListPanel(this);
        PlayerPanel playerPanel = new PlayerPanel(this);
        
        // Add screens to card panel
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(registrationPanel, "REGISTER");
        cardPanel.add(movieListPanel, "MOVIES");
        cardPanel.add(playerPanel, "PLAYER");
        
        add(cardPanel);
        
        // Show login screen first
        showScreen("LOGIN");
    }
    
    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public static void main(String[] args) {
        MovieStreamingApp app = new MovieStreamingApp();
        app.setVisible(true);
    }
    public Panel getCardPanel() {
        return cardPanel;
    }    
}
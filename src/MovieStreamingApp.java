import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MovieStreamingApp extends Frame {
    private CardLayout cardLayout;
    private Panel cardPanel;
    private Connection conn;
    
    public MovieStreamingApp() {
        super("Movie Streaming App");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        // Initialize database
        MovieDatabase.initialize();
        try {
            conn = MovieDatabase.getConnection();
        } catch (SQLException e) {
            showError("Database connection failed: " + e.getMessage());
        }
        
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
        
        // Create screens
        cardPanel.add(new LoginPanel(this), "LOGIN");
        cardPanel.add(new RegistrationPanel(this), "REGISTER");
        cardPanel.add(new MovieListPanel(this), "MOVIES");
        cardPanel.add(new PlayerPanel(this), "PLAYER");
        
        // Main layout
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        
        showScreen("LOGIN");
    }
    
    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public Panel getCardPanel() {
        return cardPanel;
    }
    
    public void showError(String message) {
        Dialog errorDialog = new Dialog(this, "Error", true);
        errorDialog.setLayout(new FlowLayout());
        errorDialog.add(new Label(message));
        Button okButton = new Button("OK");
        okButton.addActionListener(e -> errorDialog.dispose());
        errorDialog.add(okButton);
        errorDialog.setSize(300, 100);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        MovieStreamingApp app = new MovieStreamingApp();
        app.setVisible(true);
    }
}
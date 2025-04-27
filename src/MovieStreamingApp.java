import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MovieStreamingApp extends Frame {
    private CardLayout cardLayout;
    private Panel cardPanel;
    private final Color darkGray = new Color(33, 33, 33);

    public MovieStreamingApp() {
        super("Movie Streaming App");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setBackground(darkGray);

        // Initialize TMDb data
        initializeData();

        setupUI();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                dispose();
                System.exit(0);
            }
        });
    }

    private void initializeData() {
        // Initialize TMDb genres
        MovieDatabase.initialize();
    }

    private void setupUI() {
        cardLayout = new CardLayout();
        cardPanel = new Panel(cardLayout);

        // Create screens
        cardPanel.add(new LoginPanel(this), "LOGIN");
        cardPanel.add(new RegistrationPanel(this), "REGISTER");
        cardPanel.add(new MovieListPanel(this), "MOVIES");
        cardPanel.add(new WebPlayerPanel(this), "PLAYER");
        // Detail panel will be created dynamically when a movie is clicked

        // Main layout
        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);

        showScreen("LOGIN");
    }

    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);

        // Adjust title based on screen
        switch (screenName) {
            case "LOGIN":
                setTitle("Movie Streaming App - Login");
                break;
            case "REGISTER":
                setTitle("Movie Streaming App - Register");
                break;
            case "MOVIES":
                setTitle("Movie Streaming App - Browse Movies");
                break;
            case "PLAYER":
                setTitle("Movie Streaming App - Player");
                break;
            case "DETAIL":
                setTitle("Movie Streaming App - Movie Details");
                break;
        }
    }

    public Panel getCardPanel() {
        return cardPanel;
    }

    public void showError(String message) {
        Dialog errorDialog = new Dialog(this, "Message", true);
        errorDialog.setLayout(new BorderLayout());
        errorDialog.setBackground(darkGray);

        // Message panel
        Panel messagePanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        messagePanel.setBackground(new Color(66, 66, 66));

        Label messageLabel = new Label(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(240, 240, 240));
        messagePanel.add(messageLabel);

        errorDialog.add(messagePanel, BorderLayout.CENTER);

        // Button panel
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(50, 50, 50));

        Button okButton = new Button("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.setBackground(new Color(255, 204, 0));
        okButton.setForeground(Color.WHITE);
        okButton.addActionListener(e -> errorDialog.dispose());
        buttonPanel.add(okButton);

        errorDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog size and position
        errorDialog.setSize(400, 150);
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            // For Windows, set Windows look and feel
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }

        MovieStreamingApp app = new MovieStreamingApp();
        app.setVisible(true);
    }
}
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField;
    private TextField passwordField;
    private Label statusLabel;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);

    public LoginPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout(0, 20));
        setBackground(darkGray);

        // Center panel with login form
        Panel loginPanel = new Panel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 300);
            }
        };
        loginPanel.setBackground(lightGray);
        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // App title
        Label titleLabel = new Label("Movie Streaming App", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(darkYellow);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.ipady = 20;
        loginPanel.add(titleLabel, gbc);

        // Reset for other components
        gbc.ipady = 0;
        gbc.gridwidth = 1;

        // Username
        Label usernameLabel = new Label("Username:", Label.RIGHT);
        usernameLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new TextField(20);
        usernameField.setBackground(new Color(55, 55, 55));
        usernameField.setForeground(textColor);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);

        // Password
        Label passwordLabel = new Label("Password:", Label.RIGHT);
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new TextField(20);
        passwordField.setEchoChar('•');
        passwordField.setBackground(new Color(55, 55, 55));
        passwordField.setForeground(textColor);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(passwordField, gbc);

        // Status message
        statusLabel = new Label("", Label.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(statusLabel, gbc);

        // Buttons panel
        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setBackground(lightGray);

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setBackground(darkYellow);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener(e -> login());

        // Add hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(255, 215, 50));
            }

            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(darkYellow);
            }
        });

        buttonsPanel.add(loginButton);

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setBackground(lightGray);
        registerButton.setForeground(textColor);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> app.showScreen("REGISTER"));

        // Add hover effect
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(86, 86, 86));
            }

            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(lightGray);
            }
        });

        buttonsPanel.add(registerButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginPanel.add(buttonsPanel, gbc);

        // Add key listener for Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Center the login panel in the screen
        Panel centerPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(darkGray);
        centerPanel.add(loginPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with note
        Panel notePanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        notePanel.setBackground(darkGray);
        Label noteLabel = new Label("Note: For demo purposes, any username and password will work.", Label.CENTER);
        noteLabel.setForeground(new Color(150, 150, 150));
        notePanel.add(noteLabel);
        add(notePanel, BorderLayout.SOUTH);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }

        // For demo purposes, any username/password works
        app.showScreen("MOVIES");
    }
}
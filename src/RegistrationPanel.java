import java.awt.*;
import java.awt.event.*;
import utils.UserDatabase;

public class RegistrationPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField;
    private TextField passwordField;
    private TextField emailField;
    private Label statusLabel;
    private final Color darkGray = new Color(33, 33, 33);
    private final Color darkYellow = new Color(255, 204, 0);
    private final Color lightGray = new Color(66, 66, 66);
    private final Color textColor = new Color(240, 240, 240);

    public RegistrationPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new BorderLayout(0, 20));
        setBackground(darkGray);

        // Center panel with registration form
        Panel registerPanel = new Panel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 350);
            }
        };
        registerPanel.setBackground(lightGray);
        registerPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // App title
        Label titleLabel = new Label("Create an Account", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(darkYellow);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.ipady = 20;
        registerPanel.add(titleLabel, gbc);

        // Reset for other components
        gbc.ipady = 0;
        gbc.gridwidth = 1;

        // Username
        Label usernameLabel = new Label("Username:", Label.RIGHT);
        usernameLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 1;
        registerPanel.add(usernameLabel, gbc);

        usernameField = new TextField(20);
        usernameField.setBackground(new Color(55, 55, 55));
        usernameField.setForeground(textColor);
        gbc.gridx = 1;
        gbc.gridy = 1;
        registerPanel.add(usernameField, gbc);

        // Email
        Label emailLabel = new Label("Email:", Label.RIGHT);
        emailLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(emailLabel, gbc);

        emailField = new TextField(20);
        emailField.setBackground(new Color(55, 55, 55));
        emailField.setForeground(textColor);
        gbc.gridx = 1;
        gbc.gridy = 2;
        registerPanel.add(emailField, gbc);

        // Password
        Label passwordLabel = new Label("Password:", Label.RIGHT);
        passwordLabel.setForeground(textColor);
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(passwordLabel, gbc);

        passwordField = new TextField(20);
        passwordField.setEchoChar('.');
        passwordField.setBackground(new Color(55, 55, 55));
        passwordField.setForeground(textColor);
        gbc.gridx = 1;
        gbc.gridy = 3;
        registerPanel.add(passwordField, gbc);

        // Status message
        statusLabel = new Label("", Label.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        registerPanel.add(statusLabel, gbc);

        // Buttons panel
        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setBackground(lightGray);

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setBackground(darkYellow);
        registerButton.setForeground(Color.BLACK);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> register());

        // Add hover effect
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(255, 215, 50));
            }

            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(darkYellow);
            }
        });

        buttonsPanel.add(registerButton);

        // Back button
        Button backButton = new Button("Back to Login");
        backButton.setBackground(lightGray);
        backButton.setForeground(textColor);
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setPreferredSize(new Dimension(120, 30));
        backButton.addActionListener(e -> app.showScreen("LOGIN"));

        // Add hover effect
        backButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backButton.setBackground(new Color(86, 86, 86));
            }

            public void mouseExited(MouseEvent e) {
                backButton.setBackground(lightGray);
            }
        });

        buttonsPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        registerPanel.add(buttonsPanel, gbc);

        // Add key listener for Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    register();
                }
            }
        };
        usernameField.addKeyListener(enterKeyListener);
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        // Center the register panel in the screen
        Panel centerPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(darkGray);
        centerPanel.add(registerPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel with note
        Panel notePanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        notePanel.setBackground(darkGray);
        Label noteLabel = new Label("Note: For demo purposes, any input will work.", Label.CENTER);
        noteLabel.setForeground(new Color(150, 150, 150));
        notePanel.add(noteLabel);
        add(notePanel, BorderLayout.SOUTH);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            return;
        }

        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Please enter a valid email address");
            return;
        }

        // Check if username is taken
        if (UserDatabase.isUsernameTaken(username)) {
            statusLabel.setText("Username already taken");
            return;
        }

        // Check if email is taken
        if (UserDatabase.isEmailTaken(email)) {
            statusLabel.setText("Email already registered");
            return;
        }

        // Register the user
        if (UserDatabase.registerUser(username, email, password)) {
            statusLabel.setForeground(new Color(0, 200, 0));
            statusLabel.setText("Registration successful!");

            // Clear fields
            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");

            // Redirect to login after a delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    app.showScreen("LOGIN");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            statusLabel.setText("Registration failed. Please try again.");
        }
    }
}
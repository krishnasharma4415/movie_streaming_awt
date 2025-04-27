import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField;
    private TextField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Label errorLabel;

    public LoginPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBackground(new Color(220, 220, 240)); // Light gradient-like background

        // Main container with rounded corners
        Panel mainPanel = new Panel(new GridBagLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setPreferredSize(new Dimension(400, 400));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // App Logo/Title
        Label titleLabel = new Label("MOVIE STREAM", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 15, 30, 15);
        mainPanel.add(titleLabel, gbc);

        // Username Section
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 15, 5, 15);
        Label usernameLabel = new Label("Username:", Label.RIGHT);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setForeground(new Color(70, 70, 70));
        mainPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new TextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.addFocusListener(new FocusHighlighter());
        mainPanel.add(usernameField, gbc);

        // Password Section
        gbc.gridx = 0;
        gbc.gridy = 2;
        Label passwordLabel = new Label("Password:", Label.RIGHT);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(70, 70, 70));
        mainPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new TextField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setEchoChar('*');
        passwordField.addFocusListener(new FocusHighlighter());
        passwordField.addKeyListener(new EnterKeyListener());
        mainPanel.add(passwordField, gbc);

        // Error Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        errorLabel = new Label("", Label.CENTER);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setForeground(Color.RED);
        mainPanel.add(errorLabel, gbc);

        // Login Button
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 15, 10, 15);
        loginButton = new Button("LOGIN");
        styleButton(loginButton, new Color(65, 105, 225));
        loginButton.addActionListener(new LoginListener());
        mainPanel.add(loginButton, gbc);

        // Register Link
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 15, 20, 15);
        Panel registerPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        Label registerLabel = new Label("Don't have an account?", Label.CENTER);
        registerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        registerPanel.add(registerLabel);

        registerButton = new Button("Register");
        styleLinkButton(registerButton);
        registerButton.addActionListener(e -> app.showScreen("REGISTER"));
        registerPanel.add(registerButton);

        mainPanel.add(registerPanel, gbc);

        // Add main panel to the center
        add(mainPanel);
    }

    private void styleButton(Button button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));

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

    private void styleLinkButton(Button button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(new Color(65, 105, 225));
        button.setBackground(null);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(30, 80, 200));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                button.setForeground(new Color(65, 105, 225));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private class FocusHighlighter extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            Component c = e.getComponent();
            if (c instanceof TextField) {
                c.setBackground(new Color(230, 240, 255));
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            Component c = e.getComponent();
            if (c instanceof TextField) {
                c.setBackground(Color.WHITE);
            }
        }
    }

    private class EnterKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                loginButton.getActionListeners()[0].actionPerformed(
                    new ActionEvent(loginButton, ActionEvent.ACTION_PERFORMED, ""));
            }
        }
    }

    private class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            // Input validation
            if (username.isEmpty()) {
                showError("Please enter your username");
                usernameField.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                showError("Please enter your password");
                passwordField.requestFocus();
                return;
            }

            // Disable buttons during processing
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            loginButton.setLabel("Logging in...");

            // Simulate network delay (remove in production)
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Simulate network delay

                    EventQueue.invokeLater(() -> {
                        try {
                            PreparedStatement stmt = app.getConnection().prepareStatement(
                                "SELECT * FROM users WHERE username = ? AND password = ?");
                            stmt.setString(1, username);
                            stmt.setString(2, password);

                            ResultSet rs = stmt.executeQuery();

                            if (rs.next()) {
                                app.showScreen("MOVIES");
                            } else {
                                showError("Invalid username or password");
                                passwordField.setText("");
                                passwordField.requestFocus();
                            }
                        } catch (SQLException ex) {
                            showError("Database error: " + ex.getMessage());
                        } finally {
                            loginButton.setEnabled(true);
                            registerButton.setEnabled(true);
                            loginButton.setLabel("LOGIN");
                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
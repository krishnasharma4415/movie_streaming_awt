import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField;
    private TextField passwordField;
    
    public LoginPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        Label titleLabel = new Label("Movie Streaming App", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(titleLabel, gbc);
        
        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new Label("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new TextField(20);
        add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new Label("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        add(passwordField, gbc);
        
        // Buttons
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        Button loginButton = new Button("Login");
        loginButton.addActionListener(new LoginListener());
        
        Button registerButton = new Button("Register");
        registerButton.addActionListener(e -> app.showScreen("REGISTER"));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }
    
    private class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                app.showError("Please enter both username and password");
                return;
            }
            
            try {
                PreparedStatement stmt = app.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?");
                stmt.setString(1, username);
                stmt.setString(2, password);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    app.showScreen("MOVIES");
                } else {
                    app.showError("Invalid username or password");
                }
            } catch (SQLException ex) {
                app.showError("Database error: " + ex.getMessage());
            }
        }
    }
}
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistrationPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField, emailField;
    private TextField passwordField;
    
    public RegistrationPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Title
        Label titleLabel = new Label("Create New Account", Label.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
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
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new Label("Email:"), gbc);
        
        gbc.gridx = 1;
        emailField = new TextField(20);
        add(emailField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new Label("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        add(passwordField, gbc);
        
        // Buttons
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        Button registerButton = new Button("Register");
        registerButton.addActionListener(new RegisterListener());
        
        Button backButton = new Button("Back to Login");
        backButton.addActionListener(e -> app.showScreen("LOGIN"));
        
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }
    
    private class RegisterListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                app.showError("Username and password are required");
                return;
            }
            
            try {
                // Check if username exists
                PreparedStatement checkStmt = app.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username = ?");
                checkStmt.setString(1, username);
                
                if (checkStmt.executeQuery().next()) {
                    app.showError("Username already exists");
                    return;
                }
                
                // Insert new user
                PreparedStatement insertStmt = app.getConnection().prepareStatement(
                    "INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email.isEmpty() ? null : email);
                
                if (insertStmt.executeUpdate() > 0) {
                    app.showError("Registration successful! Please login.");
                    usernameField.setText("");
                    emailField.setText("");
                    passwordField.setText("");
                    app.showScreen("LOGIN");
                }
            } catch (SQLException ex) {
                app.showError("Database error: " + ex.getMessage());
            }
        }
    }
}
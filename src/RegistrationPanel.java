import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RegistrationPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField, emailField;
    private TextField passwordField;
    private Label messageLabel;
    
    public RegistrationPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new GridLayout(5, 1));
        
        Panel inputPanel = new Panel(new GridLayout(3, 2));
        inputPanel.add(new Label("Username:"));
        usernameField = new TextField(20);
        inputPanel.add(usernameField);
        
        inputPanel.add(new Label("Email:"));
        emailField = new TextField(20);
        inputPanel.add(emailField);
        
        inputPanel.add(new Label("Password:"));
        passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        inputPanel.add(passwordField);
        
        add(inputPanel);
        
        Panel buttonPanel = new Panel();
        Button registerButton = new Button("Register");
        registerButton.addActionListener(new RegisterListener());
        buttonPanel.add(registerButton);
        
        Button backButton = new Button("Back to Login");
        backButton.addActionListener(e -> app.showScreen("LOGIN"));
        buttonPanel.add(backButton);
        
        add(buttonPanel);
        
        messageLabel = new Label("", Label.CENTER);
        add(messageLabel);
    }
    
    private class RegisterListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username and password are required");
                return;
            }
            
            try {
                // Check if username exists
                PreparedStatement checkStmt = app.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username = ?");
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    messageLabel.setText("Username already exists");
                    return;
                }
                
                // Insert new user
                PreparedStatement insertStmt = app.getConnection().prepareStatement(
                    "INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, email);
                
                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    messageLabel.setText("Registration successful! Please login.");
                    usernameField.setText("");
                    emailField.setText("");
                    passwordField.setText("");
                }
            } catch (SQLException ex) {
                messageLabel.setText("Database error: " + ex.getMessage());
            }
        }
    }
}
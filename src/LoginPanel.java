import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends Panel {
    private MovieStreamingApp app;
    private TextField usernameField;
    private TextField passwordField;
    private Label messageLabel;
    
    public LoginPanel(MovieStreamingApp app) {
        this.app = app;
        setLayout(new GridLayout(4, 1));
        
        Panel inputPanel = new Panel(new GridLayout(2, 2));
        inputPanel.add(new Label("Username:"));
        usernameField = new TextField(20);
        inputPanel.add(usernameField);
        
        inputPanel.add(new Label("Password:"));
        passwordField = new TextField(20);
        passwordField.setEchoChar('*');
        inputPanel.add(passwordField);
        
        add(inputPanel);
        
        Panel buttonPanel = new Panel();
        Button loginButton = new Button("Login");
        loginButton.addActionListener(new LoginListener());
        buttonPanel.add(loginButton);
        
        Button registerButton = new Button("Register");
        registerButton.addActionListener(e -> app.showScreen("REGISTER"));
        buttonPanel.add(registerButton);
        
        add(buttonPanel);
        
        messageLabel = new Label("", Label.CENTER);
        add(messageLabel);
    }
    
    private class LoginListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            try {
                PreparedStatement stmt = app.getConnection().prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?");
                stmt.setString(1, username);
                stmt.setString(2, password);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    messageLabel.setText("Login successful!");
                    app.showScreen("MOVIES");
                } else {
                    messageLabel.setText("Invalid username or password");
                }
            } catch (SQLException ex) {
                messageLabel.setText("Database error: " + ex.getMessage());
            }
        }
    }
}
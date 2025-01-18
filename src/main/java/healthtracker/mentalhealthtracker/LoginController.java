package healthtracker.mentalhealthtracker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.*;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    private Stage primaryStage;

    public void setPrimaryStage(@SuppressWarnings("exports") Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void handleLogin() {
        if (validateLogin(usernameField.getText(), passwordField.getText())) {
            loadTrackerScene();
        } else {
            showAlert("Login failed. Please check your credentials.");
        }
    }

    @FXML
    private void handleSignUp() {
        if (createUser(usernameField.getText(), passwordField.getText())) {
            showAlert("User created successfully. Please log in.");
        } else {
            showAlert("Username already exists. Please choose a different one.");
        }
    }

    private boolean validateLogin(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean createUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadTrackerScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tracker.fxml"));
            Parent root = loader.load();
            TrackerController trackerController = loader.getController();
            trackerController.setCurrentUser(usernameField.getText());
            trackerController.loadUserData();
            Scene trackerScene = new Scene(root, 1000, 800);
            primaryStage.setScene(trackerScene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading tracker screen.");
        }
    }
}
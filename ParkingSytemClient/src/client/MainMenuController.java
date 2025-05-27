package client;

import adminGui.AdminMainMenuController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import subscriberGui.SubscriberDashboardController;

import java.io.IOException; // ✅ Required for FXMLLoader.load()

public class MainMenuController implements ChatIF {

    private ClientController client;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnCheckAvailability;

    @FXML
    private Label statusLabel;

    @Override
    public void display(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    private void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        btnCheckAvailability.setOnAction(this::checkAvailability); // ✅ Add button handler
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password.");
            return;
        }

        boolean sent = client.sendLoginRequest(username, password);
        if (!sent) {
            showAlert("Login request could not be sent.");
        }
    }

    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void redirectBasedOnRole(String role) {
        try {
            FXMLLoader loader;
            Parent root;

            switch (role) {
                case "admin":
                case "supervisor":
                    loader = new FXMLLoader(getClass().getResource("/adminGui/AdminMainMenu.fxml"));
                    break;
                case "subscriber":
                    loader = new FXMLLoader(getClass().getResource("/subscriberGui/SubscriberDashboard.fxml"));
                    break;
                default:
                    showAlert("Unknown role: " + role);
                    return;
            }

            root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AdminMainMenuController) {
                ((AdminMainMenuController) controller).setClient(client);
            } else if (controller instanceof SubscriberDashboardController) {
                ((SubscriberDashboardController) controller).setClient(client);
            }

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - " + role);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load " + role + " dashboard.");
        }
    }

    // ✅ New method for opening PublicAvailability.fxml
    @FXML
    private void checkAvailability(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/guestGui/PublicAvailability.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Public Parking Availability");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

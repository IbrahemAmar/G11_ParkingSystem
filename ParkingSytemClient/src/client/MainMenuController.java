package client;

import adminGui.AdminMainMenuController;
import common.ChatIF;
import entities.LoginRequest;
import entities.LoginResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import subscriberGui.SubscriberDashboardController;

import java.io.IOException;
import java.net.URL;

/**
 * Controller for the main client menu.
 * Handles login actions and access to public parking availability.
 */
public class MainMenuController implements ChatIF {

    private ClientController client;
    private Stage stage; // 👈 stage שמתקבל מבחוץ

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

    /**
     * Allows external controllers (like logout) to pass the current stage.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Displays messages received from the server.
     *
     * @param message The message to display.
     */
    @Override
    public void display(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * Injects the client controller instance.
     *
     * @param client The client controller.
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Initializes UI elements and their handlers.
     */
    @FXML
    private void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        btnCheckAvailability.setOnAction(this::checkAvailability);
    }

    /**
     * Handles login button click.
     * Sends login request to the server.
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password.");
            return;
        }

        try {
            ClientController.getClient().sendToServer(new LoginRequest(username, password));
        } catch (IOException e) {
            showAlert("❌ Failed to send login request.");
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert to the user.
     *
     * @param msg The message to show in the alert.
     */
    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Redirects user to the dashboard based on their role.
     *
     * @param role The role returned by the server (e.g., "admin", "subscriber").
     */
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

            // ✨ משתמשים ב-stage שהועבר מבחוץ או נשלף מהשדה אם הוא null
            Stage currentStage = this.stage != null ? this.stage : (Stage) txtUsername.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("BPARK - " + role);
            currentStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load " + role + " dashboard.");
        }
    }

    /**
     * Handles the action of checking public parking availability.
     *
     * @param event The action event triggered by button click.
     */
    @FXML
    private void checkAvailability(ActionEvent event) {
        try {
            URL fxmlLocation = getClass().getResource("/guestGui/PublicAvailability.fxml");

            if (fxmlLocation == null) {
                throw new IOException("FXML file not found: /guestGui/PublicAvailability.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("🅿️ Parking Availability");
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("❌ Failed to load Public Availability screen.");
        }
    }

    /**
     * Called automatically when a LoginResponse is received.
     *
     * @param response The LoginResponse received from the server.
     */
    public void handleLoginResponse(LoginResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                String[] parts = response.getMessage().split(":");
                String role = parts.length > 1 ? parts[1].trim() : "unknown";
                redirectBasedOnRole(role);
            } else {
                showAlert("❌ " + response.getMessage());
            }
        });
    }
}

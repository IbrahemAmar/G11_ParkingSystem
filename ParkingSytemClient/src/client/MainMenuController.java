package client;

import adminGui.AdminMainMenuController;
import common.ChatIF;
import entities.LoginRequest;
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
    private Stage stage; // The window used to change scenes

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
     *
     * @param stage The JavaFX window used for scene changes.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

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
     * Called automatically after FXML is loaded. Sets up button actions.
     */
    @FXML
    private void initialize() {
        btnLogin.setOnAction(e -> handleLogin());
        btnCheckAvailability.setOnAction(this::checkAvailability);
    }

    /**
     * Sends a login request to the server with the entered credentials.
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
     * Displays an error alert with the given message.
     *
     * @param msg The error message to display.
     */
    public void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Called by the ClientController after receiving a login response.
     *
     * @param success  Indicates if the login was successful.
     * @param message  The login message from the server.
     */
    public void handleLoginResponse(boolean success, String message) {
        Platform.runLater(() -> {
            if (success) {
                String[] parts = message.split(":");
                String role = parts.length > 1 ? parts[1].trim() : "unknown";
                redirectBasedOnRole(role);
            } else {
                showAlert("❌ " + message);
            }
        });
    }


    /**
     * Redirects the user to their respective dashboard screen based on their role.
     * Reuses the current login stage instead of opening a new one.
     *
     * @param role The role returned from the server (e.g., "subscriber", "admin").
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

            // Pass client to the correct controller
            if (controller instanceof AdminMainMenuController adminController) {
                adminController.setClient(client);
            } else if (controller instanceof SubscriberDashboardController subscriberController) {
                subscriberController.setClient(client);
            }

            // Reuse the current login stage instead of opening a new window
            Stage currentStage = this.stage;
            if (currentStage == null && txtUsername != null && txtUsername.getScene() != null) {
                currentStage = (Stage) txtUsername.getScene().getWindow();
            }
            if (currentStage == null) {
                currentStage = ClientController.getPrimaryStage(); // fallback
            }

            if (currentStage != null) {
                // ✅ Set primary stage for future event-less navigation
                ClientController.setPrimaryStage(currentStage);

                currentStage.setScene(new Scene(root));
                currentStage.setTitle("BPARK - " + role);
                currentStage.show();
            } else {
                showAlert("❌ Could not find a valid window to load the scene.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to load " + role + " dashboard: " + e.getMessage());
        }
    }


    /**
     * Navigates to the Public Availability screen (for guests).
     *
     * @param event The action event from button click.
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
}

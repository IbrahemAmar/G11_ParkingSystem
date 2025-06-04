package subscriberGui;

import bpark_common.ClientRequest;
import bpark_common.ServerResponse;
import client.ClientController;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.SceneNavigator;

/**
 * Controller for the ForgotCode screen.
 * Handles the logic for resending the parking code via email or SMS.
 */
public class ForgotCodeController {

    @FXML
    private RadioButton radioEmail;

    @FXML
    private RadioButton radioPhone;

    @FXML
    private ToggleGroup toggleMethod;

    @FXML
    private Button btnSendCode;

    @FXML
    private Button btnBack;

    @FXML
    private Label lblResult;

    /**
     * Called automatically after FXML loading; sets up listeners.
     */
    @FXML
    public void initialize() {
        btnSendCode.setOnAction(event -> handleSendCode());
        btnBack.setOnAction(event -> handleBack());
    }

    /**
     * Handles the logic for sending the code, based on the selected method.
     * If SMS is selected, simulates SMS send and goes back.
     * If email is selected, sends a request to the server to send the code by email.
     */
    private void handleSendCode() {
        if (radioPhone.isSelected()) {
            // Simulate SMS send
            showAlertAndBack("SMS Sent", "A code was sent to your phone number.", Alert.AlertType.INFORMATION);
        } else if (radioEmail.isSelected()) {
            // Send request to server for email
            Subscriber sub = ClientController.getClient().getCurrentSubscriber();
            if (sub == null) {
                lblResult.setText("❌ Subscriber not found.");
                lblResult.setStyle("-fx-text-fill: red;");
                return;
            }
            // Send client request to server (example: "send_code_email", with subscriber code param)
            ClientRequest req = new ClientRequest("send_code_email", new Object[]{sub.getSubscriberCode()});
            ClientController.getClient().sendObjectToServer(req);

            // Show loading
            lblResult.setText("⏳ Sending code to your email...");
            lblResult.setStyle("-fx-text-fill: blue;");
        } else {
            lblResult.setText("❗ Please select a method.");
            lblResult.setStyle("-fx-text-fill: orange;");
        }
    }

    /**
     * Shows an alert and navigates back to the CarPickup screen after closing.
     *
     * @param title    The alert title.
     * @param content  The message content.
     * @param type     The alert type.
     */
    private void showAlertAndBack(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Set handler for when the alert is closed
        alert.setOnHidden(event -> {
            System.out.println("Navigating to CarPickup.fxml");
            SceneNavigator.navigateTo(
                    null,
                    "/subscriberGui/CarPickup.fxml",
                    "BPARK - Retrieve Your Vehicle"
            );
        });

        alert.show(); // NON-blocking!
    }




    /**
     * Handles the Back button, returning the user to the CarPickup screen.
     */
    private void handleBack() {
        SceneNavigator.navigateTo(
                null,
                "/subscriberGui/CarPickup.fxml",
                "BPARK - Retrieve Your Vehicle"
        );
    }

    /**
     * Call this from your ClientController when a server response for send_code_email arrives.
     *
     * @param response The ServerResponse from the server.
     */
    public void handleEmailResponse(ServerResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                showAlertAndBack("Email Sent", "A code was sent to your email.", Alert.AlertType.INFORMATION);
            } else {
                lblResult.setText("❌ " + response.getMessage());
                lblResult.setStyle("-fx-text-fill: red;");
            }
        });
    }

}

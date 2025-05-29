package subscriberGui;

import java.io.IOException;

import client.ClientController;
import entities.Subscriber;
import entities.UpdateResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for EditSubscriberDetails.fxml.
 * Loads the current subscriber's information into editable fields and handles user actions.
 */
public class EditSubscriberDetailsController {

    @FXML
    private TextField txtNewEmail;

    @FXML
    private TextField txtConfirmEmail;

    @FXML
    private TextField txtNewPhone;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnBack;

    @FXML
    private Label lblStatus;  // Add this Label in your FXML for feedback

    /** Holds the current subscriber loaded from ClientController */
    private Subscriber currentSubscriber;

    @FXML
    public void initialize() {
        currentSubscriber = ClientController.getClient().getCurrentSubscriber();
        if (currentSubscriber != null) {
            System.out.println("✅ Populating fields for: " + currentSubscriber.getFullName());
            txtNewEmail.setText(currentSubscriber.getEmail());
            txtConfirmEmail.setText(currentSubscriber.getEmail());
            txtNewPhone.setText(currentSubscriber.getPhone());
        } else {
            System.out.println("⚠️ currentSubscriber is null - cannot populate fields");
        }

        // Register this controller instance with ClientController so it can callback on UpdateResponse
        ClientController.getClient().setEditSubscriberDetailsController(this);
    }


    /**
     * Handles the Back button click event.
     * Returns to the SubscriberSettings screen.
     */
    @FXML
    private void handleBack() {
        switchToSettingsScreen();
    }

    /**
     * Handles the Cancel button click event.
     * Discards changes and returns to the settings screen.
     */
    @FXML
    private void handleCancel() {
        switchToSettingsScreen();
    }

    /**
     * Handles the Save button click event.
     * Validates input and sends updated subscriber info to the server.
     * Disables Save button until response is received.
     *
     * @throws IOException if sending the updated subscriber fails
     */
    @FXML
    private void handleSave() throws IOException {
        String email = txtNewEmail.getText().trim();
        String confirmEmail = txtConfirmEmail.getText().trim();
        String phone = txtNewPhone.getText().trim();

        if (!email.equals(confirmEmail)) {
            lblStatus.setText("❌ Email confirmation does not match.");
            return;
        }

        btnSave.setDisable(true);
        lblStatus.setText("Sending update...");

        Subscriber updatedSubscriber = new Subscriber(
        	    currentSubscriber.getId(),
        	    currentSubscriber.getFullName(),
        	    currentSubscriber.getUsername(),
        	    email,
        	    phone,
        	    currentSubscriber.getSubscriberCode()
        	);


        ClientController.getClient().sendToServer(updatedSubscriber);
    }

    /**
     * Called by ClientController when an UpdateResponse is received from server.
     *
     * @param response The update response containing success status and message.
     */
    public void onUpdateResponse(UpdateResponse response) {
        Platform.runLater(() -> {
            lblStatus.setText(response.getMessage());
            btnSave.setDisable(false);

            if (response.isSuccess()) {
                // Update local subscriber data
            	currentSubscriber = new Subscriber(
            		    currentSubscriber.getId(),
            		    currentSubscriber.getFullName(),
            		    currentSubscriber.getUsername(),
            		    txtNewEmail.getText().trim(),
            		    txtNewPhone.getText().trim(),
            		    currentSubscriber.getSubscriberCode()
            		);

                ClientController.getClient().setCurrentSubscriber(currentSubscriber);
             // Show success popup
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Update Successful");
                alert.setHeaderText(null);
                alert.setContentText("Successfully updated subscriber details.");
                alert.showAndWait();

                // Switch to settings screen after popup closes
                switchToSettingsScreen();
            }
        });
    }

    /**
     * Loads the SubscriberSettings.fxml scene.
     */
    private void switchToSettingsScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("SubscriberSettings.fxml"));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Subscriber Settings");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

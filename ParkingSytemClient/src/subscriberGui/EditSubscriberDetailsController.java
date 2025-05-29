package subscriberGui;

import client.ClientController;
import entities.Subscriber;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    /**
     * Initializes the form with the current subscriber's existing details.
     */
    @FXML
    public void initialize() {
        Subscriber subscriber = ClientController.getClient().getCurrentSubscriber();
        if (subscriber != null) {
            txtNewEmail.setText(subscriber.getEmail());
            txtConfirmEmail.setText(subscriber.getEmail());
            txtNewPhone.setText(subscriber.getPhone());
        }
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
     * Validates input and (in future) saves updates to the server or DB.
     */
    @FXML
    private void handleSave() {
        String email = txtNewEmail.getText().trim();
        String confirmEmail = txtConfirmEmail.getText().trim();
        String phone = txtNewPhone.getText().trim();

        if (!email.equals(confirmEmail)) {
            System.out.println("❌ Email confirmation does not match.");
            return;
        }

        System.out.println("✅ New Email: " + email);
        System.out.println("✅ New Phone: " + phone);

        // TODO: Implement sending update to server/DB

        switchToSettingsScreen();
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


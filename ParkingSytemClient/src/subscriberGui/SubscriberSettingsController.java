package subscriberGui;

import client.ClientController;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for SubscriberSettings.fxml.
 * Displays the currently logged-in subscriber's details using labels.
 */
public class SubscriberSettingsController {

    @FXML
    private Label lblFullName;

    @FXML
    private Label lblSubscriberId;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblCurrentEmail;

    @FXML
    private Label lblCurrentPhone;

    /**
     * Called automatically after the FXML is loaded.
     * Safely updates the UI once the subscriber is available.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Subscriber subscriber = ClientController.getClient().getCurrentSubscriber();
            if (subscriber == null) {
                System.out.println("❌ No subscriber in client");
                return;
            }
            System.out.println("✅ Populating fields for: " + subscriber.getFullName());

            lblFullName.setText(subscriber.getFullName());
            lblSubscriberId.setText(String.valueOf(subscriber.getId()));
            lblUsername.setText(subscriber.getUsername());
            lblCurrentEmail.setText(subscriber.getEmail());
            lblCurrentPhone.setText(subscriber.getPhone());
        });
    }
}

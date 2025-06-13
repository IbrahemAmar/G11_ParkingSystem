package adminGui;

import client.ClientController;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.SceneNavigator;
import javafx.event.ActionEvent;

/**
 * Controller for SubscriberSettings.fxml.
 * Displays the currently logged-in subscriber's details using labels.
 */
public class AdminLogsController{
	
	private ClientController client;

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
    
    public void setClient(ClientController client) {
        this.client = client;
        client.setAdminLogsController(this);
    }

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

    /**
     * Handles the Back button click.
     * Loads the SubscriberDashboard.fxml scene.
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        AdminMainMenuController controller = SceneNavigator.navigateToAndGetController(
            event, "/adminGui/AdminMainMenu.fxml", "Admin Dashboard"
        );
        if (controller != null) controller.setClient(client);
    }
}

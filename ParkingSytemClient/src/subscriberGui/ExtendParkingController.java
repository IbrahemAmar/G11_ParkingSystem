package subscriberGui;

import client.ClientController;
import entities.ExtendParkingRequest;
import entities.Subscriber;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import utils.SceneNavigator;

import java.io.IOException;

/**
 * Controller for the Extend Parking screen.
 * Handles logic for extending parking time by 4 hours.
 */
public class ExtendParkingController {
	
	
	/**
     * Handles the "Back" button action to return to the Subscriber Dashboard.
     * Uses the SceneNavigator to ensure consistent navigation.
     *
     * @param event The ActionEvent triggered by the user's interaction.
     */
    @FXML
    private void goBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/SubscriberDashboard.fxml", "BPARK - Subscriber Dashboard");
    }

    /**
     * Handles the "Finish" button click to extend parking time.
     * Sends an ExtendParkingRequest object to the server.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void handleFinishExtendTime(ActionEvent event) {

        Subscriber currentSubscriber = ClientController.getClient().getCurrentSubscriber();

        if (currentSubscriber == null) {
            System.out.println("❌ No subscriber is currently logged in.");
            return;
        }

        // No try-catch needed here!
        ExtendParkingRequest request = new ExtendParkingRequest(currentSubscriber.getSubscriberCode());
        ClientController.getClient().sendObjectToServer(request);
    }


    /**
     * Called by ClientController when a String response is received.
     *
     * @param message the server's response message
     */
    public void onUpdateResponse(String message) {
        System.out.println("🔔 onUpdateResponse called: " + message);

        javafx.application.Platform.runLater(() -> {
            Alert alert;
            if (message.contains("successfully")) {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Extension Successful!");
                alert.setHeaderText(null);
                alert.setContentText(message);
            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Extension Failed");
                alert.setHeaderText(null);
                alert.setContentText(message);
            }

            alert.showAndWait();
        });
    }


    
    /**
     * Closes the confirmation window when the Finish button is pressed.
     *
     * @param event the action event from the button
     */
    @FXML
    private void handleClose(javafx.event.ActionEvent event) {
        // Close the window containing this button
        javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}

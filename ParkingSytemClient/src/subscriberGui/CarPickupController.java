package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;

/**
 * Controller for CarPickup.fxml.
 * Handles pickup operations and navigation back to the dashboard.
 */
public class CarPickupController {

    /**
     * Handles the "Back" button action to return to the Subscriber Dashboard.
     * Uses the centralized SceneNavigator logic.
     *
     * @param event The ActionEvent triggered by the user's interaction.
     */
    @FXML
    private void goBack(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/SubscriberDashboard.fxml", "BPARK - Subscriber Dashboard");
    }
}

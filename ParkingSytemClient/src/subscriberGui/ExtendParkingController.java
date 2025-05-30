package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;

/**
 * Controller for ExtendParking.fxml.
 * Handles extending parking time and navigation back to the dashboard.
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
    
}

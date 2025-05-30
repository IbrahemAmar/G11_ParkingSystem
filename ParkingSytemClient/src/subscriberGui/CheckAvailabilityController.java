package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;

/**
 * Controller for CheckAvailability.fxml.
 * Navigates to the available parking spots view.
 */
public class CheckAvailabilityController {

    @FXML
    private void handleCheckAvailability(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/AvailableSpots.fxml", "Available Parking Spots");
    }
}

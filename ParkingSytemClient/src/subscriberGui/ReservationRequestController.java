package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;


/**
 * Controller for ReservationRequest.fxml.
 * Handles the reservation request process and navigates back to the dashboard.
 */
public class ReservationRequestController {

    /**
     * Handles the "Back" button action to return to the Subscriber Dashboard.
     * Reloads the dashboard FXML and refreshes its data.
     *
     * @param event The ActionEvent triggered by the user's interaction.
     */
    @FXML
    private void handleBackButton(ActionEvent event) {
    	SceneNavigator.navigateTo(event, "/subscriberGui/SubscriberDashboard.fxml", "BPARK - Subscriber Dashboard");
    }

}

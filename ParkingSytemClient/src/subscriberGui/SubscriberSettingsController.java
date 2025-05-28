package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller for the subscriberSettings.fxml screen.
 * Handles button interactions on the subscriber settings page.
 */
public class SubscriberSettingsController {

    /**
     * Triggered when the "Edit" button is clicked in subscriberSettings.fxml.
     *
     * @param event The button click event.
     */
    @FXML
    private void goToEdit(ActionEvent event) {
        System.out.println("📝 Edit button clicked.");
        // Add future logic for opening edit fields here
    }
}

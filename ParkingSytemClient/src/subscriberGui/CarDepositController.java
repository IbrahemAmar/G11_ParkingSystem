package subscriberGui;

import client.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.SceneNavigator;
import java.io.IOException;

/**
 * Controller for the Car Deposit confirmation screen.
 * Displays the assigned parking spot and handles confirmation.
 *
 * This screen is shown when a subscriber chooses to deposit their car.
 */
public class CarDepositController {

    private ClientController client;

    @FXML
    private Label labelSpotNumber;

    /**
     * Injects the active client controller instance.
     *
     * @param client the active ClientController
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the assigned parking spot number to display on screen.
     *
     * @param spot the assigned parking spot (e.g., "A12")
     */
    public void setSpot(String spot) {
        labelSpotNumber.setText(spot);
    }

    /**
     * Called when the user clicks "Finish" to confirm the deposit.
     * Redirects the user back to the Subscriber Dashboard screen with client injected.
     *
     * @param event the ActionEvent from the button click
     */
    @FXML
    private void confirmDeposit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/SubscriberDashboard.fxml"));
            Parent root = loader.load();

            // Pass client back to dashboard to ensure it has proper context
            SubscriberDashboardController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BPARK - Subscriber Dashboard");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Optional "Back" action. Uses the same logic as confirmDeposit to ensure context.
     *
     * @param event the ActionEvent from the back button
     */
    @FXML
    private void goBack(ActionEvent event) {
        confirmDeposit(event);
    }
}

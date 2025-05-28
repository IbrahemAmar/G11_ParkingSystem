package subscriberGui;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Controller for the subscriber dashboard.
 */
public class SubscriberDashboardController {

    private ClientController client;

    /**
     * Called when the user clicks "Details".
     * Loads the subscriberSettings.fxml into the same window.
     *
     * @param event the button click event
     */
    @FXML
    private void openDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/subscriberSettings.fxml"));
            Parent root = loader.load();

            // Replace scene in the same stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Subscriber Settings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Injects the client controller into this controller.
     *
     * @param client the main client controller
     */
    public void setClient(ClientController client) {
        //
    }
}

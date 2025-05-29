package subscriberGui;

import client.ClientController;
import client.MainMenuController;
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
     * Injects the active client controller into this controller.
     *
     * @param client The active ClientController instance.
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    private void openDetails(ActionEvent event) {
        navigateTo(event, "/subscriberGui/subscriberSettings.fxml", "BPARK - Subscriber Settings");
    }

    @FXML
    private void openExtendParking(ActionEvent event) {
        navigateTo(event, "/subscriberGui/ExtendParking.fxml", "BPARK - Extend Parking");
    }

    @FXML
    private void openReservationRequest(ActionEvent event) {
        navigateTo(event, "/subscriberGui/ReservationRequest.fxml", "BPARK - Reserve Parking");
    }

    @FXML
    private void openCarPickup(ActionEvent event) {
        navigateTo(event, "/subscriberGui/CarPickup.fxml", "BPARK - Car Pickup");
    }

    /**
     * Opens the Car Deposit screen where the user sees the assigned spot.
     *
     * @param event The action event.
     */
    @FXML
    private void openCarDeposit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/CarDeposit.fxml"));
            Parent root = loader.load();

            // Optional: pass the client to the CarDepositController
            Object controller = loader.getController();
            if (controller instanceof CarDepositController depositController) {
                depositController.setClient(client);
                depositController.setSpot("A12"); // Optionally change to dynamic spot from DB
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("BPARK - Vehicle Deposit");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles logout: opens MainMenu.fxml and reuses the current stage.
     *
     * @param event The logout button click event.
     */
    @FXML
    private void logout(ActionEvent event) {
        try {
            // Load the MainMenu.fxml UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            // Get the current active stage (dashboard)
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new login scene on the current stage
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("BPARK - Main Menu");
            currentStage.show();

            // Pass the current stage back to the login controller
            MainMenuController controller = loader.getController();
            controller.setClient(client);
            controller.setStage(currentStage);

            // Optionally update the reference in ClientController
            ClientController.setPrimaryStage(currentStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Utility method to navigate between FXML screens.
     *
     * @param event The action event.
     * @param fxmlPath Path to the FXML file.
     * @param title Title for the new stage.
     */
    private void navigateTo(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Optional: pass the client to the next controller if it supports it

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

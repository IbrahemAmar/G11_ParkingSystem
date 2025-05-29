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
     * Handles logout: opens MainMenu.fxml and sets the stage appropriately.
     *
     * @param event The logout button click event.
     */
    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            // יצירת stage חדש
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("BPARK - Main Menu");

            // מעבירים את ה-stage לקונטרולר
            MainMenuController controller = loader.getController();
            controller.setClient(client); // 👈 להעביר את הקליינט שוב
            controller.setStage(newStage); // 👈 קריטי להמשך

            // שומרים את ה-stage הראשי גם ב-ClientController
            ClientController.setPrimaryStage(newStage);

            // מציגים את המסך
            newStage.show();

            // סוגרים את המסך הנוכחי
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

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

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

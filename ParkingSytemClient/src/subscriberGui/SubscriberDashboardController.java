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

    @FXML
    private void openDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/SubscriberSettings.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Subscriber Settings");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openExtendParking(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/ExtendParking.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Extend Parking");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openReservationRequest(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/ReservationRequest.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Reserve Parking");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openCarPickup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/CarPickup.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Car Pickup");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs out the user and returns to the main menu.
     * Loads MainMenu.fxml into a new stage and passes the stage to the controller.
     */
    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            // יצירת Stage חדש
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setTitle("BPARK - Main Menu");

            // העברת ה-Stage לקונטרולר
            MainMenuController controller = loader.getController();
            controller.setStage(newStage);  // 👈 שורה קריטית

            newStage.show();

            // סגירת החלון הנוכחי
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setClient(ClientController client) {
        this.client = client;
    }
}

package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.io.IOException;

public class CheckAvailabilityController {

    @FXML
    private void handleCheckAvailability(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AvailableSpots.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Available Parking Spots");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

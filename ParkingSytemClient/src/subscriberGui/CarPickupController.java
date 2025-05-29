package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CarPickupController {

	/**
	 * Handles the "Back" button action to return to the Subscriber Dashboard.
	 * Reuses the navigation method to switch the scene.
	 *
	 * @param event The ActionEvent triggered by the user's interaction.
	 */
	@FXML
	private void goBack(ActionEvent event) {
	    navigateTo(event, "/subscriberGui/SubscriberDashboard.fxml", "BPARK - Dashboard");
	}

	/**
	 * Utility method to navigate to a specified FXML scene.
	 * Loads the given FXML file, sets it as the current scene, and updates the window title.
	 *
	 * @param event    The ActionEvent that triggered the navigation.
	 * @param fxmlPath The relative path to the FXML file.
	 * @param title    The title to display on the stage after navigation.
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

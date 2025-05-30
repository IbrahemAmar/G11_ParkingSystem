package utils;

import client.ClientController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class to handle general scene navigation within the application.
 */
public class SceneNavigator {

    /**
     * Navigates to the specified FXML scene and sets the window title.
     * If the destination is the SubscriberDashboard, it injects the ClientController.
     *
     * @param event    The ActionEvent that triggered the navigation.
     * @param fxmlPath The relative path to the FXML file (e.g., "/subscriberGui/SubscriberDashboard.fxml").
     * @param title    The title to display on the stage after navigation.
     */
    public static void navigateTo(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Check if it's the Subscriber Dashboard and inject the client
            if (fxmlPath.equals("/subscriberGui/SubscriberDashboard.fxml")) {
                Object controller = loader.getController();
                if (controller instanceof subscriberGui.SubscriberDashboardController dashboardController) {
                    dashboardController.setClient(ClientController.getClient());
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads the given FXML and returns its controller, while showing the scene.
     * This is useful when you need to perform custom controller setup (e.g., inject client).
     *
     * @param event The ActionEvent that triggered the navigation.
     * @param fxmlPath Path to the FXML file.
     * @param title The window title.
     * @return The controller of the loaded FXML, or null if there was an error.
     */
    public static <T> T navigateToAndGetController(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            T controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    
}

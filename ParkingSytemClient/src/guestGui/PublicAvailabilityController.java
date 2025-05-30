package guestGui;

import client.ClientController;
import entities.ParkingSpace;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import subscriberGui.SubscriberDashboardController;
import utils.SceneNavigator;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the PublicAvailability.fxml.
 * Displays available parking spots to guest users.
 */
public class PublicAvailabilityController {

    @FXML
    private TableView<ParkingSpace> tableAvailability;

    @FXML
    private TableColumn<ParkingSpace, Integer> colSpotNumber;

    @FXML
    private TableColumn<ParkingSpace, String> colStatus;

    @FXML
    private Label lblAvailable;

    @FXML
    private Button btnBack;

    /**
     * Static instance used for external updates from ClientController.
     */
    public static PublicAvailabilityController instance;

    /**
     * Initializes the view when loaded.
     * Sends request to the server to retrieve available parking spots.
     */
    @FXML
    public void initialize() {
        instance = this;

        // Setup column mappings for ParkingSpace fields
        colSpotNumber.setCellValueFactory(new PropertyValueFactory<>("parkingSpaceId"));
        colStatus.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().isAvailable() ? "Available" : "Occupied"));

        // Send initial request to server
        try {
            ClientController.getClient().sendToServer("check available");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Auto-refresh every 10 seconds
        Timeline refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(10), e -> {
                try {
                    ClientController.getClient().sendToServer("check available");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            })
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Updates the table with the list of available parking spaces.
     * Called by ClientController.
     *
     * @param spots List of available parking spaces.
     */
    public static void updateTable(List<ParkingSpace> spots) {
        if (instance != null) {
            instance.tableAvailability.getItems().setAll(spots);
            instance.lblAvailable.setText("✔ " + spots.size() + " spots available");
        }
    }

    /**
     * Handles back button click.
     * Returns to the main menu if not logged in,
     * or to the subscriber dashboard if logged in as subscriber.
     *
     * @param event The action event from the button.
     */
    @FXML
    private void handleBackToMenu(ActionEvent event) {
        String role = client.ClientController.getClient().getUserRole();
        String fxmlPath;
        String title;

        if (role == null || role.isEmpty()) {
            fxmlPath = "/client/MainMenu.fxml";
            title = "BPARK - Main Menu";
        } else if ("subscriber".equalsIgnoreCase(role)) {
            fxmlPath = "/subscriberGui/SubscriberDashboard.fxml";
            title = "BPARK - Subscriber Dashboard";
        } else {
            fxmlPath = "/client/MainMenu.fxml";
            title = "BPARK - Main Menu";
        }

        if ("subscriber".equalsIgnoreCase(role)) {
            // Load and get the controller to inject the client
            SubscriberDashboardController controller = SceneNavigator.navigateToAndGetController(
                event, fxmlPath, title
            );
            if (controller != null) {
                controller.setClient(ClientController.getClient());
            }
        } else {
            // Use the simple dynamic method for MainMenu
            SceneNavigator.navigateToAndGetController(event, fxmlPath, title);
        }
    }
}

package guestGui;

import client.ClientController;
import entities.ParkingSpace;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
        try {
            String role = client.ClientController.getClient().getUserRole();
            FXMLLoader loader;
            Parent root;
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            if (role == null || role.isEmpty()) {
                // Not logged in — go to Main Menu
                loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
                root = loader.load();
                stage.setTitle("BPARK - Main Menu");
            } else if ("subscriber".equalsIgnoreCase(role)) {
                // Logged in as subscriber — go to Subscriber Dashboard
                loader = new FXMLLoader(getClass().getResource("/subscriberGui/SubscriberDashboard.fxml"));
                root = loader.load();
                stage.setTitle("BPARK - Subscriber Dashboard");
            } else {
                // Default fallback to Main Menu
                loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
                root = loader.load();
                stage.setTitle("BPARK - Main Menu");
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
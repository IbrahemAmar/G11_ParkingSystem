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
     * Returns to the main menu screen.
     *
     * @param event The action event from the button.
     */
    @FXML
    private void handleBackToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Main Menu");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package subscriberGui;

import client.ClientController;
import client.MainMenuController;
import entities.ParkingHistory;
import entities.ParkingHistoryRequest;
import entities.Subscriber;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the subscriber dashboard.
 * Handles display of parking history and navigation between screens.
 */
public class SubscriberDashboardController {

    private ClientController client;

    @FXML private TableView<ParkingHistory> tableHistory;
    @FXML private TableColumn<ParkingHistory, String> colEntryTime;
    @FXML private TableColumn<ParkingHistory, String> colExitTime;
    @FXML private TableColumn<ParkingHistory, String> colHistorySpot;
    @FXML private TableColumn<ParkingHistory, String> colWasExtended;
    @FXML private TableColumn<ParkingHistory, String> colWasLate;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Injects the active client controller into this controller.
     * @param client The active ClientController instance.
     */
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setSubscriberDashboardController(this); // Register this controller in the client
        }
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
     * @param event The action event.
     */
    @FXML
    private void openCarDeposit(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscriberGui/CarDeposit.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof CarDepositController depositController) {
                depositController.setClient(client);
                depositController.setSpot("A12"); // Change to dynamic spot if needed
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
     * Handles logout by clearing the client user session data,
     * then loading and displaying the Main Menu (login) screen
     * on the current stage.
     * @param event The ActionEvent triggered by clicking the logout button.
     */
    @FXML
    private void logout(ActionEvent event) {
        try {
            ClientController client = ClientController.getClient();
            if (client != null) {
                client.setUserRole(null);
                client.setCurrentSubscriber(null);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("BPARK - Main Menu");
            currentStage.show();

            MainMenuController controller = loader.getController();
            controller.setClient(client);
            controller.setStage(currentStage);
            ClientController.setPrimaryStage(currentStage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method to navigate between FXML screens.
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

    @FXML
    private void handleOpenPublicAvailability(ActionEvent event) {
        navigateTo(event, "/guestGui/PublicAvailability.fxml", "Public Availability");
    }

    /**
     * Sets the parking history data into the TableView.
     * @param history List of ParkingHistory objects to display.
     */
    public void setParkingHistoryData(ObservableList<ParkingHistory> history) {
        colEntryTime.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getEntryTime().format(formatter))
        );
        colExitTime.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getExitTime().format(formatter))
        );
        colHistorySpot.setCellValueFactory(cell ->
            new SimpleStringProperty(String.valueOf(cell.getValue().getParkingSpaceId()))
        );
        colWasExtended.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().isExtended() ? "Yes" : "No")
        );
        colWasLate.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().isWasLate() ? "Yes" : "No")
        );

        tableHistory.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ParkingHistory item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isWasLate()) {
                    setStyle("-fx-background-color: #FFCDD2;");
                } else {
                    setStyle("");
                }
            }
        });

        tableHistory.setItems(history);
    }

    /**
     * Initializes the dashboard controller by sending a request
     * to load the current subscriber's parking history from the server.
     * Also sets up a listener to refresh the data every time the window gains focus.
     * This method is automatically called after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        refreshParkingHistory(); // First load

        tableHistory.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((winObs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.focusedProperty().addListener((focusObs, wasFocused, isNowFocused) -> {
                            if (isNowFocused) {
                                refreshParkingHistory();
                            }
                        });
                    }
                });
            }
        });
    }


    /**
     * Requests the latest parking history from the server for the current subscriber.
     */
    public void refreshParkingHistory() {
        Platform.runLater(() -> {
            Subscriber subscriber = ClientController.getClient().getCurrentSubscriber();
            if (subscriber != null) {
                String code = subscriber.getSubscriberCode();
                ParkingHistoryRequest request = new ParkingHistoryRequest(code);
                ClientController.getClient().sendObjectToServer(request);
            } else {
                System.out.println("❌ No subscriber logged in.");
            }
        });
    }
}

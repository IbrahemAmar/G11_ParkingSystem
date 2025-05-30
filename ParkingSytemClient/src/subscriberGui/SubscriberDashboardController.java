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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import utils.SceneNavigator;

import java.time.Duration;
import java.time.LocalDateTime;
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
    @FXML private Label labelSpot;
    @FXML private Label labelEntryTime;
    @FXML private Label labelTimeRemaining;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
 // Save the last ActionEvent so we can reuse it for scene navigation
    private ActionEvent lastEvent;

    /**
     * Injects the active client controller into this controller.
     *
     * @param client The active ClientController instance.
     */
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setSubscriberDashboardController(this);
        }
    }

    @FXML
    private void openDetails(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/subscriberSettings.fxml", "BPARK - Subscriber Settings");
    }

    @FXML
    private void openExtendParking(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/ExtendParking.fxml", "BPARK - Extend Parking");
    }

    @FXML
    private void openReservationRequest(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/ReservationRequest.fxml", "BPARK - Reserve Parking");
    }

    @FXML
    private void openCarPickup(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/subscriberGui/CarPickup.fxml", "BPARK - Car Pickup");
    }

    @FXML
    private void handleOpenPublicAvailability(ActionEvent event) {
        SceneNavigator.navigateTo(event, "/guestGui/PublicAvailability.fxml", "Public Availability");
    }
    /**
     * Requests the server to verify whether the user has an active deposit.
     * If the check passes, navigates to the Car Deposit screen.
     *
     * @param event The action event triggering the request.
     */
    @FXML
    private void openCarDeposit(ActionEvent event) {
        // Save the event if needed for later navigation (optional)
        this.lastEvent = event;

        // Send the check request to the server
        String code = client.getCurrentSubscriber().getSubscriberCode();
        client.sendObjectToServer("check_active:" + code);
    }



    /**
     * Handles logout: clears session data and loads main menu screen.
     *
     * @param event The logout button action event.
     */
    @FXML
    private void logout(ActionEvent event) {
        ClientController client = ClientController.getClient();
        if (client != null) {
            client.setUserRole(null);
            client.setCurrentSubscriber(null);
        }

        MainMenuController controller = SceneNavigator.navigateToAndGetController(event,
                "/client/MainMenu.fxml", "BPARK - Main Menu");

        if (controller != null) {
            Stage stage = ClientController.getPrimaryStage();
            controller.setClient(client);
            controller.setStage(stage);
        }
    }

    /**
     * Populates the parking history TableView and updates the current parking session labels.
     *
     * @param history the list of ParkingHistory objects to display
     */
    public void setParkingHistoryData(ObservableList<ParkingHistory> history) {
        // Configure table columns
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
            new SimpleStringProperty(cell.getValue().isExtended() ? "Yes" : "No"))
        ;
        colWasLate.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().isWasLate() ? "Yes" : "No"))
        ;

        // Highlight late sessions
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

        // Bind data
        tableHistory.setItems(history);

        // Detect and display active session
        LocalDateTime now = LocalDateTime.now();
        history.stream()
            .filter(h -> !h.getEntryTime().isAfter(now) && h.getExitTime().isAfter(now))
            .findFirst()
            .ifPresentOrElse(active -> {
                labelSpot.setText(String.valueOf(active.getParkingSpaceId()));
                labelEntryTime.setText(active.getEntryTime().format(formatter));
                long minsLeft = Duration.between(now, active.getExitTime()).toMinutes();
                labelTimeRemaining.setText(minsLeft + " min");
            }, () -> {
                labelSpot.setText("---");
                labelEntryTime.setText("---");
                labelTimeRemaining.setText("---");
            });
    }


    /**
     * Initializes the dashboard controller by loading the subscriber's parking history.
     * Also refreshes when the window regains focus.
     */
    @FXML
    public void initialize() {
        refreshParkingHistory();

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

package adminGui;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import bpark_common.ClientRequest;
import client.ClientController;
import entities.ParkingHistory;
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
import utils.SceneNavigator;

public class AdminParkingHistoryController {
	
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
    
    private Subscriber selectedSubscriber;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public void setClient(ClientController client) {
        this.client = client;
        if (client != null) {
            client.setAdminParkingHistoryController(this);
        }
    }
    
    public void setSelectedSubscriber(Subscriber subscriber) {
        this.selectedSubscriber = subscriber;
    }
    
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
    
    public void refreshParkingHistory() {
        Platform.runLater(() -> {
            if (selectedSubscriber != null) {
                String code = selectedSubscriber.getSubscriberCode();
                ClientController.getClient().sendObjectToServer(
                    new ClientRequest("get_parking_history", new Object[]{code})
                );
            } else {
                System.out.println("❌ No subscriber selected for history.");
            }
        });
    }
    
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
    
    @FXML
    private void handleBack(ActionEvent event) {
    	AdminSubscribersController controller = SceneNavigator.navigateToAndGetController(
            event, "/adminGui/AdminSubscriberManagement.fxml", "Admin Dashboard"
        );
        if (controller != null) controller.setClient(client);
    }
}

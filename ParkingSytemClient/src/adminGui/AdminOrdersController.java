package adminGui;

import client.ClientController;
//import common.ParkingSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.SceneNavigator;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import bpark_common.ClientRequest;
import common.ChatIF;
import entities.ParkingHistory;

public class AdminOrdersController implements ChatIF {
    @FXML private Label lblStatus;

    @Override
    public void display(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

    private ClientController client;

    @FXML private TableView<ParkingHistory> tableActiveParking;
    @FXML private TableColumn<ParkingHistory, String> colActiveSubId;
    @FXML private TableColumn<ParkingHistory, Integer> colActiveSpotId;
    @FXML private TableColumn<ParkingHistory, String> colActiveEntryTime;
    @FXML private TableColumn<ParkingHistory, String> colActiveExpectedExitTime;
    @FXML private TableColumn<ParkingHistory, String> colActiveCode;

    @FXML private TextField txtSearchSubId;
    @FXML private TextField txtSearchSpot;

    @FXML private Button btnSearch;
    @FXML private Button btnClear;
    @FXML private Button btnRefresh;
    
    private ObservableList<ParkingHistory> allActiveSessions = FXCollections.observableArrayList();
    
    public void setClient(ClientController client) {
        this.client = client;
        client.setAdminOrdersController(this);
        loadActiveSessions();
    }

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        colActiveSubId.setCellValueFactory(data -> 
        	new javafx.beans.property.SimpleStringProperty(data.getValue().getSubscriberCode()));
        colActiveSpotId.setCellValueFactory(new PropertyValueFactory<>("parkingSpaceId"));
        colActiveEntryTime.setCellValueFactory(data -> 
        	new javafx.beans.property.SimpleStringProperty(data.getValue().getEntryTime().format(formatter)));
        colActiveExpectedExitTime.setCellValueFactory(data -> 
        	new javafx.beans.property.SimpleStringProperty(data.getValue().getExitTime().format(formatter)));
        colActiveCode.setCellValueFactory(data -> 
        	new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getParkingSpaceId())));
        
        btnSearch.setOnAction(e -> handleSearch());
        btnClear.setOnAction(e -> handleClearSearch());
        btnRefresh.setOnAction(e -> loadActiveSessions());
    }
    
    private void loadActiveSessions() {
    	allActiveSessions.clear();
        tableActiveParking.getItems().clear();
        lblStatus.setText("Loading active sessions...");

        ClientRequest request = new ClientRequest("get_parking_history_all_active", new Object[0]);
        ClientController.getClient().sendObjectToServer(request);
    }
    
    @FXML
    public void setActiveSessions(List<ParkingHistory> sessions) {
        javafx.application.Platform.runLater(() -> {
            allActiveSessions.setAll(sessions);
            tableActiveParking.setItems(allActiveSessions);
            lblStatus.setText(sessions.size() + " active sessions loaded.");
        });
    }
    
    private void handleSearch() {
    	String subId = txtSearchSubId.getText().trim();
        String spot = txtSearchSpot.getText().trim();

        List<ParkingHistory> filtered = allActiveSessions.stream()
            .filter(p -> (subId.isEmpty() || p.getSubscriberCode().contains(subId)) &&
                         (spot.isEmpty() || String.valueOf(p.getParkingSpaceId()).contains(spot)))
            .collect(Collectors.toList());

        tableActiveParking.setItems(FXCollections.observableArrayList(filtered));
        lblStatus.setText("Showing " + filtered.size() + " filtered results.");
    }
    
    private void handleClearSearch() {
    	txtSearchSubId.clear();
        txtSearchSpot.clear();
        tableActiveParking.setItems(allActiveSessions);
        lblStatus.setText("All sessions shown.");
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        AdminMainMenuController controller = SceneNavigator.navigateToAndGetController(
            event, "/adminGui/AdminMainMenu.fxml", "Admin Dashboard"
        );
        if (controller != null) controller.setClient(client);
    }
}

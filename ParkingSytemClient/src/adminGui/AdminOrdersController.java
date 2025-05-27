package adminGui;

import client.ClientController;
import common.ParkingSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

import common.ChatIF;

public class AdminOrdersController implements ChatIF {
    @FXML private Label lblStatus;

    @Override
    public void display(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }

    private ClientController client;

    @FXML private TableView<ParkingSession> tableActiveParking;
    @FXML private TableColumn<ParkingSession, Integer> colActiveSubId;
    @FXML private TableColumn<ParkingSession, String> colActiveCarPlate;
    @FXML private TableColumn<ParkingSession, Integer> colActiveSpotId;
    @FXML private TableColumn<ParkingSession, String> colActiveEntryTime;
    @FXML private TableColumn<ParkingSession, String> colActiveExpectedExitTime;
    @FXML private TableColumn<ParkingSession, String> colActiveCode;

    @FXML private TextField txtSearchSubId;
    @FXML private TextField txtSearchPlate;
    @FXML private TextField txtSearchSpot;

    @FXML private Button btnSearch;
    @FXML private Button btnClear;
    @FXML private Button btnBack;

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminGui/AdminMainMenu.fxml"));
            Parent root = loader.load();

            AdminMainMenuController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClient(ClientController client) {
        this.client = client;
        client.setAdminOrdersController(this);
    }

    @FXML
    public void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        colActiveSubId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSubscriberId()));
        colActiveCarPlate.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCarPlate()));
        colActiveSpotId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getSpotId()));
        colActiveEntryTime.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(formatter.format(data.getValue().getEntryTime())));
        colActiveExpectedExitTime.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(formatter.format(data.getValue().getExpectedExitTime())));
        colActiveCode.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getParkingCode()));
    }

    public void loadActiveParking(List<ParkingSession> sessions) {
        Platform.runLater(() -> {
            ObservableList<ParkingSession> data = FXCollections.observableArrayList(sessions);
            tableActiveParking.setItems(data);
        });
    }
}

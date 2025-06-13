package adminGui;

import java.util.List;

import bpark_common.ClientRequest;
import client.ClientController;
import entities.Subscriber;
import entities.SystemLog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import utils.SceneNavigator;
import javafx.event.ActionEvent;

/**
 * Controller for SubscriberSettings.fxml.
 * Displays the currently logged-in subscriber's details using labels.
 */
public class AdminLogsController{
	
	private ClientController client;

	@FXML private TableView<SystemLog> tableLogs;
	@FXML private TableColumn<SystemLog, Integer> colLogId;
	@FXML private TableColumn<SystemLog, String> colAction;
	@FXML private TableColumn<SystemLog, String> colTarget;
	@FXML private TableColumn<SystemLog, String> colByUser;
	@FXML private TableColumn<SystemLog, String> colTime;
	@FXML private TableColumn<SystemLog, String> colNote;

	@FXML private Button btnRefreshLogs;

    public void setClient(ClientController client) {
        this.client = client;
        client.setAdminLogsController(this);
    }

    /**
     * Called automatically after the FXML is loaded.
     * Safely updates the UI once the subscriber is available.
     */
    @FXML
    public void initialize() {
        colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colTarget.setCellValueFactory(new PropertyValueFactory<>("target"));
        colByUser.setCellValueFactory(new PropertyValueFactory<>("byUser"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("logTime"));
        colNote.setCellValueFactory(new PropertyValueFactory<>("note"));

        btnRefreshLogs.setOnAction(e -> handleRefreshLogs());
        
        handleRefreshLogs();
    }
    
    private void handleRefreshLogs() {
        ClientRequest request = new ClientRequest("get_all_system_logs", new Object[0]);
        client.sendObjectToServer(request);
    }
    
    public void setLogs(List<SystemLog> logs) {
        Platform.runLater(() -> {
            ObservableList<SystemLog> observableLogs = FXCollections.observableArrayList(logs);
            tableLogs.setItems(observableLogs);
        });
    }
    /**
     * Handles the Back button click.
     * Loads the SubscriberDashboard.fxml scene.
     *
     * @param event The action event triggered by the button click.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        AdminMainMenuController controller = SceneNavigator.navigateToAndGetController(
            event, "/adminGui/AdminMainMenu.fxml", "Admin Dashboard"
        );
        if (controller != null) controller.setClient(client);
    }
}

package adminGui;

import java.io.IOException;

import client.ClientController;
import common.ClientRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdminMainMenuController {

    private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
        if ("supervisor".equalsIgnoreCase(client.getUserRole())) {
            if (btnLogs != null) {
                btnLogs.setVisible(false);
            }
        }
    }

    @FXML private Button btnOrders;
    @FXML private Button btnSubscribers;
    @FXML private Button btnLogs;
    @FXML private Button btnExit;

    @FXML
    private void initialize() {
        btnOrders.setOnAction(e -> handleViewActiveParking());
        btnSubscribers.setOnAction(e -> handleManageSubscribers());
        btnLogs.setOnAction(e -> handleViewLogs());
        btnExit.setOnAction(e -> handleExit());
    }

    private void handleExit() {
        try {
        	client.handleMessageFromClientUI(new DisconnectRequest());
        } catch (IOException e) {
            // ui.display("❌ Failed to send disconnect request: " + e.getMessage());
        }
        System.exit(0);
    }

    private void handleViewLogs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminGui/AdminLogs.fxml"));
            Parent root = loader.load();

            AdminLogsController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnLogs.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("System Logs");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleViewActiveParking() {
        try {
            /**
             * Sends a typed request to the server using full object-based communication.
             */
            client.handleMessageFromClientUI(new GetActiveParkingRequest());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminGui/AdminOrders.fxml"));
            Parent root = loader.load();

            AdminOrdersController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnOrders.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Active Parking Details");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleManageSubscribers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminGui/AdminSubscriberManagement.fxml"));
            Parent root = loader.load();
            AdminSubscribersController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnSubscribers.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Subscribers");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

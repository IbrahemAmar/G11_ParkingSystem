package adminGui;

import java.io.IOException;

import bpark_common.ClientRequest;
import client.ClientController;
import entities.DisconnectRequest;
import entities.GetActiveParkingRequest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the Admin Main Menu screen.
 * Handles navigation to system logs, active parking orders, and subscriber management.
 */
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

    /**
     * Handles the exit button by sending a disconnect request to the server and closing the app.
     */
    private void handleExit() {
        try {
            client.handleMessageFromClientUI(new DisconnectRequest());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Handles the "View System Logs" button click.
     * Loads AdminLogs.fxml and transfers the ClientController via setClient().
     */
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

    /**
     * Handles the "View Active Parking Orders" button click.
     * Sends a typed object request to the server, and loads the AdminOrders screen.
     */
    private void handleViewActiveParking() {
        try {
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

    /**
     * Handles the "Manage Subscribers" button click.
     * Loads AdminSubscriberManagement.fxml and passes the client to the next controller.
     */
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

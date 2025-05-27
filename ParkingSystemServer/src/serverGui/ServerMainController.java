package serverGui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import server.BParkServer;
import server.ClientInfo;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Controller for the server GUI. Manages server startup, database connection,
 * and displays connected clients in a table.
 */
public class ServerMainController {

    // ─────────────── UI Components ───────────────

    @FXML private TextField serverIpField, serverPortField, dbIpField, dbPortField, dbUserField;
    @FXML private PasswordField dbPassField;
    @FXML private Label statusLabel;
    @FXML private Button connectButton, disconnectButton;
    @FXML private TableView<ClientInfo> clientTable;
    @FXML private TableColumn<ClientInfo, String> ipColumn, hostColumn, statusColumn;

    // ─────────────── Server & Client List ───────────────

    /** Reference to the main server instance */
    private BParkServer server;

    /** Observable list of connected clients displayed in the table */
    private ObservableList<ClientInfo> clients = FXCollections.observableArrayList();

    /**
     * Initializes the server GUI. Sets default values for input fields and configures the table.
     */
    @FXML
    public void initialize() {
        try {
            serverIpField.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            serverIpField.setText("Unknown");
        }
        dbIpField.setText("localhost");
        dbPortField.setText("3306");
        dbUserField.setText("root");
        dbPassField.setText("Aa123456");
        serverPortField.setText("5555");

        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        clientTable.setItems(clients);
    }

    /**
     * Handles the connect button click. Tries to connect to the database and starts the server.
     */
    @FXML
    void handleConnect() {
        if (server != null && server.isListening()) {
            statusLabel.setText("⚠️ Server is already running.");
            return;
        }

        String dbIp = dbIpField.getText();
        String dbPort = dbPortField.getText();
        String user = dbUserField.getText();
        String pass = dbPassField.getText();
        int serverPort = Integer.parseInt(serverPortField.getText());

        String jdbcUrl = "jdbc:mysql://" + dbIp + ":" + dbPort + "/bpark?serverTimezone=Asia/Jerusalem&useSSL=false&allowPublicKeyRetrieval=true";

        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
            conn.close();

            statusLabel.setText("✅ DB connected. Starting server...");

            // ✅ Pass controller reference to the server
            server = new BParkServer(serverPort, this);
            server.listen();

            statusLabel.setText("✅ Server running on port " + serverPort);
        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stops the server and updates the GUI when disconnect button is clicked.
     */
    @FXML
    void handleDisconnect() {
        if (server != null) {
            try {
                server.close();
                server = null;
                statusLabel.setText("🔴 Server stopped running.");

                for (ClientInfo client : clients) {
                    client.setStatus("Disconnected");
                }
                clientTable.refresh();
            } catch (Exception e) {
                statusLabel.setText("❌ Failed to stop server.");
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("⚠️ Server is not running.");
        }
    }

    /**
     * Exits the application completely.
     */
    @FXML
    void handleExit() {
        System.exit(0);
    }

    /**
     * Adds or updates a client entry in the GUI table.
     * Ensures duplicate or stale entries are removed.
     *
     * @param ip the IP address of the client
     * @param host the hostname of the client
     * @param id a unique identifier (usually hashcode)
     */
    public void addClient(String ip, String host, int id) {
        Platform.runLater(() -> {
            clients.removeIf(client -> client.getId() == id);
            clients.removeIf(client -> client.getIp().equals(ip) && client.getHost().equals(host));
            clients.add(new ClientInfo(ip, host, "Connected", id));
            clientTable.refresh();
        });
    }

    /**
     * Updates a client's status in the table.
     *
     * @param id the client's ID
     * @param status the new status (e.g., "Connected", "Disconnected")
     */
    public void updateClientStatus(int id, String status) {
        Platform.runLater(() -> {
            for (ClientInfo client : clients) {
                if (client.getId() == id) {
                    client.setStatus(status);
                    clientTable.refresh();
                    break;
                }
            }
        });
    }
}

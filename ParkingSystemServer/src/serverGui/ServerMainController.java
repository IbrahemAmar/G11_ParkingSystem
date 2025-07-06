package serverGui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;
import server.BParkServer;
import server.ClientInfo;
import server.DBController;

import java.net.InetAddress;
import java.sql.Connection;
import java.util.Date;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer monthlyReportTimer;


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
            startMonthlyReportScheduler();
            testGenerateReportsNow();


        } catch (Exception e) {
            statusLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Starts a background timer that generates monthly reports on the last day of each month.
     * If a timer is already running, it cancels and replaces it.
     */
    private void startMonthlyReportScheduler() {
        if (monthlyReportTimer != null) {
            monthlyReportTimer.cancel();
        }
        monthlyReportTimer = new Timer(true); // daemon

        TimerTask reportTask = new TimerTask() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1); // for the previous month
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH) + 1; // 0-based to 1-based

                System.out.printf("⚙️ Generating monthly reports for %d-%02d...\n", year, month);
                DBController.generateMonthlyReports(year, month);

                Platform.runLater(() -> statusLabel.setText("✅ Monthly reports generated for " + year + "-" + String.format("%02d", month)));
                
                // after running, reschedule for next
                startMonthlyReportScheduler();
            }
        };

        Date nextRun = getNextLastDayOfMonth();
        monthlyReportTimer.schedule(reportTask, nextRun);

        System.out.println("✅ Monthly report generation scheduled for: " + nextRun);
    }
    
    /**
     * Calculates the next scheduled time for monthly report generation,
     * set to the last day of the current month at 23:59.
     *
     * @return the Date representing the next scheduled run time
     */
    private Date getNextLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);         // next month
        cal.set(Calendar.DAY_OF_MONTH, 1);  // first day next month
        cal.add(Calendar.DATE, -1);         // last day this month
        cal.set(Calendar.HOUR_OF_DAY, 23);  // 23:59
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return  cal.getTime();
    }
    
    /**
     * Manually triggers generation of reports for June 2025 (for testing).
     * Output is printed to the console.
     */
    public void testGenerateReportsNow() {
        int year = 2025;
        int month = 6;
        DBController.generateMonthlyReports(year, month);
        System.out.println("✅ Manual generation done for " + year + "-" + String.format("%02d", month));
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
                if (monthlyReportTimer != null) {
                    monthlyReportTimer.cancel();
                    monthlyReportTimer = null;
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

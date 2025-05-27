package client;

import common.ChatIF;
import common.ClientRequest;
import common.ParkingSession;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.List;

import adminGui.AdminOrdersController;

public class ClientController extends AbstractClient {
    private ChatIF ui;
    private MainMenuController guiController;
    private AdminOrdersController adminOrdersController;
    private String userRole;

    /**
     * Constructs a new client and connects to the server.
     * @param host the server host IP/name
     * @param port the server port
     * @param ui   the UI interface (console or GUI)
     */
    public ClientController(String host, int port, ChatIF ui) throws IOException {
        super(host, port);
        this.ui = ui; 
        openConnection(); 
    }

    /**
     * Handles messages received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String response) {
            if (response.startsWith("ROLE:")) {
                this.userRole = response.substring(5);
                if (guiController != null) {
                    javafx.application.Platform.runLater(() -> guiController.redirectBasedOnRole(this.userRole));
                }
            } else if (response.equals("LOGIN_FAILED")) {
                this.userRole = null;
                if (guiController != null) {
                    javafx.application.Platform.runLater(() -> guiController.showAlert("❌ Login failed. Invalid username or password."));
                }
            } else {
                showMessage(response);
            }
            return;
        }

        if (msg instanceof List<?> list && !list.isEmpty()) {
            if (list.get(0) instanceof ParkingSession && adminOrdersController != null) {
                javafx.application.Platform.runLater(() -> {
                    adminOrdersController.loadActiveParking((List<ParkingSession>) list);
                });
                return;
            }
        }

        showMessage("⚠️ Unknown message from server.");
    }

    /**
     * Sends a request to the server.
     */
    public void sendRequest(ClientRequest request) {
        try {
            sendToServer(request);
        } catch (IOException e) {
            showMessage("❌ Failed to send request to server: " + e.getMessage());
        }
    }

    /**
     * Displays a message when the connection is closed.
     */
    @Override
    protected void connectionClosed() {
        showMessage("🔌 Client disconnected from server.");
    }

    public void closeConnectionSafely() {
        try {
            closeConnection();
        } catch (IOException e) {
            showMessage("❌ Failed to close connection: " + e.getMessage());
        }
    }

    public boolean sendLoginRequest(String username, String password) {
        try {
            sendToServer(new ClientRequest("LOGIN", new Object[] { username, password }));
            this.userRole = null;
            return true;
        } catch (IOException e) {
            showMessage("❌ Failed to send login request: " + e.getMessage());
            return false;
        }
    }

    public String getUserRole() {
        return this.userRole;
    }

    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    public void setAdminOrdersController(AdminOrdersController controller) {
        this.adminOrdersController = controller;
    }

    /**
     * Utility method for displaying UI messages.
     */
    private void showMessage(String msg) {
        if (ui != null) {
            ui.display(msg);
        } else {
            System.out.println("[Server] " + msg);
        }
    }
}

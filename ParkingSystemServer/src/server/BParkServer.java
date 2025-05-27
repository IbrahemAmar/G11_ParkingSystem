package server;

import entities.LoginRequest;
import entities.LoginResponse;
import entities.ErrorResponse;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGui.ServerMainController;


import java.io.IOException;

/**
 * BParkServer handles client messages and interacts with the database.
 */
public class BParkServer extends AbstractServer {

    private DBController dbController;

    // ✅ Reference to the GUI controller for updating the client table
    private ServerMainController guiController;

    /**
     * Constructs the server and initializes DB controller.
     *
     * @param port the port to listen on
     * @param guiController the GUI controller to update client info
     */
    public BParkServer(int port, ServerMainController guiController) {
        super(port);
        this.guiController = guiController;
        dbController = new DBController();
    }

    /**
     * Handles messages received from the client.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof LoginRequest request) {
            String role = dbController.checkUserCredentials(request.getUsername(), request.getPassword());
            boolean isValid = role != null;
            String message = isValid ? "Login successful: " + role : "Invalid credentials";
            try {
                client.sendToClient(new LoginResponse(isValid, message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                client.sendToClient(new ErrorResponse("Unsupported message type."));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when a new client connects to the server.
     * Updates the GUI client table.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getCanonicalHostName();
        int id = client.hashCode();

        if (guiController != null) {
            guiController.addClient(ip, host, id);
        }

        System.out.println("✅ Client connected: " + ip + " / " + host);
    }
}

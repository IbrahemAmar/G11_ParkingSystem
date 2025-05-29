package server;

import entities.*;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGui.ServerMainController;

import java.io.IOException;
import java.util.List;

/**
 * BParkServer handles client messages and interacts with the database.
 */
public class BParkServer extends AbstractServer {

    private final DBController dbController;

    // Reference to the GUI controller for updating the client table
    private final ServerMainController guiController;

    /**
     * Constructs the server and initializes DB controller.
     *
     * @param port          the port to listen on
     * @param guiController the GUI controller to update client info
     */
    public BParkServer(int port, ServerMainController guiController) {
        super(port);
        this.guiController = guiController;
        this.dbController = new DBController();
    }

    /**
     * Handles incoming messages from a client.
     * Supports login requests, subscriber updates, and string-based commands.
     *
     * @param msg    The message received from the client.
     * @param client The client that sent the message.
     */
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof LoginRequest request) {
                handleLoginRequest(request, client);

            } else if (msg instanceof Subscriber subscriber) {
                handleEditData(subscriber, client);

            } else if (msg instanceof String str) {
                switch (str.toLowerCase()) {
                    case "check available" -> {
                        List<ParkingSpace> spots = dbController.getAvailableParkingSpaces();
                        client.sendToClient(spots);
                    }
                    default -> client.sendToClient(new ErrorResponse("Unknown command: " + str));
                }

            } else {
                client.sendToClient(new ErrorResponse("Unsupported message type."));
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send response to client");
            e.printStackTrace();
        }
    }

    /**
     * Processes a login request by checking credentials and sending a response back to the client.
     * If the user is a subscriber, their detailed data is also sent as a Subscriber object.
     *
     * @param request the login request object containing username and password
     * @param client  the connection to the client who sent the login request
     */
    private void handleLoginRequest(LoginRequest request, ConnectionToClient client) throws IOException {
        String role = dbController.checkUserCredentials(request.getUsername(), request.getPassword());
        boolean isValid = role != null;
        String message = isValid ? "Login successful: " + role : "Invalid credentials";

        client.sendToClient(new UpdateResponse(isValid, message));

        // Send full subscriber object if applicable
        if (isValid && "subscriber".equals(role)) {
            Subscriber subscriber = dbController.getSubscriberByUsername(request.getUsername());
            if (subscriber != null) {
                client.sendToClient(subscriber);
            }
        }
    }

    /**
     * Called when a new client connects to the server.
     * Updates the GUI client table.
     *
     * @param client the client connection
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

    /**
     * Handles subscriber update requests.
     *
     * @param subscriber The Subscriber object with updated info.
     * @param client     The client connection to respond to.
     */
    private void handleEditData(Subscriber subscriber, ConnectionToClient client) throws IOException {
        boolean success = dbController.updateSubscriberInfo(subscriber);
        String message = success ? "Subscriber update successful." : "Subscriber update failed.";
        client.sendToClient(new UpdateResponse(success, message));
    }
}

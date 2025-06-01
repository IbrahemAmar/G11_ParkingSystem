package server;

import entities.*;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGui.ServerMainController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import bpark_common.ClientRequest;
import bpark_common.ServerResponse;

/**
 * BParkServer handles client messages and interacts with the database.
 * All responses are now sent using {@link ServerResponse} for consistency.
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
     * Handles incoming messages from a client and sends all responses via {@link ServerResponse}.
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

            } else if (msg instanceof ParkingHistory history) {
                handleParkingHistoryDeposit(history, client);

            } else if (msg instanceof ParkingHistoryRequest request) {
                handleParkingHistoryRequest(request, client);

            } else if (msg instanceof ExtendParkingRequest request) {
                handleExtendParkingRequest(request, client);

            } else if (msg instanceof ClientRequest request) {
                switch (request.getCommand()) {
                    case "car_pickup" -> handleCarPickup(request, client);
                    // ... other custom commands
                    default -> sendError(client, "Unknown client command: " + request.getCommand(), "CLIENT_REQUEST");
                }

            } else if (msg instanceof String str) {
                if (str.toLowerCase().startsWith("check_active:")) {
                    String subCode = str.substring("check_active:".length());
                    boolean hasActive = dbController.hasActiveReservation(subCode);
                    sendServerResponse(client, "CHECK_ACTIVE", hasActive, hasActive ? "Active deposit exists" : "No deposit", null);
                } else {
                    switch (str.toLowerCase()) {
                        case "check available" -> {
                            List<ParkingSpace> spots = dbController.getAvailableParkingSpaces();
                            sendServerResponse(client, "AVAILABLE_SPOTS", true, "Available spots fetched", spots);
                        }
                        case "get random spot" -> handleRandomSpotRequest(client);
                        default -> sendError(client, "Unknown command: " + str, "STRING_COMMAND");
                    }
                }
            } else {
                sendError(client, "Unsupported message type.", "GENERIC");
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send response to client");
            e.printStackTrace();
        }
    }

    /**
     * Processes a login request and sends the result via {@link ServerResponse}.
     * If the user is a subscriber, their detailed data is also sent.
     *
     * @param request the login request object containing username and password
     * @param client  the connection to the client who sent the login request
     */
    private void handleLoginRequest(LoginRequest request, ConnectionToClient client) throws IOException {
        String role = dbController.checkUserCredentials(request.getUsername(), request.getPassword());
        boolean isValid = role != null;
        String message = isValid ? "Login successful: " + role : "Invalid credentials";

        sendServerResponse(client, "LOGIN", isValid, message, null);

        // Send full subscriber object if applicable
        if (isValid && "subscriber".equals(role)) {
            Subscriber subscriber = dbController.getSubscriberByUsername(request.getUsername());
            if (subscriber != null) {
                sendServerResponse(client, "SUBSCRIBER_DATA", true, "Subscriber data", subscriber);
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
     * Handles subscriber update requests and sends the result via {@link ServerResponse}.
     *
     * @param subscriber The Subscriber object with updated info.
     * @param client     The client connection to respond to.
     */
    private void handleEditData(Subscriber subscriber, ConnectionToClient client) throws IOException {
        boolean success = dbController.updateSubscriberInfo(subscriber);
        String message = success ? "Subscriber update successful." : "Subscriber update failed.";
        sendServerResponse(client, "SUBSCRIBER_UPDATE", success, message, null);
    }

    /**
     * Handles a ParkingHistoryRequest from a client and sends the history list via {@link ServerResponse}.
     *
     * @param request the ParkingHistoryRequest containing the subscriber code
     * @param client  the client connection to send the result to
     */
    private void handleParkingHistoryRequest(ParkingHistoryRequest request, ConnectionToClient client) {
        String code = request.getSubscriberCode();
        List<ParkingHistory> history = dbController.getParkingHistoryForSubscriber(code);
        
            sendServerResponse(client, "HISTORY_LIST", true, "Parking history fetched.", history);
        
    }

    /**
     * Handles a request to send a random available parking space.
     * If no spot is found, returns a special {@link ParkingSpace} with ID -1 and availability false.
     *
     * @param client the requesting {@link ConnectionToClient}
     */
    private void handleRandomSpotRequest(ConnectionToClient client) {
        
            int spotId = dbController.getRandomAvailableSpotWithoutA();
            System.out.println("📤 Selected spot from DB: " + spotId);
            ParkingSpace spot = new ParkingSpace(spotId, spotId != -1);
            sendServerResponse(client, "RANDOM_SPOT", spot.isAvailable(), spot.isAvailable() ? "Spot found" : "No spot available", spot);
        
    }

    /**
     * Handles a parking deposit message sent from a subscriber client.
     * Checks for active reservations before inserting the deposit.
     *
     * @param history The ParkingHistory entity representing the deposit action.
     * @param client  The ConnectionToClient instance to send responses back.
     */
    private void handleParkingHistoryDeposit(ParkingHistory history, ConnectionToClient client) {
        System.out.println("📥 Deposit request: " + history.getSubscriberCode());

        // 🛑 Check for existing reservation
        if (dbController.hasActiveReservation(history.getSubscriberCode())) {
            sendError(client, "You already have an active parking reservation.", "PARKING_DEPOSIT");
            return;
        }

        // ✅ Insert into DB
        dbController.insertParkingHistory(history);
        dbController.setSpotAvailability(history.getParkingSpaceId(), false);
        dbController.insertSystemLog("Deposit", "Spot " + history.getParkingSpaceId(), history.getSubscriberCode());
        sendServerResponse(client, "PARKING_DEPOSIT", true, "Parking deposited successfully.", null);
    }

    /**
     * Handles the ExtendParkingRequest logic for extending parking time.
     *
     * @param request the ExtendParkingRequest object received from the client
     * @param client  the client connection to send responses to
     */
    private void handleExtendParkingRequest(ExtendParkingRequest request, ConnectionToClient client) {
        String subscriberCode = request.getSubscriberCode();

        // Step 1: Check if the subscriber has an active parking spot.
        ParkingHistory activeParking = dbController.getActiveParkingBySubscriber(subscriberCode);
        if (activeParking == null) {
            sendServerResponse(client, "EXTEND_PARKING", false, "No active parking found. Please start a new parking session.", null);
            return;
        }

        // Step 2: Calculate new exit time (add 4 hours).
        LocalDateTime newExitTime = activeParking.getExitTime().plusHours(4);

        // Step 3: Check for reservations that conflict with the new exit time.
        boolean hasConflict = dbController.isReservationConflict(
            activeParking.getParkingSpaceId(),
            activeParking.getExitTime(),
            newExitTime
        );
        if (hasConflict) {
            sendServerResponse(client, "EXTEND_PARKING", false, "Cannot extend. Another reservation exists in the selected time window.", null);
            return;
        }

        // Step 4: Update the exit time in the DB.
        int rowsUpdated = dbController.updateExitTime(subscriberCode, newExitTime);
        if (rowsUpdated > 0) {
            sendServerResponse(client, "EXTEND_PARKING", true, "Parking time extended successfully!", null);
        } else {
            sendServerResponse(client, "EXTEND_PARKING", false, "Error occurred while extending parking time.", null);
        }
    }

    /**
     * Handles the car pickup request for a specific subscriber and parking spot.
     * 
     * This method:
     * <ul>
     *   <li>Finds the pending parking session (picked_up = 0) for the given subscriber and spot.</li>
     *   <li>Checks if the user is late (actual pickup after exit_time).</li>
     *   <li>Updates exit_time to the actual pickup time, marks picked_up = 1, 
     *       and sets extended/was_late flags if late.</li>
     *   <li>Marks the parking spot as available.</li>
     *   <li>Logs the action in the system log (as "Pickup" or "Pickup (Late)").</li>
     *   <li>Sends a success or failure response to the client.</li>
     * </ul>
     *
     * @param request The client request containing subscriber code and parking spot ID.
     * @param client  The client connection to respond to.
     */
    private void handleCarPickup(ClientRequest request, ConnectionToClient client) {
        try {
            String subscriberCode = (String) request.getParams()[0];
            int parkingSpaceId = Integer.parseInt(request.getParams()[1].toString());

            // 1. Find the pending (not yet picked up) parking session for this user and spot.
            ParkingHistory pending = dbController.getPendingParkingBySubscriberAndSpot(subscriberCode, parkingSpaceId);

            if (pending == null) {
                sendServerResponse(client, "CAR_PICKUP", false, "No pending parking session found for your code and this spot.", null);
                return;
            }

            // 2. Check if the user is late.
            LocalDateTime now = LocalDateTime.now();
            boolean wasLate = now.isAfter(pending.getExitTime());

            // 3. Update DB: mark as picked up, update exit time, set late/extend if needed.
            int rowsUpdated = dbController.completePickup(subscriberCode, parkingSpaceId, wasLate, now);

            if (rowsUpdated > 0) {
                // 4. Mark the parking spot as available again.
                dbController.setSpotAvailability(parkingSpaceId, true);

                // 5. Log the action.
                dbController.insertSystemLog(
                    wasLate ? "Pickup (Late)" : "Pickup",
                    "Spot " + parkingSpaceId,
                    subscriberCode
                );

                // 6. Inform the client of success, with late message if appropriate.
                sendServerResponse(client, "CAR_PICKUP", true,
                        wasLate
                                ? "Pickup successful, but you were late. Parking was automatically extended."
                                : "Pickup successful. Your car is on the way!",
                        null);
            } else {
                sendServerResponse(client, "CAR_PICKUP", false, "Failed to update parking record.", null);
            }
        } catch (Exception e) {
            sendServerResponse(client, "CAR_PICKUP", false, "Server error during pickup.", null);
            e.printStackTrace();
        }
    }

    /**
     * Utility method for sending a ServerResponse to a client.
     *
     * @param client     the client connection
     * @param command    response command/type
     * @param success    success flag
     * @param message    human-readable message
     * @param data       optional data object (can be null)
     */
    private void sendServerResponse(ConnectionToClient client, String command, boolean success, String message, Object data) {
        try {
            client.sendToClient(new ServerResponse(command, success, message, data));
        } catch (IOException e) {
            System.err.println("❌ Failed to send ServerResponse to client: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Utility method for sending an error ServerResponse to a client.
     *
     * @param client   the client connection
     * @param message  the error message
     * @param context  the response context/type
     */
    private void sendError(ConnectionToClient client, String message, String context) {
        try {
            client.sendToClient(new ServerResponse(context, false, message, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

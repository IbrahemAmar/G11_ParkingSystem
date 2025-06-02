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
 * All protocol actions use ClientRequest/ServerResponse for consistency.
 */
public class BParkServer extends AbstractServer {

    private final DBController dbController;
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
            // System-level requests (login, direct subscriber updates) may still use entity objects:
            if (msg instanceof LoginRequest request) {
                handleLoginRequest(request, client);

            } else if (msg instanceof ClientRequest request) {
                handleClientRequest(request, client);

            } else {
                sendError(client, "Unsupported message type.", "GENERIC");
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send response to client");
            e.printStackTrace();
            sendError(client, "Server error: " + e.getMessage(), "GENERIC");
        }
    }

    /**
     * Handles a unified ClientRequest command.
     */
    private void handleClientRequest(ClientRequest request, ConnectionToClient client) {
        try {
            switch (request.getCommand()) {
                case "get_available_spots" -> handleGetAvailableSpots(client);
                case "get_random_spot" -> handleRandomSpotRequest(client);
                case "car_deposit" -> handleCarDeposit(request, client);
                case "car_pickup" -> handleCarPickup(request, client);
                case "extend_parking" -> handleExtendParkingRequest(request, client);
                case "check_active" -> handleCheckActive(request, client);
                case "get_parking_history" -> handleParkingHistoryRequest(request, client);
                case "update_subscriber" -> handleEditData(request, client);
                case "check_reservation_availability" -> handleCheckReservationAvailability(client);
                default -> sendError(client, "Unknown client command: " + request.getCommand(), "CLIENT_REQUEST");
            }
        } catch (Exception e) {
            sendError(client, "Error handling command: " + e.getMessage(), request.getCommand());
            e.printStackTrace();
        }
    }

    /**
     * Handles available parking spots request.
     */
    private void handleGetAvailableSpots(ConnectionToClient client) {
        List<ParkingSpace> spots = dbController.getAvailableParkingSpaces();
        sendServerResponse(client, "AVAILABLE_SPOTS", true, "Available spots fetched", spots);
    }

    /**
     * Handles a request to send a random available parking space.
     * If no spot is found, returns a ParkingSpace with ID -1.
     */
    private void handleRandomSpotRequest(ConnectionToClient client) {
        int spotId = dbController.getRandomAvailableSpotWithoutA();
        ParkingSpace spot = new ParkingSpace(spotId, spotId != -1);
        sendServerResponse(client, "RANDOM_SPOT", spot.isAvailable(), spot.isAvailable() ? "Spot found" : "No spot available", spot);
    }

    /**
     * Handles deposit request as a ClientRequest.
     */
    private void handleCarDeposit(ClientRequest request, ConnectionToClient client) {
        ParkingHistory history = (ParkingHistory) request.getParams()[0];

        if (dbController.hasActiveReservation(history.getSubscriberCode())) {
            sendError(client, "You already have an active parking reservation.", "PARKING_DEPOSIT");
            return;
        }
        dbController.insertParkingHistory(history);
        dbController.setSpotAvailability(history.getParkingSpaceId(), false);
        dbController.insertSystemLog("Deposit", "Spot " + history.getParkingSpaceId(), history.getSubscriberCode());
        sendServerResponse(client, "PARKING_DEPOSIT", true, "Parking deposited successfully.", null);
    }

    /**
     * Handles extend parking time request as a ClientRequest.
     */
    private void handleExtendParkingRequest(ClientRequest request, ConnectionToClient client) {
        String subscriberCode = (String) request.getParams()[0];

        ParkingHistory activeParking = dbController.getActiveParkingBySubscriber(subscriberCode);
        if (activeParking == null) {
            sendServerResponse(client, "EXTEND_PARKING", false, "No active parking found. Please start a new parking session.", null);
            return;
        }
        LocalDateTime newExitTime = activeParking.getExitTime().plusHours(4);

        boolean hasConflict = dbController.isReservationConflict(
            activeParking.getParkingSpaceId(),
            activeParking.getExitTime(),
            newExitTime
        );
        if (hasConflict) {
            sendServerResponse(client, "EXTEND_PARKING", false, "Cannot extend. Another reservation exists in the selected time window.", null);
            return;
        }
        int rowsUpdated = dbController.updateExitTime(subscriberCode, newExitTime);
        if (rowsUpdated > 0) {
            sendServerResponse(client, "EXTEND_PARKING", true, "Parking time extended successfully!", null);
        } else {
            sendServerResponse(client, "EXTEND_PARKING", false, "Error occurred while extending parking time.", null);
        }
    }

    /**
     * Handles car pickup request as a ClientRequest.
     */
    private void handleCarPickup(ClientRequest request, ConnectionToClient client) {
        String subscriberCode = (String) request.getParams()[0];
        int parkingSpaceId = Integer.parseInt(request.getParams()[1].toString());

        ParkingHistory pending = dbController.getPendingParkingBySubscriberAndSpot(subscriberCode, parkingSpaceId);

        if (pending == null) {
            sendServerResponse(client, "CAR_PICKUP", false, "No pending parking session found for your code and this spot.", null);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean wasLate = now.isAfter(pending.getExitTime());

        int rowsUpdated = dbController.completePickup(subscriberCode, parkingSpaceId, wasLate, now);

        if (rowsUpdated > 0) {
            dbController.setSpotAvailability(parkingSpaceId, true);
            dbController.insertSystemLog(
                wasLate ? "Pickup (Late)" : "Pickup",
                "Spot " + parkingSpaceId,
                subscriberCode
            );
            sendServerResponse(client, "CAR_PICKUP", true,
                wasLate
                    ? "Pickup successful, but you were late. Parking was automatically extended."
                    : "Pickup successful. Your car is on the way!",
                null);
        } else {
            sendServerResponse(client, "CAR_PICKUP", false, "Failed to update parking record.", null);
        }
    }

    /**
     * Handles check_active (check if user has an active deposit) as a ClientRequest.
     */
    private void handleCheckActive(ClientRequest request, ConnectionToClient client) {
        String subCode = (String) request.getParams()[0];
        boolean hasActive = dbController.hasActiveReservation(subCode);
        sendServerResponse(client, "CHECK_ACTIVE", hasActive, hasActive ? "Active deposit exists" : "No deposit", null);
    }

    /**
     * Handles parking history request as a ClientRequest.
     */
    private void handleParkingHistoryRequest(ClientRequest request, ConnectionToClient client) {
        String code = (String) request.getParams()[0];
        List<ParkingHistory> history = dbController.getParkingHistoryForSubscriber(code);
        sendServerResponse(client, "HISTORY_LIST", true, "Parking history fetched.", history);
    }

    /**
     * Handles subscriber update request as a ClientRequest.
     */
    private void handleEditData(ClientRequest request, ConnectionToClient client) {
        Subscriber subscriber = (Subscriber) request.getParams()[0];
        boolean success = dbController.updateSubscriberInfo(subscriber);
        String message = success ? "Subscriber update successful." : "Subscriber update failed.";
        sendServerResponse(client, "SUBSCRIBER_UPDATE", success, message, null);
    }

    /**
     * Processes a login request and sends the result via {@link ServerResponse}.
     * If the user is a subscriber, their detailed data is also sent.
     */
    private void handleLoginRequest(LoginRequest request, ConnectionToClient client) {
        String role = dbController.checkUserCredentials(request.getUsername(), request.getPassword());
        boolean isValid = role != null;
        String message = isValid ? "Login successful: " + role : "Invalid credentials";
        sendServerResponse(client, "LOGIN", isValid, message, null);

        // Send full subscriber object if applicable
        if (isValid && "subscriber".equals(role)) {
            Subscriber subscriber = dbController.getSubscriberByUsername(request.getUsername());
            if (subscriber != null) {
                sendServerResponse(client, "SUBSCRIBER_DATA", true, "Subscriber data", subscriber);
                
                // this line for the access mode
                sendServerResponse(client, "ACCESS_MODE", true, "Access mode data", request.getAccessMode());
            }
        }
    }

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
     */
    private void sendError(ConnectionToClient client, String message, String context) {
        sendServerResponse(client, context, false, message, null);
    }
    
    /**
     * Handles the check for reservation availability.
     *
     * @param client The client to send the response to.
     */
    private void handleCheckReservationAvailability(ConnectionToClient client) {
        try {
            boolean isPossible = dbController.isReservationPossible();
            String message = isPossible ? "Reservation is possible." : "Reservation is not possible (less than 40% spots available).";
            ServerResponse response = new ServerResponse(
                "check_reservation_availability",
                true,
                message,
                isPossible
            );
            client.sendToClient(response);
        } catch (Exception e) {
            sendError(client, "Error checking reservation availability: " + e.getMessage(), "check_reservation_availability");
            e.printStackTrace();
        }
    }

}

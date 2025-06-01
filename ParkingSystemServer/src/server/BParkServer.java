package server;

import entities.*;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGui.ServerMainController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import bpark_common.ClientRequest;

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

            }else if (msg instanceof ParkingHistory history) {
                handleParkingHistoryDeposit(history,client);
            }

            else if (msg instanceof ParkingHistoryRequest request) {
                handleParkingHistoryRequest(request, client);

            } else if (msg instanceof ExtendParkingRequest request) {
            	handleExtendParkingRequest(request, client);
            }else if (msg instanceof ClientRequest request) {
                switch (request.getCommand()) {
                case "car_pickup" -> handleCarPickup(request, client);

                // ... other custom commands
                default -> client.sendToClient(new ErrorResponse("Unknown client command: " + request.getCommand()));
            }

            } else if (msg instanceof String str) {
                if (str.toLowerCase().startsWith("check_active:")) {
                    String subCode = str.substring("check_active:".length());
                    boolean hasActive = dbController.hasActiveReservation(subCode);
                    try {
                        client.sendToClient(hasActive ? "active deposit exists" : "no deposit");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    switch (str.toLowerCase()) {
                        case "check available" -> {
                            List<ParkingSpace> spots = dbController.getAvailableParkingSpaces();
                            client.sendToClient(spots);
                        }

                        case "get random spot" -> handleRandomSpotRequest(client);

                        default -> client.sendToClient(new ErrorResponse("Unknown command: " + str));
                    }
                }
            }
 else {
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
    /**
     * Handles a ParkingHistoryRequest from a client.
     * Retrieves the parking history for the specified subscriber code and sends it to the client.
     *
     * @param request the ParkingHistoryRequest containing the subscriber code
     * @param client  the client connection to send the result to
     */
    private void handleParkingHistoryRequest(ParkingHistoryRequest request, ConnectionToClient client) {
        String code = request.getSubscriberCode();
        List<ParkingHistory> history = dbController.getParkingHistoryForSubscriber(code);

        try {
            client.sendToClient(history);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles a request to send a random available parking space that:
     * - Is not reserved for the next 4 hours.
     * - Has a numeric spot ID.
     *
     * If no spot is found, returns a special {@link ParkingSpace} with ID -1 and availability false.
     *
     * @param client the requesting {@link ConnectionToClient}
     */
    private void handleRandomSpotRequest(ConnectionToClient client) {
        try {
            int spotId = dbController.getRandomAvailableSpotWithoutA();
            System.out.println("📤 Selected spot from DB: " + spotId);
            // If no spot is found, use -1 as a signal
            ParkingSpace spot = new ParkingSpace(spotId, spotId != -1);
            client.sendToClient(spot);

        } catch (IOException e) {
            System.err.println("❌ Failed to send ParkingSpace to client.");
            e.printStackTrace();
        }
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
            try {
                client.sendToClient(new ErrorResponse("You already have an active parking reservation."));
            } catch (IOException e) {
                System.err.println("❌ Failed to send error to client.");
                e.printStackTrace();
            }
            return;
        }

        // ✅ Insert into DB
        dbController.insertParkingHistory(history);
        dbController.setSpotAvailability(history.getParkingSpaceId(), false);
        dbController.insertSystemLog("Deposit", "Spot " + history.getParkingSpaceId(), history.getSubscriberCode());
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
            sendStringToClient(client, "❌ No active parking found. Please start a new parking session.");
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
            sendStringToClient(client, "❌ Cannot extend. Another reservation exists in the selected time window.");
            return;
        }

        // Step 4: Update the exit time in the DB.
        int rowsUpdated = dbController.updateExitTime(subscriberCode, newExitTime);
        if (rowsUpdated > 0) {
            sendStringToClient(client, "✅ Parking time extended successfully!");
        } else {
            sendStringToClient(client, "❌ Error occurred while extending parking time.");
        }
    }
    
    /**
     * Sends a String message to the client.
     *
     * @param client  the client connection
     * @param message the message to send
     */
    private void sendStringToClient(ConnectionToClient client, String message) {
        try {
            client.sendToClient(message);
        } catch (IOException e) {
            e.printStackTrace();
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
                client.sendToClient(new ErrorResponse("No pending parking session found for your code and this spot."));
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
                client.sendToClient(new UpdateResponse(true,
                    wasLate
                        ? "Pickup successful, but you were late. Parking was automatically extended."
                        : "Pickup successful. Your car is on the way!"
                ));
            } else {
                client.sendToClient(new ErrorResponse("Failed to update parking record."));
            }
        } catch (Exception e) {
            try {
                client.sendToClient(new ErrorResponse("Server error during pickup."));
            } catch (IOException ignored) {}
            e.printStackTrace();
        }
    }




}

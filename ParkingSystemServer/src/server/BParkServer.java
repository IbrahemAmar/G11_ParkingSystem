package server;

import entities.*;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGui.ServerMainController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
                case "get_valid_start_times" -> handleGetValidStartTimes(request, client);
                case "add_reservation" -> handleReservation(request, client);
                case "send_code_email" -> handleSendCodeEmail(request, client);
                case "scan_tag_login" -> handleScanTagLogin(request, client);
                case "get_parking_history_all_active" -> handleGetAllActiveParkings(client);
                case "get_subscribers_all_active" -> handleGetAllSubscribers(client);
                case "add_subscriber" -> handleAddSubscriber(request, client);
                case "get_all_system_logs" -> handleGetLogs(client);
                case "get_monthly_parking_time" -> handleMonthlyParkingTime(client);
                case "get_monthly_subscriber_report" -> handleMonthlySubscriberReport(client);
                case "get_subscriber_contact" -> handleGetSubscriberContact(request, client);

                default -> sendError(client, "Unknown client command: " + request.getCommand(), "CLIENT_REQUEST");
            }
        } catch (Exception e) {
            sendError(client, "Error handling command: " + e.getMessage(), request.getCommand());
            e.printStackTrace();
        }
    }
    private void handleMonthlySubscriberReport(ConnectionToClient client) {
        List<Integer> dailyCounts = dbController.getMonthlySubscriberCounts();
        boolean success = dailyCounts != null;

        sendServerResponse(
            client,
            "monthly_subscriber_report_result",
            success,
            success ? "Subscriber daily activity retrieved." : "Error retrieving subscriber activity.",
            dailyCounts
        );
    }

    private void handleMonthlyParkingTime(ConnectionToClient client) {
        List<Integer> durations = dbController.getMonthlyParkingTimeSummary();
        boolean success = durations != null;

        sendServerResponse(
            client,
            "monthly_parking_time_result",
            success,
            success ? "Monthly data retrieved" : "Error retrieving data",
            durations
        );
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
     * Rounds entry and exit time to nearest quarter hour before saving.
     */
    private void handleCarDeposit(ClientRequest request, ConnectionToClient client) {
        ParkingHistory history = (ParkingHistory) request.getParams()[0];

        // עיגול הזמנים לפני שמירתם
        LocalDateTime roundedEntry = roundToQuarterHour(history.getEntryTime());
        LocalDateTime roundedExit = roundToQuarterHour(history.getExitTime());
        history.setEntryTime(roundedEntry);
        history.setExitTime(roundedExit);

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
     * Rounds the given LocalDateTime to the nearest quarter hour:
     * 0-14 → 0, 15-29 → 15, 30-44 → 30, 45-59 - > 45
     */
    private LocalDateTime roundToQuarterHour(LocalDateTime dt) {
        int minute = dt.getMinute();
        if (minute >= 0 && minute <= 14) {
            return dt.withMinute(0).withSecond(0).withNano(0);
        } else if (minute >= 15 && minute <= 29) {
            return dt.withMinute(15).withSecond(0).withNano(0);
        } else if (minute >= 30 && minute <= 44) {
            return dt.withMinute(30).withSecond(0).withNano(0);
        } else { // 46-59
            return dt.withMinute(45).withSecond(0).withNano(0);
        }
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
     * Rounds the pickup (exit) time to the nearest quarter hour before updating the record.
     *
     * @param request The client request containing subscriber code and parking space ID.
     * @param client The client connection.
     */
    private void handleCarPickup(ClientRequest request, ConnectionToClient client) {
        String subscriberCode = (String) request.getParams()[0];
        int parkingSpaceId = Integer.parseInt(request.getParams()[1].toString());

        ParkingHistory pending = dbController.getPendingParkingBySubscriberAndSpot(subscriberCode, parkingSpaceId);

        if (pending == null) {
            sendServerResponse(client, "CAR_PICKUP", false, "No pending parking session found for your code and this spot.", null);
            return;
        }

        // Round the current time to the nearest quarter hour
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime roundedNow = roundToQuarterHour(now);

        boolean wasLate = roundedNow.isAfter(pending.getExitTime());

        int rowsUpdated = dbController.completePickup(subscriberCode, parkingSpaceId, wasLate, roundedNow);

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
        
        //Save Username to access it later
        if (isValid) {
            client.setInfo("username", request.getUsername());
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
    
    /**
     * Handles the request to get valid start times for a given date and subscriber.
     */
    private void handleGetValidStartTimes(ClientRequest request, ConnectionToClient client) {
        System.out.println("✅ Server received request for valid start times! BParkServer.java");

        LocalDate selectedDate = (LocalDate) request.getParams()[0];
        String subscriberCode = (String) request.getParams()[1]; // נוסף!

        List<LocalTime> validStartTimes = dbController.getAvailableTimesForDate(selectedDate, subscriberCode);

        ServerResponse response = new ServerResponse(
            "get_valid_start_times",
            true,
            "Available start times fetched",
            validStartTimes
        );
        try {
            client.sendToClient(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles a reservation request from the client.
     * Attempts to add a reservation with a random free spot, generates a confirmation code,
     * sends an email, and adds a system log entry if successful.
     * Sends a ServerResponse to the client with the result.
     *
     * @param request The client request containing a Reservation object.
     * @param client  The client connection.
     */
    private void handleReservation(ClientRequest request, ConnectionToClient client) {
        ServerResponse response;
        try {
            // Get Reservation object from request
            Reservation reservation = (Reservation) request.getParams()[0];

            // Try to add reservation using existing dbController
            boolean success = dbController.addReservationRandomSpotWithConfirmation(reservation);

            if (success) {
                // Add to system log
                dbController.insertSystemLog(
                    "Add Reservation",
                    "Reservation attempt",
                    reservation.getSubscriberCode()
                );
            }

            // Prepare response
            response = new ServerResponse(
                "add_reservation",
                success,
                success
                    ? "✅ Reservation successful! Confirmation code sent to your email."
                    : "❌ Reservation failed. No available spots or another error occurred.",
                null
            );
            client.sendToClient(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response = new ServerResponse(
                "add_reservation",
                false,
                "❌ Server error during reservation. Please try again later.",
                null
            );
            try {
                client.sendToClient(response);
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Handles a request to resend the current active parking code to the subscriber's email.
     * The request param must be the subscriber code (String).
     * Responds with ServerResponse (success or error message).
     *
     * @param request The client request (expects subscriberCode as param[0]).
     * @param client  The client connection.
     */
    private void handleSendCodeEmail(ClientRequest request, ConnectionToClient client) {
        ServerResponse response;
        try {
            String subscriberCode = (String) request.getParams()[0];

            // 1. Get the active parking session for the subscriber
            ParkingHistory activeParking = dbController.getActiveParkingBySubscriber(subscriberCode);
            if (activeParking == null) {
                response = new ServerResponse(
                    "send_code_email", false, "No active parking session found for your code.", null
                );
                client.sendToClient(response);
                return;
            }

            // 2. Get the subscriber's email
            String email = dbController.getSubscriberEmail(subscriberCode);
            if (email == null) {
                response = new ServerResponse(
                    "send_code_email", false, "Email not found for subscriber.", null
                );
                client.sendToClient(response);
                return;
            }

            // 3. Prepare the email message with the active parking code (here: parking_space_id)
            String subject = "BPARK: Your Active Parking Code";
            String body = "Your active parking spot code is: " + activeParking.getParkingSpaceId()
                        + "\nEntry Time: " + activeParking.getEntryTime()
                        + "\nExit Time: " + activeParking.getExitTime();

            // 4. Send the email
            try {
                utils.EmailUtil.sendEmail(email, subject, body);
                response = new ServerResponse(
                    "send_code_email", true, "Parking code sent to your email.", null
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                response = new ServerResponse(
                    "send_code_email", false, "Failed to send email. Try again later.", null
                );
            }

            // 5. Respond to client
            client.sendToClient(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response = new ServerResponse(
                "send_code_email", false, "Server error while sending parking code.", null
            );
            try { client.sendToClient(response); } catch (Exception ignore) {}
        }
    }

    private void handleScanTagLogin(ClientRequest request, ConnectionToClient client) {
        try {
            String scannedId = (String) request.getParams()[0];
            String[] userData = dbController.getUserCredentialsByUserId(scannedId);

            if (userData != null) {
                ServerResponse response = new ServerResponse(
                    "scan_tag_login",
                    true,
                    "User data found for scanned ID.",
                    userData
                );
                client.sendToClient(response);
            } else {
                ServerResponse response = new ServerResponse(
                    "scan_tag_login",
                    false,
                    "ID not found.",
                    null
                );
                client.sendToClient(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient(new ServerResponse(
                    "scan_tag_login",
                    false,
                    "Server error while processing scan tag login.",
                    null
                ));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends all currently active parking sessions to the admin GUI.
     */
    private void handleGetAllActiveParkings(ConnectionToClient client) {
        List<ParkingHistory> activeList = dbController.getAllActiveParkings();
        sendServerResponse(client, "ADMIN_ACTIVE_SESSIONS", true, "All active parkings fetched.", activeList);
    }
    
    private void handleGetAllSubscribers(ConnectionToClient client) {
        List<Subscriber> activeList = dbController.getAllSubscribers();
        sendServerResponse(client, "ADMIN_SUBSCRIBERS", true, "All Subscribers fetched.", activeList);
    }
    
    private void handleAddSubscriber(ClientRequest request, ConnectionToClient client) {
        try {
            Subscriber subscriber = (Subscriber) request.getParams()[0];
            String password = (String) request.getParams()[1];
            String firstName = (String) request.getParams()[2];
            String lastName = (String) request.getParams()[3];
            boolean success = dbController.addSubscriber(subscriber, password, firstName, lastName);
            
            if(success == true) {
            	String username = (String) client.getInfo("username");
            	int byUserId = dbController.getUserIdByUsername(username);
            	String target = "Target-" + subscriber.getId();//dbController.getNextSystemLogId();
            	dbController.insertSubscriberSystemLog("Add User", target, byUserId);
            }
            
            String message = success ? "Subscriber added successfully." : "Failed to add subscriber.";
            sendServerResponse(client, "ADMIN_SUBSCRIBERS", success, message, dbController.getAllSubscribers());
        } catch (Exception e) {
            sendError(client, "Server error: " + e.getMessage(), "ADMIN_SUBSCRIBERS");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles request to fetch all system logs for the admin.
     */
    private void handleGetLogs(ConnectionToClient client) {
        try {
            List<SystemLog> logs = dbController.getAllSystemLogs();
            sendServerResponse(client, "ADMIN_LOGS", true, "System logs fetched.", logs);
        } catch (Exception e) {
            e.printStackTrace();
            sendServerResponse(client, "ADMIN_LOGS", false, "Failed to retrieve logs.", null);
        }
    }
    /**
     * Handles a client request to retrieve a subscriber's contact information (email and phone)
     * using their subscriber code. This is typically used by the ForgotCode screen to update
     * the displayed contact method options.
     * <p>
     * Expected client request: <br>
     * {@code new ClientRequest("get_subscriber_contact", new Object[] {subscriberCode})}
     * <p>
     * Server sends back: <br>
     * A {@link ServerResponse} containing a {@code Map<String, String>} with keys:
     * <ul>
     *     <li>{@code "email"} – The subscriber's email address</li>
     *     <li>{@code "phone"} – The subscriber's phone number</li>
     * </ul>
     * or an error message if the subscriber is not found or contact info is missing.
     *
     * @param request The {@link ClientRequest}, expected to contain a single parameter: subscriberCode.
     * @param client  The {@link ConnectionToClient} to send the response to.
     */
    private void handleGetSubscriberContact(ClientRequest request, ConnectionToClient client) {
        try {
            String subscriberCode = (String) request.getParams()[0];

            // Fetch contact info from the database
            Map<String, String> contactInfo = dbController.getSubscriberContactByCode(subscriberCode);

            if (contactInfo != null && contactInfo.get("email") != null) {
                ServerResponse response = new ServerResponse(
                    "get_subscriber_contact",
                    true,
                    "Contact info retrieved.",
                    contactInfo
                );
                client.sendToClient(response);
            } else {
                ServerResponse response = new ServerResponse(
                    "get_subscriber_contact",
                    false,
                    "Subscriber not found or missing email.",
                    null
                );
                client.sendToClient(response);
            }

        } catch (Exception e) {
            sendError(client, "Error retrieving contact info: " + e.getMessage(), "get_subscriber_contact");
            e.printStackTrace();
        }
    }



}

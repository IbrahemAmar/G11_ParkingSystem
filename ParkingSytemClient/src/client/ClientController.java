package client;

import entities.*;
import guestGui.PublicAvailabilityController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ocsf.client.AbstractClient;
import subscriberGui.CarDepositController;
import subscriberGui.EditSubscriberDetailsController;
import utils.SceneNavigator;

import java.io.IOException;
import java.util.List;

/**
 * The main client-side controller that manages the connection to the server,
 * handles incoming messages, and provides access to GUI components.
 * 
 * This class extends {@link AbstractClient} and implements the core logic
 * for message handling and GUI coordination in the BPARK system.
 * 
 * @author BPARK
 */
public class ClientController extends AbstractClient {

    private static ClientController clientInstance;
    private MainMenuController guiController;
    private EditSubscriberDetailsController editSubscriberDetailsController;
    private subscriberGui.SubscriberDashboardController subscriberDashboardController;
    private String userRole;
    private Subscriber currentSubscriber;
    private static Stage primaryStage;
    private CarDepositController carDepositController;
    private subscriberGui.ExtendParkingController extendParkingController;


    /**
     * Sets the active singleton instance of the client.
     * 
     * @param client The client controller instance.
     */
    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    /**
     * Gets the current singleton client instance.
     * 
     * @return the active {@link ClientController} instance.
     */
    public static ClientController getClient() {
        return clientInstance;
    }

    /**
     * Constructs a new client controller and opens the connection to the server.
     *
     * @param host         the server host
     * @param port         the server port
     * @param guiController the main menu GUI controller
     * @throws IOException if connection fails
     */
    public ClientController(String host, int port, MainMenuController guiController) throws IOException {
        super(host, port);
        this.guiController = guiController;
        openConnection();
    }

    /**
     * Sets the reference to the main menu GUI controller.
     *
     * @param guiController the main GUI controller
     */
    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    /**
     * Sets the controller responsible for editing subscriber details.
     *
     * @param controller the edit subscriber controller
     */
    public void setEditSubscriberDetailsController(EditSubscriberDetailsController controller) {
        this.editSubscriberDetailsController = controller;
    }

    /**
     * Sets the subscriber dashboard controller.
     *
     * @param controller the dashboard controller
     */
    public void setSubscriberDashboardController(subscriberGui.SubscriberDashboardController controller) {
        this.subscriberDashboardController = controller;
    }
    /**
     * Stores reference to the CarDepositController.
     */
    public void setCarDepositController(CarDepositController controller) {
        this.carDepositController = controller;
    }

    /**
     * Gets the current CarDepositController instance.
     */
    public CarDepositController getCarDepositController() {
        return carDepositController;
    }
    /**
     * Gets the current user role.
     *
     * @return the user role string
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Sets the current user role.
     *
     * @param userRole the user role string
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * Gets the currently logged-in subscriber.
     *
     * @return the current {@link Subscriber}
     */
    public Subscriber getCurrentSubscriber() {
        return currentSubscriber;
    }

    /**
     * Sets the current subscriber.
     *
     * @param subscriber the current {@link Subscriber}
     */
    public void setCurrentSubscriber(Subscriber subscriber) {
        this.currentSubscriber = subscriber;
    }

    /**
     * Sets the primary application stage for GUI transitions.
     *
     * @param stage the JavaFX primary stage
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Gets the primary application stage.
     *
     * @return the JavaFX primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Sends an object to the server via the OCSF framework.
     *
     * @param msg the message object to send
     */
    public void sendObjectToServer(Object msg) {
        try {
            super.sendToServer(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles incoming messages from the server and delegates to appropriate handlers.
     *
     * @param msg the received message
     */
    @Override
    protected void handleMessageFromServer(Object msg) {

        if (msg instanceof UpdateResponse response) {
            handleUpdateResponse(response);

        } else if (msg instanceof ErrorResponse error) {
            handleErrorResponse(error);

        } else if (msg instanceof List<?> list && !list.isEmpty()) {
            handleListResponse(list);

        } else if (msg instanceof Subscriber subscriber) {
            handleSubscriberResponse(subscriber);

        } else if (msg instanceof ParkingSpace spot) {
            handleParkingSpaceResponse(spot);

        } else if (msg instanceof String str) {
            switch (str.toLowerCase()) {
            case "active deposit exists" -> handleActiveDepositExists();
            case "no deposit" -> handleNoActiveDeposit();
            case "✅ parking time extended successfully!" -> handleExtendParkingSuccess(str);
            case "❌ unable to extend parking time." -> handleExtendParkingFailure(str);
            // ... (other cases like "check available", etc.)
            }
        }
        
        else {
            System.out.println("⚠️ Unknown message from server: " + msg);
        }
    }


    /**
     * Parses the user role from a login success message and stores it.
     *
     * @param response the login {@link UpdateResponse}
     */
    private void parseAndSetUserRole(UpdateResponse response) {
        if (response.isSuccess() && response.getMessage() != null) {
            String prefix = "Login successful: ";
            String message = response.getMessage();
            if (message.startsWith(prefix)) {
                String role = message.substring(prefix.length());
                setUserRole(role);
                System.out.println("User role set to: " + role);
                return;
            }
        }
        setUserRole("unknown");
        System.out.println("User role set to unknown");
    }

    /**
     * Handles {@link UpdateResponse} messages.
     *
     * @param response the update response
     */
    private void handleUpdateResponse(UpdateResponse response) {
        String msgText = response.getMessage().toLowerCase();

        if (msgText.contains("login successful")) {
            parseAndSetUserRole(response);
            if (guiController != null) {
                guiController.handleLoginResponse(response);
            }
        } else if (editSubscriberDetailsController != null) {
            editSubscriberDetailsController.onUpdateResponse(response);
        } else {
            System.out.println("UpdateResponse received but no controller instance set");
        }
    }

    /**
     * Handles an error message sent from the server.
     * If the message relates to a deposit failure, the depositRejected flag is set on the CarDepositController.
     *
     * @param error The ErrorResponse object from the server.
     */
    private void handleErrorResponse(ErrorResponse error) {
        Platform.runLater(() -> {
            // If deposit error, suppress success alert
            if ("You already have an active parking reservation.".equals(error.getErrorMessage())) {
                CarDepositController controller = getCarDepositController();
                if (controller != null) {
                    controller.setDepositRejected(true);
                }
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Action Failed");
            alert.setHeaderText(null);
            alert.setContentText("❌ " + error.getErrorMessage());
            alert.showAndWait();
        });
    }


    /**
     * Handles {@link List} type messages from the server.
     *
     * @param list the list object received
     */
    @SuppressWarnings("unchecked")
    private void handleListResponse(List<?> list) {
        Object first = list.get(0);

        if (first instanceof ParkingSpace) {
            Platform.runLater(() -> {
                PublicAvailabilityController.updateTable((List<ParkingSpace>) list);
            });
        } else if (first instanceof ParkingHistory) {
            List<ParkingHistory> historyList = (List<ParkingHistory>) list;
            Platform.runLater(() -> {
                System.out.println("📋 Received parking history: " + historyList.size() + " records");

                if (subscriberDashboardController != null) {
                    subscriberDashboardController.setParkingHistoryData(
                        FXCollections.observableArrayList(historyList)
                    );
                } else {
                    System.out.println("⚠️ subscriberDashboardController is null.");
                }
            });
        } else {
            System.out.println("⚠️ Received unknown List<?> type.");
        }
    }

    /**
     * Handles {@link Subscriber} objects sent from the server.
     *
     * @param subscriber the received subscriber
     */
    private void handleSubscriberResponse(Subscriber subscriber) {
        this.currentSubscriber = subscriber;
        System.out.println("👤 Subscriber received: " + subscriber.getFullName());
    }
    /**
     * Handles a ParkingSpace object sent from the server.
     * If the spot ID is -1, it means no available spot; show a message and go back to dashboard.
     *
     * @param spot the received ParkingSpace
     */
    private void handleParkingSpaceResponse(ParkingSpace spot) {
        Platform.runLater(() -> {
            if (spot.getParkingSpaceId() == -1) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Available Parking");
                alert.setHeaderText(null);
                alert.setContentText("🚫 There are currently no available parking spots. Please try again later.");
                alert.showAndWait();

                SceneNavigator.navigateTo(null,
                    "/subscriberGui/SubscriberDashboard.fxml",
                    "BPARK - Subscriber Dashboard");
            } else {
                CarDepositController controller = this.getCarDepositController();
                if (controller != null) {
                    controller.setSpot(String.valueOf(spot.getParkingSpaceId()));
                } else {
                    System.out.println("⚠️ CarDepositController is not registered.");
                }
            }
        });
    }
    /**
     * Handles the case where the server indicates the subscriber
     * already has an active parking reservation.
     * Displays an alert and stays on the dashboard.
     */
    private void handleActiveDepositExists() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Deposit Blocked");
            alert.setHeaderText(null);
            alert.setContentText("🚫 You already have an active parking reservation.");
            alert.showAndWait();
        });
    }

    /**
     * Handles the case where the subscriber does not have an active deposit.
     * Loads the Car Deposit screen and initializes it.
     */
    private void handleNoActiveDeposit() {
        Platform.runLater(() -> {
            CarDepositController controller = SceneNavigator.navigateToAndGetController(
                null, "/subscriberGui/CarDeposit.fxml", "BPARK - Vehicle Deposit");

            if (controller != null) {
                controller.setClient(this);                // Inject client controller
                controller.onLoaded();                     // Request spot
                this.setCarDepositController(controller);  // Register controller for callbacks
            } else {
                System.out.println("⚠️ Failed to load CarDepositController.");
            }
        });
    }
    
    /**
     * Handles the string indicating the extend parking was successful.
     *
     * @param message the success message
     */
    private void handleExtendParkingSuccess(String message) {
    	 System.out.println("TESTTTTTTTTTTTTTTT");
        if (extendParkingController != null) {
            extendParkingController.onUpdateResponse(message);
        } SceneNavigator.navigateTo(null, "/subscriberGui/ExtendConfirmation.fxml", "Extension Confirmed");

    }


    /**
     * Handles the string indicating the extend parking failed.
     *
     * @param message the failure message
     */
    private void handleExtendParkingFailure(String message) {
        if (extendParkingController != null) {
            extendParkingController.onUpdateResponse(message);
        } else {
            System.out.println("⚠️ No ExtendParkingController registered for failure message.");
        }
    }



}

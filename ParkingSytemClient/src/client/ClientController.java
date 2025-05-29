package client;

import entities.*;
import guestGui.PublicAvailabilityController;
import javafx.application.Platform;
import javafx.stage.Stage;
import ocsf.client.AbstractClient;

import java.io.IOException;
import java.util.List;

/**
 * The ClientController manages the connection between the client GUI and the server.
 * It uses a Singleton pattern to allow global access across GUI controllers.
 */
public class ClientController extends AbstractClient {

    private static ClientController clientInstance;
    private MainMenuController guiController;

    /** The role of the currently logged-in user (e.g., "admin", "subscriber", "supervisor"). */
    private String userRole;

    /** The currently logged-in subscriber object (if role is subscriber). */
    private Subscriber currentSubscriber;

    /** The globally shared JavaFX stage used for transitions (fallback). */
    private static Stage primaryStage;

    /**
     * Returns the singleton instance of the client controller.
     *
     * @return the current ClientController instance.
     */
    public static ClientController getClient() {
        return clientInstance;
    }

    /**
     * Sets the singleton instance of the client controller.
     *
     * @param client the ClientController to set.
     */
    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    /**
     * Constructor that establishes connection to the server.
     *
     * @param host          the server hostname.
     * @param port          the server port.
     * @param guiController the MainMenuController for initial UI communication.
     * @throws IOException if connection fails.
     */
    public ClientController(String host, int port, MainMenuController guiController) throws IOException {
        super(host, port);
        this.guiController = guiController;
        openConnection();
    }

    /**
     * Sets the GUI controller that handles login responses.
     *
     * @param guiController the main menu controller.
     */
    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    /**
     * Returns the currently logged-in role.
     *
     * @return the user role string.
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Sets the user role (used after successful login).
     *
     * @param userRole the role to set.
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * Returns the logged-in subscriber entity.
     *
     * @return the current Subscriber, or null if not a subscriber.
     */
    public Subscriber getCurrentSubscriber() {
        return currentSubscriber;
    }

    /**
     * Sets the current subscriber object.
     *
     * @param subscriber the subscriber to store.
     */
    public void setCurrentSubscriber(Subscriber subscriber) {
        this.currentSubscriber = subscriber;
    }

    /**
     * Sets the global JavaFX stage for fallback use across controllers.
     *
     * @param stage the JavaFX primary stage.
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Gets the global JavaFX stage for fallback use.
     *
     * @return the JavaFX primary stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Handles messages received from the server.
     *
     * @param msg the object received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof LoginResponse response) {
            System.out.println("🔐 Login: " + response.getMessage());

            if (guiController != null) {
                guiController.handleLoginResponse(response);
            }

        } else if (msg instanceof ErrorResponse error) {
            System.out.println("❌ Server error: " + error.getErrorMessage());

        } else if (msg instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ParkingSpace) {
            Platform.runLater(() -> {
                PublicAvailabilityController.updateTable((List<ParkingSpace>) list);
            });

        } else if (msg instanceof Subscriber subscriber) {
            this.currentSubscriber = subscriber;
            System.out.println("👤 Subscriber received: " + subscriber.getFullName());

        } else {
            System.out.println("⚠️ Unknown message from server: " + msg);
        }
    }
}

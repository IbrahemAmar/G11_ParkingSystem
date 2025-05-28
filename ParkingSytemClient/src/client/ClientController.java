package client;

import entities.*;
import guestGui.PublicAvailabilityController;
import javafx.application.Platform;
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

    /**
     * The role of the currently logged-in user (e.g., "admin", "subscriber", "supervisor").
     */
    private String userRole;

    /**
     * Returns the role of the currently logged-in user.
     *
     * @return the user's role, or null if not set.
     */
    public String getUserRole() {
        return userRole;
    }

    /**
     * Sets the user's role after successful login.
     *
     * @param userRole the role of the logged-in user.
     */
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    /**
     * Private constructor with GUI controller, host and port.
     * Automatically opens the connection.
     *
     * @param host         The server host.
     * @param port         The server port.
     * @param guiController Reference to the login/main menu controller.
     * @throws IOException if the connection fails to open.
     */
    public ClientController(String host, int port, MainMenuController guiController) throws IOException {
        super(host, port);
        this.guiController = guiController;
        openConnection();
    }

    /**
     * Singleton setter.
     *
     * @param client The ClientController instance to set.
     */
    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    /**
     * Singleton getter.
     *
     * @return The current ClientController instance.
     */
    public static ClientController getClient() {
        return clientInstance;
    }

    /**
     * Allows assigning/changing the GUI controller at runtime.
     *
     * @param guiController The controller to attach to.
     */
    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    /**
     * Handles messages received from the server and dispatches them to the appropriate GUI.
     *
     * @param msg The message received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof LoginResponse) {
            LoginResponse response = (LoginResponse) msg;
            System.out.println("🔐 Login: " + response.getMessage());

            if (guiController != null) {
                guiController.handleLoginResponse(response);
            }

        } else if (msg instanceof ErrorResponse) {
            ErrorResponse error = (ErrorResponse) msg;
            System.out.println("❌ Server error: " + error.getErrorMessage());

        } else if (msg instanceof List<?>) {
            List<?> list = (List<?>) msg;

            if (!list.isEmpty() && list.get(0) instanceof ParkingSpace) {
                Platform.runLater(() -> {
                    PublicAvailabilityController.updateTable((List<ParkingSpace>) list);
                });
            }

        } else {
            System.out.println("⚠️ Unknown message from server.");
        }
    }
}

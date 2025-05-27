package client;

import entities.*;
import ocsf.client.AbstractClient;
import java.io.IOException;

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
     */
    public ClientController(String host, int port, MainMenuController guiController) throws IOException {
        super(host, port);
        this.guiController = guiController;
        openConnection();
    }

    /**
     * Singleton setter.
     */
    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    /**
     * Singleton getter.
     */
    public static ClientController getClient() {
        return clientInstance;
    }

    /**
     * Allows assigning/changing the GUI controller at runtime (optional).
     */
    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    /**
     * Handles messages received from the server.
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

        } else {
            System.out.println("⚠️ Unknown message from server.");
        }
    }
}

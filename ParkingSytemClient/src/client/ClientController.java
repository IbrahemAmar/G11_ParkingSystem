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

    /** The role of the currently logged-in user (e.g., "admin", "subscriber", "supervisor"). */
    private String userRole;

    /** The currently logged-in subscriber object. */
    private Subscriber currentSubscriber;

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
     * Returns the currently logged-in subscriber object.
     *
     * @return the Subscriber object.
     */
    public Subscriber getCurrentSubscriber() {
        return currentSubscriber;
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
     *
     * @param client The client controller instance.
     */
    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    /**
     * Singleton getter.
     *
     * @return the client controller instance.
     */
    public static ClientController getClient() {
        return clientInstance;
    }

    /**
     * Allows assigning/changing the GUI controller at runtime (optional).
     *
     * @param guiController The main menu controller.
     */
    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    /**
     * Handles messages received from the server.
     *
     * @param msg The message sent by the server.
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
            // Store the subscriber object received from the server
            this.currentSubscriber = subscriber;
            System.out.println("👤 Subscriber received: " + subscriber.getFullName());
         
        }else if (msg instanceof Subscriber sub) {
            System.out.println("✅ Received subscriber from server: " + sub.getFullName()); // check console
            setCurrentSubscriber(sub);
            
        }else {
            System.out.println("⚠️ Unknown message from server: " + msg);
        }
    }

	private void setCurrentSubscriber(Subscriber sub) {
		// TODO Auto-generated method stub
		
	}
}

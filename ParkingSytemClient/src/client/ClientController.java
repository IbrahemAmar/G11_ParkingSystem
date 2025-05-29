package client;

import entities.*;
import guestGui.PublicAvailabilityController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import ocsf.client.AbstractClient;
import subscriberGui.EditSubscriberDetailsController;

import java.io.IOException;
import java.util.List;

public class ClientController extends AbstractClient {

    private static ClientController clientInstance;
    private MainMenuController guiController;

    // Reference to the EditSubscriberDetailsController for update callbacks
    private EditSubscriberDetailsController editSubscriberDetailsController;

    private String userRole;
    private Subscriber currentSubscriber;
    private static Stage primaryStage;
    
    private subscriberGui.SubscriberDashboardController subscriberDashboardController;

    public void setSubscriberDashboardController(subscriberGui.SubscriberDashboardController controller) {
        this.subscriberDashboardController = controller;
    }

 
    public static ClientController getClient() {
        return clientInstance;
    }

    public static void setClient(ClientController client) {
        clientInstance = client;
    }

    public ClientController(String host, int port, MainMenuController guiController) throws IOException {
        super(host, port);
        this.guiController = guiController;
        openConnection();
    }

    public void setGuiController(MainMenuController guiController) {
        this.guiController = guiController;
    }

    public void setEditSubscriberDetailsController(EditSubscriberDetailsController controller) {
        this.editSubscriberDetailsController = controller;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Subscriber getCurrentSubscriber() {
        return currentSubscriber;
    }

    public void setCurrentSubscriber(Subscriber subscriber) {
        this.currentSubscriber = subscriber;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof UpdateResponse response) {
            String msgText = response.getMessage().toLowerCase();

            if (msgText.contains("login successful")) {
                parseAndSetUserRole(response);
                if (guiController != null) {
                    guiController.handleLoginResponse(response);
                }
            } else {
                // Forward update response to the edit subscriber controller if set
                if (editSubscriberDetailsController != null) {
                    editSubscriberDetailsController.onUpdateResponse(response);
                } else {
                    System.out.println("UpdateResponse received but no controller instance set");
                }
            }

        } else if (msg instanceof ErrorResponse error) {
            System.out.println("❌ Server error: " + error.getErrorMessage());

        } else if (msg instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ParkingSpace) {
            Platform.runLater(() -> {
                PublicAvailabilityController.updateTable((List<ParkingSpace>) list);
            });

        } else if (msg instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof ParkingHistory) {
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
        }


        else if (msg instanceof Subscriber subscriber) {
            this.currentSubscriber = subscriber;
            System.out.println("👤 Subscriber received: " + subscriber.getFullName());

        } else {
            System.out.println("⚠️ Unknown message from server: " + msg);
        }
    }

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
     * Sends an object to the server safely via OCSF AbstractClient.
     *
     * @param msg The object to send to the server.
     */
    public void sendObjectToServer(Object msg) {
        try {
            super.sendToServer(msg); // ✅ calls the real method in AbstractClient
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
}

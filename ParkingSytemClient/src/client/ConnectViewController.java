package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the initial connection screen.
 */
public class ConnectViewController {

    @FXML
    private TextField txtServerIp;

    @FXML
    private TextField txtServerPort;

    @FXML
    private Button connectButton;

    @FXML
    private Label statusLabel;
    
    @FXML
    public void initialize() {
        txtServerIp.setText("localhost");
        txtServerPort.setText("5555");
        statusLabel.setText("Enter server details to connect.");
    }


    @FXML
    void handleConnect(ActionEvent event) {
        String ip = txtServerIp.getText().trim();
        String portText = txtServerPort.getText().trim();

        if (ip.isEmpty() || portText.isEmpty()) {
            statusLabel.setText("❌ Please enter server IP and port.");
            return;
        }

        try {
            int port = Integer.parseInt(portText);

            // Load MainMenu scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
            Parent root = loader.load();

            // Get MainMenuController
            MainMenuController mainMenuController = loader.getController();

            // Create and connect client
            ClientController.setClient(new ClientController(ip, port, mainMenuController));
            mainMenuController.setClient(ClientController.getClient());

            // Switch scene
            Stage stage = (Stage) connectButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Main Menu");
            stage.show();

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Invalid port number.");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Could not connect to server.");
        }
    }
}

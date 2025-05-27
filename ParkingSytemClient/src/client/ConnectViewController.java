package client;

import java.net.InetAddress;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectViewController {

    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            this.ipField.setText(localIp);
        } catch (Exception e) {
            this.ipField.setText("localhost");
        }
        this.portField.setText("5555"); 
    }

    @FXML
    private void handleConnect() {
        int port;
        String ip = this.ipField.getText().trim();

        try {
            port = Integer.parseInt(this.portField.getText().trim());
        } catch (NumberFormatException e) {
            this.statusLabel.setText("Invalid port number.");
            return;  
        }

        try { 
            // Load MainMenu.fxml
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/MainMenu.fxml"));
        	Parent root = loader.load();

        	MainMenuController controller = loader.getController();

        	// ✅ pass the controller as ChatIF
        	ClientController client = new ClientController(ip, port, controller);
        	controller.setClient(client);
        	client.setGuiController(controller);

            // Open main menu scene
            Stage stage = (Stage) this.ipField.getScene().getWindow();
            stage.setTitle("Main Menu");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            this.statusLabel.setText("Failed to connect or load menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

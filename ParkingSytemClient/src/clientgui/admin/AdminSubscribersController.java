package clientgui.admin;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AdminSubscribersController {

    private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML private Button btnBack;

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> handleBack());
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientgui/admin/AdminMainMenu.fxml"));
            Parent root = loader.load();

            AdminMainMenuController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

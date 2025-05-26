package subscriberGui;

import adminGui.AdminMainMenuController;
import client.ClientController;
import common.ChatIF;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class MainMenuController implements ChatIF {

	private ClientController client;

	@FXML
	private TextField txtUsername;

	@FXML
	private PasswordField txtPassword;

	@FXML
	private Button btnLogin;

	@FXML
	private Button btnCheckAvailability;

	@FXML
	private Label statusLabel;

	@Override
	public void display(String message) {
		Platform.runLater(() -> statusLabel.setText(message));
	}

	public void setClient(ClientController client) {
		this.client = client;
	}

	@FXML
	private void initialize() {
		btnLogin.setOnAction(e -> handleLogin());
	}

	private void handleLogin() {
		String username = txtUsername.getText().trim();
		String password = txtPassword.getText().trim();

		if (username.isEmpty() || password.isEmpty()) {
			showAlert("Please enter both username and password.");
			return;
		}

		boolean sent = client.sendLoginRequest(username, password);
		if (!sent) {
			showAlert("Login request could not be sent.");
		}
	}

	public void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Login Error");
		alert.setHeaderText(null);
		alert.setContentText(msg);
		alert.showAndWait();
	}

	public void redirectBasedOnRole(String role) {
		try {
			FXMLLoader loader;
			Parent root;

			switch (role) {
			case "admin":
			case "supervisor":
				loader = new FXMLLoader(getClass().getResource("/subscriberGui/admin/AdminMainMenu.fxml"));
				break;
			case "subscriber":
				loader = new FXMLLoader(
						getClass().getResource("/subscriberGui/SubscriberDashboard.fxml"));
				break;
			default:
				showAlert("Unknown role: " + role);
				return;
			}

			root = loader.load();
			Object controller = loader.getController();
			if (controller instanceof AdminMainMenuController) {
				((AdminMainMenuController) controller).setClient(client);
			} else if (controller instanceof SubscriberDashboardController) {
				((SubscriberDashboardController) controller).setClient(client);
			}

			Stage stage = (Stage) txtUsername.getScene().getWindow();
			stage.setScene(new Scene(root));
			stage.setTitle("BPARK - " + role);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
			showAlert("Failed to load " + role + " dashboard.");
		}
	}

}

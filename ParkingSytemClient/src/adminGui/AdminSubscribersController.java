package adminGui;

import java.util.List;
import java.util.stream.Collectors;

import bpark_common.ClientRequest;
import client.ClientController;
import entities.Subscriber;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import utils.SceneNavigator;

/**
 * Controller for AdminSubscriberManagement.fxml.
 * Manages subscriber list display, addition, search, and navigation.
 */
public class AdminSubscribersController {

	private ClientController client;

	@FXML private TableView<Subscriber> subscriberTable;
	@FXML private TableColumn<Subscriber, String> colSubCode;
	@FXML private TableColumn<Subscriber, Integer> colId;
	@FXML private TableColumn<Subscriber, String> colUsername;
	@FXML private TableColumn<Subscriber, String> colFullName;
	@FXML private TableColumn<Subscriber, String> colEmail;
	@FXML private TableColumn<Subscriber, String> colPhone;

	@FXML private TextField txtSearchId;
	@FXML private TextField txtID;
	@FXML private PasswordField txtPassword;
	@FXML private TextField txtFirstName;
	@FXML private TextField txtLastName;
	@FXML private TextField txtEmail;
	@FXML private TextField txtPhone;

	@FXML private Button btnSearch;
	@FXML private Button btnRefresh;
	@FXML private Button btnParkingHistory;
	@FXML private Button btnAddSubscriber;

	@FXML private Label lblStatus;

	private ObservableList<Subscriber> allSubscribers = FXCollections.observableArrayList();

	/**
	 * Sets the client controller and requests all subscribers.
	 *
	 * @param client the client controller used for communication
	 */
	public void setClient(ClientController client) {
		this.client = client;
		client.setAdminSubscribersController(this);
		loadAllSubscribers();
	}

	/**
	 * Initializes table columns and button actions.
	 * Called automatically after FXML is loaded.
	 */
	@FXML
	public void initialize() {
		colSubCode.setCellValueFactory(new PropertyValueFactory<>("subscriberCode"));
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));
		colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
		colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

		btnSearch.setOnAction(e -> handleSearch());
		btnRefresh.setOnAction(e -> loadAllSubscribers());
		btnAddSubscriber.setOnAction(e -> handleAddSubscriber());
	}

	/**
	 * Sends a request to the server to load all subscribers.
	 */
	private void loadAllSubscribers() {
		allSubscribers.clear();
		subscriberTable.getItems().clear();
		lblStatus.setText("Loading subscribers...");

		ClientRequest request = new ClientRequest("get_subscribers_all_active", new Object[0]);
		ClientController.getClient().sendObjectToServer(request);
	}

	/**
	 * Sets the observable list of all subscribers received from the server.
	 *
	 * @param subscribers the list of subscribers to display
	 */
	@FXML
	public void setAllSubscribers(List<Subscriber> subscribers) {
		javafx.application.Platform.runLater(() -> {
			allSubscribers.setAll(subscribers);
			subscriberTable.setItems(allSubscribers);
			lblStatus.setText(subscribers.size() + " subscribers loaded.");
		});
	}

	/**
	 * Filters the subscriber table based on ID entered in the search field.
	 */
	private void handleSearch() {
		String idText = txtSearchId.getText().trim();

		List<Subscriber> filtered = allSubscribers.stream()
				.filter(s -> (idText.isEmpty() || String.valueOf(s.getId()).equals(idText)))
				.collect(Collectors.toList());

		subscriberTable.setItems(FXCollections.observableArrayList(filtered));
		lblStatus.setText("🔍 Showing " + filtered.size() + " filtered result(s).");
	}

	/**
	 * Handles the Parking History button click.
	 * Navigates to the history screen and sends a request for the selected subscriber's data.
	 *
	 * @param event the action event
	 */
	@FXML
	private void handleParkingHistory(ActionEvent event) {
		Subscriber selected = subscriberTable.getSelectionModel().getSelectedItem();
		if (selected == null) {
			lblStatus.setText("⚠️ Please select a subscriber to view history.");
			return;
		}

		AdminParkingHistoryController controller = SceneNavigator.navigateToAndGetController(
			event, "/adminGui/AdminParkingHistory.fxml", "Parking History for " + selected.getFullName()
		);
		if (controller != null) {
			controller.setClient(client);
			controller.setSelectedSubscriber(selected);
			controller.refreshParkingHistory();
		}
	}

	/**
	 * Handles the Add Subscriber button click.
	 * Validates input, builds the Subscriber object, and sends it to the server.
	 * Also resets the form fields after submission.
	 */
	private void handleAddSubscriber() {
		String idText = txtID.getText().trim();
		String username = "";
		String password = txtPassword.getText().trim();
		String firstName = txtFirstName.getText().trim();
		String lastName = txtLastName.getText().trim();
		String email = txtEmail.getText().trim();
		String phoneTrim = txtPhone.getText().trim();

		if (idText.isEmpty() || password.isEmpty() ||
			firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneTrim.isEmpty()) {
			lblStatus.setText("⚠️ Please fill in all fields.");
			return;
		}

		// Validate ID
		int id;
		try {
			id = Integer.parseInt(idText);
		} catch (NumberFormatException e) {
			lblStatus.setText("⚠️ ID must be a number.");
			return;
		}

		// Validate phone
		int phoneNum;
		try {
			phoneNum = Integer.parseInt(phoneTrim);
		} catch (NumberFormatException e) {
			lblStatus.setText("⚠️ Phone must be a number.");
			return;
		}

		// Validate first name
		for (int i = 0; i <= 9; i++) {
			if (firstName.contains(String.valueOf(i))) {
				lblStatus.setText("⚠️ FirstName must be a string.");
				return;
			}
		}

		// Validate last name
		for (int i = 0; i <= 9; i++) {
			if (lastName.contains(String.valueOf(i))) {
				lblStatus.setText("⚠️ LastName must be a string.");
				return;
			}
		}

		// Validate email format
		boolean emailForm = false;
		int atIndex = email.indexOf('@');
		if (atIndex > 0 && atIndex < email.length() - 1) {
			if (email.lastIndexOf('@') == atIndex) {
				int dotIndex = email.indexOf('.', atIndex + 1);
				if (dotIndex > atIndex + 1 && dotIndex < email.length() - 1) {
					emailForm = true;
				}
			}
		}
		if (!emailForm) {
			lblStatus.setText("⚠️ Email must be in the form of 'example@example.example'");
			return;
		}

		// Format phone
		int len = phoneTrim.length();
		String phone;
		if (len <= 3) {
			phone = phoneTrim;
		} else {
			int prefixLen = len - 4;
			StringBuilder phoneFormatted = new StringBuilder();
			for (int i = 0; i < prefixLen; i++) {
				phoneFormatted.append(phoneTrim.charAt(i));
				if ((i + 1) % 3 == 0 && (i + 1) < prefixLen) {
					phoneFormatted.append("-");
				}
			}
			if (prefixLen > 0) phoneFormatted.append("-");
			phoneFormatted.append(phoneTrim.substring(len - 4));
			phone = phoneFormatted.toString();
		}

		Subscriber newSubscriber = new Subscriber(id, firstName + " " + lastName, username, email, phone, null);
		Object[] params = { newSubscriber, password, firstName, lastName };
		ClientRequest request = new ClientRequest("add_subscriber", params);
		ClientController.getClient().sendObjectToServer(request);

		lblStatus.setText("⏳ Adding subscriber...");

		txtPassword.clear();
		txtFirstName.clear();
		txtLastName.clear();
		txtEmail.clear();
		txtPhone.clear();
		txtID.clear();
	}

	/**
	 * Handles the Back button click.
	 * Navigates to the Admin Dashboard screen.
	 *
	 * @param event the action event triggered by the button click
	 */
	@FXML
	private void handleBack(ActionEvent event) {
		AdminMainMenuController controller = SceneNavigator.navigateToAndGetController(
			event, "/adminGui/AdminMainMenu.fxml", "Admin Dashboard"
		);
		if (controller != null) controller.setClient(client);
	}
}

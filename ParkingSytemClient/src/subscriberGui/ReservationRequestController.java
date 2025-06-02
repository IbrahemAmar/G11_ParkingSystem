package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import bpark_common.ClientRequest;
import client.ClientController;


/**
 * Controller for ReservationRequest.fxml.
 * Handles the reservation request process and navigates back to the dashboard.
 */
public class ReservationRequestController {
	
	@FXML
	private DatePicker datePicker;

	@FXML
	private ComboBox<LocalTime> timeCombo;

	@FXML
	private Label lblResult;


    /**
     * Handles the "Back" button action to return to the Subscriber Dashboard.
     * Reloads the dashboard FXML and refreshes its data.
     *
     * @param event The ActionEvent triggered by the user's interaction.
     */
    @FXML
    private void handleBackButton(ActionEvent event) {
    	SceneNavigator.navigateTo(event, "/subscriberGui/SubscriberDashboard.fxml", "BPARK - Subscriber Dashboard");
    }

    /**
     * Initializes the date picker and sets up valid date and time ranges.
     */
    @FXML
    public void initialize() {
    	System.out.println("✅ ReservationRequestController initialized!");
    	
    	// Register this controller in the ClientController
        ClientController.getClient().reservationRequestController = this;
        
        // Restrict date picker to 24h from now up to 7 days
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                LocalDate minDate = LocalDate.now().plusDays(1);
                LocalDate maxDate = LocalDate.now().plusDays(7);

                setDisable(empty || date.isBefore(minDate) || date.isAfter(maxDate));
            }
        });

        // Populate time combo box when date is selected
        datePicker.setOnAction(event -> onDatePicked());
    }

    /**
     * Populates the time combo box with valid start times for the selected date.
     * - Previously added times locally, but now just sends request to server and disables the combo box until data arrives.
     */
    private void updateAvailableTimes() {
        timeCombo.getItems().clear(); // Clear old data
        timeCombo.setDisable(true);   // Disable until server data arrives

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;

        // Show loading state
        lblResult.setText("⏳ Loading available times...");
        lblResult.setStyle("-fx-text-fill: blue;");

        // Let server handle real validation
        ClientRequest request = new ClientRequest("get_valid_start_times", new Object[]{selectedDate});
        ClientController.getClient().sendObjectToServer(request);
    }



    /**
     * Called when the user picks a date.
     * Sends a request to the server to get valid start times for the selected date.
     */
    @FXML
    private void onDatePicked() {
        System.out.println("✅ onDatePicked called!");

    	
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;
        
        // Immediately update available times locally
        updateAvailableTimes();
        
        // Use the existing ClientRequest class
        ClientRequest request = new ClientRequest("get_valid_start_times", new Object[]{selectedDate});

        // Send the request to the server
        ClientController.getClient().sendObjectToServer(request);
    }


    /**
     * Called by ClientController when the server sends available times.
     */
    public void updateTimeComboBox(List<LocalTime> availableTimes) {
        timeCombo.setDisable(false); // Enable the combo box

        timeCombo.getItems().clear();
        timeCombo.getItems().addAll(availableTimes);

        if (availableTimes.isEmpty()) {
            lblResult.setText("❌ No available time slots. Please choose another date.");
            lblResult.setStyle("-fx-text-fill: red;");
        } else {
            lblResult.setText("✅ Available slots loaded!");
            lblResult.setStyle("-fx-text-fill: green;");
        }
    }




}

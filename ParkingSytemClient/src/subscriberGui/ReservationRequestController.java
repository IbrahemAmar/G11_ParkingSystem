package subscriberGui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import utils.SceneNavigator;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


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
        datePicker.setOnAction(event -> updateAvailableTimes());
    }

    /**
     * Populates the time combo box with valid start times for the selected date.
     * - If the selected date is tomorrow but no valid start times exist (due to 24-hour rule),
     *   automatically switches to the next day and shows all slots.
     * - For other days, shows all 30-minute slots.
     * - Prevents infinite loops and ensures user can always see valid times.
     */
    private void updateAvailableTimes() {
        timeCombo.getItems().clear();

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;

        LocalDateTime nowPlus24h = LocalDateTime.now().plusHours(24);
        LocalTime endOfDay = LocalTime.of(23, 30);

        if (selectedDate.equals(LocalDate.now().plusDays(1))) {
            // For tomorrow, only allow times after 24 hours from now
            LocalTime start = nowPlus24h.toLocalTime();

            if (start.isAfter(endOfDay)) {
                // ✅ No valid times left for tomorrow, move to the next day
                selectedDate = selectedDate.plusDays(1);
                datePicker.setValue(selectedDate); // update the date picker
                start = LocalTime.of(0, 0); // start fresh at midnight
            }

            while (!start.isAfter(endOfDay)) {
                timeCombo.getItems().add(start);
                start = start.plusMinutes(30);
            }
        } else {
            // For other days, show all 30-minute slots
            LocalTime start = LocalTime.of(0, 0);

            while (!start.isAfter(endOfDay)) {
                timeCombo.getItems().add(start);
                start = start.plusMinutes(30);
            }
        }
    }






}

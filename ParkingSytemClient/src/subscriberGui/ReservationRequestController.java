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
     * - For the 7th day (today + 7), shows times from 00:00 until the closest past 30-min slot of the current time.
     * - For all other days, only shows slots at least 24 hours in the future.
     * - No recursion, no crashes.
     */
    private void updateAvailableTimes() {
        timeCombo.getItems().clear();

        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) return;

        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusDays(7);

        LocalDateTime minAllowedDateTime = LocalDateTime.now().plusHours(24);

        for (int hour = 0; hour < 24; hour++) {
            for (int min = 0; min < 60; min += 30) {
                LocalTime slotTime = LocalTime.of(hour, min);
                LocalDateTime slotDateTime = selectedDate.atTime(slotTime);

                // 🔥 If it's the 7th day, only show times before or equal to current time (rounded down to 30-min)
                if (selectedDate.equals(maxAllowedDate)) {
                    LocalTime now = LocalTime.now();
                    int roundedMin = (now.getMinute() >= 30) ? 30 : 0;
                    LocalTime roundedCurrentTime = LocalTime.of(now.getHour(), roundedMin);

                    // Skip any slot after the closest 30-min phase
                    if (slotTime.isAfter(roundedCurrentTime)) {
                        continue;
                    }
                } else {
                    // 24-hour from now rule for other days
                    if (slotDateTime.isBefore(minAllowedDateTime)) {
                        continue;
                    }
                }

                timeCombo.getItems().add(slotTime);
            }
        }

        if (timeCombo.getItems().isEmpty()) {
            lblResult.setText("❌ No valid start times for this date. Please choose another date.");
            lblResult.setStyle("-fx-text-fill: red;");
        } else {
            lblResult.setText("");
        }
    }




 


}

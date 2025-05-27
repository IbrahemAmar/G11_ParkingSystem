package guestGui;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PublicAvailabilityController {

    @FXML
    private TableView<?> tableAvailability;

    @FXML
    private TableColumn<?, ?> colSpotNumber;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    private TableColumn<?, ?> colLevel;

    @FXML
    private TableColumn<?, ?> colLastUpdated;

    @FXML
    private Label lblAvailable;

    @FXML
    private Button btnBack;

    @FXML
    public void initialize() {
        lblAvailable.setText("✔ 12 spots available");
        // You can set up columns and data here later
    }
}

package adminGui;



import client.ClientController;
import entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminSubscribersController {

	private ClientController client;

    @FXML private TableView<User> subscriberTable;
    @FXML private TableColumn<User, String> colSubCode;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFirstName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;

    @FXML private TextField txtSearchId;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;

    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnAddSubscriber;
    @FXML private Button btnBack;
    
    @FXML private Label lblStatus;

    private ObservableList<User> allSubscribers = FXCollections.observableArrayList();

    public void setClient(ClientController client) {
        this.client = client;
    }

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> handleBack());
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/adminGui/AdminMainMenu.fxml"));
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

package serverGui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher for the Server GUI.
 */
public class ServerGUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // ✅ Tell FXMLLoader where to find the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ServerMain.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Server GUI");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

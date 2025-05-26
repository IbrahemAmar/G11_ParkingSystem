module ParkingSytemC {
	requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    opens subscriberGui to javafx.fxml;
    opens client to javafx.graphics; // 👈 This is required to let JavaFX instantiate ClientMain
    opens adminGui to javafx.fxml; // ✅ this line is required now

    exports client;
    exports subscriberGui;
    exports common;
}

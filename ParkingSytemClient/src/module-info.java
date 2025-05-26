module ParkingSytemC {
	requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    opens clientgui to javafx.fxml;
    opens client to javafx.graphics; // 👈 This is required to let JavaFX instantiate ClientMain
    opens clientgui.admin to javafx.fxml; // ✅ this line is required now

    exports client;
    exports clientgui;
    exports common;
}

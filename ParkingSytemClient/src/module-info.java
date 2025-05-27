module ParkingSytemC {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
	requires java.desktop;
	requires javafx.base;

    // Required so JavaFX can inject @FXML fields in these packages
    opens client to javafx.fxml;
    opens subscriberGui to javafx.fxml;
    opens adminGui to javafx.fxml;
    opens guestGui to javafx.fxml;


    // Exported packages for use elsewhere
    exports client;
    exports subscriberGui;
    exports adminGui;
    exports common;
}

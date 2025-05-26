module ParkingSystem {
    requires java.sql;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.base;

    exports servergui;
    exports common;


    opens servergui to javafx.fxml;
}

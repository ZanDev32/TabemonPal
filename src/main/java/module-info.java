module com.starlight {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;

    opens com.starlight to javafx.fxml;
    opens com.starlight.controller to javafx.fxml;
    
    exports com.starlight.controller;
    exports com.starlight;
}
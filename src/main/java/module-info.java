module com.starlight {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;
    requires MaterialFX;

    opens com.starlight to javafx.fxml;
    opens com.starlight.controller to javafx.fxml;
    
    exports com.starlight;
    exports com.starlight.controller;
    exports com.starlight.util;
}
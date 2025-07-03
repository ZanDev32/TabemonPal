/**
 * Java module declaration for the TabemonPal application.
 */
module com.starlight {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;
    requires transitive MaterialFX;
    requires xstream;
    requires jdk.httpserver;

    opens com.starlight to javafx.fxml;
    opens com.starlight.controller to javafx.fxml;
    opens com.starlight.models;
    opens com.starlight.util;
    
    exports com.starlight;
    exports com.starlight.controller;
    exports com.starlight.util;
    exports com.starlight.api;
    exports com.starlight.models;
}
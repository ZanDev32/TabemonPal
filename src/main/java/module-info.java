module com.starlight {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.logging;
    requires MaterialFX;
    requires xstream;
    requires jdk.httpserver;

    opens com.starlight to javafx.fxml;
    opens com.starlight.controller to javafx.fxml;
    opens com.starlight.models to xstream;
    
    exports com.starlight;
    exports com.starlight.controller;
    exports com.starlight.util;
    exports com.starlight.api;
    exports com.starlight.models;
}
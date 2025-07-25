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
    requires java.net.http;

    opens com.starlight to javafx.fxml;
    opens com.starlight.controller;
    opens com.starlight.model;
    opens com.starlight.util;
    opens com.starlight.api;
    
    exports com.starlight;
    exports com.starlight.controller;
    exports com.starlight.util;
    exports com.starlight.api;
    exports com.starlight.model;
}
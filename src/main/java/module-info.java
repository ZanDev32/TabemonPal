module com.starlight {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    opens com.starlight to javafx.fxml;
    
    exports com.starlight;
}
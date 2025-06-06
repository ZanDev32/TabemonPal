module com.starlight {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    opens com.starlight to javafx.fxml;
    
    exports com.starlight;
}

module com.example.image {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.image to javafx.fxml;
    exports com.example.image;
}
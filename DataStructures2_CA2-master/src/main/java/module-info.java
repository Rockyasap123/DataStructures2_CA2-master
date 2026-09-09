module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jmh.core;

    opens org.example to javafx.fxml;
    exports org.example;
    exports org.example.jmh_generated;
    opens org.example.jmh_generated to jmh.core;
}
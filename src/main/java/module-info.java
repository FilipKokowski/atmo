module org.atmo.atmo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.net.http;
    requires org.json;
    requires org.testng;

    opens org.atmo.atmo to javafx.fxml;
    exports org.atmo.atmo;
}
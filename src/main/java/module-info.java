module net.ryanland.dfschematics {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.slf4j;
    requires org.java_websocket;
    requires schematic4j;

    opens net.ryanland.dfschematics to javafx.fxml;
    exports net.ryanland.dfschematics;
}
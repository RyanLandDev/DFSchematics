module net.ryanland.dfschematics {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires org.slf4j;
    requires org.java_websocket;
    requires schematic4j;
    requires net.kyori.adventure;
    requires net.kyori.adventure.text.serializer.gson;
    requires net.kyori.adventure.text.serializer.legacy;

    opens net.ryanland.dfschematics to javafx.fxml;
    exports net.ryanland.dfschematics;
}
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
    requires org.jetbrains.annotations;
    requires java.logging;
    requires jo.nbt;
    requires static lombok;

    opens net.ryanland.dfschematics to javafx.fxml;
    exports net.ryanland.dfschematics;
    exports net.ryanland.dfschematics.fxml;
    opens net.ryanland.dfschematics.fxml to javafx.fxml;
}
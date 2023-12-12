package net.ryanland.dfschematics;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class DFSchematics extends Application {

    public static Stage stage;
    public static HostServices hostServices;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DFSchematics.class.getResource("scene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("DFSchematics");
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(DFSchematics.class.getResource("logo.png"))));
        stage.show();

        DFSchematics.stage = stage;
        DFSchematics.hostServices = getHostServices();
        ItemAPIManager.attemptConnections();
    }

    public static void main(String[] args) {
        launch();
    }


}
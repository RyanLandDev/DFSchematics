package net.ryanland.dfschematics;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.ryanland.dfschematics.df.api.API;

import java.io.IOException;
import java.util.Properties;

public class DFSchematics extends Application {

    public static Stage stage;
    public static HostServices hostServices;
    public static String version;
    public static String builderTemplate;

    @Override
    public void start(Stage stage) throws IOException {
        fetchProperties();

        FXMLLoader fxmlLoader = new FXMLLoader(DFSchematics.class.getResource("fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        //setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        stage.setTitle("DFSchematics");
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(DFSchematics.class.getResource("logo.png"))));
        stage.setResizable(false);
        stage.show();

        DFSchematics.stage = stage;
        DFSchematics.hostServices = getHostServices();
        API.attemptConnections();
    }

    private void fetchProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        version = properties.getProperty("version");
        builderTemplate = properties.getProperty("template");
    }

    public static void main(String[] args) {
        launch();
    }
}
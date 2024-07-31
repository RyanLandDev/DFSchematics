package net.ryanland.dfschematics.fxml;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import net.ryanland.dfschematics.DFSchematics;
import net.ryanland.dfschematics.df.api.API;
import net.ryanland.dfschematics.df.api.CodeClientAPI;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.schematic.DFSchematic;
import net.sandrohc.schematic4j.SchematicLoader;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainController implements Initializable {

    public static MainController instance;

    @FXML BorderPane root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        versionLabel.setText("v" + DFSchematics.version);
        Executors.newScheduledThreadPool(0).schedule(UpdateCheckController::checkForUpdates, 500, TimeUnit.MILLISECONDS);
    }

    public void disableRoot(boolean disable) {
        root.setDisable(disable);
    }

    // CONNECTIONS ----------------

    @FXML Label recodeStatus;
    @FXML Label codeClientStatus;
    @FXML public Button retryButton;

    @FXML
    void retryConnections() {
        API.attemptConnections();
    }

    public void setRecodeStatus(String text, boolean success) {
        recodeStatus.setTextFill(success ? Color.GREEN : Color.RED);
        recodeStatus.setText(text);
    }

    public void setCodeClientStatus(String text, boolean success) {
        codeClientStatus.setTextFill(success ? Color.GREEN : Color.RED);
        codeClientStatus.setText(text);
    }

    // PICK SCHEMATIC ----------------

    @FXML private Button filePicker;
    @FXML private Label fileStatus;

    public static File selectedFile;
    public static DFSchematic schematic;

    @FXML
    void pickFile() {
        // create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Schematic...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        // extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Schematic files (*.schem *.litematic *.schematic)", "*.schem", "*.litematic", "*.schematic");
        fileChooser.getExtensionFilters().add(extFilter);

        // choose file
        File file = fileChooser.showOpenDialog(DFSchematics.stage);
        if (file == null) {
            System.out.println("No file selected");
            return;
        }

        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.LIGHTBLUE);
        fileStatus.setText("Reading...");

        // file selected
        System.out.println("Selected file: " + file.getAbsolutePath());
        filePicker.setText(file.getName());
        selectedFile = file;
        String format = file.getName().replaceAll("^.+\\.", "");

        // read schematic
        Platform.runLater(() -> {
            try {
                schematic = new DFSchematic(SchematicLoader.load(file));
            } catch (Exception e) {
                error("Error: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            API.attemptConnections();
            configureButton.setDisable(false);

            System.out.println(schematic.getSchematic().format());
            success("Successfully loaded (Size: %sx%sx%s)"
                .formatted(schematic.getSchematic().width(), schematic.getSchematic().height(), schematic.getSchematic().length()));
            System.out.println("Loaded Schematic: " + file.getName());
        });
    }

    public void error(String msg) {
        Platform.runLater(() -> {
            fileStatus.setVisible(true);
            fileStatus.setTextFill(Color.RED);
            fileStatus.setText(msg);
        });
    }

    public void success(String msg) {
        Platform.runLater(() -> {
            fileStatus.setVisible(true);
            fileStatus.setTextFill(Color.GREEN);
            fileStatus.setText(msg);
        });
    }

    // CONFIGURE SCHEMATIC --------------

    @FXML private Button configureButton;

    @FXML @SneakyThrows
    void configure(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("configuration.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Schematic Configuration");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(DFSchematics.class.getResource("logo.png"))));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) event.getSource()).getScene().getWindow());
        stage.setOnCloseRequest(evt -> this.root.setDisable(false));
        this.root.setDisable(true);
        stage.showAndWait();
    }

    // SEND ------------------

    @FXML public Button sendTemplates;
    @FXML public Button placeTemplates;
    @FXML public Button sendBuilder;

    @FXML
    void sendTemplates() {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate();
        API.sendTemplates(codeLines, selectedFile.getName());
        success(codeLines.size() + " template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    @FXML
    void sendBuilderTemplate() {
        API.sendRawTemplates(List.of(DFSchematics.builderTemplate), "DFSchematics Builder");
        success("Template sent");
    }

    @FXML
    void placeTemplates() {// CodeClient only
        CodeClientAPI.auth();
    }

    @FXML
    void viewInstructions() {
        DFSchematics.hostServices.showDocument("http://github.com/RyanLandDev/DFSchematics#how-to-use");
    }

    // VERSION -------------------

    @FXML private Label versionLabel;

    @FXML
    void onVersionClick() {
        DFSchematics.hostServices.showDocument("http://github.com/RyanLandDev/DFSchematics");
    }

    @FXML
    void onVersionEnter() {
        versionLabel.setUnderline(true);
    }

    @FXML
    void onVersionExit() {
        versionLabel.setUnderline(false);
    }
}
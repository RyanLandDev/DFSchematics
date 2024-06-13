package net.ryanland.dfschematics;

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
import net.ryanland.dfschematics.df.ItemAPIManager;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.schematic.DFSchematic;
import net.sandrohc.schematic4j.SchematicLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public static BorderPane root;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button sendRecode;
    @FXML
    private Button sendCodeClient;
    @FXML
    private Button sendBuilderRecode;
    @FXML
    private Button sendBuilderCodeClient;
    @FXML
    private Label recodeStatus;
    @FXML
    private Label codeClientStatus;
    @FXML
    private Button retryButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        versionLabel.setText("v" + properties.getProperty("version"));

        root = borderPane;

        ItemAPIManager.recodeStatus = recodeStatus;
        ItemAPIManager.sendRecode = sendRecode;
        ItemAPIManager.sendBuilderRecode = sendBuilderRecode;
        ItemAPIManager.codeClientStatus = codeClientStatus;
        ItemAPIManager.sendCodeClient = sendCodeClient;
        ItemAPIManager.sendBuilderCodeClient = sendBuilderCodeClient;
        ItemAPIManager.retryButton = retryButton;
    }

    @FXML
    void retryConnections() {
        ItemAPIManager.attemptConnections();
    }

    @FXML
    private Button filePicker;
    @FXML
    private Label fileStatus;

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
        String format = file.getName().replaceAll("^.*\\.", "");

        // read schematic
        try {
            schematic = new DFSchematic(SchematicLoader.load(file));
        } catch (Exception e) {
            error("Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        ItemAPIManager.attemptConnections();
        if (ItemAPIManager.recodeConnected) sendRecode.setDisable(false);
        if (ItemAPIManager.codeClientConnected && ItemAPIManager.codeClientAuthorized) sendCodeClient.setDisable(false);
        configureButton.setDisable(false);
        System.out.println(schematic.getSchematic().format());
        success("Successfully loaded (Size: %sx%sx%s)"
            .formatted(schematic.getSchematic().width(), schematic.getSchematic().height(), schematic.getSchematic().length()));
        System.out.println("Loaded Schematic: " + file.getName());
    }

    private void error(String msg) {
        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.RED);
        fileStatus.setText(msg);
    }

    private void success(String msg) {
        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.GREEN);
        fileStatus.setText(msg);
    }

    @FXML
    private Button configureButton;

    @FXML
    void configure(ActionEvent event) {
        try {
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
            stage.setOnCloseRequest(evt -> borderPane.setDisable(false));
            borderPane.setDisable(true);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sendTemplateToRecode() throws InterruptedException {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(true);
        ItemAPIManager.sendTemplatesToRecode(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    @FXML
    void sendTemplateToCodeClient() {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(true);
        ItemAPIManager.sendTemplatesToCodeClient(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    private static final String BUILDER_TEMPLATE = "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";

    @FXML
    void sendBuilderTemplateToRecode() throws InterruptedException {
        ItemAPIManager.sendRawTemplatesToRecode(List.of(BUILDER_TEMPLATE), "DFSchematics Builder");
        success("Template sent");
    }

    @FXML
    void sendBuilderTemplateToCodeClient() {
        ItemAPIManager.sendRawTemplatesToCodeClient(List.of(BUILDER_TEMPLATE), "DFSchematics Builder");
        success("Template sent");
    }

    @FXML
    void viewInstructions() {
        DFSchematics.hostServices.showDocument("http://github.com/RyanLandDev/DFSchematics#how-to-use");
    }

    @FXML
    private Label versionLabel;

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
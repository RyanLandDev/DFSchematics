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
import net.sandrohc.schematic4j.exception.ParsingException;

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
    void pickFile() throws IOException {
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
        } catch (ParsingException e) {
            error("Error: " + e.getMessage());
            return;
        }

        ItemAPIManager.attemptConnections();
        if (ItemAPIManager.recodeConnected) sendRecode.setDisable(false);
        if (ItemAPIManager.codeClientConnected && ItemAPIManager.codeClientAuthorized) sendCodeClient.setDisable(false);
        configureButton.setDisable(false);
        success("Successfully loaded");
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
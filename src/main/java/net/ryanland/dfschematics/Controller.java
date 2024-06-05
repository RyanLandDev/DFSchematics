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
            return;
        }

        ItemAPIManager.attemptConnections();
        if (ItemAPIManager.recodeConnected) sendRecode.setDisable(false);
        if (ItemAPIManager.codeClientConnected && ItemAPIManager.codeClientAuthorized) sendCodeClient.setDisable(false);
        configureButton.setDisable(false);
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
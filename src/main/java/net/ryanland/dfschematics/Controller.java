package net.ryanland.dfschematics;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
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
    void sendTemplateToRecode() throws InterruptedException {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(selectedFile.getName().replaceAll("\\.schem$|\\.litematic$|\\.schematic$|", ""), true);
        ItemAPIManager.sendTemplatesToRecode(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    @FXML
    void sendTemplateToCodeClient() {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(selectedFile.getName().replaceAll("\\.schem$|\\.litematic$|\\.schematic$|", ""), true);
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
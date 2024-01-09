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
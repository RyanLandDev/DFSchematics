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
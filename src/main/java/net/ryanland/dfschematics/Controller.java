package net.ryanland.dfschematics;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.schematic.DFSchematic;
import net.ryanland.dfschematics.schematic.TemplateFactory;
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
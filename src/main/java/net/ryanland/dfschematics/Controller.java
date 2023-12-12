package net.ryanland.dfschematics;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.schematic.DFSchematic;
import net.sandrohc.schematic4j.SchematicUtil;
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
    void retryConnections(ActionEvent event) {
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
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Sponge schematic files (*.schem)", "*.schem");
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
            schematic = new DFSchematic(SchematicUtil.load(file));
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
    void sendTemplateToRecode() throws IOException, InterruptedException {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(selectedFile.getName(), true);
        ItemAPIManager.sendTemplatesToRecode(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    @FXML
    void sendTemplateToCodeClient() {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(selectedFile.getName(), true);
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
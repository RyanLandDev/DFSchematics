package net.ryanland.dfschematics;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateCheckController implements Initializable {

    public static UpdateChecker checker = new UpdateChecker("RyanLandDev", "DFSchematics", DFSchematics.version);

    @FXML
    private Label desc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        desc.setText("There is a new version available (v%s).\nYou are currently using v%s."
            .formatted(checker.getLatestVersion(), checker.getCurrentVersion()));
    }

    @FXML
    void viewRelease() {
        DFSchematics.hostServices.showDocument("https://github.com/RyanLandDev/DFSchematics/releases");
    }
}

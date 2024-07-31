package net.ryanland.dfschematics.fxml;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import net.ryanland.dfschematics.DFSchematics;
import net.ryanland.dfschematics.UpdateChecker;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateCheckController implements Initializable {

    public static UpdateChecker checker = new UpdateChecker("RyanLandDev", "DFSchematics", DFSchematics.version);

    @FXML private Label desc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        desc.setText("There is a new version available (v%s).\nYou are currently using v%s."
            .formatted(checker.getLatestVersion(), checker.getCurrentVersion()));
    }

    @FXML
    void viewRelease() {
        DFSchematics.hostServices.showDocument("https://github.com/RyanLandDev/DFSchematics/releases");
    }

    public static void checkForUpdates() {
        Platform.runLater(() -> {
            checker.check();
            if (checker.isUpdateAvailable()) {
                showUpdateAvailable();
            }
        });
    }

    @SneakyThrows
    private static void showUpdateAvailable() {
        FXMLLoader loader = new FXMLLoader(UpdateCheckController.class.getResource("update.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Update Available");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.getIcons().add(new Image(String.valueOf(DFSchematics.class.getResource("logo.png"))));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(DFSchematics.stage.getScene().getWindow());
        stage.setOnCloseRequest(evt -> MainController.instance.disableRoot(false));
        MainController.instance.disableRoot(true);
        stage.showAndWait();
    }
}

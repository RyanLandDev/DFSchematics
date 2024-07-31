package net.ryanland.dfschematics.df.api;

import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.fxml.MainController;

import java.util.List;

public class API {

    public static void attemptConnections() {
        RecodeItemAPI.attemptConnection();
        CodeClientAPI.attemptConnection();
        if (RecodeItemAPI.connected && CodeClientAPI.connected) {
            MainController.instance.retryButton.setDisable(true);
        }
        if (RecodeItemAPI.connected || CodeClientAPI.connected) {
            if (MainController.selectedFile != null) MainController.instance.sendTemplates.setDisable(false);
            MainController.instance.sendBuilder.setDisable(false);
        }
        if (CodeClientAPI.connected && MainController.selectedFile != null) {
            MainController.instance.placeTemplates.setDisable(false);
        }
    }

    public static void sendTemplates(List<CodeLine> codeLines, String name) {
        if (RecodeItemAPI.connected) {
            RecodeItemAPI.sendTemplates(codeLines, name);
        } else if (CodeClientAPI.connected) {
            CodeClientAPI.sendTemplates(codeLines, name);
        } else {
            throw new IllegalStateException("No connections available");
        }
    }

    public static void sendRawTemplates(List<String> codeLines, String name) {
        if (RecodeItemAPI.connected) {
            RecodeItemAPI.sendRawTemplates(codeLines, name);
        } else if (CodeClientAPI.connected) {
            CodeClientAPI.sendRawTemplates(codeLines, name);
        } else {
            throw new IllegalStateException("No connections available");
        }
    }
}

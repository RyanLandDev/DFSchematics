package net.ryanland.dfschematics.df.api;

import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.fxml.MainController;

import java.net.URI;
import java.util.List;

public class RecodeItemAPI {

    private static WSClient socket;
    public static boolean connected = false;

    @SneakyThrows
    public static void attemptConnection() {
        if (connected) return;

        socket = new WSClient(new URI("ws://localhost:31371"));  // recode API endpoint
        if (!socket.connectBlocking()) {
            // Failed connecting
            MainController.instance.setRecodeStatus("Failed connecting to recode", false);
            return;
        }
        // connected
        MainController.instance.setRecodeStatus("Connected to recode", true);
        connected = true;
    }

    @SneakyThrows
    public static void sendTemplates(List<CodeLine> codeLines, String name) {
        sendRawTemplates(codeLines.stream().map(CodeLine::toCompressedJson).toList(), name);
    }

    @SneakyThrows
    public static void sendRawTemplates(List<String> codeLines, String name) {
        int i = 0;
        for (String codeLine : codeLines) {
            if (i != 0) Thread.sleep(400);//delay necessary to send multiple items
            i++;
            JsonObject json = new JsonObject();
            json.addProperty("type", "template");
            JsonObject data = new JsonObject();
            data.addProperty("data", codeLine);
            data.addProperty("name", name + (codeLines.size() > 1 ? " "+i+"/"+codeLines.size() : ""));
            json.addProperty("data", data.toString());
            json.addProperty("source", "DFSchematics");

            socket.send(json + "\n");
        }
    }
}

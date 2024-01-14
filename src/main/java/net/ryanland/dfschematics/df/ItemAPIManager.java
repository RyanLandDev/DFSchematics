package net.ryanland.dfschematics.df;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import net.ryanland.dfschematics.Controller;
import net.ryanland.dfschematics.df.code.CodeLine;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ItemAPIManager {

    private static WSClient recodeSocket;
    private static WSClient codeClientSocket;

    public static Button retryButton;
    public static boolean recodeConnected = false;
    public static boolean codeClientConnected = false;
    public static boolean codeClientAuthorized = false;

    public static void attemptConnections() {
        if (!recodeConnected) attemptRecodeConnection();
        if (!codeClientConnected) attemptCodeClientConnection();
        if (recodeConnected && codeClientConnected) retryButton.setDisable(true);
    }

    public static WSClient getRecodeSocket() {
        return recodeSocket;
    }

    public static Label recodeStatus;
    public static Button sendRecode;
    public static Button sendBuilderRecode;

    public final static int RECODE_PORT = 31371;

    public static void attemptRecodeConnection() {
        boolean success;
        try {
            recodeSocket = new WSClient(new URI("ws://localhost:" + RECODE_PORT));  // recode ItemAPI port
            success = recodeSocket.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        if (!success) {
            recodeStatus.setTextFill(Color.RED);
            recodeStatus.setText("Failed connecting to recode");
            sendRecode.setDisable(true);
            sendBuilderRecode.setDisable(true);
            return;
        }
        // connected
        recodeStatus.setTextFill(Color.GREEN);
        recodeStatus.setText("Connected to recode");
        if (Controller.selectedFile != null) sendRecode.setDisable(false);
        sendBuilderRecode.setDisable(false);
        recodeConnected = true;
    }

    public static void sendTemplatesToRecode(List<CodeLine> codeLines, String name) throws InterruptedException {
        sendRawTemplatesToRecode(codeLines.stream().map(codeLine -> {
            try {
                return codeLine.toCompressedJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).toList(), name);
    }

    //jpackage --input out/artifacts/DFSchematics_jar --dest out/result --name DFSchematics --main-jar DFSchematics.jar --main-class net.ryanland.dfschematics.DFSchematics --type exe

    public static void sendRawTemplatesToRecode(List<String> codeLines, String name) throws InterruptedException {
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

            recodeSocket.send(json + "\n");
        }
    }

    public static Label codeClientStatus;
    public static Button sendCodeClient;
    public static Button sendBuilderCodeClient;

    public static void attemptCodeClientConnection() {
        boolean success;
        try {
            codeClientSocket = new WSClient(new URI("ws://localhost:31375")); // codeclient api port
            success = codeClientSocket.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        if (!success) {
            codeClientStatus.setTextFill(Color.RED);
            codeClientStatus.setText("Failed connecting to CodeClient");
            sendCodeClient.setDisable(true);
            sendBuilderCodeClient.setDisable(true);
            return;
        }
        // connected but unauthorized
        codeClientStatus.setTextFill(Color.ORANGE);
        codeClientStatus.setText("Unauthorized in CodeClient");
        sendCodeClient.setDisable(true);
        sendBuilderCodeClient.setDisable(true);
        codeClientConnected = true;
    }

    private static void onCodeClientAuthorized() {
        Platform.runLater(() -> {
            // connected and authorized
            codeClientStatus.setTextFill(Color.GREEN);
            codeClientStatus.setText("Connected to CodeClient");
            if (Controller.selectedFile != null) sendCodeClient.setDisable(false);
            sendBuilderCodeClient.setDisable(false);
            codeClientAuthorized = true;
        });
    }

    public static void sendTemplatesToCodeClient(List<CodeLine> codeLines, String name) {
        sendRawTemplatesToCodeClient(codeLines.stream().map(codeLine -> {
            try {
                return codeLine.toCompressedJson();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).toList(), name);
    }

    public static void sendRawTemplatesToCodeClient(List<String> codeLines, String name) {
        int i = 0;
        for (String codeLine : codeLines) {
            i++;
            String counter = codeLines.size() > 1 ? " "+i+"/"+codeLines.size() : "";
            String nbt = "{Count:1b,id:\"minecraft:ender_chest\",tag:{PublicBukkitValues:{\"hypercube:codetemplatedata\":'{\"author\":\"RyanLand\",\"name\":\"&b" + name + counter + " \",\"version\":1,\"code\":\"" +
                codeLine + "\"}'},display:{Name:'{\"extra\":[{\"color\":\"aqua\",\"text\":\"" + name + counter + "\"}],\"text\":\"\"}'}}}";
            codeClientSocket.send("give " + nbt);
        }
    }

    static class WSClient extends WebSocketClient {

        public WSClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onMessage(String s) {
            if (s.equals("auth")) onCodeClientAuthorized();
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {}

        @Override
        public void onClose(int i, String s, boolean b) {}

        @Override
        public void onError(Exception e) {}
    }

}

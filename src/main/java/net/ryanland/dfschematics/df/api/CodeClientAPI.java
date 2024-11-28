package net.ryanland.dfschematics.df.api;

import lombok.SneakyThrows;
import net.ryanland.dfschematics.df.code.CodeBlock;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.fxml.MainController;

import java.net.URI;
import java.util.List;

public class CodeClientAPI {

    private static WSClient socket;
    public static boolean connected = false;

    @SneakyThrows
    public static void attemptConnection() {
        if (connected) return;

        socket = new WSClient(new URI("ws://localhost:31375")){
            @Override
            public void onMessage(String s) {
                handleMessage(s);
            }
        }; // codeclient api endpoint
        if (!socket.connectBlocking()) {
            // Failed connecting
            MainController.instance.setCodeClientStatus("Failed connecting to CodeClient", false);
            return;
        }
        // connected
        MainController.instance.setCodeClientStatus("Connected to CodeClient", true);
        connected = true;
    }

    public static void sendTemplates(List<CodeLine> codeLines, String name) {
        sendRawTemplates(codeLines.stream().map(CodeLine::toCompressedJson).toList(), name);
    }

    public static void sendRawTemplates(List<String> codeLines, String name) {
        int i = 0;
        for (String codeLine : codeLines) {
            i++;
            String counter = codeLines.size() > 1 ? " "+i+"/"+codeLines.size() : "";
            String nbt = "{components:{\"minecraft:custom_data\":{PublicBukkitValues:{\"hypercube:codetemplatedata\":'{\"author\":\"RyanLand\",\"name\":\"" + name + counter + "\",\"version\":1,\"code\":\""+ codeLine + "\"}'}},\"minecraft:custom_name\":'{\"text\":\""+ name + counter +"\", \"color\":\"aqua\"}'},count:1,id:\"minecraft:ender_chest\"}";
            socket.send("give " + nbt);
        }
    }

    private static void handleMessage(String msg) {
        if (msg.equals("default")) {
            // Attempting to place templates but unauthorized
            socket.send("scopes write_code read_plot");//sends user an auth request
            MainController.instance.error("Please authorize DFSchematics to place templates in-game...");
        }
        if (msg.equals("auth") || msg.equals("write_code read_plot default")) {
            // User authorized DFSchematics or has already authorized, get plot size
            socket.send("size");
        }
        if (msg.equals("BASIC") || msg.equals("LARGE") || msg.equals("MASSIVE") || msg.equals("MEGA")) {
            // Got plot size, check and place templates
            List<CodeBlock> codeBlocks = MainController.schematic.getTemplateFactory().generateCodeBlocks();
            // codeclient currently returns wrong plot size
            //if (codeBlocks.size() * 2 > getPlotSize(msg)) {
            //    MainController.instance.error("Plot size too small for templates");
            //    return;
            //}
            placeTemplates(MainController.schematic.getTemplateFactory().splitCodeBlocks(codeBlocks));
        }
    }

    private static int getPlotSize(String type) {
        return switch (type) {
            case "BASIC" -> 51;
            case "LARGE" -> 101;
            case "MASSIVE", "MEGA" ->//Mega codespace limit is 301, not 1001
                301;
            default -> 0;
        };
    }

    public static void auth() {
        socket.send("scopes");//Ask codeclient what scopes we have
    }

    public static void placeTemplates(List<CodeLine> codeLines) {
        socket.send("place compact");
        for (CodeLine codeLine : codeLines) {
            socket.send("place " + codeLine.toCompressedJson());
        }
        socket.send("place go");
        MainController.instance.success("Templates placed");
    }
}

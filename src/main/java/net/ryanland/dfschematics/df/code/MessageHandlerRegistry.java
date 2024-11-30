package net.ryanland.dfschematics.df.code;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageHandlerRegistry {


    private static final Map<String, MessageHandler> handlers = new HashMap<>();

    static {
        handlers.put("default", new DefaultHandler());
        handlers.put("auth", new AuthHandler());
        handlers.put("write_code read_plot default", new AuthHandler());
        handlers.put("BASIC", new SizeHandler());
        handlers.put("LARGE", new SizeHandler());
        handlers.put("MASSIVE", new SizeHandler());
        handlers.put("MEGA", new SizeHandler());
    }

    interface MessageHandler {
        void handle(String msg);
    }

    // Implementación del manejador para el mensaje "default"
    static class DefaultHandler implements MessageHandler {
        @Override
        public void handle(String msg) {
            socket.send("scopes write_code read_plot");
            MainController.instance.error("Please authorize DFSchematics to place templates in-game...");
        }
    }

    static class AuthHandler implements MessageHandler {
        @Override
        public void handle(String msg) {
            socket.send("size");
        }
    }

    static class SizeHandler implements MessageHandler {
        @Override
        public void handle(String msg) {
            List<CodeBlock> codeBlocks = MainController.schematic.getTemplateFactory().generateCodeBlocks();
            placeTemplates(MainController.schematic.getTemplateFactory().splitCodeBlocks(codeBlocks));
        }
    }
}

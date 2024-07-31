package net.ryanland.dfschematics.df.api;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

//Todo better close/error handling?
public class WSClient extends WebSocketClient {

    public WSClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onMessage(String s) {}

    @Override
    public void onOpen(ServerHandshake serverHandshake) {}

    @Override
    public void onClose(int i, String s, boolean b) {}

    @Override
    public void onError(Exception e) {}
}
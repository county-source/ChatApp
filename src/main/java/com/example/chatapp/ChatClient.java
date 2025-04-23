package com.example.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A simple TCP chat client for use with a JavaFX/WebView UI.
 * Exposes connect(), sendName(), and sendMessage() to JavaScript.
 */
public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UIUpdater uiUpdater;

    /** Callback interface for pushing messages back into the UI */
    public interface UIUpdater {
        void update(String user, String text);
    }

    /** Register the UI callback (from MainApp) */
    public void setUIUpdater(UIUpdater updater) {
        this.uiUpdater = updater;
    }

    /** Ensure we’re connected, otherwise connect to localhost:12345 */
    private void ensureConnected() {
        if (socket == null || socket.isClosed()) {
            connect();
        }
    }

    /** Connect to localhost:12345 */
    public void connect() {
        connect("localhost", 12345);
    }

    /** Connect to specified host and port */
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new PrintWriter(socket.getOutputStream(), true);  // autoFlush
            new Thread(this::listen).start();
            System.out.println("ChatClient: connected to " + host + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Send the chosen username, then immediately notify UI of join */
    public void sendName(String name) {
        ensureConnected();
        if (out != null) {
            out.println(name);
            System.out.println("ChatClient: sent name = " + name);
            // Notify UI that this user has joined
            if (uiUpdater != null) {
                uiUpdater.update("", name + " se připojil.");
            }
        }
    }

    /** Send a chat message */
    public void sendMessage(String text) {
        ensureConnected();
        if (out != null) {
            out.println(text);
            System.out.println("ChatClient: sent message = " + text);
        }
    }

    /** Listen for incoming broadcasts and dispatch to UI */
    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String user = "";
                String msg  = line;
                int idx = line.indexOf(": ");
                if (idx != -1) {
                    user = line.substring(0, idx);
                    msg  = line.substring(idx + 2);
                }
                if (uiUpdater != null) {
                    uiUpdater.update(user, msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

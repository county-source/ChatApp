package com.example.chatapp;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UIUpdater uiUpdater;

    public interface UIUpdater { void update(String user, String text); }

    public void setUIUpdater(UIUpdater u) { this.uiUpdater = u; }

    /** Connect to specified host/port */
    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this::listen).start();
            System.out.println("ChatClient: connected to " + host + ":" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Default connect to localhost:12345 */
    public void connect() {
        connect("localhost", 12345);
    }

    /** Send username and immediately notify UI */
    public void sendName(String name) {
        if (out == null) connect();
        out.println(name);
        if (uiUpdater != null) uiUpdater.update("", name + " se připojil.");
        System.out.println("ChatClient: sent name = " + name);
    }

    /** Send a chat message */
    public void sendMessage(String text) {
        if (out == null) connect();
        out.println(text);
        System.out.println("ChatClient: sent message = " + text);
    }

    /** Listen for server broadcasts */
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
                if (uiUpdater != null) uiUpdater.update(user, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.chatapp;

import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    protected void onServerPrompt(String prompt) {}

    protected void onMessageReceived(String message) {
        // Zavoláno z vlákna naslouchání, rozdělení zprávy
        String user = message;
        String text = message;
        int idx = message.indexOf(": ");
        if (idx != -1) {
            user = message.substring(0, idx);
            text = message.substring(idx + 2);
        }
        notifyUI(user, text);
    }

    protected void onError(Exception e) { e.printStackTrace(); }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String prompt = in.readLine();
        onServerPrompt(prompt);
    }

    public void sendName(String name) {
        out.println(name);
        new Thread(this::listen).start();
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                onMessageReceived(line);
            }
        } catch (IOException e) {
            onError(e);
        }
    }

    public void sendMessage(String text) {
        out.println(text);
    }

    // Externí callback registrovaný v MainApp
    private UIUpdater uiUpdater;

    public void setUIUpdater(UIUpdater updater) {
        this.uiUpdater = updater;
    }

    private void notifyUI(String user, String text) {
        if (uiUpdater != null) uiUpdater.update(user, text);
    }

    public interface UIUpdater { void update(String user, String text); }
}
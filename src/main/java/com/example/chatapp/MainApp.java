package com.example.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import java.net.URL;

public class MainApp extends Application {
    private ChatClient client;

    @Override
    public void start(Stage primaryStage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Initialize ChatClient and UI updater
        client = new ChatClient();
        client.setUIUpdater((user, text) -> {
            Platform.runLater(() -> {
                if (text.endsWith("se připojil.") || text.endsWith("odešel.")) {
                    // Connection/disconnection notification
                    String escaped = text.replace("'", "\'");
                    engine.executeScript("appendNotification('" + escaped + "')");
                } else {
                    // Regular chat message
                    String u = user.replace("'", "\'");
                    String t = text.replace("'", "\'");
                    engine.executeScript("appendMessage('" + u + "','" + t + "')");
                }
            });
        });

        // Expose javaClient to JavaScript after page load
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaClient", client);
            }
        });

        // Load HTML UI
        URL url = getClass().getResource("/com/example/chatapp/index.html");
        if (url == null) {
            throw new IllegalStateException(
                    "index.html nenalezeno v resources/com/example/chatapp");
        }
        engine.load(url.toExternalForm());

        // Show JavaFX scene
        primaryStage.setTitle("Chat App");
        primaryStage.setScene(new Scene(webView, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
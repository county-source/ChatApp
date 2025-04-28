package com.example.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

import java.net.URL;

public class MainApp extends Application {
    private ChatClient client;

    @Override
    public void start(Stage stage) {
        // Use an undecorated window so we can draw our own title bar
        stage.initStyle(StageStyle.UNDECORATED);

        // --- Title Bar ---
        HBox titleBar = new HBox(8);
        titleBar.setPadding(new Insets(5));
        titleBar.setStyle("-fx-background-color: #121212;");

        // Push controls to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().add(spacer);

        // Close, Minimize, Maximize circles
        titleBar.getChildren().addAll(
                makeCircle("#FF5F57", e -> Platform.exit()),
                makeCircle("#FFBD2E", e -> stage.setIconified(true)),
                makeCircle("#28C840", e -> stage.setMaximized(!stage.isMaximized()))
        );

        // Enable dragging window by title bar
        Delta dragDelta = new Delta();
        titleBar.setOnMousePressed(event -> {
            dragDelta.x = stage.getX() - event.getScreenX();
            dragDelta.y = stage.getY() - event.getScreenY();
            titleBar.setCursor(Cursor.MOVE);
        });
        titleBar.setOnMouseReleased(event -> titleBar.setCursor(Cursor.DEFAULT));
        titleBar.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) {
                stage.setX(event.getScreenX() + dragDelta.x);
                stage.setY(event.getScreenY() + dragDelta.y);
            }
        });

        // --- WebView and JS Bridge ---
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Initialize ChatClient and hook into JS
        client = new ChatClient();
        client.setUIUpdater((user, text) ->
                Platform.runLater(() -> {
                    String escText = text.replace("'", "\\'");
                    if (user == null || user.isEmpty()) {
                        engine.executeScript("appendNotification('" + escText + "')");
                    } else {
                        String escUser = user.replace("'", "\\'");
                        engine.executeScript("appendMessage('" + escUser + "','" + escText + "')");
                    }
                })
        );

        // After HTML loads, inject the javaClient bridge
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaClient", client);
            }
        });

        // Load our HTML UI from resources
        URL html = getClass().getResource("/com/example/chatapp/index.html");
        if (html == null) {
            throw new IllegalStateException("index.html not found in resources/com/example/chatapp");
        }
        engine.load(html.toExternalForm());

        // --- Scene Setup ---
        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(webView);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Helper to create a little macOS-style control circle.
     */
    private Circle makeCircle(String colorHex, EventHandler<MouseEvent> onClick) {
        Circle circle = new Circle(6, Color.web(colorHex));
        circle.setStroke(Color.rgb(0, 0, 0, 0.2));
        circle.setOnMouseClicked(onClick);
        circle.setOnMouseEntered(e -> circle.setOpacity(0.8));
        circle.setOnMouseExited(e -> circle.setOpacity(1.0));
        return circle;
    }

    public static void main(String[] args) {
        launch(args);
    }

    /** Utility for dragging the window around */
    private static class Delta {
        double x, y;
    }
}

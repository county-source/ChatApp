package com.example.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;
import javafx.event.EventHandler;              // JavaFX event handler interface :contentReference[oaicite:0]{index=0}
import javafx.scene.input.MouseEvent;           // Mouse event type for dragging :contentReference[oaicite:1]{index=1}
import javafx.scene.paint.Color;               // For coloring the control circles
import javafx.scene.shape.Circle;              // For drawing the mac-style buttons


import java.net.URL;

public class MainApp extends Application {
    private ChatClient client;

    @Override
    public void start(Stage stage) {
        // 1) Undecorated window
        stage.initStyle(StageStyle.UNDECORATED);

        // 2) Title bar (mac-style circles on right)
        HBox titleBar = new HBox( 8 );
        titleBar.setPadding(new Insets(4));
        titleBar.setStyle("-fx-background-color: #121212;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().add(spacer);

        // Create macOS-style control circles
        Circle btnClose = createCircle("#FF5F57", e -> Platform.exit());
        Circle btnMin   = createCircle("#FFBD2E", e -> stage.setIconified(true));
        Circle btnMax   = createCircle("#28C840", e -> toggleMaximize(stage));

        titleBar.getChildren().addAll(btnMin, btnMax, btnClose);

        // Drag support
        final Delta drag = new Delta();
        titleBar.setOnMousePressed(e -> {
            drag.x = stage.getX() - e.getScreenX();
            drag.y = stage.getY() - e.getScreenY();
            titleBar.setCursor(Cursor.MOVE);
        });
        titleBar.setOnMouseReleased(e -> titleBar.setCursor(Cursor.DEFAULT));
        titleBar.setOnMouseDragged(e -> {
            if (!stage.isFullScreen()) {
                stage.setX(e.getScreenX() + drag.x);
                stage.setY(e.getScreenY() + drag.y);
            }
        });

        // 3) WebView UI
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        client = new ChatClient();
        client.setUIUpdater((user, text) ->
                Platform.runLater(() -> {
                    String escapedText = text.replace("'", "\\'");
                    if (user.isEmpty()) {
                        engine.executeScript("appendNotification('" + escapedText + "')");
                    } else {
                        String escUser = user.replace("'", "\\'");
                        engine.executeScript("appendMessage('" + escUser + "','" + escapedText + "')");
                    }
                })
        );

        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaClient", client);
            }
        });

        URL url = getClass().getResource("/com/example/chatapp/index.html");
        if (url == null) throw new IllegalStateException("index.html not found");
        engine.load(url.toExternalForm());

        // 4) Layout
        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(webView);

        // 5) Show
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /** Helper to create control circle */
    private Circle createCircle(String colorHex, EventHandler<MouseEvent> action) {
        Circle c = new Circle(6, Color.web(colorHex));
        c.setStroke(Color.rgb(0,0,0,0.2));
        c.setOnMouseClicked(action);
        c.setOnMouseEntered(e -> c.setOpacity(0.8));
        c.setOnMouseExited(e -> c.setOpacity(1.0));
        return c;
    }

    /** Toggle maximize / restore */
    private void toggleMaximize(Stage stage) {
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /** Simple helper for mouse drag delta */
    private static class Delta {
        double x, y;
    }
}

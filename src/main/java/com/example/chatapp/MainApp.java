package com.example.chatapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);

        // Title bar with Mac-style controls
        HBox titleBar = new HBox(8);
        titleBar.setPadding(new Insets(5));
        titleBar.setStyle("-fx-background-color: #121212;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().add(spacer);
        titleBar.getChildren().addAll(
                makeCircle("#FF5F57", e -> Platform.exit()),
                makeCircle("#FFBD2E", e -> stage.setIconified(true)),
                makeCircle("#28C840", e -> stage.setMaximized(!stage.isMaximized()))
        );
        Delta drag = new Delta();
        titleBar.setOnMousePressed(e -> {
            drag.x = stage.getX() - e.getScreenX();
            drag.y = stage.getY() - e.getScreenY();
            titleBar.setCursor(Cursor.MOVE);
        });
        titleBar.setOnMouseReleased(e -> titleBar.setCursor(Cursor.DEFAULT));
        titleBar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() + drag.x);
                stage.setY(e.getScreenY() + drag.y);
            }
        });

        // WebView and JS bridge
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        ChatClient client = new ChatClient();
        client.setUIUpdater((user, text) ->
                Platform.runLater(() -> {
                    String t = text.replace("'", "\\'");
                    if (user.isEmpty()) {
                        engine.executeScript("appendNotification('" + t + "')");
                    } else {
                        String u = user.replace("'", "\\'");
                        engine.executeScript("appendMessage('" + u + "','" + t + "')");
                    }
                })
        );
        engine.getLoadWorker().stateProperty().addListener(
                (obs,old,newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject win = (JSObject) engine.executeScript("window");
                        win.setMember("javaClient", client);
                    }
                }
        );
        URL url = getClass().getResource("/com/example/chatapp/index.html");
        if (url == null) throw new IllegalStateException("index.html not found");
        engine.load(url.toExternalForm());

        BorderPane root = new BorderPane(webView, titleBar, null, null, null);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private Circle makeCircle(String hex, EventHandler<MouseEvent> h) {
        Circle c = new Circle(6, Color.web(hex));
        c.setStroke(Color.rgb(0,0,0,0.2));
        c.setOnMouseClicked(h);
        c.setOnMouseEntered(e -> c.setOpacity(0.8));
        c.setOnMouseExited(e -> c.setOpacity(1.0));
        return c;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class Delta { double x, y; }
}

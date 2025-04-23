package com.example.chatapp;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.application.Platform;

public class MainViewController {
    @FXML private Button btnMin;
    @FXML private Button btnClose;

    private Stage stage;

    // Called by MainApp after loading FXML
    public void setStage(Stage stage) {
        this.stage = stage;

        btnMin.setOnAction(e -> stage.setIconified(true));  // :contentReference[oaicite:2]{index=2}
        btnClose.setOnAction(e -> Platform.exit());         // :contentReference[oaicite:3]{index=3}
    }
}

package com.anikanov.controller;

import com.anikanov.paper.crawler.service.LinksTreeBuilderService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@FxmlView("main-stage.fxml")
@RequiredArgsConstructor
public class ApplicationController {
    @FXML
    private Button crawlButton;

    @FXML
    private TextFlow outputArea;

    @FXML
    private Button selectFileButton;

    @FXML
    private Label selectedFileLabel;

    private final LinksTreeBuilderService treeBuilderService;

    private final FileChooser fileChooser = new FileChooser();

    private File selectedFile = null;

    @FXML
    private void initialize() {
        final Window window = Window.getWindows().get(0);
        selectFileButton.setOnAction(actionEvent -> {
            selectedFile = fileChooser.showOpenDialog(window);
            if (selectedFile == null) {
                selectedFileLabel.setText("Error");
            }
        });

        crawlButton.setOnAction(actionEvent -> {

        });
    }
}

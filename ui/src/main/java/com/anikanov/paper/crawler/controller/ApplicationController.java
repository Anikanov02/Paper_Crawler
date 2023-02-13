package com.anikanov.paper.crawler.controller;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.config.GlobalConstantsUi;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.DepthProcessor;
import com.anikanov.paper.crawler.service.ProgressCallback;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@FxmlView("main-stage.fxml")
@RequiredArgsConstructor
public class ApplicationController {
    @FXML
    private Button crawlButton;

    @FXML
    private Label infoLabel;

    @FXML
    private TextArea outputArea;

    @FXML
    private Label progressLabel;

    @FXML
    private Button selectFileButton;

    @FXML
    private Spinner<Integer> maxDepthSpinner;

    private final AppProperties properties;

    @Qualifier(GlobalConstants.CROSSREF_DEPTH)
    private final DepthProcessor depthProcessorService;

    private final FileChooser fileChooser = new FileChooser();

    private File selectedFile = null;

    private Long executed = 0L;

    private StringProperty stringProperty = new SimpleStringProperty();

    @FXML
    private void initialize() {
        progressLabel.textProperty().bind(stringProperty);

        selectFileButton.setOnAction(actionEvent -> {
            selectedFile = fileChooser.showOpenDialog(infoLabel.getScene().getWindow());
            if (selectedFile != null) {
                infoLabel.setText(selectedFile.getName());
            }
        });

        crawlButton.setOnAction(actionEvent -> {
            flushProgress();
            properties.setMaxDepth(new BigDecimal(maxDepthSpinner.getValue()));
            String message = "";
            if (selectedFile == null) {
                message = "File not selected";
            } else if (!selectedFile.exists()) {
                message = "File not exists";
            } else if (!notADirectory(selectedFile)) {
                message = "File should not be a directory";
            } else if (!acceptableExtension(selectedFile)) {
                message = "File should have one of following extensions: " + String.join(",", GlobalConstantsUi.SUPPORTED_EXTENSIONS);
            } else {
                try {
                    final StringBuilder output = new StringBuilder("Result: ").append(System.lineSeparator());
                    final Map<AggregatedLinkInfo, Long> result = depthProcessorService.process(new FileInputStream(selectedFile), new AppCallback());
                    final List<AggregatedLinkInfo> keySet = result.keySet().stream().toList();
                    for (int i = 0; i < keySet.size(); i++) {
                        final AggregatedLinkInfo info = keySet.get(i);
                        output.append("Paper ").append(i).append(":").append(System.lineSeparator())
                                .append("INFO: ").append(System.lineSeparator())
                                .append("TEXT REFERENCE: ").append(info.getText()).append(System.lineSeparator())
                                .append("HYPERLINK: ").append(info.getLink()).append(System.lineSeparator())
                                .append("Occurrences: ").append(result.get(info)).append(System.lineSeparator())
                                .append("----------------------------------------------------------")
                                .append(System.lineSeparator());
                    }
                    message = output.toString();
                } catch (IOException e) {
                    message = e.getMessage();
                }
            }
            outputArea.setText(message);
        });
    }

    private void flushProgress() {
        executed = 0L;
        stringProperty.set("0");
    }

    private boolean notADirectory(File file) {
        return Optional.of(selectedFile).filter(f -> f.getName().contains(".")).isPresent();
    }

    private boolean acceptableExtension(File file) {
        final String ext = Optional.of(selectedFile).filter(f -> f.getName().contains("."))
                .map(f -> f.getName().substring(f.getName().lastIndexOf(".") + 1)).orElse("");
        return GlobalConstantsUi.SUPPORTED_EXTENSIONS.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(ext));
    }

    public class AppCallback extends ProgressCallback {
        @Override
        public void callback() {
            executed++;
            stringProperty.set(String.valueOf(executed));
        }
    }
}

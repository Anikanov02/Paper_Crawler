package com.anikanov.paper.crawler.controller;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.config.GlobalConstantsUi;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.DepthProcessor;
import com.anikanov.paper.crawler.service.ProgressCallback;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
@FxmlView("main-stage.fxml")
@RequiredArgsConstructor
public class ApplicationController {
    @FXML
    private ChoiceBox<ApproachOption> approachChooser;

    @FXML
    private Button crawlButton;

    @FXML
    private TextField doiInput;

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

    private final ExecutorService executorService;

    private final FileChooser fileChooser = new FileChooser();

    private File selectedFile = null;

    private Long executed = 0L;

    @FXML
    private void initialize() {
        final ObservableList<ApproachOption> options = FXCollections.observableList(Arrays.stream(ApproachOption.values()).collect(Collectors.toList()));
        approachChooser.setItems(options);
        approachChooser.setValue(ApproachOption.PDF);
        visualizeOption();

        approachChooser.setOnAction(actionEvent -> {
            visualizeOption();
        });

        selectFileButton.setOnAction(actionEvent -> {
            selectedFile = fileChooser.showOpenDialog(infoLabel.getScene().getWindow());
            if (selectedFile != null) {
                infoLabel.setText(selectedFile.getName());
            }
        });

        crawlButton.setOnAction(actionEvent -> {
            executorService.submit(() -> {
                switch (approachChooser.getValue()) {
                    case PDF -> executePdfInputCrawling();
                    case DOI -> executeDoiInputCrawling();
                }
            });
        });
    }

    private void executePdfInputCrawling() {
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
                final Map<AggregatedLinkInfo, Long> result = depthProcessorService.process(new FileInputStream(selectedFile), new AppCallback());
                message = buildOutputMessage(result);
            } catch (IOException e) {
                message = e.getMessage();
            }
        }
        outputArea.setText(message);
    }

    private void executeDoiInputCrawling() {
        flushProgress();
        properties.setMaxDepth(new BigDecimal(maxDepthSpinner.getValue()));
        String message = "";
        final String doi = doiInput.textProperty().getValue().trim();
        if (Objects.equals("", doi)) {
            message = "You should specify DOI";
        } else {
            final Map<AggregatedLinkInfo, Long> result = depthProcessorService.process(doi, new AppCallback());
            message = buildOutputMessage(result);
        }
        outputArea.setText(message);
    }

    private String buildOutputMessage(Map<AggregatedLinkInfo, Long> result) {
        final StringBuilder output = new StringBuilder("Result: ").append(System.lineSeparator());
        final List<AggregatedLinkInfo> keySet = result.keySet().stream().sorted(Comparator.comparing(result::get).reversed()).toList();
        if (keySet.isEmpty()) {
            output.append("Unreadable input or nothing to extract.");
        } else {
            for (int i = 0; i < keySet.size(); i++) {
                final AggregatedLinkInfo info = keySet.get(i);
                output.append("Paper ").append(i + 1).append(":").append(System.lineSeparator())
                        .append("INFO: ").append(System.lineSeparator())
                        .append("TEXT REFERENCE: ").append(info.getText()).append(System.lineSeparator())
                        .append("DOI: ").append(info.getDoi()).append(System.lineSeparator())
                        .append("Occurrences: ").append(result.get(info)).append(System.lineSeparator())
                        .append("----------------------------------------------------------")
                        .append(System.lineSeparator());
            }
        }
        return output.toString();
    }

    private void visualizeOption() {
        switch (approachChooser.getValue()) {
            case DOI -> {
                selectFileButton.setVisible(false);
                selectFileButton.setDisable(true);
                doiInput.setVisible(true);
                doiInput.setDisable(false);
            }
            case PDF -> {
                selectFileButton.setVisible(true);
                selectFileButton.setDisable(false);
                doiInput.setVisible(false);
                doiInput.setDisable(true);
            }
        }
    }

    private void flushProgress() {
        executed = 0L;
        Platform.runLater(() -> progressLabel.setText(""));
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
        private Long minorIteration;
        private Long majorIteration;
        private BigDecimal depth;

        public AppCallback() {
            minorIteration = 0L;
            majorIteration = 0L;
            depth = BigDecimal.ZERO;
        }

        @Override
        public void notifyMinor() {
            minorIteration = ++executed;
            Platform.runLater(() -> progressLabel.setText(String.format("Depth %s: %s/%s", depth.toPlainString(), minorIteration, majorIteration)));
        }

        @Override
        public void notifyMajor(Long newLayerRequestCount, BigDecimal depth) {
            flushProgress();
            majorIteration = newLayerRequestCount;
            this.depth = depth;
        }
    }

    private enum ApproachOption {
        DOI("doi input"),
        PDF("pdf input");

        private String code;

        ApproachOption(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
           return code;
        }
    }
}

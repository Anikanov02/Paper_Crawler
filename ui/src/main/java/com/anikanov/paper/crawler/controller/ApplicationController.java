package com.anikanov.paper.crawler.controller;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.config.GlobalConstantsUi;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.DepthProcessorResult;
import com.anikanov.paper.crawler.domain.KeyWordsFilter;
import com.anikanov.paper.crawler.service.*;
import com.anikanov.paper.crawler.service.cookie.ChromeCookiesExtractor;
import com.anikanov.paper.crawler.service.processor.CrossrefDepthProcessorService;
import com.anikanov.paper.crawler.service.processor.DepthProcessor;
import com.anikanov.paper.crawler.source.GenericPdfSource;
import com.anikanov.paper.crawler.source.PdfSource;
import com.anikanov.paper.crawler.source.sciHub.SciHubPdfSource;
import com.anikanov.paper.crawler.util.OutputUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Component
@FxmlView("main-stage.fxml")
@RequiredArgsConstructor
public class ApplicationController implements Stoppable {
    @FXML
    private ChoiceBox<ApproachOption> approachChooser;

    @FXML
    private ChoiceBox<PdfDownloader.FiltrationOption> filtrationModeChooser;

    @FXML
    private Label dictionaryInfoLabel;

    @FXML
    private Button failedPdfsDeletionButton;

    @FXML
    private Spinner<Integer> pdfsLimitSpinner;

    @FXML
    private Button crawlButton;

    @FXML
    private TextField doiInput;

    @FXML
    private Label infoLabel;

    @FXML
    private TextField keyWordField;

    @FXML
    private TextArea outputArea;

    @FXML
    private Spinner<Integer> outputLimitSpinner;

    @FXML
    private Label progressLabel;

    @FXML
    private TextField pdfSource;

    @FXML
    private Button selectPdfFileButton;

    @FXML
    private Button selectDoiFileButton;

    @FXML
    private Button selectDictionaryFileButton;

    @FXML
    private Spinner<Integer> maxDepthSpinner;

    private final AppProperties properties;

    @Qualifier(GlobalConstants.CROSSREF_DEPTH)
    private final DepthProcessor depthProcessorService;

    private final ExecutorService executorService;

    private final BibTexSerializer serializer;

    private final PdfDownloader pdfDownloader;

    private final SciHubPdfSource sciHubPdfSource;

    private final ChromeCookiesExtractor cookiesExtractor;

    private final FileChooser fileChooser = new FileChooser();

    private final FileChooser dictionaryChooser = new FileChooser();

    private File pdfFile = null;
    private File doiFile = null;

    private File dictionary = null;

    private KeyWordsFilter keyWordsFilter;

    private Long executed = 0L;

    private boolean running = false;

    @FXML
    private void initialize() {
        final ObservableList<ApproachOption> approachOptions = FXCollections.observableList(Arrays.stream(ApproachOption.values()).collect(Collectors.toList()));
        final ObservableList<PdfDownloader.FiltrationOption> filtrationOptions = FXCollections.observableList(Arrays.stream(PdfDownloader.FiltrationOption.values()).collect(Collectors.toList()));
        approachChooser.setItems(approachOptions);
        approachChooser.setValue(ApproachOption.PDF);
        visualizeOption();

        filtrationModeChooser.setItems(filtrationOptions);
        filtrationModeChooser.setValue(PdfDownloader.FiltrationOption.GENERAL);

        failedPdfsDeletionButton.setDisable(true);

        approachChooser.setOnAction(actionEvent -> {
            visualizeOption();
        });

        selectPdfFileButton.setOnAction(actionEvent -> {
            pdfFile = fileChooser.showOpenDialog(infoLabel.getScene().getWindow());
            if (pdfFile != null) {
                infoLabel.setText(pdfFile.getName());
            }
        });

        selectDoiFileButton.setOnAction(actionEvent -> {
            doiFile = fileChooser.showOpenDialog(infoLabel.getScene().getWindow());
            if (doiFile != null) {
                infoLabel.setText(doiFile.getName());
            }
        });

        selectDictionaryFileButton.setOnAction(actionEvent -> {
            dictionary = dictionaryChooser.showOpenDialog(dictionaryInfoLabel.getScene().getWindow());
            if (dictionary != null && !getExtension(dictionary).equalsIgnoreCase("txt")) {
                outputArea.setText("dictionary should have txt extension");
            } else if (dictionary != null) {
                dictionaryInfoLabel.setText(dictionary.getName());
                try {
                    GlobalConstants.applyDictionary(dictionary);
                } catch (Exception e) {
                    outputArea.setText(e.getMessage());
                }
            }
        });

        crawlButton.setOnAction(actionEvent -> {
            if (running) {
                stop();
                return;
            }
            running = true;
            executorService.submit(() -> {
                if (depthProcessorService instanceof CrossrefDepthProcessorService) {
                    ((CrossrefDepthProcessorService) depthProcessorService).setLimit(outputLimitSpinner.getValue());
                }
                switch (approachChooser.getValue()) {
                    case PDF -> executePdfInputCrawling();
                    case DOI ->
                            executeDoiInputCrawling(Arrays.stream(doiInput.textProperty().getValue().trim().split(",")).toList());
                    case DOI_FILE -> executeDoiInputFileCrawling();
                }
            });
        });

        //todo
        failedPdfsDeletionButton.setOnAction(actionEvent -> {
//            try {
//                FileUtils.cleanDirectory(OutputUtil.getOutputDir(input, OutputUtil.OutputOption.PDF_FAILED));
//            } catch (IOException e) {
//                outputArea.setText(e.getMessage());
//            }
        });
    }

    private void executePdfInputCrawling() {
        flushProgress();
        properties.setMaxDepth(new BigDecimal(maxDepthSpinner.getValue()));
        String message = "";
        if (pdfFile == null) {
            message = "File not selected";
        } else if (!pdfFile.exists()) {
            message = "File not exists";
        } else if (!notADirectory(pdfFile)) {
            message = "File should not be a directory";
        } else if (!acceptableExtension(pdfFile)) {
            message = "File should have one of following extensions: " + String.join(",", GlobalConstantsUi.SUPPORTED_EXTENSIONS);
        } else if (dictionary != null && !getExtension(dictionary).equalsIgnoreCase("txt")) {
            message = "dictionary should have txt extension";
        } else {
            try {
                start();
                OutputUtil.refreshOutputDir(pdfFile.getName());
                final AppCallback callback = new AppCallback(1);
                final DepthProcessorResult result = depthProcessorService.process(new FileInputStream(pdfFile), callback);
                message = manageOutput(pdfFile.getName(), result, callback);
                stop();
            } catch (Exception e) {
                message = e.getMessage();
            }
        }
        outputArea.setText(message);
    }

    private void executeDoiInputCrawling(List<String> dois) {
        flushProgress();
        properties.setMaxDepth(new BigDecimal(maxDepthSpinner.getValue()));
        String message = "";
        if (dois.isEmpty()) {
            message = "You should specify at least one DOI";
        } else if (dictionary != null && !getExtension(dictionary).equalsIgnoreCase("txt")) {
            message = "dictionary should have txt extension";
        } else {
            try {
                start();
                final AppCallback callback = new AppCallback(dois.size());
                for (String doi : dois) {
                    try {
                        callback.nextInput();
                        OutputUtil.refreshOutputDir(doi);
                        final DepthProcessorResult result = depthProcessorService.process(doi, callback);
                        message = manageOutput(doi, result, callback);
                    } catch (Exception e) {
                        log.error("Failed Processing doi: {}, error: {}", doi, e.getMessage());
                    }
                }
                stop();
            } catch (Exception e) {
                message = e.getMessage();
            }
        }
        outputArea.setText(message);
    }

    private void executeDoiInputFileCrawling() {
        flushProgress();
        properties.setMaxDepth(new BigDecimal(maxDepthSpinner.getValue()));
        String message = "";
        if (doiFile == null) {
            message = "File not selected";
        } else if (!doiFile.exists()) {
            message = "File not exists";
        } else if (!notADirectory(doiFile)) {
            message = "File should not be a directory";
        } else if (doiFile != null && !getExtension(doiFile).equalsIgnoreCase("txt")) {
            message = "doi file should have txt extension";
        } else {
            try {
                final List<String> dois = new ArrayList<>();
                final Scanner scanner = new Scanner(doiFile);
                while (scanner.hasNextLine()) {
                    dois.add(scanner.nextLine());
                }
                executeDoiInputCrawling(dois);
                return;
            } catch (FileNotFoundException e) {
                message = e.getMessage();
            }
        }
        outputArea.setText(message);
    }

    private String manageOutput(String input, DepthProcessorResult result, AppCallback callback) {
        final StringBuilder output = new StringBuilder("Result: ").append(System.lineSeparator());
        final Map<AggregatedLinkInfo, Long> map = result.getResult();

        List<AggregatedLinkInfo> keySet = map.keySet().stream().sorted(Comparator.comparing(map::get).reversed()).toList();
        if (keySet.size() > outputLimitSpinner.getValue()) {
            keySet = keySet.subList(0, outputLimitSpinner.getValue());
        }
        if (keySet.isEmpty()) {
            output.append("Unreadable input or nothing to extract.");
        } else {
            for (int i = 0; i < keySet.size(); i++) {
                final AggregatedLinkInfo info = keySet.get(i);
                output.append("Paper ").append(i + 1).append(":").append(System.lineSeparator())
                        .append("INFO: ").append(System.lineSeparator())
                        .append("TEXT REFERENCE: ").append(info.getText()).append(System.lineSeparator())
                        .append("DOI: ").append(info.getDoi()).append(System.lineSeparator())
                        .append("Occurrences: ").append(map.get(info)).append(System.lineSeparator())
                        .append("----------------------------------------------------------")
                        .append(System.lineSeparator());
            }
            for (AggregatedLinkInfo info : result.getBrokenLinks()) {
                output.append("<======================================================>").append(System.lineSeparator())
                        .append("Broken links: ").append(System.lineSeparator())
                        .append("TEXT REFERENCE: ").append(info.getText()).append(System.lineSeparator())
                        .append("DOI: ").append(info.getDoi()).append(System.lineSeparator())
                        .append("Occurrences: ").append(map.get(info)).append(System.lineSeparator())
                        .append("----------------------------------------------------------")
                        .append(System.lineSeparator());
            }
            try {
                serializer.saveData(input, keySet.stream().map(AggregatedLinkInfo::toBibtex).collect(Collectors.toList()));
                final String pdfSrc = pdfSource.getText().trim();
                final PdfSource source = pdfSrc.isEmpty() ? sciHubPdfSource : new GenericPdfSource(pdfSrc, cookiesExtractor);
                pdfDownloader.download(input, keySet, pdfsLimitSpinner.getValue(), keyWordsFilter, filtrationModeChooser.getValue(), source, callback);
            } catch (Exception e) {
                output.append(e.getMessage());
            }
        }
        return output.toString();
    }

    private void visualizeOption() {
        switch (approachChooser.getValue()) {
            case DOI -> {
                selectPdfFileButton.setVisible(false);
                selectPdfFileButton.setDisable(true);
                selectDoiFileButton.setVisible(false);
                selectDoiFileButton.setDisable(true);
                doiInput.setVisible(true);
                doiInput.setDisable(false);
            }
            case PDF -> {
                selectPdfFileButton.setVisible(true);
                selectPdfFileButton.setDisable(false);
                selectDoiFileButton.setVisible(false);
                selectDoiFileButton.setDisable(true);
                doiInput.setVisible(false);
                doiInput.setDisable(true);
            }
            case DOI_FILE -> {
                selectPdfFileButton.setVisible(false);
                selectPdfFileButton.setDisable(true);
                selectDoiFileButton.setVisible(true);
                selectDoiFileButton.setDisable(false);
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
        return Optional.of(file).filter(f -> f.getName().contains(".")).isPresent();
    }

    private String getExtension(File file) {
        return Optional.of(file).filter(f -> f.getName().contains("."))
                .map(f -> f.getName().substring(f.getName().lastIndexOf(".") + 1)).orElse("");
    }

    private boolean acceptableExtension(File file) {
        final String ext = getExtension(file);
        return GlobalConstantsUi.SUPPORTED_EXTENSIONS.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(ext));
    }

    private void start() {
        this.keyWordsFilter = new KeyWordsFilter(keyWordField.getText());
        Platform.runLater(() -> {
            failedPdfsDeletionButton.setDisable(true);
            crawlButton.setText("Stop");
        });
    }

    @Override
    public void stop() {
        this.running = false;
        depthProcessorService.stop();
        pdfDownloader.stop();
        Platform.runLater(() -> {
            failedPdfsDeletionButton.setDisable(false);
            crawlButton.setText("Crawl");
        });
    }

    public class AppCallback extends ProgressCallback {
        @Setter
        private Integer inputs;
        @Setter
        private Integer input;
        private Long minorIteration;
        private List<Long> executionTimes;
        private Long majorIteration;
        private BigDecimal depth;
        private EventType type;

        public AppCallback(Integer inputs) {
            this.inputs = inputs;
            input = 0;
            minorIteration = 0L;
            majorIteration = 0L;
            depth = BigDecimal.ZERO;
            this.executionTimes = new ArrayList<>();
        }

        public void nextInput() {
            input++;
        }

        @Override
        public void notifyMinor(Long executionTime) {
            minorIteration = ++executed;
            executionTimes.add(executionTime);
            final String processName = String.format("Input: %s/%s%s", input, inputs, System.lineSeparator()) + type.getName() + Optional.ofNullable(depth).map(d -> " " + d.toPlainString()).orElse("");
            Platform.runLater(() -> progressLabel.setText(String.format("%s: %s %s/%s %s est. time sec: %s",
                    processName,
                    System.lineSeparator(),
                    minorIteration,
                    majorIteration,
                    System.lineSeparator(),
                    calculateEstimatedExecutionTime())));
        }

        @Override
        public void notifyMajor(EventType type, Long newLayerRequestCount, BigDecimal depth) {
            flushProgress();
            if (type == EventType.FINISHED) {
                Platform.runLater(() -> progressLabel.setText(type.getName()));
                return;
            }
            majorIteration = newLayerRequestCount;
            this.executionTimes = new ArrayList<>();
            this.type = type;
            this.depth = depth;
        }

        private Long calculateEstimatedExecutionTime() {
            final long leftIterations = majorIteration - minorIteration;
            final long avgTime = executionTimes.stream().reduce(0L, Long::sum) / minorIteration;
            return avgTime * leftIterations / 1000;
        }
    }

    private enum ApproachOption {
        DOI("doi input"),
        DOI_FILE("doi file input"),
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

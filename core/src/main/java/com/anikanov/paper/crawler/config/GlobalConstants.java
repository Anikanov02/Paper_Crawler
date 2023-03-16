package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.domain.Dictionary;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GlobalConstants {
    public static final List<String> linkMatchingRegexes = new ArrayList<>();

    public static File BIBTEX_OUTPUT;

    public static File BIBTEX_FILTRATION_FAILED;

    public static File BIBTEX_FILTERED;

    public static File DOWNLOADED_PDFS_DIR;

    public static File FILTERED_PDFS_DIR;

    public static final Dictionary DICTIONARY = new Dictionary();

    public static final String basePath = new File("").getAbsolutePath();

    static {
        try {
            refreshOutputDir();
            final String filePathBuilder = basePath + "/core/src/main/resources/regexes.txt";
            final Scanner regexLoader = new Scanner(new File(filePathBuilder));
            while (regexLoader.hasNext()) {
                linkMatchingRegexes.add(regexLoader.nextLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void refreshOutputDir() throws IOException {
        BIBTEX_OUTPUT = new File(basePath + "/output/bibtex.txt");
        if (BIBTEX_OUTPUT.exists()) {
            BIBTEX_OUTPUT.delete();
        }
        BIBTEX_OUTPUT.createNewFile();

        BIBTEX_FILTRATION_FAILED = new File(basePath + "/output/bibtex-failed.txt");
        if (BIBTEX_FILTRATION_FAILED.exists()) {
            BIBTEX_FILTRATION_FAILED.delete();
        }
        BIBTEX_FILTRATION_FAILED.createNewFile();

        BIBTEX_FILTERED = new File(basePath + "/output/bibtex-filtered.txt");
        if (BIBTEX_FILTERED.exists()) {
            BIBTEX_FILTERED.delete();
        }
        BIBTEX_FILTERED.createNewFile();

        DOWNLOADED_PDFS_DIR = new File(basePath + "/output/pdfs");
        if (!DOWNLOADED_PDFS_DIR.exists()) {
            DOWNLOADED_PDFS_DIR.createNewFile();
        }

        FILTERED_PDFS_DIR = new File(basePath + "/output/filteredPdfs");
        if (!FILTERED_PDFS_DIR.exists()) {
            FILTERED_PDFS_DIR.createNewFile();
        }

        FileUtils.cleanDirectory(DOWNLOADED_PDFS_DIR);
        FileUtils.cleanDirectory(FILTERED_PDFS_DIR);
    }

    public static void applyDictionary(File file) throws Exception {
        DICTIONARY.applyNewDictionary(file);
    }

    public static final String CROSSREF_RESPONSE_STATUS_OK = "ok";

    //Bean names
    public static final String CROSSREF_DEPTH = "CrossrefDepth";
    public static final String SCHOLARS_DEPTH = "ScholarsDepth";
    public static final String GENERAL_DEPTH = "GeneralDepth";
}

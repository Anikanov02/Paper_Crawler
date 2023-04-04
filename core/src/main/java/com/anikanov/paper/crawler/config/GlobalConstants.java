package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.domain.Dictionary;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class GlobalConstants {
    public static final List<String> linkMatchingRegexes = new ArrayList<>();

    public static File BIBTEX_OUTPUT;

    public static File BIBTEX_FILTRATION_FAILED;

    public static File BIBTEX_FILTERED;

    public static File FAILED_PDFS_DIR;

    public static File FILTERED_PDFS_DIR;

    public static final Dictionary DICTIONARY = new Dictionary();

    public static final String basePath = new File("").getAbsolutePath();

    public static String jarPath;

    static {
        final Scanner regexLoader = new Scanner(GlobalConstants.class.getClassLoader().getResourceAsStream("regexes.txt"));
        while (regexLoader.hasNext()) {
            linkMatchingRegexes.add(regexLoader.nextLine());
        }
    }

    public static void setJarPath(String path) {
        jarPath = path;
    }

    public static void refreshOutputDir() throws IOException {
        final String path = Objects.isNull(jarPath) ? basePath : jarPath;
        BIBTEX_OUTPUT = new File(path + "/output/bibtex.txt");
        if (BIBTEX_OUTPUT.exists()) {
            BIBTEX_OUTPUT.delete();
        }
        BIBTEX_OUTPUT.createNewFile();

        BIBTEX_FILTRATION_FAILED = new File(path + "/output/bibtex-failed.txt");
        if (BIBTEX_FILTRATION_FAILED.exists()) {
            BIBTEX_FILTRATION_FAILED.delete();
        }
        BIBTEX_FILTRATION_FAILED.createNewFile();

        BIBTEX_FILTERED = new File(path + "/output/bibtex-filtered.txt");
        if (BIBTEX_FILTERED.exists()) {
            BIBTEX_FILTERED.delete();
        }
        BIBTEX_FILTERED.createNewFile();

        FAILED_PDFS_DIR = new File(path + "/output/failedPdfs");
        if (!FAILED_PDFS_DIR.exists()) {
            FAILED_PDFS_DIR.mkdir();
        }

        FILTERED_PDFS_DIR = new File(path + "/output/filteredPdfs");
        if (!FILTERED_PDFS_DIR.exists()) {
            FILTERED_PDFS_DIR.mkdir();
        }

        FileUtils.cleanDirectory(FAILED_PDFS_DIR);
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
    public static final int DEFAULT_REPEATING_REQUESTS_LIMIT = 3;
}

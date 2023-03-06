package com.anikanov.paper.crawler.config;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GlobalConstants {
    public static final List<String> linkMatchingRegexes = new ArrayList<>();

    public static final File BIBTEX_OUTPUT;

    public static final File DOWNLOADED_DPFS_DIR;

    public static final File FILTERED_DPFS_DIR;

    static {
        try {
            final String basePath = new File("").getAbsolutePath();
            BIBTEX_OUTPUT = new File(basePath + "/output/bibtex.txt");
            if (!BIBTEX_OUTPUT.exists()) {
                BIBTEX_OUTPUT.createNewFile();
            }
            DOWNLOADED_DPFS_DIR = new File(basePath + "/output/pdfs");
            if (!DOWNLOADED_DPFS_DIR.exists()) {
                DOWNLOADED_DPFS_DIR.createNewFile();
            }
            FILTERED_DPFS_DIR = new File(basePath + "/output/filteredPdfs");
            if (!FILTERED_DPFS_DIR.exists()) {
                FILTERED_DPFS_DIR.createNewFile();
            }

            FileUtils.cleanDirectory(DOWNLOADED_DPFS_DIR);
            FileUtils.cleanDirectory(FILTERED_DPFS_DIR);

            final String filePathBuilder = basePath + "/core/src/main/resources/regexes.txt";
            final Scanner regexLoader = new Scanner(new File(filePathBuilder));
            while (regexLoader.hasNext()) {
                linkMatchingRegexes.add(regexLoader.nextLine());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String CROSSREF_RESPONSE_STATUS_OK = "ok";

    //Bean names
    public static final String CROSSREF_DEPTH = "CrossrefDepth";
    public static final String SCHOLARS_DEPTH = "ScholarsDepth";
    public static final String GENERAL_DEPTH = "GeneralDepth";
}

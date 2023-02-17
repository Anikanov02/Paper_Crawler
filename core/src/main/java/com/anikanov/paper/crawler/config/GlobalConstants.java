package com.anikanov.paper.crawler.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GlobalConstants {
    public static final List<String> linkMatchingRegexes = new ArrayList<>();

    static {
        try {
            final String filePathBuilder = new File("").getAbsolutePath() +
                    "/core/src/main/resources/regexes.txt";
            final Scanner regexLoader = new Scanner(new File(filePathBuilder));
            while (regexLoader.hasNext()) {
                linkMatchingRegexes.add(regexLoader.nextLine());
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static final String CROSSREF_RESPONSE_STATUS_OK = "ok";

    //Bean names
    public static final String CROSSREF_DEPTH = "CrossrefDepth";
    public static final String SCHOLARS_DEPTH = "ScholarsDepth";
    public static final String GENERAL_DEPTH = "GeneralDepth";
}

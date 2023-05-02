package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.domain.Dictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GlobalConstants {
    public static final List<String> linkMatchingRegexes = new ArrayList<>();

    public static final Dictionary DICTIONARY = new Dictionary();

    static {
        final Scanner regexLoader = new Scanner(GlobalConstants.class.getClassLoader().getResourceAsStream("regexes.txt"));
        while (regexLoader.hasNext()) {
            linkMatchingRegexes.add(regexLoader.nextLine());
        }
    }

    public static void applyDictionary(File file) throws Exception {
        DICTIONARY.applyNewDictionary(file);
    }

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";
    public static final String CROSSREF_RESPONSE_STATUS_OK = "ok";

    //Bean names
    public static final String CROSSREF_DEPTH = "CrossrefDepth";
    public static final String SCHOLARS_DEPTH = "ScholarsDepth";
    public static final String GENERAL_DEPTH = "GeneralDepth";
    public static final int DEFAULT_REPEATING_REQUESTS_LIMIT = 3;
}

package com.anikanov.paper.crawler.config;

import java.util.List;

public class GlobalConstants {
    public static final String SQUARE_BRACKETS_LINK_SEPARATOR = "\\[\\d+\\]([^\\[]+)";
    public static final String ROUND_BRACKETS_LINK_SEPARATOR = "\\(\\d+\\)([^\\(]+)";
    public static final String DIGIT_DOT_LINK_SEPARATOR = "\\d+\\.(.+)";
    public static final List<String> linkMatchingRegexes = List.of(SQUARE_BRACKETS_LINK_SEPARATOR,
            ROUND_BRACKETS_LINK_SEPARATOR,
            DIGIT_DOT_LINK_SEPARATOR);
    public static final String CROSSREF_RESPONSE_STATUS_OK = "ok";

    //Bean names
    public static final String CROSSREF_DEPTH = "CrossrefDepth";
    public static final String SCHOLARS_DEPTH = "ScholarsDepth";
    public static final String GENERAL_DEPTH = "GeneralDepth";
}

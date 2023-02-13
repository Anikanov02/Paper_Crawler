package com.anikanov.paper.crawler.source.scholars.api.models;

public enum Engine {
    ORGANIC("google_scholar"),
    CITE("google_scholar_cite");

    private String code;

    Engine(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}

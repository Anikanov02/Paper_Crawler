package com.anikanov.paper.crawler.service;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
public abstract class ProgressCallback {
    public abstract void notifyMinor();
    public abstract void notifyMajor(EventType type, Long newLayerRequestCount, BigDecimal depth);

    public enum EventType {
        DEPTH("Depth"),
        APPLYING_EXTRA_DATA("Applying xtra data"),
        PDF_DOWNLOADING("Pdf downloading"),
        OTHER("Other");

        @Getter
        private String name;

        EventType(String name) {
            this.name = name;
        }
    }
}

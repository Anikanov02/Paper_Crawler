package com.anikanov.paper.crawler.service;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class ProgressCallback {
    private static final List<ProgressCallback> callbacks = new ArrayList<>();
    public abstract void callback();

    public static void registerCallback(ProgressCallback callback) {
        callbacks.add(callback);
    }
}

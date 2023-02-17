package com.anikanov.paper.crawler.service;

import lombok.Data;

import java.math.BigDecimal;

@Data
public abstract class ProgressCallback {
    public abstract void notifyMinor();
    public abstract void notifyMajor(Long newLayerRequestCount, BigDecimal depth);
}

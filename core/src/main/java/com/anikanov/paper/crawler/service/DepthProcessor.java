package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface DepthProcessor {
    Map<AggregatedLinkInfo, Long> process(InputStream inputStream, ProgressCallback callback) throws IOException;

    Map<AggregatedLinkInfo, Long> process(String doi, ProgressCallback callback);
}

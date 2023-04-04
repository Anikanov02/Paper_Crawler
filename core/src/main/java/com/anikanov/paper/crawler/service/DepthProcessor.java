package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.DepthProcessorResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface DepthProcessor extends Stoppable {
    DepthProcessorResult process(InputStream inputStream, ProgressCallback callback) throws IOException;

    DepthProcessorResult process(String doi, ProgressCallback callback);
}

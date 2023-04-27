package com.anikanov.paper.crawler.service.processor;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.DepthProcessorResult;
import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.service.Stoppable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface DepthProcessor extends Stoppable {
    DepthProcessorResult process(InputStream inputStream, ProgressCallback callback) throws IOException;

    DepthProcessorResult process(String doi, ProgressCallback callback);
}

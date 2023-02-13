package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.ProgressCallback;

import java.io.InputStream;

public interface PaperSource {
    InputStream getData(AggregatedLinkInfo info, ProgressCallback callback);
}

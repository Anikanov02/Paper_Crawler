package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;

import java.io.IOException;
import java.net.URL;

public interface PdfSource {
    URL getPaperUrl(AggregatedLinkInfo info) throws IOException;
}

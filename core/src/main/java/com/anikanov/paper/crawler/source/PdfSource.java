package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.Stoppable;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface PdfSource {
    URL getPaperUrl(AggregatedLinkInfo info, Mirror mirror) throws IOException;
    List<Mirror> matchingMirrors(AggregatedLinkInfo info);
    @Data
    @RequiredArgsConstructor
    class Mirror {
        private final String baseUrl;
        private final List<String> domains;
    }
}

package com.anikanov.paper.crawler.source.scholars;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.SourceName;
import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.source.SinglePaperSource;
import com.anikanov.paper.crawler.source.scholars.api.impl.ScholarsApiService;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsOrganicSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ScholarsPaperSource implements SinglePaperSource {
    private final ScholarsApiService apiService;
    private final AppProperties properties;

    @Override
    public InputStream getData(AggregatedLinkInfo info, ProgressCallback callback) {
        final ScholarsOrganicSearchResponse response = apiService.getOrganicResults(info.getText(), callback);
        return null;
    }

    @Override
    public SourceName getSourceName() {
        return SourceName.CROSSREF;
    }
}

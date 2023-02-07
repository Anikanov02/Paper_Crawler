package com.anikanov.paper.crawler.source.crossref;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.SinglePaperSource;
import com.anikanov.paper.crawler.source.SourceName;
import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.WorksBibliographicSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossrefPaperSource implements SinglePaperSource {
    private final CrossrefApiService apiService;

    private static final List<String> selectedFieldsForRequest = List.of("DOI", "reference");

    @Override
    public InputStream getData(AggregatedLinkInfo info) {
        try {
        final WorksBibliographicSearchRequest request = WorksBibliographicSearchRequest.builder()
                .requestText(info.getText())
                .rows(1)
                .select(String.join(",", selectedFieldsForRequest))
                .build();
            final WorksBibliographicSearchResponse response = apiService.getWork(request);
            log.info("Got response from crossref, request{} parameters:{},{} response: {}", System.lineSeparator(), request, System.lineSeparator(), response);
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SourceName getSourceName() {
        return SourceName.CROSSREF;
    }
}

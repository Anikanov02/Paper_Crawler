package com.anikanov.paper.crawler.source.crossref;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.SinglePaperSource;
import com.anikanov.paper.crawler.domain.SourceName;
import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
            final CrossrefMetadataResponse response = apiService.getWorks(request);
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

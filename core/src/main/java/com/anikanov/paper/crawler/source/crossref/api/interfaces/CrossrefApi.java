package com.anikanov.paper.crawler.source.crossref.api.interfaces;

import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;

import java.io.IOException;

public interface CrossrefApi {
    CrossrefMetadataResponse getWorks(WorksBibliographicSearchRequest request) throws IOException;

    CrossrefMetadataResponse getWork(String doi) throws IOException;
}

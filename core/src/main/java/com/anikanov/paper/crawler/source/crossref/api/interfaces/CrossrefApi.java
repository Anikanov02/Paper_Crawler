package com.anikanov.paper.crawler.source.crossref.api.interfaces;

import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.WorksBibliographicSearchResponse;

import java.io.IOException;

public interface CrossrefApi {
    WorksBibliographicSearchResponse getWork(WorksBibliographicSearchRequest request) throws IOException;
}

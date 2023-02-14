package com.anikanov.paper.crawler.source.crossref.api.interfaces;

import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;

import java.io.IOException;

public interface CrossrefApi {
    CrossrefMetadataResponse getWorks(WorksBibliographicSearchRequest request, ProgressCallback callback) throws IOException;

    CrossrefMetadataResponse.Item getWork(String doi, ProgressCallback callback) throws IOException;
}

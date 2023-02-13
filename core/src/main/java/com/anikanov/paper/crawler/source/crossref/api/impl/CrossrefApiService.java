package com.anikanov.paper.crawler.source.crossref.api.impl;

import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApi;
import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApiRetrofit;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class CrossrefApiService extends CrossrefApiRetrofitImpl<CrossrefApiRetrofit> implements CrossrefApi {
    public CrossrefApiService(String baseUrl, String apiKey, String secret) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.secret = secret;
    }

    @Override
    public CrossrefMetadataResponse getWorks(WorksBibliographicSearchRequest request) {
        final CrossrefMetadataResponse response;
        try {
            response = executeSync(getAPIImpl().getWorks(request.getRequestText(), request.getRows(), request.getSelect()));
        } catch (IOException e) {
            log.error("Error occurred (getWorks): message:{}, requesting again...", e.getMessage());
            return getWorks(request);
        }
        log(request, response);
        return response;
    }

    @Override
    public CrossrefMetadataResponse getWork(String doi) {
        final CrossrefMetadataResponse response;
        try {
            response = executeSync(getAPIImpl().getWork(doi));
        } catch (IOException e) {
            log.error("Error occurred (getWork): message:{}, requesting again...", e.getMessage());
            return getWork(doi);
        }
        log(doi, response);
        return response;
    }

    private void log(Object request, Object response) {
        log.info("Got response from crossref, request{} parameters:{},{} response: {}", System.lineSeparator(), request, System.lineSeparator(), response);
    }
}

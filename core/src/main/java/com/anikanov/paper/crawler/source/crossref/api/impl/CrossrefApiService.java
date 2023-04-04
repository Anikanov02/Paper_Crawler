package com.anikanov.paper.crawler.source.crossref.api.impl;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApi;
import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApiRetrofit;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class CrossrefApiService extends CrossrefApiRetrofitImpl<CrossrefApiRetrofit> implements CrossrefApi {
    private final int maxRepeatingRequests;

    public CrossrefApiService(String baseUrl, String apiKey, String secret, Integer maxRepeatingRequests) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.secret = secret;
        this.maxRepeatingRequests = Objects.isNull(maxRepeatingRequests) ? GlobalConstants.DEFAULT_REPEATING_REQUESTS_LIMIT : maxRepeatingRequests;
    }

    @Override
    public CrossrefMetadataResponse getWorks(WorksBibliographicSearchRequest request, ProgressCallback callback) {
        return getWorks(request, callback, 0);
    }

    private CrossrefMetadataResponse getWorks(WorksBibliographicSearchRequest request, ProgressCallback callback, int iteration) {
        final CrossrefMetadataResponse response;
        try {
            response = executeSync(getAPIImpl().getWorks(request.getRequestText(), request.getRows(), request.getSelect()));
            callback.notifyMinor();
            log(request, response);
        } catch (IOException e) {
            String message = e.getMessage();
            log.error("Error occurred (getWorks): message:{}", message);
            if ((message.contains("timeout") || message.contains("Read timed out")) && iteration <= maxRepeatingRequests) {
                log.info("timeout encountered, requesting again...");
                return getWorks(request, callback, ++iteration);
            } else {
                log.info("invalid request, skipping");
                return null;
            }
        }
        return response;
    }

    @Override
    public CrossrefMetadataResponse.Item getWork(String doi, ProgressCallback callback) {
        return getWork(doi, callback, 0);
    }

    private CrossrefMetadataResponse.Item getWork(String doi, ProgressCallback callback, int iteration) {
        final CrossrefMetadataResponse.Item response;
        try {
            response = executeSync(getAPIImpl().getWork(doi));
            callback.notifyMinor();
            log(doi, response);
        } catch (IOException e) {
            String message = e.getMessage();
            log.error("Error occurred (getWorks): message:{}", message);
            if (message.contains("timeout") && iteration <= maxRepeatingRequests) {
                log.info("timeout encountered, requesting again...");
                return getWork(doi, callback, ++iteration);
            } else {
                log.info("invalid request, skipping");
                return null;
            }
        }
        return response;
    }

    private void log(Object request, Object response) {
        log.info("Got response from crossref, request{} parameters:{},{} response: {}", System.lineSeparator(), request, System.lineSeparator(), response);
    }
}

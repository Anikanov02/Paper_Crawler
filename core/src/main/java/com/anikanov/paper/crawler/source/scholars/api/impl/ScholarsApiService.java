package com.anikanov.paper.crawler.source.scholars.api.impl;

import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.source.scholars.api.interfaces.ScholarsApi;
import com.anikanov.paper.crawler.source.scholars.api.interfaces.ScholarsApiRetrofit;
import com.anikanov.paper.crawler.source.scholars.api.models.Engine;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsMetadataResponse;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsOrganicSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ScholarsApiService extends ScholarsApiRetrofitImpl<ScholarsApiRetrofit> implements ScholarsApi {
    public ScholarsApiService(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public ScholarsOrganicSearchResponse getOrganicResults(String query, ProgressCallback callback) {
        final ScholarsOrganicSearchResponse response;
        try {
            response = executeSync(getAPIImpl().getOrganicResults(Engine.ORGANIC, query, this.apiKey));
            callback.callback();
            log(query, response);
        } catch (IOException e) {
            log.error("Error occurred (getOrganicResults): message:{}, requesting again...", e.getMessage());
            return getOrganicResults(query, callback);
        }
        return response;
    }

    @Override
    public ScholarsMetadataResponse getSearchMetadata(String query, ProgressCallback callback) {
        final ScholarsMetadataResponse response;
        try {
            response = executeSync(getAPIImpl().getMetadata(Engine.CITE, query, this.apiKey));
            callback.callback();
            log(query, response);
        } catch (IOException e) {
            log.error("Error occurred (getSearchMetadata): message:{}, requesting again...", e.getMessage());
            return getSearchMetadata(query, callback);
        }
        return response;
    }

    private void log(Object request, Object response) {
        log.info("Got response from scholars, request{} parameters:{},{} response: {}", System.lineSeparator(), request, System.lineSeparator(), response);
    }
}

package com.anikanov.paper.crawler.source.crossref.api.impl;

import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApi;
import com.anikanov.paper.crawler.source.crossref.api.interfaces.CrossrefApiRetrofit;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.WorksBibliographicSearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

public class CrossrefApiService extends CrossrefApiRetrofitImpl<CrossrefApiRetrofit> implements CrossrefApi {
    public CrossrefApiService(String baseUrl, String apiKey, String secret) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.secret = secret;
    }

    @Override
    public WorksBibliographicSearchResponse getWork(WorksBibliographicSearchRequest request) throws IOException {
        return executeSync(getAPIImpl().getWorks(request.getRequestText(), request.getRows(), request.getSelect()));
    }
}

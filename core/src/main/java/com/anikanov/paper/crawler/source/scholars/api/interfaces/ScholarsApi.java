package com.anikanov.paper.crawler.source.scholars.api.interfaces;

import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsMetadataResponse;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsOrganicSearchResponse;

import java.io.IOException;

public interface ScholarsApi {
    ScholarsOrganicSearchResponse getOrganicResults(String request) throws IOException;

    ScholarsMetadataResponse getSearchMetadata(String request) throws IOException;
}

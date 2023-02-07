package com.anikanov.paper.crawler.source.crossref.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorksBibliographicSearchRequest {
    private String requestText;
    private Integer rows;
    private String select;
}

package com.anikanov.paper.crawler.source.scholars.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScholarsSearchRequest {
    private String q;
    private String apiKey;
}

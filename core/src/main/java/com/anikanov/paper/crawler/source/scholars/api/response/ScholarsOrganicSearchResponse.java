package com.anikanov.paper.crawler.source.scholars.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScholarsOrganicSearchResponse {
    @JsonProperty("organic_results")
    private List<OrganicResponse> organicResults;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrganicResponse {
        private String title;
        @JsonProperty("result_id")
        private String resultId;
        private String snippet;
    }
}

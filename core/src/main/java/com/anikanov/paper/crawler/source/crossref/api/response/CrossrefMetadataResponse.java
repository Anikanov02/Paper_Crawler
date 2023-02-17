package com.anikanov.paper.crawler.source.crossref.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossrefMetadataResponse {
    @JsonProperty("total-results")
    private Long totalResults;

    private List<Item> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("DOI")
        private String doi;

        private List<Reference> reference;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Reference {
            private String issn;
            @JsonProperty("standards-body")
            private String standardsBody;
            private String issue;
            private String key;
            @JsonProperty("series-title")
            private String seriesTitle;
            @JsonProperty("isbn-type")
            private String isbnType;
            @JsonProperty("doi-asserted-by")
            private String doiAssertedBy;
            @JsonProperty("firstPage")
            private String firstPage;
            private String isbn;
            @JsonProperty("DOI")
            private String doi;
            private String component;
            @JsonProperty("article-title")
            private String articleTitle;
            @JsonProperty("volume-title")
            private String volumeTitle;
            private String volume;
            private String author;
            @JsonProperty("standard-designator")
            private String standardDesignator;
            private String year;
            private String unstructured;
            private String edition;
            @JsonProperty("journal-title")
            private String journalTitle;
            @JsonProperty("issn-type")
            private String issnType;
        }
    }
}

package com.anikanov.paper.crawler.source.crossref.api.response;

import com.anikanov.paper.crawler.domain.LocalDateTimeDeserialized;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;
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

        private String publisher;

        private String issue;

        private List<Author> author;

        private String volume;

        private Created created;

        @JsonProperty("short-container-title")
        private List<String> journalTitles;

        private List<String> title;

        private List<Reference> reference;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Author {
            private String given;
            private String family;

            @Override
            public String toString() {
                return given + family;
            }
        }

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

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Created {
            @JsonProperty("date-parts")
            private List<List<String>> dateParts;

            private long timestamp;

            @JsonProperty("date-time")
            @JsonDeserialize(using = LocalDateTimeDeserialized.class)
            private LocalDateTime dateTime;
        }
    }
}

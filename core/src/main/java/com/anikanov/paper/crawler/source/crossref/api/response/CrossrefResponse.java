package com.anikanov.paper.crawler.source.crossref.api.response;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrossrefResponse<T> {
    private String status;
    @JsonProperty(value = "message-type")
    private String messageType;
    @JsonProperty(value = "message-version")
    private String messageVersion;
    @JsonProperty(value = "message")
    private T data;

    public boolean isSuccessful() {
        return Objects.equals(status, GlobalConstants.CROSSREF_RESPONSE_STATUS_OK);
    }
}

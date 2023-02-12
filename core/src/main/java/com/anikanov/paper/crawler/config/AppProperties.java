package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.domain.SourceName;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "crawler")
public class AppProperties {
    private BigDecimal maxDepth;
    private List<SourceName> sourcePriorityOrder;
    private CrossrefConfig crossref;

    @Data
    public static class CrossrefConfig {
        private String baseUrl;
        private String apiKey;
        private String secret;
    }
}

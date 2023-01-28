package com.anikanov.paper.crawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "crawler")
public class AppProperties {
    private BigDecimal maxDepth;
}

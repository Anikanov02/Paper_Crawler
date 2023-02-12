package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties
public class CoreConfiguration {
    @Bean
    public CrossrefApiService crossrefApiService(AppProperties properties) {
        final AppProperties.CrossrefConfig crossrefConfig = properties.getCrossref();
        return new CrossrefApiService(crossrefConfig.getBaseUrl(), crossrefConfig.getApiKey(), crossrefConfig.getSecret());
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}

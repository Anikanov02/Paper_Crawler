package com.anikanov.paper.crawler.config;

import com.anikanov.paper.crawler.service.ProgressCallback;
import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import com.anikanov.paper.crawler.source.scholars.api.impl.ScholarsApiService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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
    public ScholarsApiService scholarsApiService(AppProperties properties) {
        final AppProperties.ScholarsConfig scholarsConfig = properties.getScholars();
        return new ScholarsApiService(scholarsConfig.getBaseUrl(), scholarsConfig.getApiKey());
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}

package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.scholars.api.impl.ScholarsApiService;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsMetadataResponse;
import com.anikanov.paper.crawler.source.scholars.api.response.ScholarsOrganicSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(GlobalConstants.SCHOLARS_DEPTH)
@RequiredArgsConstructor
public class ScholarsDepthProcessorService implements DepthProcessor {
    private final AppProperties properties;
    private final ScholarsApiService apiService;
    private final LinkExtractorService extractorService;

    @Override
    public Map<AggregatedLinkInfo, Long> process(InputStream inputStream) throws IOException {
        List<AggregatedLinkInfo> inputReferences = extractorService.extract(inputStream);
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        process(result, inputReferences, BigDecimal.ONE);
        return result.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private void process(List<AggregatedLinkInfo> result, List<AggregatedLinkInfo> input, BigDecimal depth) {
        depth = depth.add(BigDecimal.ONE);
        if (depth.compareTo(properties.getMaxDepth()) <= 0) {
            result.addAll(input);
            for (AggregatedLinkInfo link : input) {
                final ScholarsOrganicSearchResponse organicSearchResponse = apiService.getOrganicResults(link.getText());
                if (!organicSearchResponse.getOrganicResults().isEmpty()) {
                    ScholarsOrganicSearchResponse.OrganicResponse res = organicSearchResponse.getOrganicResults().get(0);
                    final ScholarsMetadataResponse response = apiService.getSearchMetadata(res.getResultId());
                    process(result, toAggregatedLinks(response), depth);
                }
            }
        }
    }

    private List<AggregatedLinkInfo> toAggregatedLinks(ScholarsMetadataResponse response) {
        return response.getCitations().stream()
                .map(ref -> {
                    final StringBuilder text = new StringBuilder()
                            .append("Title: ").append(ref.getTitle())
                            .append(" Snippet: ").append(ref.getSnippet());
                    return AggregatedLinkInfo.builder()
                            .text(text.toString())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

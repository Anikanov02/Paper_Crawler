package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.PaperSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Service
@RequiredArgsConstructor
public class LinksDepthProcessorService {
    private final AppProperties properties;
    private final LinkExtractorService extractorService;
    @Qualifier("aggregatedSource")
    private final PaperSource source;

    public Map<AggregatedLinkInfo, Long> process(InputStream inputStream) throws IOException {
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        process(result, inputStream, BigDecimal.ONE);
        return result.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private void process(List<AggregatedLinkInfo> result, InputStream inputStream, BigDecimal depth) throws IOException {
        if (depth.compareTo(properties.getMaxDepth()) <= 0) {
            depth = depth.add(BigDecimal.ONE);
            final List<AggregatedLinkInfo> paperLinks = extractorService.extract(inputStream);
            result.addAll(paperLinks);
            for (AggregatedLinkInfo info : paperLinks) {
                final InputStream childStream = source.getData(info);
                if (Objects.nonNull(childStream)) {
                    process(result, childStream, depth);
                }
            }
        }
    }
}

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

@Service("GeneralDepth")
@RequiredArgsConstructor
public class LinksDepthProcessorService implements DepthProcessor {
    private final AppProperties properties;
    private final LinkExtractorService extractorService;
    @Qualifier("aggregatedSource")
    private final PaperSource source;
    private ProgressCallback callback;

    @Override
    public Map<AggregatedLinkInfo, Long> process(InputStream inputStream, ProgressCallback callback) throws IOException {
        this.callback = callback;
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
                final InputStream childStream = source.getData(info, callback);
                if (Objects.nonNull(childStream)) {
                    process(result, childStream, depth);
                }
            }
        }
    }
}

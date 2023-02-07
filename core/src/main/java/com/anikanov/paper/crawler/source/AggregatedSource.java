package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component("aggregatedSource")
@RequiredArgsConstructor
public class AggregatedSource implements PaperSource {
    private final AppProperties properties;
    private final List<SinglePaperSource> sources;

    @Override
    public InputStream getData(AggregatedLinkInfo info) {
        for (SourceName sourceName : properties.getSourcePriorityOrder()) {
            final Optional<SinglePaperSource> sourceOpt = sources.stream()
                    .filter(singlePaperSource -> singlePaperSource.getSourceName().equals(sourceName)).findAny();
            if (sourceOpt.isEmpty()) {
                continue;
            }
            final SinglePaperSource source = sourceOpt.get();

            final InputStream stream = source.getData(info);
            if (Objects.nonNull(stream)) {
                return stream;
            }
        }
        return null;
    }
}

package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service(GlobalConstants.CROSSREF_DEPTH)
@RequiredArgsConstructor
public class CrossrefDepthProcessorService implements DepthProcessor {
    private final AppProperties properties;
    private final CrossrefApiService apiService;
    private final LinkExtractorService extractorService;
    private final ExecutorService executorService;
    private ProgressCallback callback;

    @Override
    public Map<AggregatedLinkInfo, Long> process(InputStream inputStream, ProgressCallback callback) throws IOException {
        this.callback = callback;
        List<AggregatedLinkInfo> inputReferences = extractorService.extract(inputStream);
        inputReferences = enrichLinksAndFilterIrrelevant(inputReferences);
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        process(result, inputReferences, BigDecimal.ZERO);
        return result.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private void process(List<AggregatedLinkInfo> result, List<AggregatedLinkInfo> input, BigDecimal depth) {
        depth = depth.add(BigDecimal.ONE);
        if (depth.compareTo(properties.getMaxDepth()) <= 0) {
            result.addAll(input);
            for (AggregatedLinkInfo link : input) {
                if (Objects.nonNull(link.getDoi())) {
                    final CrossrefMetadataResponse.Item response = apiService.getWork(link.getDoi(), callback);
                    if (Objects.nonNull(response)) {
                        if (Objects.nonNull(response.getReference())) {
                            process(result, toAggregatedLinks(response), depth);
                        }
                    } else {
                        result.remove(link);
                    }
                }
            }
        }
    }

    private void processParallel(List<AggregatedLinkInfo> result, List<AggregatedLinkInfo> input, BigDecimal depth) throws IOException {
        depth = depth.add(BigDecimal.ONE);
        if (depth.compareTo(properties.getMaxDepth()) <= 0) {
            result.addAll(input);
            final List<Future<CrossrefMetadataResponse.Item>> responseFutures = new ArrayList<>();
            for (AggregatedLinkInfo link : input) {
                if (Objects.nonNull(link.getDoi())) {
                    final Future<CrossrefMetadataResponse.Item> respFuture = executorService.submit(() -> apiService.getWork(link.getDoi(), callback));
                    responseFutures.add(respFuture);
                }
            }

            final List<CrossrefMetadataResponse.Item> responses = responseFutures.stream().map(respF -> {
                try {
                    return respF.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            for (CrossrefMetadataResponse.Item response : responses) {
                processParallel(result, toAggregatedLinks(response), depth);
            }
        }
    }

    private List<AggregatedLinkInfo> toAggregatedLinks(CrossrefMetadataResponse.Item item) {
        return item.getReference().stream()
                .filter(ref -> Objects.nonNull(ref.getDoi()))
                .map(ref -> {
                    final StringBuilder text = new StringBuilder()
                            .append("Article title: ").append(ref.getArticleTitle())
                            .append(" Author: ").append(ref.getAuthor())
                            .append(" Year: ").append(ref.getYear())
                            .append(" Journal title: ").append(ref.getJournalTitle())
                            .append(" doi: ").append(ref.getDoi());
                    return AggregatedLinkInfo.builder()
                            .text(text.toString())
                            .doi(ref.getDoi())
                            .build();
                })
                .collect(Collectors.toList());
    }

    //extracts DOIs for input links
    private List<AggregatedLinkInfo> enrichLinksAndFilterIrrelevant(List<AggregatedLinkInfo> links) throws IOException {
        final List<AggregatedLinkInfo> result = new ArrayList<>(links);
        log.info("Applying DOIs to input links");
        for (AggregatedLinkInfo info : result) {
            final WorksBibliographicSearchRequest request = WorksBibliographicSearchRequest.builder()
                    .requestText(info.getText())
                    .rows(1)
                    .select("DOI")
                    .build();
            final CrossrefMetadataResponse response = apiService.getWorks(request, callback);
            if (Objects.isNull(response.getItems()) || response.getItems().isEmpty()) {
                //nothing found then remove
                result.remove(info);
            } else {
                info.setDoi(response.getItems().get(0).getDoi());
            }
        }
        return result.stream().distinct().collect(Collectors.toList());
    }
}

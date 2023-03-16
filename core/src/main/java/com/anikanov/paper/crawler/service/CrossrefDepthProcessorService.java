package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.AppProperties;
import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.DepthProcessorResult;
import com.anikanov.paper.crawler.source.crossref.api.impl.CrossrefApiService;
import com.anikanov.paper.crawler.source.crossref.api.request.WorksBibliographicSearchRequest;
import com.anikanov.paper.crawler.source.crossref.api.response.CrossrefMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
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

    @Override
    public DepthProcessorResult process(InputStream inputStream, ProgressCallback callback) throws IOException {
        List<AggregatedLinkInfo> inputReferences = extractorService.extract(inputStream);
        inputReferences = enrichLinksAndFilterIrrelevant(inputReferences, callback);
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        final List<AggregatedLinkInfo> encounteredInstances = new ArrayList<>(result);
        final List<AggregatedLinkInfo> brokenLinks = new ArrayList<>();
        process(result, inputReferences, encounteredInstances, brokenLinks, BigDecimal.ONE, callback);
        return DepthProcessorResult.builder()
                .brokenLinks(brokenLinks)
                .result(result.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
                .build();
    }

    @Override
    public DepthProcessorResult process(String doi, ProgressCallback callback) {
        callback.notifyMajor(ProgressCallback.EventType.DEPTH, 1L, BigDecimal.ONE);
        final CrossrefMetadataResponse.Item response = apiService.getWork(doi, callback);
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        final List<AggregatedLinkInfo> encounteredInstances = new ArrayList<>();
        final List<AggregatedLinkInfo> brokenLinks = new ArrayList<>();
        process(result, toAggregatedLinks(response), encounteredInstances, brokenLinks, BigDecimal.ONE, callback);
        return DepthProcessorResult.builder()
                .brokenLinks(brokenLinks)
                .result(result.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())))
                .build();
    }

    private void process(List<AggregatedLinkInfo> result,
                         List<AggregatedLinkInfo> input,
                         List<AggregatedLinkInfo> encounteredInstances,
                         List<AggregatedLinkInfo> brokenLinks,
                         BigDecimal depth, ProgressCallback callback) {
        depth = depth.add(BigDecimal.ONE);
        result.addAll(input);
        if (depth.compareTo(properties.getMaxDepth()) <= 0) {
            List<AggregatedLinkInfo> layerData = new ArrayList<>();
            input = input.stream().distinct().filter(link -> !encounteredInstances.contains(link)).collect(Collectors.toList());
            encounteredInstances.addAll(input);
            callback.notifyMajor(ProgressCallback.EventType.DEPTH, (long) input.size(), depth);
            for (AggregatedLinkInfo link : input) {
                if (Objects.nonNull(link.getDoi())) {
                    final CrossrefMetadataResponse.Item response = apiService.getWork(link.getDoi(), callback);
                    if (Objects.nonNull(response)) {
                        if (Objects.nonNull(response.getReference())) {
                            layerData.addAll(toAggregatedLinks(response));
                        }
                    } else {
                        brokenLinks.add(link);
                    }
                }
            }
            process(result, layerData, encounteredInstances, brokenLinks, depth, callback);
        }
    }

    private void processParallel(List<AggregatedLinkInfo> result, List<AggregatedLinkInfo> input, BigDecimal depth, ProgressCallback callback) throws IOException {
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
                processParallel(result, toAggregatedLinks(response), depth, callback);
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
//                            .text(text.toString())
                            .doi(ref.getDoi())
                            .build();
                })
                .collect(Collectors.toList());
    }

    //extracts DOIs for input links
    private List<AggregatedLinkInfo> enrichLinksAndFilterIrrelevant(List<AggregatedLinkInfo> links, ProgressCallback callback) throws IOException {
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        log.info("Applying DOIs to input links");
        callback.notifyMajor(ProgressCallback.EventType.DEPTH, (long) links.size(), BigDecimal.ONE);
        for (AggregatedLinkInfo info : links) {
            final WorksBibliographicSearchRequest request = WorksBibliographicSearchRequest.builder()
                    .requestText(info.getText())
                    .rows(1)
                    .select("DOI")
                    .build();
            final CrossrefMetadataResponse response = apiService.getWorks(request, callback);
            if (Objects.nonNull(response) && Objects.nonNull(response.getItems()) && !response.getItems().isEmpty()) {
                result.add(info);
                info.setDoi(response.getItems().get(0).getDoi());
            }
        }
        return result.stream().distinct().collect(Collectors.toList());
    }

    private void applyExtraData(AggregatedLinkInfo link, CrossrefMetadataResponse.Item item) {
        final StringBuilder text = new StringBuilder();
        final String title = Optional.ofNullable(item.getTitle()).map(titles -> String.join(" ", titles)).orElse("");
        final String publisher = Optional.ofNullable(item.getPublisher()).orElse("");
        final List<String> authors = Optional.ofNullable(item.getAuthor()).map(authrs -> authrs.stream().map(CrossrefMetadataResponse.Item.Author::getFamily).collect(Collectors.toList())).orElse(Collections.emptyList());
        final Integer year = Optional.ofNullable(item.getCreated()).flatMap(created -> Optional.ofNullable(created.getDateTime()).map(LocalDateTime::getYear)).orElse(null);
        final String issue = Optional.ofNullable(item.getIssue()).orElse("");
        final Integer month = Optional.ofNullable(item.getCreated()).flatMap(created -> Optional.ofNullable(created.getDateTime()).map(dt-> dt.getMonth().getValue())).orElse(null);
        final String volume = Optional.ofNullable(item.getVolume()).orElse("");
//        final String pages = Optional.ofNullable().orElse();//?
        final String journalTitle = Optional.ofNullable(item.getJournalTitles()).map((titles) -> titles.isEmpty() ? "" : titles.get(0)).orElse("");
        text.append("Title: ").append(title)
                .append(", Publisher: ").append(publisher);
        link.setText(text.toString());
        link.setTitle(title);
        link.setPublisher(publisher);
        link.setAuthors(authors);
        link.setYear(year);
        link.setIssue(issue);
        link.setMonth(month);
        link.setVolume(volume);
//        link.setPages(pages);
        link.setJournalTitle(journalTitle);
    }

    public void enrichWithTitle(List<AggregatedLinkInfo> links, ProgressCallback callback) {
        callback.notifyMajor(ProgressCallback.EventType.APPLYING_EXTRA_DATA, (long) links.size(), null);
        links.stream().forEach(link -> {
            final CrossrefMetadataResponse.Item item = apiService.getWork(link.getDoi(), callback);
            applyExtraData(link, item);
        });
    }
}

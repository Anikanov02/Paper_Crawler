package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkExtractorService {
    public List<AggregatedLinkInfo> extract(InputStream inputStream) throws IOException {
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        final PdfReader reader = new PdfReader(inputStream);

        for (int i = 1; i <= reader.getNumberOfPages(); ++i) {
            TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
            String text = PdfTextExtractor.getTextFromPage(reader, i, strategy);
            final List<PdfAnnotation.PdfImportedLink> links = reader.getLinks(i);
            result.addAll(extractLinksFromPageIfExist(text, links));
        }

        reader.close();
        return result;
    }

    private List<AggregatedLinkInfo> extractLinksFromPageIfExist(String pageText, List<PdfAnnotation.PdfImportedLink> links) {
        List<String> textLinks = new ArrayList<>();
        for (String regex : GlobalConstants.linkMatchingRegexes) {
            final List<String> potentialResult = new ArrayList<>();
            final Matcher matcher = Pattern.compile(regex).matcher(pageText);
            while (matcher.find()) {
                potentialResult.add(matcher.group(1));
            }
            if (potentialResult.size() > textLinks.size()) {
                textLinks = potentialResult;
            }
        }
        return applyLinks(textLinks, links);
    }

    private List<AggregatedLinkInfo> applyLinks(List<String> textLinks, List<PdfAnnotation.PdfImportedLink> links) {
        return textLinks.stream().map(textLink -> {
            final PdfAnnotation.PdfImportedLink link = links.stream().filter(l -> {
                String linkText = new String(l.getParameters().get(PdfName.ACTUALTEXT).getBytes());
                return linkText.contains(textLink.trim());
            }).findAny().orElse(null);
            return AggregatedLinkInfo.builder()
                    .text(textLink)
                    .link(link)
                    .build();
        }).collect(Collectors.toList());
    }
}

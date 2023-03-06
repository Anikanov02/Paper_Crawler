package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
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
    private static final int MAX_LINK_LENGTH = 1000;
    private final PdfFullTextExtractor fullTextExtractor;
    public List<AggregatedLinkInfo> extract(InputStream inputStream) throws IOException {
        List<AggregatedLinkInfo> result = new ArrayList<>();
        final String fullText = fullTextExtractor.getText(inputStream);

        for (String regex : GlobalConstants.linkMatchingRegexes) {
            final List<AggregatedLinkInfo> regexSpecificResult = new ArrayList<>(extractLinksFromPageIfExist(fullText, regex));
            if (regexSpecificResult.size() > result.size()) {
                result = regexSpecificResult;
            }
        }
        return result;
    }

    private List<AggregatedLinkInfo> extractLinksFromPageIfExist(String pageText, String regex) {
        List<String> textLinks = new ArrayList<>();
        final Matcher matcher = Pattern.compile(regex).matcher(pageText);
        long currRef = 1;
        while (matcher.find()) {
            final String indexSpecificRegex = regex.replace("\\d+", String.valueOf(currRef));
            if (matcher.group(0).matches(indexSpecificRegex)) {
                currRef++;
                String text = matcher.group(1);
                if (text.length() > MAX_LINK_LENGTH) {
                    text = text.substring(0, MAX_LINK_LENGTH - 1);
                }
                //todo add to map
                textLinks.add(text);
            }
        }
        return textLinks.stream().map(txtLink -> AggregatedLinkInfo.builder()
                .text(txtLink)
                .build()).collect(Collectors.toList());
    }
}

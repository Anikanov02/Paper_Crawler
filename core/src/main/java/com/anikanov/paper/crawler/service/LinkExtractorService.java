package com.anikanov.paper.crawler.service;

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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LinkExtractorService {
    public List<AggregatedLinkInfo> extract(InputStream inputStream) throws IOException {
        final List<AggregatedLinkInfo> result = new ArrayList<>();
        final PdfReader reader = new PdfReader(inputStream);

        for (int i = 1; i <= reader.getNumberOfPages(); ++i) {
            TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
            String text = PdfTextExtractor.getTextFromPage(reader, i, strategy);
            result.addAll(extractLinksFromPageIfExist(text));
        }

        reader.close();
        return result;
    }

    private List<AggregatedLinkInfo> extractLinksFromPageIfExist(String pageText) {
        final Matcher linkMatcher = Pattern.compile("").matcher(pageText);
        return Collections.emptyList();
    }
}

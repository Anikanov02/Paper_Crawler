package com.anikanov.paper.crawler.source.sciHub;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.cookie.ChromeCookiesExtractor;
import com.anikanov.paper.crawler.source.PdfSource;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SciHubPdfSource implements PdfSource {
    private final List<Mirror> mirrors = List.of(new Mirror("https://www.sci-hub.se/", List.of(".sci-hub.se", ".www.sci-hub.se")),
            new Mirror("https://sci-hub.st/", List.of(".sci-hub.st", ".www.sci-hub.st")),
            new Mirror("https://sci-hub.shop/", List.of(".sci-hub.shop", ".www.sci-hub.shop")),
            new Mirror("https://sci-hub.ee/", List.of(".sci-hub.ee", ".www.sci-hub.ee")),
            new Mirror("https://sci.hubg.org/", List.of(".sci.hubg.org", ".www.sci.hubg.org")),
            new Mirror("https://sci-hub.wf/", List.of(".sci-hub.wf", ".www.sci-hub.wf")));
    private final ChromeCookiesExtractor cookiesExtractor;

    @Override
    public URL getPaperUrl(AggregatedLinkInfo info, Mirror mirror) throws IOException {
        final Document doc = Objects.isNull(mirror) ? null : Jsoup.connect(mirror.getBaseUrl() + info.getDoi()).cookies(fetchCookies(mirror)).get();
        if (Objects.nonNull(doc) && doc.getElementById("article") != null && doc.getElementById("article").getElementById("pdf") != null) {
            final Element pdf = doc.getElementById("article").getElementById("pdf");
            final String src = pdf.attributes().get("src");
            return resolveSource(mirror, src);
        }
        return null;
    }

    private URL resolveSource(Mirror mirror, String src) throws MalformedURLException {
        final URL context = new URL(mirror.getBaseUrl());
        return src.startsWith("//") ? new URL(context, src) :
                src.startsWith("http") || src.startsWith("ftp") || src.startsWith("file")  ? new URL(src) :
                        new URL(mirror.getBaseUrl() + src);
    }

    @Override
    public List<Mirror> matchingMirrors(AggregatedLinkInfo info) {
        return mirrors.stream().filter(mirror -> {
            try {
                final Document doc = Jsoup.connect(mirror.getBaseUrl() + info.getDoi()).cookies(fetchCookies(mirror)).get();
                return doc.getElementById("article") != null && doc.getElementById("article").getElementById("pdf") != null;
            } catch (IOException e) {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private Map<String, String> fetchCookies(Mirror mirror) {
        return mirror.getDomains().stream().flatMap(domain -> cookiesExtractor.getCookies(domain).stream())
                .collect(Collectors.toMap(ChromeCookiesExtractor.DecryptedCookie::getName, ChromeCookiesExtractor.DecryptedCookie::getDecryptedValue, (existing, replacement) -> replacement));
    }
}

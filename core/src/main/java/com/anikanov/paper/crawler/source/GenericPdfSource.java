package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.cookie.ChromeCookiesExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenericPdfSource implements PdfSource {
    private final Mirror mirror;
    private final ChromeCookiesExtractor cookiesExtractor;

    public GenericPdfSource(String baseUrl, ChromeCookiesExtractor cookiesExtractor) {
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.cookiesExtractor = cookiesExtractor;
        this.mirror = new Mirror(baseUrl, resolveDomains(baseUrl));
    }

    @Override
    public URL getPaperUrl(AggregatedLinkInfo info, Mirror mirror) throws IOException {
        final Map<String, String> cookies = fetchCookies();
        final URL base = new URL(mirror.getBaseUrl());
        final Document doc = Jsoup.connect(mirror.getBaseUrl() + info.getDoi()).cookies(cookies).get();
        final Elements pdfs = doc.select("[type=application/pdf]");
        if (!pdfs.isEmpty()) {
            final Element pdf = pdfs.get(0);
            final String src = pdf.attributes().get("src");
            final String path = src.contains("http") ? src : base.getProtocol() + ":" + src;
            return new URL(path);
        } else {
            return null;
        }
    }

    @Override
    public List<Mirror> matchingMirrors(AggregatedLinkInfo info) {
        return List.of(mirror);
    }

    private List<String> resolveDomains(String baseUrl) {
        String normalized = baseUrl
                .replace("http://", "")
                .replace("https://", "")
                .replace("www.", "");
        normalized = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
        return List.of(".www." + normalized, "." + normalized);
    }

    private Map<String, String> fetchCookies() {
        return mirror.getDomains().stream().flatMap(domain -> cookiesExtractor.getCookies(domain).stream())
                .collect(Collectors.toMap(ChromeCookiesExtractor.DecryptedCookie::getName, ChromeCookiesExtractor.DecryptedCookie::getDecryptedValue, (existing, replacement) -> replacement));
    }
}

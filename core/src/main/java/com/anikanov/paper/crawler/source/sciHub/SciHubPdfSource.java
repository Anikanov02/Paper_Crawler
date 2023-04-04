package com.anikanov.paper.crawler.source.sciHub;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.service.ChromeCookiesExtractor;
import com.anikanov.paper.crawler.source.PdfSource;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.CookieStore;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SciHubPdfSource implements PdfSource {
    private static final String BASE_URL = "https://www.sci-hub.se/";
    private static List<String> domains = List.of(".sci-hub.se", ".www.sci-hub.se");
    private final ChromeCookiesExtractor cookiesExtractor;

    @Override
    public URL getPaperUrl(AggregatedLinkInfo info) throws IOException {
        final Map<String, String> cookies = fetchCookies();
        final Document doc = Jsoup.connect(BASE_URL + info.getDoi()).cookies(cookies).get();
        final Element pdf = doc.getElementById("article").getElementById("pdf");
        return new URL("https:" + pdf.attributes().get("src"));
    }

    private Map<String, String> fetchCookies() {
        return domains.stream().flatMap(domain -> cookiesExtractor.getCookies(domain).stream())
                .collect(Collectors.toMap(ChromeCookiesExtractor.DecryptedCookie::getName, ChromeCookiesExtractor.DecryptedCookie::getDecryptedValue, (existing, replacement) -> replacement));
    }
}

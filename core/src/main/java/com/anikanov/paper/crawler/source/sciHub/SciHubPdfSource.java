package com.anikanov.paper.crawler.source.sciHub;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.PdfSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

@Service
public class SciHubPdfSource implements PdfSource {
    private static final String BASE_URL = "https://www.sci-hub.se/";

    @Override
    public URL getPaperUrl(AggregatedLinkInfo info) throws IOException {
            final Document doc = Jsoup.connect(BASE_URL + info.getDoi()).get();
            final Element pdf = doc.getElementById("article").getElementById("pdf");
            return new URL("https:" + pdf.attributes().get("src"));
    }
}

package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

@AllArgsConstructor
public class GenericPdfSource implements PdfSource {
    private String baseUrl;

    @Override
    public URL getPaperUrl(AggregatedLinkInfo info) throws IOException {
        final URL base = new URL(baseUrl);
        final Document doc = Jsoup.connect(baseUrl + info.getDoi()).get();
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
}

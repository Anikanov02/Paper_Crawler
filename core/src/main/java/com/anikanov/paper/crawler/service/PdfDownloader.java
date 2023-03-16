package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.KeyWordsFilter;
import com.anikanov.paper.crawler.source.PdfSource;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PdfDownloader {
    private static final String DOI_ORG_BASE_URL = "https://www.doi.org/";
    private final PdfFullTextExtractor fullTextExtractor;

    private final BibTexSerializer serializer;

    private static final List<String> illegalCharacters = List.of();

    public void download(List<AggregatedLinkInfo> links, Integer limit, KeyWordsFilter keyWordsFilter, FiltrationOption filtrationOption, PdfSource source, ProgressCallback callback) throws IOException, DocumentException {
        callback.notifyMajor(ProgressCallback.EventType.LINKS_PROCESSING, (long) links.size(), null);
        try (final FileWriter filteredWriter = new FileWriter(GlobalConstants.BIBTEX_FILTERED, true);
             final FileWriter filtrationFailedWriter = new FileWriter(GlobalConstants.BIBTEX_FILTRATION_FAILED, true)) {
            int downloaded = 0;
            for (AggregatedLinkInfo link : links) {
                File downloadedFile;
                callback.notifyMinor();
                if (keyWordsFilter.accepts(link.getTitle())) {
                    downloadedFile = download(link, source.getPaperUrl(link), GlobalConstants.FILTERED_PDFS_DIR);
                    downloaded++;
                    serializer.saveData(List.of(link.toBibtex()), filteredWriter);
//                    callback.notifyMinor();
                } else {
                    final String html = fetchFullPageHtml(DOI_ORG_BASE_URL + link.getDoi());
                    if (keyWordsFilter.accepts(html)) {
                        //FILTERED
                        downloadedFile = download(link, source.getPaperUrl(link), GlobalConstants.FILTERED_PDFS_DIR);
                        downloaded++;
                        serializer.saveData(List.of(link.toBibtex()), filteredWriter);
//                        callback.notifyMinor();
                        continue;
                    }
                    if (filtrationOption == FiltrationOption.ADVANCED) {
                        downloadedFile = download(link, source.getPaperUrl(link), GlobalConstants.DOWNLOADED_PDFS_DIR);
                        downloaded++;
//                        callback.notifyMinor();
                        final String fulltext = fullTextExtractor.getText(new FileInputStream(downloadedFile));
                        if (keyWordsFilter.accepts(fulltext)) {
                            FileCopyUtils.copy(downloadedFile, new File(GlobalConstants.FILTERED_PDFS_DIR, normalizeName(link.getTitle()) + ".pdf"));
                            serializer.saveData(List.of(link.toBibtex()), filteredWriter);
                            downloadedFile.delete();//delete from general directory
                            continue;
                        }
                    }
                    serializer.saveData(List.of(link.toBibtex()), filtrationFailedWriter);
                }
                if (downloaded >= limit) {
                    break;
                }
            }
        }
    }

    public File download(AggregatedLinkInfo info, URL url, File directory) throws IOException, DocumentException {
        final InputStream in = url.openStream();
        final String fileName = normalizeName(info.getTitle());
        final File downloaded = new File(directory, fileName + ".pdf");
        final FileOutputStream fos = new FileOutputStream(downloaded, false);
        final Document doc = new Document();
        final PdfCopy writer = new PdfCopy(doc, fos);
        doc.open();
        final PdfReader reader = new PdfReader(in);
        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
            PdfImportedPage page = writer.getImportedPage(reader, i);
            writer.addPage(page);
        }
        doc.close();
        return downloaded;
    }

    private String normalizeName(String initial) {
        String normalized = initial;

        return initial.replace("<", "")
                .replace(">", "")
                .replace(".", "")
                .replace("$", "")
                .replace("+", "")
                .replace("#", "")
                .replace("&", "")
                .replace("'", "")
                .replace("|", "")
                .replace("=", "");
    }

    private String fetchFullPageHtml(String initialUrl) throws IOException {
        return Jsoup.connect(getFinalURL(initialUrl)).get().html();
    }

    private String getFinalURL(String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        connection.getInputStream();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = connection.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        return url;
    }

    public enum FiltrationOption {
        GENERAL("general filtration"),
        ADVANCED("advanced filtration");

        private String code;

        FiltrationOption(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }
}

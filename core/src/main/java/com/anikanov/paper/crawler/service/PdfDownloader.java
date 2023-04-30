package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.domain.KeyWordsFilter;
import com.anikanov.paper.crawler.source.PdfSource;
import com.anikanov.paper.crawler.util.OutputUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jbibtex.BibTeXObject;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfDownloader implements Stoppable {
    private static final String DOI_ORG_BASE_URL = "https://www.doi.org/";
    private final PdfFullTextExtractor fullTextExtractor;
    private final BibTexSerializer serializer;

    private boolean running = false;

    public void download(String input, List<AggregatedLinkInfo> links, Integer limit, KeyWordsFilter keyWordsFilter, FiltrationOption filtrationOption, PdfSource source, ProgressCallback callback) throws IOException {
        running = true;
        callback.notifyMajor(ProgressCallback.EventType.LINKS_PROCESSING, (long) links.size(), null);
        try (final FileWriter filteredWriter = new FileWriter(OutputUtil.getOutputDir(input, OutputUtil.OutputOption.BIBTEX_FILTERED), true);
             final FileWriter filtrationFailedWriter = new FileWriter(OutputUtil.getOutputDir(input, OutputUtil.OutputOption.BIBTEX_FAILED), true);
             final FileWriter exceptionFailedWriter = new FileWriter(OutputUtil.getOutputDir(input, OutputUtil.OutputOption.BIBTEX_EXCEPTION_FAILED), true)) {
            int downloaded = 0;
            final List<BibTeXObject> filtered = new ArrayList<>();
            final List<BibTeXObject> failed = new ArrayList<>();
            final List<BibTeXObject> exceptionFailed = new ArrayList<>();
            for (AggregatedLinkInfo link : links) {
                long startTime = System.currentTimeMillis();
                try {
                    if (!running) {
                        break;
                    }
                    File downloadedFile = null;
                    if (keyWordsFilter.accepts(link.getTitle())) {
                        filtered.add(link.toBibtex());
                        final URL url = source.getPaperUrl(link);
                        if (url != null) {
                            downloadedFile = download(link, url, OutputUtil.getOutputDir(input, OutputUtil.OutputOption.PDF_FILTERED));
                        }
                        downloaded++;
                        callback.notifyMinor(System.currentTimeMillis() - startTime);
                    } else {
                        String html;
                        boolean abstractFetched;
                        try {
                            html = fetchFullPageHtml(DOI_ORG_BASE_URL + link.getDoi());
                            abstractFetched = true;
                        } catch (Exception e) {
                            html = "";
                            abstractFetched = false;
                            log.error("error fetching abstract DOI: {}, error: {}, WILL download pdf for fulltext search instead",link.getDoi(), e.getMessage());
                        }
                        if (keyWordsFilter.accepts(link.getTitle() + html)) {
                            //FILTERED
                            filtered.add(link.toBibtex());
                            final URL url = source.getPaperUrl(link);
                            if (url != null) {
                                downloadedFile = download(link, url, OutputUtil.getOutputDir(input, OutputUtil.OutputOption.PDF_FILTERED));
                            }
                            downloaded++;
                            continue;
                        }
                        if (filtrationOption == FiltrationOption.ADVANCED || !abstractFetched) {
                            final URL url = source.getPaperUrl(link);
                            if (url != null) {
                                downloadedFile = download(link, url, OutputUtil.getOutputDir(input, OutputUtil.OutputOption.PDF_FAILED));
                            }
                            downloaded++;
                            if (downloadedFile != null) {
                                final String fulltext = fullTextExtractor.getText(new FileInputStream(downloadedFile));
                                if (keyWordsFilter.accepts(link.getTitle() + html + fulltext)) {
                                    filtered.add(link.toBibtex());
                                    FileCopyUtils.copy(downloadedFile, new File(OutputUtil.getOutputDir(input, OutputUtil.OutputOption.PDF_FILTERED), OutputUtil.normalizeName(link.getTitle()) + ".pdf"));
                                    downloadedFile.delete();//delete from general directory
                                    callback.notifyMinor(System.currentTimeMillis() - startTime);
                                    continue;
                                }
                            }
                        }
                        failed.add(link.toBibtex());
                        callback.notifyMinor(System.currentTimeMillis() - startTime);
                    }
                    if (downloaded >= limit) {
                        break;
                    }
                    //handling too many requests exception (429)
                    Thread.sleep(1500);
                } catch (Exception e) {
                    log.error("input DOI: {}, error: {}",link.getDoi(), e.getMessage());
                    exceptionFailed.add(link.toBibtex());
                    callback.notifyMinor(System.currentTimeMillis() - startTime);
                }
            }
            serializer.saveData(filtered, filteredWriter);
            serializer.saveData(failed, filtrationFailedWriter);
            serializer.saveData(exceptionFailed, exceptionFailedWriter);
            callback.notifyMajor(ProgressCallback.EventType.FINISHED, null, null);
            stop();
        }
    }

    public File download(AggregatedLinkInfo info, URL url, File directory) throws IOException, DocumentException {
        final InputStream in = url.openStream();
        final String fileName = OutputUtil.normalizeName(info.getTitle());
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

    private String fetchFullPageHtml(String initialUrl) throws IOException {
        return Jsoup.connect(getFinalURL(initialUrl)).get().html();
    }

    private String getFinalURL(String inputUrl) throws IOException {
        final URL url = new URL(inputUrl);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        connection.getInputStream();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String location = connection.getHeaderField("Location");
            location = location.startsWith("/") ? url.getProtocol() + "://" + url.getHost() + location : location;
            return getFinalURL(location);
        }
        return inputUrl;
    }

    @Override
    public void stop() {
        running = false;
    }

    public enum FiltrationOption {
        GENERAL("fast(title + abstract)"),
        ADVANCED("slow(title + abstract + text)");

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

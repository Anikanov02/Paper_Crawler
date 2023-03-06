package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.GlobalConstants;
import com.anikanov.paper.crawler.domain.AggregatedLinkInfo;
import com.anikanov.paper.crawler.source.PdfSource;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfDownloader {
    private final PdfFullTextExtractor fullTextExtractor;

    public void download(List<AggregatedLinkInfo> links, String keyWord, PdfSource source, ProgressCallback callback) throws IOException, DocumentException {
        callback.notifyMajor(ProgressCallback.EventType.PDF_DOWNLOADING, (long) links.size(), null);
        for (AggregatedLinkInfo link : links) {
            if (!link.getTitle().toLowerCase().contains(keyWord.toLowerCase())) {
                final File downloadedFile = download(link, source.getPaperUrl(link));
                callback.notifyMinor();
                final String fulltext = fullTextExtractor.getText(new FileInputStream(downloadedFile));
                if (fulltext.toLowerCase().contains(keyWord.toLowerCase())) {
                    FileCopyUtils.copy(downloadedFile, new File(GlobalConstants.FILTERED_DPFS_DIR, link.getTitle() + ".pdf"));
                }
            }
        }
    }

    public File download(AggregatedLinkInfo info, URL url) throws IOException, DocumentException {
        final InputStream in = url.openStream();
        final File downloaded = new File(GlobalConstants.DOWNLOADED_DPFS_DIR, info.getTitle() + ".pdf");
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
}

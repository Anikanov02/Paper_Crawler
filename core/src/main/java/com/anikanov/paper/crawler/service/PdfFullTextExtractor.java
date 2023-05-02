package com.anikanov.paper.crawler.service;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class PdfFullTextExtractor {
    public String getText(InputStream stream) throws IOException {
//        final PdfReader reader = new PdfReader(stream);
//
//        final TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
//        final StringBuilder fullText = new StringBuilder();
//        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
//            fullText.append(PdfTextExtractor.getTextFromPage(reader, i, strategy));
//        }
//        reader.close();
//        stream.close();
//        return fullText.toString();
        try (stream; PDDocument document = PDDocument.load(stream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            return stripper.getText(document);
        }
    }
}

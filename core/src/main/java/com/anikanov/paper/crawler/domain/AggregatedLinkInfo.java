package com.anikanov.paper.crawler.domain;

import com.itextpdf.text.pdf.PdfAnnotation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbibtex.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregatedLinkInfo {
    private String text;
    private String title;
    private String publisher;
    private String doi;
    private List<String> authors;
    private Integer year;
    private String issue;
    private String journalTitle;
    private Integer month;
    private String pages;
    private String volume;
    private PdfAnnotation.PdfImportedLink link;
    private Path file;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedLinkInfo info = (AggregatedLinkInfo) o;
        return doi.equals(info.doi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi);
    }

    public BibTeXEntry toBibtex() {
        final String firstAuthor = authors == null || authors.isEmpty() ? null : authors.get(0);
        final BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_ARTICLE, new Key(firstAuthor + year));

        if (Objects.nonNull(doi)) {
            entry.addField(BibTeXEntry.KEY_DOI, new StringValue(doi, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(title)) {
            entry.addField(BibTeXEntry.KEY_TITLE, new StringValue(title, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(publisher)) {
            entry.addField(BibTeXEntry.KEY_PUBLISHER, new StringValue(publisher, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(authors)) {
            entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue(String.join(",", authors), StringValue.Style.BRACED));
        }
        if (Objects.nonNull(year)) {
            entry.addField(BibTeXEntry.KEY_YEAR, new StringValue(year.toString(), StringValue.Style.BRACED));
        }
        if (Objects.nonNull(issue)) {
            entry.addField(new Key("issue"), new StringValue(issue, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(journalTitle)) {
            entry.addField(BibTeXEntry.KEY_JOURNAL, new StringValue(journalTitle, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(month)) {
            entry.addField(BibTeXEntry.KEY_MONTH, new StringValue(month.toString(), StringValue.Style.BRACED));
        }
        if (Objects.nonNull(pages)) {
            entry.addField(BibTeXEntry.KEY_PAGES, new StringValue(pages, StringValue.Style.BRACED));
        }
        if (Objects.nonNull(volume)) {
            entry.addField(BibTeXEntry.KEY_VOLUME, new StringValue(volume, StringValue.Style.BRACED));
        }
        return entry;
    }
}

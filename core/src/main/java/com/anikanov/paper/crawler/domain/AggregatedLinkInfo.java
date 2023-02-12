package com.anikanov.paper.crawler.domain;

import com.itextpdf.text.pdf.PdfAnnotation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregatedLinkInfo {
    private String text;
    private PdfAnnotation.PdfImportedLink link;
    private String doi;

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
}

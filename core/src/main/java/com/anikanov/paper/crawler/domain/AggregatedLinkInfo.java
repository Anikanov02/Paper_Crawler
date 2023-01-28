package com.anikanov.paper.crawler.domain;

import com.itextpdf.text.Anchor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregatedLinkInfo {
    private String text;
    private Anchor link;
}

package com.anikanov.paper.crawler.source;

import com.anikanov.paper.crawler.domain.SourceName;

public interface SinglePaperSource extends PaperSource {
    SourceName getSourceName();
}

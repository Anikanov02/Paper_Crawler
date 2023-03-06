package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.config.GlobalConstants;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXObject;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Service
public class BibTexSerializer {
    public void saveData(List<BibTeXObject> objects) throws IOException {
        saveData(objects, new FileWriter(GlobalConstants.BIBTEX_OUTPUT));
    }

    public void saveData(List<BibTeXObject> objects, Writer writer) throws IOException {
        final BibTeXDatabase db = new BibTeXDatabase();
        objects.forEach(db::addObject);
        new BibTeXFormatter().format(db, writer);
    }
}

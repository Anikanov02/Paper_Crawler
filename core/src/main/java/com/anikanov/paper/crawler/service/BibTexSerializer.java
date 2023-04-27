package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.util.OutputUtil;
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
    public void saveData(String identifier, List<BibTeXObject> objects) throws IOException {
        saveData(objects, new FileWriter(OutputUtil.getOutputDir(identifier, OutputUtil.OutputOption.BIBTEX_GENERAL)));
    }

    public void saveData(List<BibTeXObject> objects, Writer writer) throws IOException {
        final BibTeXFormatter formatter = new BibTeXFormatter();
        for (BibTeXObject object : objects) {
            //manually writing every object because articles of the same authors are considered the same and some of them may be missing
            final BibTeXDatabase db = new BibTeXDatabase();
            db.addObject(object);
            formatter.format(db, writer);
            writer.write(System.lineSeparator());
        }
    }
}

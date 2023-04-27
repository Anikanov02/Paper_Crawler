package com.anikanov.paper.crawler.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class OutputUtil {
    public static final String basePath = new File("").getAbsolutePath();

    public static String jarPath;

    public static void setJarPath(String path) {
        jarPath = path;
    }

    public static void refreshOutputDir(String input) throws IOException {
        final File BIBTEX_OUTPUT;
        final File BIBTEX_FILTRATION_FAILED;
        final File BIBTEX_EXCEPTION_FAILED;
        final File BIBTEX_FILTERED;
        final File FAILED_PDFS_DIR;
        final File FILTERED_PDFS_DIR;
        input = normalizeName(input);
        BIBTEX_OUTPUT = getOutputDir(input, OutputOption.BIBTEX_GENERAL);
        if (BIBTEX_OUTPUT.exists()) {
            BIBTEX_OUTPUT.delete();
        }
        BIBTEX_OUTPUT.createNewFile();

        BIBTEX_FILTRATION_FAILED = getOutputDir(input, OutputOption.BIBTEX_FAILED);
        if (BIBTEX_FILTRATION_FAILED.exists()) {
            BIBTEX_FILTRATION_FAILED.delete();
        }
        BIBTEX_FILTRATION_FAILED.createNewFile();

        BIBTEX_EXCEPTION_FAILED = getOutputDir(input, OutputOption.BIBTEX_EXCEPTION_FAILED);
        if (BIBTEX_EXCEPTION_FAILED.exists()) {
            BIBTEX_EXCEPTION_FAILED.delete();
        }
        BIBTEX_EXCEPTION_FAILED.createNewFile();

        BIBTEX_FILTERED = getOutputDir(input, OutputOption.BIBTEX_FILTERED);
        if (BIBTEX_FILTERED.exists()) {
            BIBTEX_FILTERED.delete();
        }
        BIBTEX_FILTERED.createNewFile();

        FAILED_PDFS_DIR = getOutputDir(input, OutputOption.PDF_FAILED);
        if (!FAILED_PDFS_DIR.exists()) {
            FAILED_PDFS_DIR.mkdir();
        }

        FILTERED_PDFS_DIR = getOutputDir(input, OutputOption.PDF_FILTERED);
        if (!FILTERED_PDFS_DIR.exists()) {
            FILTERED_PDFS_DIR.mkdir();
        }

        FileUtils.cleanDirectory(FAILED_PDFS_DIR);
        FileUtils.cleanDirectory(FILTERED_PDFS_DIR);
    }

    public static void clearOutput() throws IOException {
        final String path = getPath();
        final File outputDir = new File(path + "/output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        FileUtils.cleanDirectory(outputDir);
    }

    public static File getOutputDir(String identifier, OutputOption outputOption) throws IOException {
        identifier = normalizeName(identifier);
        final File outputDir = new File(getPath() + "/output/" + identifier);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        switch (outputOption) {
            case PDF_FAILED -> {
                return new File(getPath() + "/output/" + identifier + "/failedPdfs");
            }
            case PDF_FILTERED -> {
                return new File(getPath() + "/output/" + identifier + "/filteredPdfs");
            }
            case BIBTEX_GENERAL -> {
                return new File(getPath() + "/output/" + identifier, "bibtex.txt");
            }
            case BIBTEX_FAILED -> {
                return new File(getPath() + "/output/" + identifier, "bibtex-failed.txt");
            }
            case BIBTEX_EXCEPTION_FAILED -> {
                return new File(getPath() + "/output/" + identifier, "bibtex-exception-failed.txt");
            }
            case BIBTEX_FILTERED -> {
                return new File(getPath() + "/output/" + identifier, "bibtex-filtered.txt");
            }
            default -> {
                throw new RuntimeException("No such output option : " + outputOption);
            }
        }
    }

    private static String getPath() {
        return Objects.isNull(jarPath) ? basePath : jarPath;
    }

    public static String normalizeName(String initial) {
        String normalized = initial;
        return initial.replace("<", "")
                .replace(">", "")
                .replace(".", "")
                .replace("$", "")
                .replace("+", "")
                .replace("#", "")
                .replace("&", "")
                .replace("%", "")
                .replace(";", "")
                .replace(":", "")
                .replace("'", "")
                .replace("|", "")
                .replace("=", "")
                .replace("/", "")
                .replace("\"", "")
                .replace("\\", "")
                .replace("\n", "")
                .replace("\r", "");
    }

    public enum OutputOption {
        BIBTEX_GENERAL,
        BIBTEX_FAILED,
        BIBTEX_EXCEPTION_FAILED,
        BIBTEX_FILTERED,
        PDF_FILTERED,
        PDF_FAILED;
    }
}

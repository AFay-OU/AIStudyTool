package org.aistudytool.aistudytool;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFHandler {

    public boolean isValidPDF(File file) {
        if (file == null) return false;
        if (!file.exists()) return false;
        if (!file.getName().toLowerCase().endsWith(".pdf")) return false;
        return true;
    }

    public String extractText(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            return stripper.getText(document);
        }
    }
}

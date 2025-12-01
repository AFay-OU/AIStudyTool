package org.aistudytool.aistudytool;

import java.io.File;

public class PDF {

    private File file;
    private PDFHandler handler = new PDFHandler();

    public boolean load(File f) {
        if (!handler.isValidPDF(f))
            return false;

        this.file = f;
        return true;
    }

    public String extractText() throws Exception {
        if (file == null)
            throw new IllegalStateException("PDF file not loaded.");

        return handler.extractText(file);
    }

    public File getFile() {
        return file;
    }
}

package org.aistudytool.aistudytool;

import java.io.File;

public class RedoState {

    public enum Mode { NONE, PDF, SCREENSHOT }

    private Mode mode = Mode.NONE;

    // PDF redo data
    private File pdfFile;
    private int pdfPage;
    private double normX, normY, normW, normH;

    // Screenshot redo
    private java.awt.image.BufferedImage screenshotImage;

    public void savePDFSelection(File file, int page,
                                 double nx, double ny, double nw, double nh) {
        mode = Mode.PDF;
        this.pdfFile = file;
        this.pdfPage = page;
        this.normX = nx;
        this.normY = ny;
        this.normW = nw;
        this.normH = nh;
    }

    public boolean hasPDF() {
        return mode == Mode.PDF && pdfFile != null && pdfFile.exists();
    }

    public void clear() {
        mode = Mode.NONE;
        pdfFile = null;
        screenshotImage = null;
    }


    public File getPdfFile() { return pdfFile; }
    public int getPdfPage() { return pdfPage; }
    public double getNormX() { return normX; }
    public double getNormY() { return normY; }
    public double getNormW() { return normW; }
    public double getNormH() { return normH; }

    public void saveScreenshot(java.awt.image.BufferedImage img) {
        mode = Mode.SCREENSHOT;
        this.screenshotImage = img;
    }

    public boolean hasScreenshot() {
        return mode == Mode.SCREENSHOT && screenshotImage != null;
    }

    public java.awt.image.BufferedImage getScreenshotImage() {
        return screenshotImage;
    }
}

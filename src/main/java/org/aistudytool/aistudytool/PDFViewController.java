package org.aistudytool.aistudytool;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class PDFViewController {

    @FXML public StackPane pdfContainer;
    @FXML private ImageView pdfImageView;
    @FXML private Pane overlayPane;
    @FXML private Label pageLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private ToggleButton selectModeToggle;

    private PDDocument document;
    private PDFRenderer renderer;
    private File loadedPDF;

    private int currentPage = 0;

    private double startX, startY;
    private Rectangle selectionRect;

    private double zoomFactor = 1.0;
    private boolean selectMode = false;

    private Runnable onCancelCallback;


    private java.util.function.Consumer<String> onConfirmCallback;
    private static final float RENDER_DPI = 140f;

    public void initialize() {
        dragOverlay();

        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        scrollPane.addEventFilter(MouseEvent.ANY, e -> {
            boolean selecting = selectMode || e.isShiftDown();
            scrollPane.setPannable(!selecting);
        });
    }

    public void loadPDF(File file) throws Exception {
        this.loadedPDF = file;
        this.document = PDDocument.load(file);
        this.renderer = new PDFRenderer(document);
        displayPage(0);
    }

    public void displayPage(int pageIndex) throws Exception {
        BufferedImage img = renderer.renderImageWithDPI(pageIndex, RENDER_DPI);
        Image fxImage = SwingFXUtils.toFXImage(img, null);
        pdfImageView.setImage(fxImage);

        currentPage = pageIndex;

        applyZoom();
        pageLabel.setText("Page " + (pageIndex + 1));
    }

    private void applyZoom() {
        Image img = pdfImageView.getImage();
        if (img == null) return;

        double scaledW = img.getWidth() * zoomFactor;
        double scaledH = img.getHeight() * zoomFactor;

        pdfImageView.setFitWidth(scaledW);
        pdfImageView.setFitHeight(scaledH);
        overlayPane.setPrefSize(scaledW, scaledH);
    }

    @FXML
    private void onFitWidth() {
        Image img = pdfImageView.getImage();
        if (img == null) return;

        double imgWidth = img.getWidth();
        double viewWidth = scrollPane.getViewportBounds().getWidth();

        if (viewWidth == 0) {
            scrollPane.viewportBoundsProperty().addListener((_, _, _) -> onFitWidth());
            return;
        }

        zoomFactor = viewWidth / imgWidth;
        applyZoom();
    }


    @FXML private void onZoomIn() {
        zoomFactor = Math.min(zoomFactor * 1.25, 5.0);
        applyZoom();
    }

    @FXML private void onZoomOut() {
        zoomFactor = Math.max(zoomFactor / 1.25, 0.25);
        applyZoom();
    }

    @FXML private void onZoomReset() {
        zoomFactor = 1.0;
        applyZoom();
    }

    @FXML
    private void onToggleSelectMode() {
        selectMode = selectModeToggle.isSelected();

        if (selectMode) {
            overlayPane.setMouseTransparent(false);
            overlayPane.setCursor(Cursor.CROSSHAIR);
        } else {
            overlayPane.setMouseTransparent(true);
            overlayPane.setCursor(Cursor.OPEN_HAND);

            if (selectionRect != null) {
                overlayPane.getChildren().remove(selectionRect);
                selectionRect = null;
            }
        }
    }

    private void dragOverlay() {
        overlayPane.setMouseTransparent(true);
        overlayPane.setCursor(Cursor.OPEN_HAND);

        overlayPane.setOnMousePressed(e -> {
            boolean selecting = selectMode || e.isShiftDown();

            if (!selecting) {
                selectionRect = null;
                overlayPane.setCursor(Cursor.CLOSED_HAND);
                return;
            }

            startX = e.getX();
            startY = e.getY();

            if (selectionRect != null)
                overlayPane.getChildren().remove(selectionRect);

            selectionRect = new Rectangle();
            selectionRect.setStroke(Color.YELLOW);
            selectionRect.setFill(Color.color(1, .9, .2, 0.2));
            selectionRect.setStrokeWidth(1.5);

            overlayPane.getChildren().add(selectionRect);
        });

        overlayPane.setOnMouseDragged(e -> {
            boolean selecting = selectMode || e.isShiftDown();

            if (!selecting) return;

            overlayPane.setMouseTransparent(false);
            e.consume();
            overlayPane.setCursor(Cursor.CROSSHAIR);

            if (selectionRect == null) return;

            double w = Math.abs(e.getX() - startX);
            double h = Math.abs(e.getY() - startY);

            selectionRect.setX(Math.min(startX, e.getX()));
            selectionRect.setY(Math.min(startY, e.getY()));
            selectionRect.setWidth(w);
            selectionRect.setHeight(h);
        });

        overlayPane.setOnMouseReleased(e -> {
            boolean selecting = selectMode || e.isShiftDown();

            if (!selecting) {
                overlayPane.setCursor(Cursor.OPEN_HAND);
                overlayPane.setMouseTransparent(true);
            } else {
                overlayPane.setCursor(Cursor.CROSSHAIR);
            }
        });
    }

    @FXML
    private void onPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            try { displayPage(currentPage); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void onNextPage() {
        if (currentPage < document.getNumberOfPages() - 1) {
            currentPage++;
            try { displayPage(currentPage); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void onCancel() {
        closeWindow();
        if (onCancelCallback != null) onCancelCallback.run();
    }

    public void setOnCancel(Runnable r) { this.onCancelCallback = r; }
    public void setOnConfirm(java.util.function.Consumer<String> c) { this.onConfirmCallback = c; }

    @FXML
    private void onConfirm() {
        try {
            String extracted = extractText();

            if (extracted == null || extracted.isBlank()) {
                showError("No text found in the selection.");
                return;
            }

            saveRedoInfo();

            closeWindow();

            if (onConfirmCallback != null)
                onConfirmCallback.accept(extracted);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Could not extract text from selected region.");
        }
    }


    private void saveRedoInfo() {
        if (selectionRect == null) return;

        Image img = pdfImageView.getImage();
        double originalW = img.getWidth();
        double originalH = img.getHeight();

        double zoomedW = pdfImageView.getFitWidth();
        double zoomedH = pdfImageView.getFitHeight();

        double scaleX = originalW / zoomedW;
        double scaleY = originalH / zoomedH;

        double pixelX = selectionRect.getX() * scaleX;
        double pixelY = selectionRect.getY() * scaleY;
        double pixelW = selectionRect.getWidth() * scaleX;
        double pixelH = selectionRect.getHeight() * scaleY;

        double nx = pixelX / originalW;
        double ny = pixelY / originalH;
        double nw = pixelW / originalW;
        double nh = pixelH / originalH;

        AppState.getRedoState().savePDFSelection(
                loadedPDF,
                currentPage,
                nx, ny, nw, nh
        );
    }

    public void restoreSelection(double nx, double ny, double nw, double nh) {
        Image img = pdfImageView.getImage();
        if (img == null) return;

        double originalW = img.getWidth();
        double originalH = img.getHeight();

        double pixelX = nx * originalW;
        double pixelY = ny * originalH;
        double pixelW = nw * originalW;
        double pixelH = nh * originalH;

        double zoomedW = pdfImageView.getFitWidth();
        double zoomedH = pdfImageView.getFitHeight();

        double scaleX = zoomedW / originalW;
        double scaleY = zoomedH / originalH;

        double viewX = pixelX * scaleX;
        double viewY = pixelY * scaleY;
        double viewW = pixelW * scaleX;
        double viewH = pixelH * scaleY;

        if (selectionRect != null)
            overlayPane.getChildren().remove(selectionRect);

        selectionRect = new Rectangle(viewW, viewH);
        selectionRect.setX(viewX);
        selectionRect.setY(viewY);
        selectionRect.setStroke(Color.YELLOW);
        selectionRect.setFill(Color.color(1, .9, .2, 0.2));
        selectionRect.setStrokeWidth(1.5);

        overlayPane.getChildren().add(selectionRect);

        selectModeToggle.setSelected(true);
        selectMode = true;
        overlayPane.setMouseTransparent(false);
    }

    private void closeWindow() {
        try {
            if (document != null) {
                document.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Stage stage = (Stage) overlayPane.getScene().getWindow();
        stage.close();
    }


    private String extractText() throws Exception {
        if (selectionRect == null)
            throw new IllegalStateException("No selected region.");

        Image img = pdfImageView.getImage();
        double originalW = img.getWidth();
        double originalH = img.getHeight();

        double zoomedW = pdfImageView.getFitWidth();
        double zoomedH = pdfImageView.getFitHeight();

        double rectX = selectionRect.getX();
        double rectY = selectionRect.getY();

        double scaleX = originalW / zoomedW;
        double scaleY = originalH / zoomedH;

        double pixelX = rectX * scaleX;
        double pixelY = rectY * scaleY;
        double pixelW = selectionRect.getWidth() * scaleX;
        double pixelH = selectionRect.getHeight() * scaleY;

        double ptFactor = 72.0 / RENDER_DPI;

        double pdfX = pixelX * ptFactor;
        double pdfY = pixelY * ptFactor;
        double pdfW = pixelW * ptFactor;
        double pdfH = pixelH * ptFactor;

        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle2D rect = new Rectangle2D.Double(pdfX, pdfY, pdfW, pdfH);
        stripper.addRegion("region", rect);

        stripper.extractRegions(document.getPage(currentPage));
        return stripper.getTextForRegion("region");
    }


    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }
}

package org.aistudytool.aistudytool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class ScreenshotHandler extends JWindow {

    private Point startPointScreen;
    private Point dragStartWindow;
    private final Rectangle selection = new Rectangle();
    private static final Logger logger = Logger.getLogger(ScreenshotHandler.class.getName());
    private Consumer<String> onOCRComplete;

    public ScreenshotHandler() {
        setBackground(new Color(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Point winPos = getLocationOnScreen();
                    dragStartWindow = new Point(e.getX(), e.getY());
                    startPointScreen = new Point(
                            winPos.x + e.getX(),
                            winPos.y + e.getY()
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    Robot robot = new Robot();
                    BufferedImage sh = robot.createScreenCapture(selection);

                    File dir = new File("Screenshots");
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
                    }

                    ImageIO.write(sh, "png", new File("Screenshots/screenshot.png"));
                    System.out.println("Screenshot saved to: " + dir.getAbsolutePath());

                    AppState.getRedoState().saveScreenshot(sh);
                    ConfirmText();

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Screenshot capture failed", ex);
                }

                dispose();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {

                try {
                    Point winPos = getLocationOnScreen();

                    int x1 = startPointScreen.x;
                    int y1 = startPointScreen.y;

                    int x2 = winPos.x + e.getX();
                    int y2 = winPos.y + e.getY();

                    int x = Math.min(x1, x2);
                    int y = Math.min(y1, y2);
                    int w = Math.abs(x2 - x1);
                    int h = Math.abs(y2 - y1);

                    selection.setBounds(x, y, w, h);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                repaint();
            }
        });

        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setVisible(true);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setComposite(AlphaComposite.Src);
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (selection.width > 0 && selection.height > 0) {

            try {
                Point winPos = getLocationOnScreen();

                int localX = selection.x - winPos.x;
                int localY = selection.y - winPos.y;

                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(localX, localY, selection.width, selection.height);

                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(localX, localY, selection.width, selection.height);

            } catch (Exception ignored) {}
        }

        g2.dispose();
    }

    public void ConfirmText() {

        System.setProperty("jna.library.path", "/opt/homebrew/opt/tesseract/lib");

        ITesseract tesseract = new Tesseract();

        try {
            // Use Homebrew tessdata folder
            tesseract.setDatapath("/opt/homebrew/share/tessdata");
            tesseract.setLanguage("eng");

            File imageFile = new File("Screenshots/screenshot.png");
            if (!imageFile.exists()) {
                logger.severe("Screenshot not found: " + imageFile.getAbsolutePath());
                return;
            }

            String text = tesseract.doOCR(imageFile);
            System.out.println("Extracted text:\n" + text);

            if (onOCRComplete != null)
                onOCRComplete.accept(text);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "OCR failed", e);
        }
    }


    public void setOnOCRComplete(Consumer<String> callback) {
        this.onOCRComplete = callback;
    }
}

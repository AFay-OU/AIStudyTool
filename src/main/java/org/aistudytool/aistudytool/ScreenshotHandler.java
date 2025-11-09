package org.aistudytool.aistudytool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenshotHandler extends JWindow{

    private Point startPoint;
    private final Rectangle selection = new Rectangle();
    private static final Logger logger = Logger.getLogger(ScreenshotHandler.class.getName());

    public ScreenshotHandler() {
        setBackground(new Color(0,0,0,0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
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

                    ImageIO.write(sh, "png", new java.io.File("Screenshots/screenshot.png"));
                    System.out.println("Screenshot saved to: " + dir.getAbsolutePath());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Screenshot capture failed", ex);                }
                dispose();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e){
                int x = Math.min(startPoint.x, e.getX());
                int y = Math.min(startPoint.y, e.getY());
                int w = Math.abs(e.getX() - startPoint.x);
                int h = Math.abs(e.getY() - startPoint.y);

                selection.setBounds(x, y, w, h);
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
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(selection.x, selection.y, selection.width, selection.height);

            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Color.LIGHT_GRAY);
            g2.setStroke(new BasicStroke(2));
            g2.draw(selection);
        }

        g2.dispose();
    }

    private void confirmText(){

    }
}

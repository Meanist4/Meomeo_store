package util;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import views.ViewOrderDetailFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class InvoicePdfExporter {

    public static void exportInvoice(int orderId, File targetFile) throws Exception {
        final Exception[] err = new Exception[1];
        
        // Ensure the Swing component creation and painting run on Event Dispatch Thread (EDT)
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                generate(orderId, targetFile);
            } catch (Exception ex) {
                err[0] = ex;
            }
        } else {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    generate(orderId, targetFile);
                } catch (Exception ex) {
                    err[0] = ex;
                }
            });
        }
        
        if (err[0] != null) {
            throw err[0];
        }
    }
    
    private static void generate(int orderId, File targetFile) throws Exception {
        ViewOrderDetailFrame frame = new ViewOrderDetailFrame(orderId);
        frame.pack();
        
        // Hide the Close button for clean printing
        hideCloseButton(frame);
        
        // Force layout update after hiding the button
        frame.getContentPane().invalidate();
        frame.getContentPane().validate();
        frame.pack();
        
        // Render to high-resolution image
        BufferedImage image = captureComponent(frame.getContentPane(), 2.0);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        
        // Convert screen pixel size to PDF points (1 inch = 72 points, assuming 96 DPI screen)
        float widthInPoints = frame.getContentPane().getWidth() * 72f / 96f;
        float heightInPoints = frame.getContentPane().getHeight() * 72f / 96f;
        
        Rectangle pageSize = new Rectangle(widthInPoints, heightInPoints);
        Document document = new Document(pageSize, 0, 0, 0, 0); // 0 margin
        
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));
        document.open();
        
        Image pdfImage = Image.getInstance(imageBytes);
        pdfImage.scaleAbsolute(widthInPoints, heightInPoints);
        pdfImage.setAbsolutePosition(0, 0);
        
        document.add(pdfImage);
        document.close();
    }
    
    private static void hideCloseButton(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText() != null && btn.getText().toLowerCase().contains("close")) {
                    btn.setVisible(false);
                    return;
                }
            } else if (comp instanceof Container) {
                hideCloseButton((Container) comp);
            }
        }
    }
    
    private static BufferedImage captureComponent(Component component, double scale) {
        int w = (int) (component.getWidth() * scale);
        int h = (int) (component.getHeight() * scale);
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.scale(scale, scale);
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        
        component.paint(g);
        g.dispose();
        
        return image;
    }
}

package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

public class ScannerButtonUI extends BasicButtonUI {

    private final Color backgroundColor;

    public ScannerButtonUI() {
        this(new Color(115, 61, 29));
    }

    public ScannerButtonUI(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = c.getWidth();
        int h = c.getHeight();

        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, w, h, 20, 20);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = w / 2;
        int cy = h / 2;
        int size = 10;
        int len = 4;

        g2.drawLine(cx - size, cy - size + len, cx - size, cy - size);
        g2.drawLine(cx - size, cy - size, cx - size + len, cy - size);
        g2.drawLine(cx + size, cy - size + len, cx + size, cy - size);
        g2.drawLine(cx + size, cy - size, cx + size - len, cy - size);
        g2.drawLine(cx - size, cy + size - len, cx - size, cy + size);
        g2.drawLine(cx - size, cy + size, cx - size + len, cy + size);
        g2.drawLine(cx + size, cy + size - len, cx + size, cy + size);
        g2.drawLine(cx + size, cy + size, cx + size - len, cy + size);

        g2.drawLine(cx - 5, cy, cx + 5, cy);

        g2.dispose();
    }
}
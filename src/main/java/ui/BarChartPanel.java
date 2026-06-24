package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class BarChartPanel extends JPanel {

    private String[] labels = {};
    private long[]   values = {};

    private static final Color BAR_COLOR      = new Color(230, 100, 20);
    private static final Color GRID_COLOR     = new Color(220, 220, 220);
    private static final Color LABEL_COLOR    = new Color(100, 100, 100);
    private static final Color HOVER_COLOR    = new Color(255, 140, 50);
    private static final int   PADDING_LEFT   = 70;
    private static final int   PADDING_RIGHT  = 20;
    private static final int   PADDING_TOP    = 20;
    private static final int   PADDING_BOTTOM = 40;
    private static final int   GRID_LINES     = 4;

    private int hoveredIndex = -1; // cột đang hover

    public BarChartPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);

        // Tooltip khi hover
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                hoveredIndex = getBarIndexAt(e.getX());
                repaint();
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredIndex = -1;
                repaint();
            }
        });
    }

    public void setData(String[] labels, long[] values) {
        this.labels = labels;
        this.values = values;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.length == 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w           = getWidth();
        int h           = getHeight();
        int chartWidth  = w - PADDING_LEFT - PADDING_RIGHT;
        int chartHeight = h - PADDING_TOP - PADDING_BOTTOM;

        long maxValue = 0;
        for (long v : values) if (v > maxValue) maxValue = v;
        if (maxValue == 0) maxValue = 1;

        // ── Vẽ grid lines + label trục Y ──
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        for (int i = 0; i <= GRID_LINES; i++) {
            int y = PADDING_TOP + chartHeight - (chartHeight * i / GRID_LINES);
            long yValue = maxValue * i / GRID_LINES;

            g2.setColor(GRID_COLOR);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(PADDING_LEFT, y, w - PADDING_RIGHT, y);

            g2.setColor(LABEL_COLOR);
            String yLabel = formatValue(yValue);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(yLabel, PADDING_LEFT - fm.stringWidth(yLabel) - 6, y + 4);
        }

        // ── Vẽ các cột ──
        int n        = values.length;
        int barGap   = 10;
        int barWidth = (chartWidth / n) - barGap;
        if (barWidth < 8) barWidth = 8;

        for (int i = 0; i < n; i++) {
            int barHeight = (int) ((double) values[i] / maxValue * chartHeight);
            int x = PADDING_LEFT + i * (chartWidth / n) + barGap / 2;
            int y = PADDING_TOP + chartHeight - barHeight;

            // Màu cam, sáng hơn khi hover
            g2.setColor(i == hoveredIndex ? HOVER_COLOR : BAR_COLOR);
            g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barHeight, 6, 6));

            // Label trục X
            g2.setColor(LABEL_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            String lbl = labels != null && i < labels.length ? labels[i] : "";
            int lx = x + (barWidth - fm.stringWidth(lbl)) / 2;
            g2.drawString(lbl, lx, h - PADDING_BOTTOM + 18);

            // Tooltip giá trị khi hover
            if (i == hoveredIndex) {
                String tip = formatValue(values[i]) + "đ";
                FontMetrics fm2 = g2.getFontMetrics();
                int tw = fm2.stringWidth(tip) + 12;
                int th = fm2.getHeight() + 6;
                int tx = x + barWidth / 2 - tw / 2;
                int ty = y - th - 4;

                g2.setColor(new Color(50, 50, 50, 210));
                g2.fillRoundRect(tx, ty, tw, th, 8, 8);
                g2.setColor(Color.WHITE);
                g2.drawString(tip, tx + 6, ty + th - 6);
            }
        }
    }

    // Tìm cột đang hover theo tọa độ x chuột
    private int getBarIndexAt(int mouseX) {
        if (values == null || values.length == 0) return -1;
        int chartWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        int n = values.length;
        for (int i = 0; i < n; i++) {
            int x = PADDING_LEFT + i * (chartWidth / n);
            if (mouseX >= x && mouseX < x + (chartWidth / n)) return i;
        }
        return -1;
    }

    // Format số: 8500000 → "8.5M" hoặc "8,500K"
    private String formatValue(long value) {
        if (value >= 1_000_000) {
            double m = value / 1_000_000.0;
            return (m == (long) m)
                ? (long) m + "M"
                : String.format("%.1fM", m);
        }
        if (value >= 1_000) return (value / 1_000) + "K";
        return String.valueOf(value);
    }
}
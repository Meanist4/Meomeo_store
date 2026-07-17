package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import repository.OrderRepository.TopProductRow;

public class TopSalesPanel extends JPanel {

    private List<TopProductRow> data = new java.util.ArrayList<>();

    private static final Color BAR_COLOR = new Color(230, 100, 20);
    private static final Color BG_BAR_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_COLOR = new Color(30, 41, 59);
    private static final Color SUB_COLOR = new Color(100, 116, 139);

    // Tối ưu: Định nghĩa sẵn Font để tái sử dụng, tránh tạo mới trong hàm vẽ
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private static final int ROW_HEIGHT = 56;
    private static final int START_Y = 12;

    public TopSalesPanel() {
        setBackground(Color.WHITE);
        setOpaque(true);
    }

    public void setData(List<TopProductRow> data) {
        this.data = data != null ? data : new java.util.ArrayList<>();

        // SỬA LỖI 1: Tính toán lại kích thước mong muốn dựa trên số lượng phần tử thực
        // tế
        int totalHeight = START_Y + (this.data.size() * ROW_HEIGHT) + 10;
        setPreferredSize(new Dimension(200, totalHeight)); // Chiều rộng cho tự co giãn, chiều cao cố định theo data

        revalidate(); // Báo cho Java Layout hay tin kích thước đã thay đổi
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (data == null || data.isEmpty()) {
            g.setColor(SUB_COLOR);
            g.setFont(FONT_NORMAL);
            String msg = "No data available";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(msg,
                    (getWidth() - fm.stringWidth(msg)) / 2,
                    (getHeight() + fm.getAscent()) / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int paddingLeft = 16;
        int paddingRight = 16;
        int barAreaWidth = w - paddingLeft - paddingRight;
        int barHeight = 6;

        int maxSold = data.stream().mapToInt(r -> r.quantitySold).max().orElse(1);

        for (int i = 0; i < data.size(); i++) {
            TopProductRow row = data.get(i);
            int y = START_Y + i * ROW_HEIGHT;

            // Tên sản phẩm
            g2.setFont(FONT_NORMAL);
            g2.setColor(TEXT_COLOR);
            g2.drawString(row.productName, paddingLeft, y + 16);

            // Chỉ số Đã bán / Tồn kho
            String qtyText = row.quantitySold + " / " + row.stock;
            g2.setFont(FONT_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(TEXT_COLOR);
            g2.drawString(qtyText, w - paddingRight - fm.stringWidth(qtyText), y + 16);

            // Vẽ thanh nền xám
            int barY = y + 26;
            g2.setColor(BG_BAR_COLOR);
            g2.fillRoundRect(paddingLeft, barY, barAreaWidth, barHeight, barHeight, barHeight);

            // Vẽ phần trăm thanh màu cam
            int fillWidth = (int) ((double) row.quantitySold / maxSold * barAreaWidth);
            if (fillWidth > 0) {
                g2.setColor(BAR_COLOR);
                g2.fillRoundRect(paddingLeft, barY, fillWidth, barHeight, barHeight, barHeight);
            }
        }
    }
}
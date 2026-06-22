package ui;

import java.awt.Font;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JTable;
import javax.swing.JViewport;

/**
 * Một JViewport tuỳ chỉnh: khi bảng không có dữ liệu,
 * sẽ vẽ icon mèo và dòng chữ thông báo ở giữa viewport.
 *
 * Cách dùng trong DashBoardFrame:
 * <pre>
 *   JScrollPane scrollPane = (JScrollPane) ((JViewport) table.getParent()).getParent();
 *   scrollPane.setViewport(new EmptyTableViewport(table, "Nothing found!"));
 *   scrollPane.setViewportView(table);
 * </pre>
 */
public class EmptyTableViewport extends JViewport {

    private final JTable table;
    private final String emptyMessage;

    private static final int ICON_SIZE      = 110;
    private static final int TEXT_GAP       = 30;   // khoảng cách từ đáy icon đến chữ
    private static final float FONT_SIZE    = 14f;
    private static final Color TEXT_COLOR   = new Color(148, 163, 184);
    private static final int VERTICAL_SHIFT = 30;   // dịch toàn bộ hình lên một chút

    public EmptyTableViewport(JTable table, String emptyMessage) {
        this.table        = table;
        this.emptyMessage = emptyMessage;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        if (table.getRowCount() != 0) {
            return; // có dữ liệu → không vẽ gì thêm
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w  = getWidth();
            int h  = getHeight();

            // Xoá nền
            g2.setColor(table.getBackground());
            g2.fillRect(0, 0, w, h);

            // Toạ độ tâm icon
            int cx = w / 2;
            int cy = h / 2 - VERTICAL_SHIFT;

            // Vẽ icon mèo (delegate sang EmployeeTableRenderer)
            EmployeeTableRenderer.drawCatInBox(
                    g2,
                    cx - ICON_SIZE / 2,
                    cy - ICON_SIZE / 2,
                    ICON_SIZE
            );

            // Vẽ chữ thông báo bên dưới icon
            g2.setFont(table.getFont().deriveFont(Font.BOLD, FONT_SIZE));
            g2.setColor(TEXT_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(emptyMessage)) / 2;
            int textY = cy + ICON_SIZE / 2 + TEXT_GAP;
            g2.drawString(emptyMessage, textX, textY);
        } finally {
            g2.dispose();
        }
    }
}
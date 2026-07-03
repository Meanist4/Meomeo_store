package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class HistoryTableRenderer extends DefaultTableCellRenderer {

    private static final Color CLR_PAID = new Color(0x16A34A);
    private static final Color CLR_PAID_BG = new Color(0xDCFCE7);
    private static final Color CLR_CANCEL = new Color(0xDC2626);
    private static final Color CLR_CANCEL_BG = new Color(0xFEE2E2);
    private static final Color CLR_PENDING = new Color(0xD97706);
    private static final Color CLR_PENDING_BG = new Color(0xFEF3C7);

    private static final Color CLR_ORDER_ID = new Color(0xC47529);
    private static final Color CLR_WALK_IN = new Color(0x94A3B8);

    private static final Color CLR_TEXT = new Color(0x1E293B);
    private static final Color CLR_SUB = new Color(0x64748B);
    private static final Color CLR_ROW_ODD = Color.WHITE;
    private static final Color CLR_ROW_EVEN = new Color(0xF9FAFB);

    private static final Color BTN_VIEW_FG = new Color(0xC47529);
    private static final Color BTN_VIEW_BG = new Color(0xFFF7ED);
    private static final Color BTN_VIEW_BORDER = new Color(0xFBBF79);

    private static final Color BTN_CANCEL_FG = new Color(0xDC2626);
    private static final Color BTN_CANCEL_BG = new Color(0xFFF1F1);
    private static final Color BTN_CANCEL_BORDER = new Color(0xFCA5A5);

    private static final Color BTN_EXPORT_FG = new Color(0x0F766E);
    private static final Color BTN_EXPORT_BG = new Color(0xF0FDFA);
    private static final Color BTN_EXPORT_BORDER = new Color(0x99F6E4);

    private static final Color BADGE_BG = new Color(0xF1F5F9);
    private static final Color BADGE_FG = new Color(0x334155);

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        String status = "";
        int statusCol = 6;
        if (table.getModel().getColumnCount() > statusCol) {
            int modelRow = table.convertRowIndexToModel(row);
            Object sv = table.getModel().getValueAt(modelRow, statusCol);
            if (sv != null) {
                status = sv.toString().trim();
            }
        }

        Color rowBg = isSelected
                ? new Color(0xF8F6F2)
                : (row % 2 == 0 ? CLR_ROW_ODD : CLR_ROW_EVEN);

        if (column == 7) {
            return buildActionPanel(status, rowBg, isSelected);
        }

        if (column == 6) {
            return buildStatusBadge(status, rowBg);
        }

        if (column == 4) {
            return buildPaymentBadge(value, rowBg);
        }

        if (column == 0) {
            JLabel lbl = plain(value, rowBg);
            lbl.setForeground(CLR_ORDER_ID);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
            return lbl;
        }

        if (column == 3 && value != null && value.toString().equalsIgnoreCase("Walk-in customer")) {
            JLabel lbl = plain(value, rowBg);
            lbl.setForeground(CLR_WALK_IN);
            lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
            return lbl;
        }

        JLabel lbl = plain(value, rowBg);
        lbl.setForeground(CLR_TEXT);
        return lbl;
    }

    private JLabel plain(Object value, Color bg) {
        JLabel lbl = new JLabel(value != null ? value.toString() : "");
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(CLR_TEXT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        return lbl;
    }

    private JPanel buildStatusBadge(String status, Color rowBg) {
        Color fg;
        Color bg;
        String label;
        switch (status.toUpperCase()) {
            case "PAID" -> {
                fg = CLR_PAID;
                bg = CLR_PAID_BG;
                label = "● Paid";
            }
            case "CANCELLED", "CANCELED" -> {
                fg = CLR_CANCEL;
                bg = CLR_CANCEL_BG;
                label = "● Cancelled";
            }
            case "PENDING" -> {
                fg = CLR_PENDING;
                bg = CLR_PENDING_BG;
                label = "● Pending";
            }
            default -> {
                fg = CLR_SUB;
                bg = BADGE_BG;
                label = status;
            }
        }

        JLabel badge = new JLabel(label);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        JPanel badgePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.add(badge);

        return wrapCentered(badgePanel, rowBg);
    }

    private JPanel buildPaymentBadge(Object value, Color rowBg) {
        String text = value != null ? value.toString().trim() : "";
        String display = switch (text.toUpperCase()) {
            case "CASH", "TIỀN MẶT", "TIEN MAT" ->
                "Cash";
            case "TRANSFER", "CHUYỂN KHOẢN", "CHUYEN KHOAN" ->
                "Transfer";
            case "CARD", "THẺ" ->
                "Card";
            case "E-WALLET", "EWALLET", "VÍ ĐIỆN TỬ" ->
                "E-Wallet";
            default ->
                text;
        };

        JLabel lbl = new JLabel(display);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(BADGE_FG);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JPanel badgePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BADGE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0xE2E8F0));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badgePanel.setOpaque(false);
        badgePanel.add(lbl);

        return wrapCentered(badgePanel, rowBg);
    }

    private JPanel wrapCentered(JComponent content, Color rowBg) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(rowBg);
        wrap.setOpaque(true);
        wrap.add(content);
        return wrap;
    }

    private JPanel buildActionPanel(String status, Color rowBg, boolean isSelected) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        panel.setBackground(rowBg);
        panel.setOpaque(true);

        panel.add(makeBtn("⤓ Export", BTN_EXPORT_FG, BTN_EXPORT_BG, BTN_EXPORT_BORDER));
        panel.add(Box.createHorizontalStrut(8));
        panel.add(makeBtn("⊙ View", BTN_VIEW_FG, BTN_VIEW_BG, BTN_VIEW_BORDER));

        if ("PENDING".equalsIgnoreCase(status)) {
            panel.add(Box.createHorizontalStrut(8));
            panel.add(makeBtn("✕ Cancel", BTN_CANCEL_FG, BTN_CANCEL_BG, BTN_CANCEL_BORDER));
        }

        return panel;
    }

    private JPanel makeBtn(String text, Color fg, Color bg, Color border) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        JPanel btn = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.add(lbl);
        Dimension size = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(Math.max(86, size.width), 30));
        btn.setMaximumSize(btn.getPreferredSize());
        btn.setMinimumSize(btn.getPreferredSize());
        return btn;
    }
}

package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class OrderTableRenderer extends DefaultTableCellRenderer {

    private static final Color PAID_FG           = new Color(34, 197, 94);
    private static final String PAID_BADGE_STYLE  = "background: #DCFCE7; arc: 999; border: 4,12,4,12;";

    private static final Color CANCELED_FG            = new Color(239, 68, 68);
    private static final String CANCELED_BADGE_STYLE   = "background: #FEE2E2; arc: 999; border: 4,12,4,12;";

    private static final Color COLOR_BOLD   = new Color(15, 23, 42);
    private static final Color COLOR_NORMAL = new Color(71, 85, 105);
    private static final Color COLOR_MUTED  = new Color(148, 163, 184);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        String status = String.valueOf(table.getValueAt(row, 5));
        boolean isCanceled = "CANCELED".equals(status);

        // Cột 5 — badge pill PAID / CANCELED
        if (column == 5) {
            JLabel lblBadge = new JLabel(String.valueOf(value), SwingConstants.CENTER);
            lblBadge.setFont(table.getFont().deriveFont(Font.BOLD, 10f));
            if (isCanceled) {
                lblBadge.setForeground(CANCELED_FG);
                lblBadge.putClientProperty(FlatClientProperties.STYLE, CANCELED_BADGE_STYLE);
            } else {
                lblBadge.setForeground(PAID_FG);
                lblBadge.putClientProperty(FlatClientProperties.STYLE, PAID_BADGE_STYLE);
            }
            JPanel cell = new JPanel(new GridBagLayout());
            cell.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cell.add(lblBadge);
            return cell;
        }

        // Cột 1–4
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (isCanceled) {
                label.setForeground(COLOR_MUTED);
                String text = label.getText();
                if (!text.startsWith("<html>")) {
                    label.setText("<html><s>" + text + "</s></html>");
                }
                label.setFont(table.getFont());
            } else {
                label.setFont(table.getFont());
                if (column == 1 || column == 4) {
                    label.setFont(table.getFont().deriveFont(Font.BOLD));
                    label.setForeground(COLOR_BOLD);
                } else {
                    label.setForeground(COLOR_NORMAL);
                }
            }
        }
        return c;
    }
}
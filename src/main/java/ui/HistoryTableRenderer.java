package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class HistoryTableRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        String status = String.valueOf(table.getValueAt(row, 5));
        boolean isCanceled = "CANCELED".equals(status);

        if (column == 3) {
            String pm = String.valueOf(value);
            String iconStr = pm.equalsIgnoreCase("Cash") ? "💵 "
                    : pm.equalsIgnoreCase("QR Pay") ? "📱 " : "💳 ";
            JLabel lblPay = new JLabel(iconStr + pm, SwingConstants.CENTER);
            lblPay.setFont(table.getFont());
            lblPay.setForeground(isCanceled ? new Color(148, 163, 184) : new Color(71, 85, 105));

            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.add(lblPay);
            return p;
        }

        if (column == 5) {
            JLabel lblStatus = new JLabel(String.valueOf(value), SwingConstants.CENTER);
            lblStatus.setFont(table.getFont().deriveFont(Font.BOLD, 10f));
            if (isCanceled) {
                lblStatus.setForeground(new Color(239, 68, 68));
                lblStatus.putClientProperty(FlatClientProperties.STYLE, "background: #FEE2E2; arc: 999; border: 4,12,4,12;");
            } else {
                lblStatus.setForeground(new Color(34, 197, 94));
                lblStatus.putClientProperty(FlatClientProperties.STYLE, "background: #DCFCE7; arc: 999; border: 4,12,4,12;");
            }
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.add(lblStatus);
            return p;
        }

        if (column == 6) {
            JLabel lblAction = new JLabel(isCanceled ? "—" : "View", SwingConstants.CENTER);
            lblAction.setFont(table.getFont().deriveFont(Font.BOLD));
            lblAction.setForeground(isCanceled ? new Color(203, 213, 225) : new Color(100, 116, 139));
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            p.add(lblAction);
            return p;
        }

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (column == 0) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }
            if (column == 4) {
                label.setFont(table.getFont().deriveFont(Font.BOLD));
                label.setForeground(new Color(15, 23, 42));
            } else {
                label.setForeground(new Color(71, 85, 105));
            }
            if (isCanceled) {
                label.setForeground(new Color(161, 161, 170));
                String txt = label.getText();
                if (!txt.startsWith("<html>")) {
                    label.setText("<html><s>" + txt + "</s></html>");
                }
            }
        }
        return c;
    }
}

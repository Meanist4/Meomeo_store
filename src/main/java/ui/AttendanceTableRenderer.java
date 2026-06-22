package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class AttendanceTableRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (column == 7) {
            String status = (value != null) ? value.toString() : "ABSENT";
            JLabel lblBadge = new JLabel(status, SwingConstants.CENTER);
            lblBadge.setFont(table.getFont().deriveFont(Font.BOLD, 10f));

            switch (status) {
                case "ON TIME" -> {
                    lblBadge.setForeground(new Color(22, 163, 74));
                    lblBadge.putClientProperty(FlatClientProperties.STYLE, "background: #F0FDF4; arc: 999; border: 4,12,4,12;");
                }
                case "LATE" -> {
                    lblBadge.setForeground(new Color(217, 119, 6));
                    lblBadge.putClientProperty(FlatClientProperties.STYLE, "background: #FFFBEB; arc: 999; border: 4,12,4,12;");
                }
                case "ON LEAVE" -> {
                    lblBadge.setForeground(new Color(37, 99, 235));
                    lblBadge.putClientProperty(FlatClientProperties.STYLE, "background: #EFF6FF; arc: 999; border: 4,12,4,12;");
                }
                default -> {  // ABSENT
                    lblBadge.setForeground(new Color(220, 38, 38));
                    lblBadge.putClientProperty(FlatClientProperties.STYLE, "background: #FEF2F2; arc: 999; border: 4,12,4,12;");
                }
            }

            JPanel cellContainer = new JPanel(new GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.add(lblBadge);
            return cellContainer;
        }

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            if (column == 0 || column == 3 || column == 4 || column == 5 || column == 6) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }
            if (column == 1) {
                label.setFont(table.getFont().deriveFont(Font.BOLD));
                label.setForeground(new Color(15, 23, 42));
            } else {
                label.setForeground(new Color(71, 85, 105));
            }
        }
        return c;
    }
}

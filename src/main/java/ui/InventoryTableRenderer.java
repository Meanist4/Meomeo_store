package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import java.util.function.Function;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class InventoryTableRenderer extends DefaultTableCellRenderer {

    private final Function<String, ImageIcon> iconLoader;

    public InventoryTableRenderer(Function<String, ImageIcon> iconLoader) {
        this.iconLoader = iconLoader;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (column == 1) {
            JLabel lblImg = new JLabel("", SwingConstants.CENTER);
            lblImg.setPreferredSize(new Dimension(36, 36));
            lblImg.putClientProperty(FlatClientProperties.STYLE, "background: #F1F5F9; arc: 999;");
            lblImg.setOpaque(true);

            ImageIcon icon = iconLoader.apply(value != null ? value.toString().trim() : "");
            if (icon != null) {
                lblImg.setIcon(icon);
            } else {
                lblImg.setText("📦");
            }

            JPanel cellContainer = new JPanel(new GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.add(lblImg);
            return cellContainer;
        }

        if (column == 3) {
            JLabel lblCat = new JLabel(String.valueOf(value), SwingConstants.CENTER);
            lblCat.setFont(table.getFont().deriveFont(Font.BOLD, 10f));
            lblCat.setForeground(new Color(120, 110, 95));
            lblCat.putClientProperty(FlatClientProperties.STYLE, "background: #F5F3EF; arc: 12; border: 2,8,2,8;");

            JPanel cellContainer = new JPanel(new GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.add(lblCat);
            return cellContainer;
        }

        if (column == 6) {
            String statusStr = String.valueOf(value);
            JLabel lblStatus = new JLabel(statusStr, SwingConstants.CENTER);
            lblStatus.setFont(table.getFont().deriveFont(Font.BOLD, 10f));
            if ("Active".equalsIgnoreCase(statusStr)) {

                lblStatus.setForeground(new Color(22, 163, 74));

                lblStatus.putClientProperty(
                        FlatClientProperties.STYLE,
                        "background: #DCFCE7; arc: 999; border: 4,12,4,12;"
                );

            } else if ("Inactive".equalsIgnoreCase(statusStr)) {

                lblStatus.setForeground(new Color(239, 68, 68));

                lblStatus.putClientProperty(
                        FlatClientProperties.STYLE,
                        "background: #FEE2E2; arc: 999; border: 4,12,4,12;"
                );

            } else {
                lblStatus.setForeground(new Color(239, 68, 68));
                lblStatus.putClientProperty(FlatClientProperties.STYLE, "background: #FEE2E2; arc: 999; border: 4,12,4,12;");
            }

            JPanel cellContainer = new JPanel(new GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.add(lblStatus);
            return cellContainer;
        }

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (column == 0 || column == 5) {
                label.setHorizontalAlignment(SwingConstants.CENTER);
            }
            if (column == 2 || column == 4) {
                label.setFont(table.getFont().deriveFont(Font.BOLD));
                label.setForeground(new Color(15, 23, 42));
            } else {
                label.setForeground(new Color(71, 85, 105));
            }
        }
        return c;
    }
}

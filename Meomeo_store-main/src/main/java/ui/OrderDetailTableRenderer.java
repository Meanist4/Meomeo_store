package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class OrderDetailTableRenderer extends DefaultTableCellRenderer {

    private static final Color CLR_TEXT = new Color(0x1E293B);
    private static final Color CLR_MUTED = new Color(0x94A3B8);
    private static final Color CLR_ROW_ODD = Color.WHITE;
    private static final Color CLR_ROW_EVEN = Color.WHITE;
    private static final Color CLR_SELECTED = new Color(0xFFF7ED);

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {

        int modelCol = table.convertColumnIndexToModel(column);

        Color rowBg = isSelected ? CLR_SELECTED
                : (row % 2 == 0 ? CLR_ROW_ODD : CLR_ROW_EVEN);

        JLabel lbl = new JLabel(value != null ? value.toString() : "");
        lbl.setOpaque(true);
        lbl.setBackground(rowBg);
        lbl.setForeground(CLR_TEXT);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setVerticalAlignment(SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF1F5F9)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        switch (modelCol) {
            case 0 -> {
                String raw = value != null ? value.toString() : "";
                try {
                    lbl.setText(String.format("%02d", Integer.parseInt(raw)));
                } catch (NumberFormatException ex) {
                    lbl.setText(raw);
                }
                lbl.setForeground(CLR_MUTED);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            case 1 -> {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }
            case 2 -> {
                lbl.setForeground(CLR_MUTED);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            case 3 ->
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            case 4 -> {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            default ->
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
        }

        return lbl;
    }
}

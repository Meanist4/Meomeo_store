package ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProductActionCellRenderer implements TableCellRenderer {
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JButton btnEdit   = new JButton("Edit");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnBarcode = new JButton("Barcode");

    public ProductActionCellRenderer() {
        styleBtn(btnEdit,    new Color(59, 130, 246), Color.WHITE, 45);
        styleBtn(btnDelete,  new Color(239, 68, 68),  Color.WHITE, 55);
        styleBtn(btnBarcode, new Color(16, 185, 129), Color.WHITE, 60);
        panel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnEdit, gbc);
        panel.add(btnDelete, gbc);
        panel.add(btnBarcode, gbc);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return panel;
    }

    private void styleBtn(JButton btn, Color bg, Color fg, int width) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(width, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
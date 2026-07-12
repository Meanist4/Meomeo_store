package ui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.function.IntConsumer;

public class ProductActionCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JButton btnEdit   = new JButton("Edit");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnBarcode = new JButton("Barcode");
    private int currentRow;

    public ProductActionCellEditor(IntConsumer onEdit, IntConsumer onDelete, IntConsumer onShowBarcode) {
        styleBtn(btnEdit,    new Color(59, 130, 246), Color.WHITE, 45);
        styleBtn(btnDelete,  new Color(239, 68, 68),  Color.WHITE, 55);
        styleBtn(btnBarcode, new Color(16, 185, 129), Color.WHITE, 60);

        btnEdit.addActionListener(e -> {
            fireEditingStopped();
            onEdit.accept(currentRow);
        });
        btnDelete.addActionListener(e -> {
            fireEditingStopped();
            onDelete.accept(currentRow);
        });
        btnBarcode.addActionListener(e -> {
            fireEditingStopped();
            onShowBarcode.accept(currentRow);
        });

        panel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 2, 0, 2);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnEdit, gbc);
        panel.add(btnDelete, gbc);
        panel.add(btnBarcode, gbc);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        currentRow = row;
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
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
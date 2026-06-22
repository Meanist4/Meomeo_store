package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

public class StandardTableHeaderRenderer extends DefaultTableCellRenderer {

    private static final Color FG_COLOR = new Color(100, 116, 139);  // slate-500
    private static final Color BG_COLOR = new Color(248, 250, 252);  // slate-50
    private static final Color BORDER_COLOR = new Color(226, 232, 240);  // slate-200

    private final int alignment;
    private final int horizontalPadding; // padding trái/phải (dùng cho LEFT alignment)

    public StandardTableHeaderRenderer() {
        this(SwingConstants.CENTER, 0);
    }

    public StandardTableHeaderRenderer(int alignment, int horizontalPadding) {
        this.alignment = alignment;
        this.horizontalPadding = horizontalPadding;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        lbl.setHorizontalAlignment(alignment);
        lbl.setFont(table.getFont().deriveFont(Font.BOLD, 11f));
        lbl.setForeground(FG_COLOR);
        lbl.setBackground(BG_COLOR);

        Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR);
        if (horizontalPadding > 0) {
            Border padding = BorderFactory.createEmptyBorder(0, horizontalPadding, 0, horizontalPadding);
            lbl.setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));
        } else {
            lbl.setBorder(bottomLine);
        }

        return lbl;
    }
}

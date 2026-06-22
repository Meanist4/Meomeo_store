package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import repository.ScheduleRepository.ShiftCell;

public class ScheduleTableRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        JPanel cellContainer = new JPanel();
        cellContainer.setBackground(table.getBackground());
        cellContainer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        if (column == 0) {
            cellContainer.setLayout(new GridBagLayout());
            String[] lines = value != null ? value.toString().split("\n") : new String[]{""};

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

            JLabel lblName = new JLabel(lines.length > 0 ? lines[0] : "");
            lblName.setFont(table.getFont().deriveFont(Font.BOLD, 13f));
            lblName.setForeground(new Color(15, 23, 42));

            JLabel lblCode = new JLabel(lines.length > 1 ? lines[1] : "");
            lblCode.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            lblCode.setForeground(new Color(148, 163, 184));

            textPanel.add(lblName);
            textPanel.add(lblCode);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            cellContainer.add(textPanel, gbc);
            return cellContainer;
        }

        cellContainer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;

        if (value instanceof ShiftCell shift) {
            JPanel shiftBox = new JPanel();
            shiftBox.setLayout(new BoxLayout(shiftBox, BoxLayout.Y_AXIS));
            shiftBox.setOpaque(true);
            shiftBox.setBackground(new Color(241, 245, 249));
            shiftBox.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            String startStr = shift.start.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String endStr = shift.end.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            JLabel lblStart = new JLabel(startStr, SwingConstants.CENTER);
            lblStart.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            lblStart.setForeground(new Color(71, 85, 105));
            lblStart.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblEnd = new JLabel("-" + endStr, SwingConstants.CENTER);
            lblEnd.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            lblEnd.setForeground(new Color(100, 116, 139));
            lblEnd.setAlignmentX(Component.CENTER_ALIGNMENT);

            shiftBox.add(lblStart);
            shiftBox.add(lblEnd);
            cellContainer.add(shiftBox, gbc);
        } else {
            JLabel lblDash = new JLabel("—");
            lblDash.setFont(table.getFont().deriveFont(14f));
            lblDash.setForeground(new Color(203, 213, 225));
            cellContainer.add(lblDash, gbc);
        }

        return cellContainer;
    }

    /**
     * Header renderer 2 dòng: "MON" trên, "16" dưới.
     * Cột 0 chỉ hiển thị text thông thường.
     */
    public static javax.swing.table.DefaultTableCellRenderer createHeaderRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                String text = value != null ? value.toString() : "";
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBackground(new Color(248, 250, 252));
                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

                if (column == 0) {
                    JLabel lbl = new JLabel(text);
                    lbl.setFont(table.getFont().deriveFont(Font.BOLD, 11f));
                    lbl.setForeground(new Color(100, 116, 139));
                    panel.add(lbl);
                    return panel;
                }

                String[] parts = text.split(" ");
                String dayName = parts.length > 0 ? parts[0] : text;
                String dayNumber = parts.length > 1 ? parts[1] : "";

                JPanel inner = new JPanel();
                inner.setOpaque(false);
                inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

                JLabel lblDay = new JLabel(dayName, SwingConstants.CENTER);
                lblDay.setFont(table.getFont().deriveFont(Font.BOLD, 10f));
                lblDay.setForeground(new Color(100, 116, 139));
                lblDay.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblNum = new JLabel(dayNumber, SwingConstants.CENTER);
                lblNum.setFont(table.getFont().deriveFont(Font.BOLD, 13f));
                lblNum.setForeground(new Color(30, 41, 59));
                lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);

                inner.add(lblDay);
                inner.add(lblNum);
                panel.add(inner);
                return panel;
            }
        };
    }
}

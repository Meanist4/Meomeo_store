package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class EmployeeTableRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (column == 4) {
            javax.swing.JLabel lblPill = new javax.swing.JLabel(value != null ? value.toString() : "");
            lblPill.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
            lblPill.setForeground(new java.awt.Color(71, 85, 105));
            lblPill.setOpaque(true);
            lblPill.setBackground(new java.awt.Color(241, 245, 249));
            lblPill.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));

            javax.swing.JPanel cellContainer = new javax.swing.JPanel(new java.awt.GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            cellContainer.add(lblPill, gbc);
            return cellContainer;
        }

        if (column == 5) {
            String status = (value != null) ? value.toString() : "Inactive";
            boolean active = "Active".equalsIgnoreCase(status);
            javax.swing.JLabel lblBadge = new javax.swing.JLabel((active ? "● Active" : "● Inactive"));
            lblBadge.setFont(table.getFont().deriveFont(java.awt.Font.PLAIN, 11f));
            lblBadge.setOpaque(true);
            lblBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 4, 12));
            if (active) {
                lblBadge.setForeground(new java.awt.Color(22, 163, 74));
                lblBadge.setBackground(new java.awt.Color(240, 253, 244));
            } else {
                lblBadge.setForeground(new java.awt.Color(100, 116, 139));
                lblBadge.setBackground(new java.awt.Color(241, 245, 249));
            }

            javax.swing.JPanel cellContainer = new javax.swing.JPanel(new java.awt.GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            cellContainer.add(lblBadge, gbc);
            return cellContainer;
        }

        if (column == 6) { // Cột Actions - chỉ tượng trưng, CHƯA nối logic, canh giữa dòng
            javax.swing.JButton btnEdit = new javax.swing.JButton("✎");
            javax.swing.JButton btnLock = new javax.swing.JButton("🔒");
            for (javax.swing.JButton b : new javax.swing.JButton[]{btnEdit, btnLock}) {
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                b.setBorderPainted(false);
                b.setOpaque(false);
                b.setForeground(new java.awt.Color(148, 163, 184));
                b.setFont(table.getFont().deriveFont(13f));
                b.setMargin(new java.awt.Insets(0, 6, 0, 6));
                b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
            javax.swing.JPanel cellContainer = new javax.swing.JPanel(new java.awt.GridBagLayout());
            cellContainer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            cellContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 0));
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.anchor = java.awt.GridBagConstraints.WEST;
            javax.swing.JPanel buttonRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
            buttonRow.setOpaque(false);
            buttonRow.add(btnEdit);
            buttonRow.add(btnLock);
            cellContainer.add(buttonRow, gbc);
            return cellContainer;
        }

        java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof javax.swing.JLabel label) {
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
            label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            switch (column) {
                case 1 -> {
                    label.setFont(table.getFont().deriveFont(java.awt.Font.BOLD));
                    label.setForeground(new java.awt.Color(15, 23, 42));
                }
                case 2 ->
                    label.setForeground(new java.awt.Color(37, 99, 235));
                case 0, 3 -> {
                    label.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
                    label.setForeground(new java.awt.Color(100, 116, 139));
                }
                default ->
                    label.setForeground(new java.awt.Color(71, 85, 105));
            }
        }
        return c;
    }

    public static void drawCatInBox(Graphics2D g, int x, int y, int size) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        int boxW = size;
        int boxH = (int) (size * 0.6);
        int boxX = x;
        int boxY = y + size - boxH;

        g2.setColor(new java.awt.Color(0, 0, 0, 25));
        g2.fillOval(boxX + 6, boxY + boxH - 4, boxW - 12, 14);

        g2.setColor(new java.awt.Color(222, 178, 122));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
        g2.setColor(new java.awt.Color(206, 158, 100));
        g2.fillRoundRect(boxX, boxY + boxH / 2, boxW, boxH / 2, 10, 10);
        g2.setColor(new java.awt.Color(168, 124, 76));
        g2.setStroke(new java.awt.BasicStroke(2f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        int flapH = boxH / 3 + 4;
        g2.setColor(new java.awt.Color(232, 192, 140));
        g2.fillPolygon(
                new int[]{boxX, boxX + boxW / 2 - 6, boxX},
                new int[]{boxY, boxY, boxY - flapH}, 3);
        g2.fillPolygon(
                new int[]{boxX + boxW, boxX + boxW / 2 + 6, boxX + boxW},
                new int[]{boxY, boxY, boxY - flapH}, 3);
        g2.setColor(new java.awt.Color(168, 124, 76));
        g2.drawPolygon(
                new int[]{boxX, boxX + boxW / 2 - 6, boxX},
                new int[]{boxY, boxY, boxY - flapH}, 3);
        g2.drawPolygon(
                new int[]{boxX + boxW, boxX + boxW / 2 + 6, boxX + boxW},
                new int[]{boxY, boxY, boxY - flapH}, 3);

        g2.setColor(new java.awt.Color(244, 230, 200, 200));
        g2.fillRect(boxX + boxW / 2 - 4, boxY, 8, boxH);
        g2.fillRect(boxX, boxY + boxH / 2 - 4, boxW, 8);

        int pawW = 14, pawH = 8;
        g2.setColor(new java.awt.Color(232, 168, 96));
        g2.fillRoundRect(boxX + boxW / 2 - pawW - 6, boxY - pawH / 2, pawW, pawH, 6, 6);
        g2.fillRoundRect(boxX + boxW / 2 + 6, boxY - pawH / 2, pawW, pawH, 6, 6);

        int headSize = (int) (size * 0.52);
        int headX = x + (size - headSize) / 2;
        int headY = boxY - headSize + flapH / 2 + 6;

        int earSize = headSize / 3;
        java.awt.geom.Path2D earL = new java.awt.geom.Path2D.Double();
        earL.moveTo(headX + earSize * 0.5, headY + earSize * 0.6);
        earL.lineTo(headX - earSize * 0.15, headY - earSize * 0.5);
        earL.curveTo(headX + earSize * 0.1, headY - earSize * 0.55,
                headX + earSize * 0.7, headY - earSize * 0.1,
                headX + earSize * 0.9, headY + earSize * 0.5);
        earL.closePath();
        java.awt.geom.Path2D earR = new java.awt.geom.Path2D.Double();
        earR.moveTo(headX + headSize - earSize * 0.5, headY + earSize * 0.6);
        earR.lineTo(headX + headSize + earSize * 0.15, headY - earSize * 0.5);
        earR.curveTo(headX + headSize - earSize * 0.1, headY - earSize * 0.55,
                headX + headSize - earSize * 0.7, headY - earSize * 0.1,
                headX + headSize - earSize * 0.9, headY + earSize * 0.5);
        earR.closePath();
        g2.setColor(new java.awt.Color(232, 168, 96));
        g2.fill(earL);
        g2.fill(earR);
        g2.setColor(new java.awt.Color(250, 200, 170));
        g2.fillPolygon(
                new int[]{headX + (int) (earSize * 0.25), headX + (int) (earSize * 0.05), headX + (int) (earSize * 0.6)},
                new int[]{headY + (int) (earSize * 0.45), headY - (int) (earSize * 0.2), headY + (int) (earSize * 0.15)}, 3);

        g2.setColor(new java.awt.Color(238, 178, 110));
        g2.fillOval(headX, headY, headSize, headSize);

        g2.setColor(new java.awt.Color(218, 152, 86, 160));
        g2.fillOval(headX + headSize - headSize / 4, headY + 4, headSize / 4, headSize / 3);

        g2.setColor(new java.awt.Color(60, 45, 30));
        g2.setStroke(new java.awt.BasicStroke(2.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        int eyeY = headY + headSize / 2 - 2;
        g2.drawArc(headX + headSize / 3 - 6, eyeY - 2, 12, 8, 0, 180);
        g2.drawArc(headX + headSize * 2 / 3 - 6, eyeY - 2, 12, 8, 0, 180);

        g2.setColor(new java.awt.Color(214, 120, 100));
        g2.fillOval(headX + headSize / 2 - 4, eyeY + 7, 8, 6);

        g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawArc(headX + headSize / 2 - 7, eyeY + 9, 7, 6, 200, 140);
        g2.drawArc(headX + headSize / 2, eyeY + 9, 7, 6, 200, 140);

        g2.setColor(new java.awt.Color(120, 100, 80, 180));
        g2.setStroke(new java.awt.BasicStroke(1.2f));
        int whiskerY = eyeY + 10;
        g2.drawLine(headX + 2, whiskerY, headX - 12, whiskerY - 3);
        g2.drawLine(headX + 2, whiskerY + 5, headX - 12, whiskerY + 5);
        g2.drawLine(headX + headSize - 2, whiskerY, headX + headSize + 12, whiskerY - 3);
        g2.drawLine(headX + headSize - 2, whiskerY + 5, headX + headSize + 12, whiskerY + 5);

        g2.setColor(new java.awt.Color(255, 180, 170, 110));
        g2.fillOval(headX + headSize / 4 - 4, eyeY + 3, 8, 6);
        g2.fillOval(headX + headSize * 3 / 4 - 4, eyeY + 3, 8, 6);

        g2.dispose();
    }
}

package ui;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.AbstractBorder;

public class CancelButtonStyler {

    private static final Color BORDER_NORMAL = Color.decode("#FEE2E2");
    private static final Color BORDER_HOVER  = Color.decode("#FEB2B2");

    private static final String STYLE_NORMAL =
            "background: #FFFFFF; foreground: #EF4444; iconTextGap: 8; font: bold 11pt;";
    private static final String STYLE_HOVER  =
            "background: #FFF5F5; foreground: #DC2626; iconTextGap: 8; font: bold 11pt;";

    private static final Insets PADDING = new Insets(8, 20, 8, 24);

    private CancelButtonStyler() {}

    public static void apply(JButton btn) {
        btn.setContentAreaFilled(false);
        btn.setFocusable(false);
        btn.setText("CANCEL SELECTED INVOICE");
        btn.setIcon(MenuIcons.trash());

        Color[] currentBorderColor = {BORDER_NORMAL};

        btn.setBorder(new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(currentBorderColor[0]);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return PADDING;
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(PADDING.top, PADDING.left, PADDING.bottom, PADDING.right);
                return insets;
            }
        });

        btn.putClientProperty(FlatClientProperties.STYLE, STYLE_NORMAL);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentBorderColor[0] = BORDER_HOVER;
                btn.putClientProperty(FlatClientProperties.STYLE, STYLE_HOVER);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentBorderColor[0] = BORDER_NORMAL;
                btn.putClientProperty(FlatClientProperties.STYLE, STYLE_NORMAL);
                btn.repaint();
            }
        });

        btn.revalidate();
        btn.repaint();
    }
}
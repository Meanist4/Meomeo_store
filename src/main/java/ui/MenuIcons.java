package ui;

import javax.swing.Icon;
import java.awt.*;

public class MenuIcons {

    public static Icon dashboard() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.fillRoundRect(x, y, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y, 7, 7, 2, 2);
                g2.fillRoundRect(x, y + 9, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y + 9, 7, 7, 2, 2);
            }
        };
    }

    public static Icon inventory() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolygon(
                        new int[]{x + 8, x + 16, x + 16, x + 8, x, x},
                        new int[]{y + 1, y + 5, y + 12, y + 15, y + 12, y + 5}, 6);
                g2.drawLine(x + 8, y + 1, x + 8, y + 15);
                g2.drawLine(x, y + 5, x + 8, y + 8);
                g2.drawLine(x + 16, y + 5, x + 8, y + 8);
            }
        };
    }

    public static Icon history() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(x + 1, y, 14, 16, 2, 2);
                g2.drawLine(x + 5, y + 5, x + 11, y + 5);
                g2.drawLine(x + 5, y + 9, x + 11, y + 9);
                g2.drawLine(x + 5, y + 12, x + 9, y + 12);
            }
        };
    }

    public static Icon humanResources() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(x + 3, y + 1, 6, 6);
                g2.drawArc(x, y + 9, 12, 8, 0, 180);
                g2.drawLine(x + 11, y + 12, x + 13, y + 14);
                g2.drawLine(x + 13, y + 14, x + 16, y + 9);
            }
        };
    }

    public static Icon trash() {
        return new SimpleIcon(14, 14) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawLine(x + 2, y + 3, x + 12, y + 3);
                g2.fillRect(x + 5, y + 1, 4, 2);
                g2.drawRect(x + 3, y + 4, 8, 10);
                g2.drawLine(x + 5, y + 6, x + 5, y + 12);
                g2.drawLine(x + 9, y + 6, x + 9, y + 12);
            }
        };
    }

    public static Icon calendar() {
        return new SimpleIcon(14, 14) {
            private final Color COLOR = Color.decode("#64748B");

            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setColor(COLOR);          // override màu foreground
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y + 2, 13, 11, 2, 2);
                g2.drawLine(x, y + 5, x + 13, y + 5);
                g2.fillRect(x + 3, y, 2, 2);
                g2.fillRect(x + 8, y, 2, 2);
            }
        };
    }

    private abstract static class SimpleIcon implements Icon {

        private final int w, h;

        SimpleIcon(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public final void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());   // default; draw() có thể override
            draw(g2, x, y);
            g2.dispose();
        }

        protected abstract void draw(Graphics2D g2, int x, int y);

        @Override
        public int getIconWidth() {
            return w;
        }

        @Override
        public int getIconHeight() {
            return h;
        }
    }
}

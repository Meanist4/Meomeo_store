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

    public static Icon backToSaleCounter() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.setStroke(new BasicStroke(1.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 5, y + 4, x + 1, y + 8);
                g2.drawLine(x + 1, y + 8, x + 5, y + 12);
                g2.drawLine(x + 1, y + 8, x + 7, y + 8);
                g2.drawLine(x + 8, y + 12, x + 14, y + 12);
                g2.drawRoundRect(x + 9, y + 6, 5, 5, 1, 1);
                g2.drawLine(x + 12, y + 5, x + 12, y + 4);
            }
        };
    }

    public static Icon customerManagement() {
        return new SimpleIcon(16, 16) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.fillOval(x + 9, y + 2, 4, 4);
                g2.fillRoundRect(x + 7, y + 7, 8, 7, 3, 3);
                g2.setColor(g2.getColor());
                g2.fillOval(x + 2, y + 3, 6, 6);
                g2.fillRect(x + 1, y + 10, 10, 6);
                g2.setColor(g2.getColor());
                g2.fillOval(x + 3, y + 4, 4, 4);
                g2.fillRoundRect(x + 1, y + 9, 8, 6, 3, 3);
            }
        };
    }

    public static Icon inventory() {
        return new SimpleIcon(18, 18) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Hộp lập phương - thu nhỏ vào trong 18x18
                g2.drawPolygon(
                        new int[]{x + 9, x + 17, x + 17, x + 9, x + 1, x + 1},
                        new int[]{y + 1, y + 5, y + 13, y + 16, y + 13, y + 5}, 6);
                g2.drawLine(x + 9, y + 1, x + 9, y + 16);
                g2.drawLine(x + 1, y + 5, x + 9, y + 9);
                g2.drawLine(x + 17, y + 5, x + 9, y + 9);
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
        return new SimpleIcon(18, 18) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Người - điều chỉnh tọa độ vào trong 18x18
                g2.drawOval(x + 3, y + 1, 7, 7);
                g2.drawArc(x + 1, y + 9, 12, 8, 0, 180);
                // Checkmark bên phải
                g2.drawLine(x + 12, y + 13, x + 14, y + 15);
                g2.drawLine(x + 14, y + 15, x + 17, y + 10);
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

    /**
     * Icon QR vẽ tay (finder pattern giả) dùng cho qrBtn ở SalesCounterFrame.
     * Chuyển nguyên logic từ SalesCounterFrame sang đây để gom mọi icon vào 1 nơi.
     */
    public static Icon paymentQr() {
        return new SimpleIcon(18, 18) {
            @Override
            protected void draw(Graphics2D g2, int x, int y) {
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                final double u = 1.2;

                java.util.function.BiConsumer<Integer, Integer> drawRoundFinderPattern = (ox, oy) -> {
                    int px = (int) (x + ox * u);
                    int py = (int) (y + oy * u);
                    int sizeOuter = (int) (5 * u);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(px, py, sizeOuter - 1, sizeOuter - 1, 4, 4);
                    int coreSize = (int) (1.5 * u);
                    int offset = (int) (1.6 * u);
                    g2.fillRoundRect(px + offset, py + offset, coreSize, coreSize, 2, 2);
                };
                drawRoundFinderPattern.accept(0, 0);
                drawRoundFinderPattern.accept(9, 0);
                drawRoundFinderPattern.accept(0, 9);

                int ax = (int) (x + 10 * u);
                int ay = (int) (y + 10 * u);
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(ax, ay, (int) (3 * u) - 1, (int) (3 * u) - 1, 2, 2);
                g2.fillRoundRect((int) (ax + u), (int) (ay + u), (int) u, (int) u, 1, 1);

                int[][] dataPoints = {
                    {6, 0}, {7, 1}, {6, 2}, {7, 3}, {6, 4}, {7, 5}, {6, 6}, {7, 7},
                    {0, 6}, {2, 6}, {4, 6}, {5, 6}, {9, 6}, {11, 6}, {13, 6},
                    {6, 9}, {6, 11}, {6, 13},
                    {9, 8}, {13, 8}, {13, 9}, {13, 11}, {11, 13}, {12, 13}
                };
                int dotSize = (int) Math.max(1, u);
                for (int[] pt : dataPoints) {
                    g2.fillRoundRect((int) (x + pt[0] * u), (int) (y + pt[1] * u), dotSize, dotSize, 2, 2);
                }
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
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
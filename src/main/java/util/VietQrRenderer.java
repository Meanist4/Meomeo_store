package util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public final class VietQrRenderer {

    private static final String DEFAULT_BANK_ID = "TPB";
    private static final String DEFAULT_ACCOUNT_NO = "05807910101";
    private static final String DEFAULT_ACCOUNT_NAME = "HOANG TRONG NGHIA";
    private static final String DEFAULT_DESCRIPTION = "Mon Staring Cat Shop";

    private VietQrRenderer() {
    }

    public static ImageIcon staticQr(Image baseImg, int size) {
        BufferedImage combined = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combined.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new RoundRectangle2D.Float(0, 0, size, size, 20, 20));
        g2d.drawImage(baseImg, 0, 0, size, size, null);
        g2d.dispose();
        return new ImageIcon(combined);
    }

    public static void renderAsync(double amount, int size, Color tintColor,
            Consumer<ImageIcon> onReady, Consumer<String> onError) {
        renderAsync(amount, size, tintColor, DEFAULT_BANK_ID, DEFAULT_ACCOUNT_NO,
                DEFAULT_ACCOUNT_NAME, DEFAULT_DESCRIPTION, onReady, onError);
    }

    public static void renderAsync(double amount, int size, Color tintColor,
            String bankId, String accountNo, String accountName, String description,
            Consumer<ImageIcon> onReady, Consumer<String> onError) {

        final String vietQrUrl;
        try {
            vietQrUrl = buildUrl(bankId, accountNo, accountName, description, amount);
        } catch (UnsupportedEncodingException e) {
            if (onError != null) {
                onError.accept("Lỗi khởi tạo cấu hình VietQR: " + e.getMessage());
            }
            return;
        }

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                URL url = new URL(vietQrUrl);
                return ImageIO.read(url);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage rawQrImage = get();
                    if (rawQrImage == null) {
                        if (onError != null) {
                            onError.accept("Không đọc được ảnh QR trả về.");
                        }
                        return;
                    }
                    BufferedImage tinted = tint(rawQrImage, tintColor);
                    BufferedImage rounded = roundCorners(tinted, size);
                    if (onReady != null) {
                        onReady.accept(new ImageIcon(rounded));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    if (onError != null) {
                        onError.accept("Lỗi hiển thị ảnh QR ở luồng xử lý: " + e.getMessage());
                    }
                }
            }
        }.execute();
    }

    private static String buildUrl(String bankId, String accountNo, String accountName,
            String description, double amount) throws UnsupportedEncodingException {
        return String.format(
                "https://img.vietqr.io/image/%s-%s-qr_only.jpg?amount=%.0f&addInfo=%s&accountName=%s",
                bankId, accountNo, amount,
                URLEncoder.encode(description, "UTF-8"),
                URLEncoder.encode(accountName, "UTF-8")
        );
    }

    private static BufferedImage tint(BufferedImage raw, Color tintColor) {
        BufferedImage out = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int tintRGB = tintColor.getRGB();
        int whiteRGB = Color.WHITE.getRGB();

        for (int x = 0; x < raw.getWidth(); x++) {
            for (int y = 0; y < raw.getHeight(); y++) {
                int pixel = raw.getRGB(x, y);
                if ((pixel & 0x00FFFFFF) < 0x007F7F7F) {
                    out.setRGB(x, y, tintRGB);
                } else {
                    out.setRGB(x, y, whiteRGB);
                }
            }
        }
        return out;
    }

    private static BufferedImage roundCorners(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new RoundRectangle2D.Float(0, 0, size, size, 20, 20));
        g2d.drawImage(src, 0, 0, size, size, null);
        g2d.dispose();
        return out;
    }
}
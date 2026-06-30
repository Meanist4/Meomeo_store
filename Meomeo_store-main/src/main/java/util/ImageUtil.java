package util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtil {

    public static Image scale(Image nguon, int kichThuoc) {
        BufferedImage hienTai = toBufferedImage(nguon);
        int w = hienTai.getWidth(), h = hienTai.getHeight();
        while (w / 2 >= kichThuoc && h / 2 >= kichThuoc) {
            w /= 2;
            h /= 2;
            hienTai = resizeStep(hienTai, w, h);
        }
        return resizeStep(hienTai, kichThuoc, kichThuoc);
    }

    private static BufferedImage resizeStep(BufferedImage src, int w, int h) {
        var out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage bi) {
            return bi;
        }
        int w = img.getWidth(null) > 0 ? img.getWidth(null) : 100;
        int h = img.getHeight(null) > 0 ? img.getHeight(null) : 100;
        var out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        var g2 = out.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return out;
    }
}

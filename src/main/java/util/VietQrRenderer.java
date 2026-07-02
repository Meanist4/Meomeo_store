package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;

public final class VietQrRenderer {

    private static final String DEFAULT_BANK_BIN = "970423"; // TPBank BIN
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
        renderAsync(amount, size, tintColor, DEFAULT_BANK_BIN, DEFAULT_ACCOUNT_NO,
                DEFAULT_ACCOUNT_NAME, DEFAULT_DESCRIPTION, onReady, onError);
    }

    public static void renderAsync(double amount, int size, Color tintColor,
            String bankBin, String accountNo, String accountName, String description,
            Consumer<ImageIcon> onReady, Consumer<String> onError) {

        // Auto map "TPB" -> "970423"
        final String finalBankBin = "TPB".equalsIgnoreCase(bankBin) || "TPBank".equalsIgnoreCase(bankBin) 
                ? "970423" : bankBin;

        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                // 1. Tạo chuỗi EMVCo VietQR chuẩn
                String qrContent = generateVietQRString(finalBankBin, accountNo, amount, description);
                
                // 2. Tạo QR Code image sử dụng thư viện ZXing
                MultiFormatWriter writer = new MultiFormatWriter();
                Map<EncodeHintType, Object> hints = new HashMap<>();
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hints.put(EncodeHintType.MARGIN, 1);
                BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, size, size, hints);
                return MatrixToImageWriter.toBufferedImage(bitMatrix);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage rawQrImage = get();
                    if (rawQrImage == null) {
                        if (onError != null) {
                            onError.accept("Không tạo được ảnh QR.");
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

    private static String generateVietQRString(String bankBin, String accountNo, double amount, String description) {
        StringBuilder sb = new StringBuilder();
        // Tag 00: Payload Format Indicator (000201)
        sb.append("000201");
        
        // Tag 01: Point of Initiation Method (010212 for dynamic)
        sb.append("010212");
        
        // Tag 38: Merchant Account Information
        StringBuilder sub38 = new StringBuilder();
        sub38.append("0010A000000727"); // GUID for NAPAS
        
        StringBuilder subOrgan = new StringBuilder();
        subOrgan.append("0006").append(bankBin);
        subOrgan.append(String.format("01%02d%s", accountNo.length(), accountNo));
        
        sub38.append(String.format("01%02d%s", subOrgan.length(), subOrgan.toString()));
        sub38.append("0208QRIBFTTA"); // Service Code
        
        sb.append(String.format("38%02d%s", sub38.length(), sub38.toString()));
        
        // Tag 53: Transaction Currency (5303704 - VND)
        sb.append("5303704");
        
        // Tag 54: Transaction Amount
        String amountStr = String.format("%.0f", amount);
        sb.append(String.format("54%02d%s", amountStr.length(), amountStr));
        
        // Tag 58: Country Code (5802VN)
        sb.append("5802VN");
        
        // Tag 62: Additional Data Field Template
        if (description != null && !description.trim().isEmpty()) {
            StringBuilder sub62 = new StringBuilder();
            String cleanDesc = removeAccents(description);
            sub62.append(String.format("08%02d%s", cleanDesc.length(), cleanDesc));
            sb.append(String.format("62%02d%s", sub62.length(), sub62.toString()));
        }
        
        // Tag 63: CRC
        sb.append("6304");
        
        // Calculate CRC-16
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        int crc = calculateCrc16(bytes);
        sb.append(String.format("%04X", crc));
        
        return sb.toString();
    }

    private static int calculateCrc16(byte[] bytes) {
        int polynomial = 0x1021;
        int crc = 0xFFFF; // Initial value

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        return crc & 0xFFFF;
    }

    private static String removeAccents(String str) {
        if (str == null) return "";
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
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
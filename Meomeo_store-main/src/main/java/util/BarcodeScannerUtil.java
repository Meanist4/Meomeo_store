package util;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BarcodeScannerUtil {

    public static void startScan(java.awt.Frame parent, Consumer<String> onScanned) {
        // Chạy trên thread riêng để không block EDT
        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                SwingUtilities.invokeLater(()
                        -> JOptionPane.showMessageDialog(parent,
                                "Không tìm thấy webcam!", "Lỗi",
                                JOptionPane.ERROR_MESSAGE));
                return;
            }
            webcam.setViewSize(new java.awt.Dimension(640, 480));
            webcam.open();

            // ── Tạo dialog preview ────────────────────────────────────────
            JDialog dialog = new JDialog(parent, "Scan Barcode", true);
            dialog.setSize(660, 540);
            dialog.setLocationRelativeTo(parent);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JLabel lblPreview = new JLabel();
            lblPreview.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel lblStatus = new JLabel("Hướng barcode vào camera...");
            lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblStatus.setForeground(new Color(102, 102, 102));

            JButton btnCancel = new JButton("Hủy");

            AtomicBoolean cancelled = new AtomicBoolean(false);
            AtomicBoolean found = new AtomicBoolean(false);

            btnCancel.addActionListener(e -> {
                cancelled.set(true);
                dialog.dispose();
            });
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancelled.set(true);
                    dialog.dispose();
                }
            });

            dialog.setLayout(new BorderLayout(8, 8));
            dialog.add(lblPreview, BorderLayout.CENTER);
            dialog.add(lblStatus, BorderLayout.NORTH);
            dialog.add(btnCancel, BorderLayout.SOUTH);

            MultiFormatReader reader = new MultiFormatReader();

            Thread scanThread = new Thread(() -> {
                while (!cancelled.get() && !found.get()) {
                    BufferedImage img = webcam.getImage();
                    if (img == null) {
                        continue;
                    }

                    ImageIcon icon = new ImageIcon(
                            img.getScaledInstance(640, 480, Image.SCALE_FAST));
                    SwingUtilities.invokeLater(() -> lblPreview.setIcon(icon));

                    try {
                        LuminanceSource source = new BufferedImageLuminanceSource(img);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = reader.decodeWithState(bitmap);

                        String rawBarcode = result.getText();
                        found.set(true);

                        SwingUtilities.invokeLater(() -> {
                            lblStatus.setForeground(new Color(0, 150, 0));
                            lblStatus.setText("✓ Đã quét: " + rawBarcode);
                        });
                        Thread.sleep(800);

                        SwingUtilities.invokeLater(() -> {
                            dialog.dispose();
                            onScanned.accept(rawBarcode); // trả về EDT
                        });
                    } catch (NotFoundException ignored) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancelled.set(true);
                        SwingUtilities.invokeLater(dialog::dispose);
                    }
                }

                // Đóng webcam sau khi xong
                if (webcam.isOpen()) {
                    webcam.close();
                }
            });

            scanThread.setDaemon(true);
            scanThread.start();
            SwingUtilities.invokeLater(() -> dialog.setVisible(true));

        }, "barcode-scan-thread").start();
    }
}

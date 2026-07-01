package views;

import entity.Product;

public class ProductCard extends javax.swing.JPanel {

    private Product product;
    private final java.util.Map<String, javax.swing.ImageIcon> cardImageCache = new java.util.concurrent.ConcurrentHashMap<>();

    public ProductCard() {
        initComponents();
        this.setLayout(null);
        this.setPreferredSize(new java.awt.Dimension(180, 210));
        this.setBackground(java.awt.Color.WHITE);
        this.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, ""
                + "arc: 15;"
                + "border: 1,#E2E8F0;");
        if (lblTag != null) {
            lblTag.setBounds(12, 10, 45, 18);
            lblTag.setOpaque(true);
            lblTag.setBackground(new java.awt.Color(247, 242, 237));
            lblTag.setForeground(new java.awt.Color(139, 92, 26));
            lblTag.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblTag.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc: 8; font: 10;");
        }
        if (lblImage != null) {
            lblImage.setBounds(15, 35, 130, 100);
            lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
        if (lblProductName != null) {
            lblProductName.setBounds(12, 140, 136, 38);
            lblProductName.setForeground(new java.awt.Color(45, 55, 72));
            lblProductName.setText("<html><body style='width: 105px;'>" + lblProductName.getText() + "</body></html>");
            lblProductName.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "font: bold 12;");
        }
        if (lblPrice != null) {
            lblPrice.setBounds(12, 182, 136, 20);
            lblPrice.setForeground(new java.awt.Color(226, 135, 67));
            lblPrice.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "font: bold 14;");
        }
    }

    private javax.swing.ImageIcon getCachedCardIcon(String imgPath) {
        // Xử lý trường hợp chuỗi trống hoặc null, trả về ảnh default meomeo.png
        if (imgPath == null || imgPath.isBlank() || "null".equals(imgPath)) {
            imgPath = "meomeo.png";
        }

        return cardImageCache.computeIfAbsent(imgPath, path -> {
            try {
                // 1. Logic bóc tách tên file y hệt Dashboard
                String fileName = path.contains("/")
                        ? path.substring(path.lastIndexOf('/') + 1)
                        : path;
                String resourcePath = "/images/" + fileName;

                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl == null) {
                    // Nếu ảnh custom lỗi, cố gắng fallback về ảnh mặc định meomeo.png
                    imgUrl = getClass().getResource("/images/meomeo.png");
                    if (imgUrl == null) {
                        return null;
                    }
                }

                // 2. Định nghĩa kích thước Logic cho Card
                int targetW = 130;
                int targetH = 100;

                // 3. Tính toán tỷ lệ màn hình (HiDPI / Retina) để nhân kích thước thực tế
                double scale = 1.0;
                var gc = getGraphicsConfiguration();
                if (gc != null) {
                    scale = gc.getDefaultTransform().getScaleX();
                }

                int thucW = Math.max(targetW, (int) Math.round(targetW * scale));
                int thucH = Math.max(targetH, (int) Math.round(targetH * scale));

                // 4. Sinh ra 2 phiên bản ảnh: Ảnh thường (Logic) và Ảnh nét (Thực tế khi màn hình scale)
                // Sử dụng hàm lọc mịn toSharpImage sẵn có của bạn
                java.awt.Image imgLogic = toSharpImage(imgUrl, targetW, targetH);
                java.awt.Image imgThuc = (thucW == targetW)
                        ? imgLogic
                        : toSharpImage(imgUrl, thucW, thucH);

                // 5. Trả về ảnh đa độ phân giải chống mờ
                return new javax.swing.ImageIcon(
                        new java.awt.image.BaseMultiResolutionImage(imgLogic, imgThuc));
            } catch (Exception ex) {
                System.err.println("❌ Lỗi load ảnh card: " + ex.getMessage());
                return null;
            }
        });
    }

    public void setProductData(entity.Product product, String categoryName) {
        this.product = product;

        if (lblTag != null) {
            lblTag.setText(categoryName);
        }
        if (lblProductName != null) {
            lblProductName.setText("<html><body style='width: 105px;'>" + product.getProductName() + "</body></html>");
        }
        if (lblPrice != null) {
            lblPrice.setText(String.format("%,.0f đ", product.getPrice()));
        }

        if (lblImage != null) {
            javax.swing.ImageIcon cardIcon = getCachedCardIcon(product.getImagePath());

            if (cardIcon != null) {
                lblImage.setIcon(cardIcon);
                lblImage.setText("");
            } else {
                lblImage.setIcon(null);
                lblImage.setText("No Image");
            }
        }
    }

    private java.awt.Image toSharpImage(java.net.URL imgURL, int targetW, int targetH) {
        try {
            java.awt.image.BufferedImage srcImg = javax.imageio.ImageIO.read(imgURL);
            if (srcImg == null) {
                return new javax.swing.ImageIcon(imgURL).getImage()
                        .getScaledInstance(targetW, targetH, java.awt.Image.SCALE_SMOOTH);
            }

            int srcW = srcImg.getWidth();
            int srcH = srcImg.getHeight();
            double scale = Math.min((double) targetW / srcW, (double) targetH / srcH);
            int scaledW = Math.max(1, (int) Math.round(srcW * scale));
            int scaledH = Math.max(1, (int) Math.round(srcH * scale));
            java.awt.image.BufferedImage current = srcImg;
            int w = srcW, h = srcH;
            while (w / 2 > scaledW && h / 2 > scaledH) {
                w = Math.max(w / 2, scaledW);
                h = Math.max(h / 2, scaledH);
                java.awt.image.BufferedImage tmp = new java.awt.image.BufferedImage(
                        w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D gtmp = tmp.createGraphics();
                gtmp.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gtmp.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                        java.awt.RenderingHints.VALUE_RENDER_QUALITY);
                gtmp.drawImage(current, 0, 0, w, h, null);
                gtmp.dispose();
                current = tmp;
            }
            java.awt.image.BufferedImage resized = new java.awt.image.BufferedImage(
                    scaledW, scaledH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(current, 0, 0, scaledW, scaledH, null);
            g2d.dispose();
            resized = sharpen(resized, 0.2f);
            java.awt.image.BufferedImage output = new java.awt.image.BufferedImage(
                    targetW, targetH, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D go = output.createGraphics();
            go.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            go.setColor(java.awt.Color.WHITE);
            go.fillRect(0, 0, targetW, targetH);
            int x = (targetW - scaledW) / 2;
            int y = (targetH - scaledH) / 2;
            go.drawImage(resized, x, y, null);
            go.dispose();

            return output;
        } catch (Exception e) {
            return new javax.swing.ImageIcon(imgURL).getImage()
                    .getScaledInstance(targetW, targetH, java.awt.Image.SCALE_SMOOTH);
        }
    }

    private java.awt.image.BufferedImage sharpen(java.awt.image.BufferedImage src, float amount) {
        float[] kernelData = {
            0f, -amount, 0f,
            -amount, 1f + 4f * amount, -amount,
            0f, -amount, 0f
        };
        java.awt.image.Kernel kernel = new java.awt.image.Kernel(3, 3, kernelData);
        java.awt.image.ConvolveOp op = new java.awt.image.ConvolveOp(
                kernel, java.awt.image.ConvolveOp.EDGE_NO_OP, null);

        java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(
                src.getWidth(), src.getHeight(), src.getType());
        op.filter(src, dst);
        return dst;
    }

    public Product getProduct() {
        return this.product;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblImage = new javax.swing.JLabel();
        lblTag = new javax.swing.JLabel();
        lblProductName = new javax.swing.JLabel();
        lblPrice = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(175, 200));
        setPreferredSize(new java.awt.Dimension(175, 220));

        lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImage.setText("Image");

        lblTag.setFont(new java.awt.Font("Segoe UI", 1, 10)); // NOI18N
        lblTag.setForeground(new java.awt.Color(107, 114, 128));
        lblTag.setText("Food");

        lblProductName.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblProductName.setForeground(new java.awt.Color(31, 41, 55));
        lblProductName.setText("Tên sản phẩm");

        lblPrice.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPrice.setForeground(new java.awt.Color(226, 135, 67));
        lblPrice.setText("Giá");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(117, 117, 117)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblPrice)
                    .addComponent(lblProductName)
                    .addComponent(lblTag)
                    .addComponent(lblImage))
                .addContainerGap(205, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(lblImage)
                .addGap(29, 29, 29)
                .addComponent(lblTag)
                .addGap(18, 18, 18)
                .addComponent(lblProductName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPrice)
                .addContainerGap(139, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblTag;
    // End of variables declaration//GEN-END:variables
}

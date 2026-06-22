package views;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import service.CategoryService;
import service.ProductService;
import service.impl.CategoryServiceImpl;
import service.impl.ProductServiceImpl;

public final class SalesCounterFrame extends javax.swing.JFrame {

    private final CategoryService categoryService = new CategoryServiceImpl();
    private final ProductService productService = new ProductServiceImpl();

    public SalesCounterFrame() {
        initComponents();

        String panelStyle = "arc: 20; background: #FFFFFF;";

        panelBarcode.putClientProperty(FlatClientProperties.STYLE, panelStyle);
        panelCashier.putClientProperty(FlatClientProperties.STYLE, panelStyle);
        panelOrderSummary.putClientProperty(FlatClientProperties.STYLE, panelStyle);
        panelPopular.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #F8F6F2;");
        panelCurrentOrder.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #F8F6F2;");
        panelChangeDue.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; background: #FFFFFF; border: 0,#00000000;");

        scrollPopular.setBorder(null);
        scrollPopular.setOpaque(false);
        scrollPopular.getVerticalScrollBar().setUnitIncrement(16);

        txtBarcodeSearch.putClientProperty(FlatClientProperties.STYLE, "arc: 15;");
        managerBtn.putClientProperty(FlatClientProperties.STYLE,
                "arc: 30; borderWidth: 1; borderColor: #E28743;");

        cashBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #E28743;"
                + "foreground: #FFFFFF;"
                + "borderWidth: 0;"
                + "arc: 15;");

        qrBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #4A5568;"
                + "border: 1,#E2E8F0;"
                + "arc: 15;");

        qrBtn.setIcon(new javax.swing.Icon() {
            @Override
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);
                g2.setColor(c.getForeground());
                double u = 1.2;
                java.util.function.BiConsumer<Integer, Integer> drawRoundFinderPattern = (ox, oy) -> {
                    int px = (int) (x + ox * u);
                    int py = (int) (y + oy * u);
                    int sizeOuter = (int) (5 * u);
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
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
                g2.setStroke(new java.awt.BasicStroke(1.0f));
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

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        });

        if (jScrollPane1 != null) {
            jScrollPane1.setOpaque(false);
            jScrollPane1.getViewport().setOpaque(true);
            jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
            jScrollPane1.putClientProperty(FlatClientProperties.STYLE, "arc: 0 0 20 20; clipChildren: true;");
            jScrollPane1.getViewport().putClientProperty(FlatClientProperties.STYLE,
                    "arc: 0 0 20 20; clipChildren: true;");
        }

        tableCurrentOrder.setOpaque(true);
        tableCurrentOrder.setShowVerticalLines(false);
        tableCurrentOrder.setRowHeight(35);

        var header = tableCurrentOrder.getTableHeader();
        if (header != null) {
            header.setReorderingAllowed(false);
        }

        java.awt.Dimension avatarSize = new java.awt.Dimension(65, 65);
        lbAvatarShop.setPreferredSize(avatarSize);
        lbAvatarShop.setMinimumSize(avatarSize);
        lbAvatarShop.setMaximumSize(avatarSize);
        lbAvatarShop.setOpaque(false);

        var imgURL = getClass().getResource("/images/avatar_round.png");
        if (imgURL != null) {
            var imgGoc = new javax.swing.ImageIcon(imgURL).getImage();

            double scale = 1.0;
            var gc = getGraphicsConfiguration();
            if (gc != null) {
                scale = gc.getDefaultTransform().getScaleX();
            }

            int kichThuocLogic = 65;
            int kichThuocThuc = Math.max(kichThuocLogic, (int) Math.round(kichThuocLogic * scale));

            var imgLogic = veLaiChatLuongCao(imgGoc, kichThuocLogic);
            var imgThuc = (kichThuocThuc == kichThuocLogic) ? imgLogic : veLaiChatLuongCao(imgGoc, kichThuocThuc);

            var imgMultiRes = new java.awt.image.BaseMultiResolutionImage(imgLogic, imgThuc);
            lbAvatarShop.setIcon(new javax.swing.ImageIcon(imgMultiRes));
            lbAvatarShop.setText("");
        } else {
            lbAvatarShop.setOpaque(true);
            lbAvatarShop.setBackground(new java.awt.Color(220, 220, 220));
            lbAvatarShop.setText("AD");
            lbAvatarShop.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lbAvatarShop.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        }

        panelCardParent.setOpaque(false);
        panelCardParent.putClientProperty(FlatClientProperties.STYLE, "background: null; arc: 0; border: 0,#00000000;");

        int sizeLogic = 180;
        java.awt.Dimension qrSizeFinal = new java.awt.Dimension(sizeLogic, sizeLogic);
        lbQRCode.setPreferredSize(qrSizeFinal);
        lbQRCode.setMinimumSize(qrSizeFinal);
        lbQRCode.setMaximumSize(qrSizeFinal);
        lbQRCode.setOpaque(false);

        var qrURL = getClass().getResource("/images/QR.jpg");
        if (qrURL != null) {
            var imgGocQR = new javax.swing.ImageIcon(qrURL).getImage();

            double scaleQR = 1.0;
            var gcQR = getGraphicsConfiguration();
            if (gcQR != null) {
                scaleQR = gcQR.getDefaultTransform().getScaleX();
            }

            int sizeThuc = Math.max(sizeLogic, (int) Math.round(sizeLogic * scaleQR));

            var imgLogicQR = veLaiChatLuongCao(imgGocQR, sizeLogic);
            var imgThucQR = (sizeThuc == sizeLogic) ? imgLogicQR : veLaiChatLuongCao(imgGocQR, sizeThuc);

            var imgMultiResQR = new java.awt.image.BaseMultiResolutionImage(imgLogicQR, imgThucQR);
            lbQRCode.setIcon(new javax.swing.ImageIcon(imgMultiResQR));
            lbQRCode.setText("");
        } else {
            lbQRCode.setText("Không tìm thấy ảnh QR.jpg");
            lbQRCode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }

        String subPanelStyle = "background: #FFFFFF; arc: 20; border: 1,#20000000,20,0;";
        panelCashView.putClientProperty(FlatClientProperties.STYLE, subPanelStyle);
        panelQRView.putClientProperty(FlatClientProperties.STYLE, subPanelStyle);

        javax.swing.table.DefaultTableModel orderModel = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"STT", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền", "Thao tác"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        tableCurrentOrder.setModel(orderModel);
        tableCurrentOrder.getColumnModel().getColumn(0).setPreferredWidth(35);  // STT
        tableCurrentOrder.getColumnModel().getColumn(1).setPreferredWidth(160); // Tên sản phẩm
        tableCurrentOrder.getColumnModel().getColumn(2).setPreferredWidth(55);  // Số lượng
        tableCurrentOrder.getColumnModel().getColumn(3).setPreferredWidth(75);  // Đơn giá
        tableCurrentOrder.getColumnModel().getColumn(4).setPreferredWidth(85);  // Thành tiền
        tableCurrentOrder.getColumnModel().getColumn(5).setPreferredWidth(65);  // Thao tác (Nút trừ)

        tableCurrentOrder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        tableCurrentOrder.setRowHeight(38);
        tableCurrentOrder.setShowHorizontalLines(true);
        tableCurrentOrder.setShowVerticalLines(false);
        tableCurrentOrder.setGridColor(new java.awt.Color(230, 235, 240));
        tableCurrentOrder.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        var orderHeader = tableCurrentOrder.getTableHeader();
        if (orderHeader != null) {
            orderHeader.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            orderHeader.setBackground(new java.awt.Color(245, 247, 250));
            orderHeader.setForeground(new java.awt.Color(74, 85, 104));
            orderHeader.setReorderingAllowed(false);

            orderHeader.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(218, 224, 233)),
                    javax.swing.BorderFactory.createEmptyBorder(6, 0, 6, 0)
            ));
        }
        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        tableCurrentOrder.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // STT -> Giữa
        tableCurrentOrder.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Số lượng -> Giữa
        tableCurrentOrder.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);  // Đơn giá -> Phải
        tableCurrentOrder.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);  // Thành tiền -> Phải

        class TableButtonEditor extends javax.swing.AbstractCellEditor
                implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {

            private final javax.swing.JButton button;
            private int currentRow;

            public TableButtonEditor() {
                button = new javax.swing.JButton("-");
                button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                button.setFocusable(false);
                button.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, ""
                        + "background: #E57373;"
                        + "foreground: #FFFFFF;"
                        + "arc: 8;"
                        + "borderWidth: 0;");
                button.addActionListener(e -> {
                    removeOneProductFromOrder(currentRow);
                    fireEditingStopped();
                });
            }

            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                return button;
            }

            @Override
            public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, int row, int column) {
                this.currentRow = row;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                return "-";
            }
        }

        btnScan.setContentAreaFilled(false);
        btnScan.setFocusPainted(false);
        btnScan.setBorderPainted(false);
        btnScan.setText("");
        btnScan.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                int w = c.getWidth();
                int h = c.getHeight();
                g2.setColor(new java.awt.Color(115, 61, 29));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.setColor(java.awt.Color.WHITE);
                g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int cx = w / 2;
                int cy = h / 2;
                int size = 10;
                int len = 4;
                g2.drawLine(cx - size, cy - size + len, cx - size, cy - size);
                g2.drawLine(cx - size, cy - size, cx - size + len, cy - size);
                g2.drawLine(cx + size, cy - size + len, cx + size, cy - size);
                g2.drawLine(cx + size, cy - size, cx + size - len, cy - size);
                g2.drawLine(cx - size, cy + size - len, cx - size, cy + size);
                g2.drawLine(cx - size, cy + size, cx - size + len, cy + size);
                g2.drawLine(cx + size, cy + size - len, cx + size, cy + size);
                g2.drawLine(cx + size, cy + size, cx + size - len, cy + size);
                g2.drawLine(cx - 5, cy, cx + 5, cy);
                g2.dispose();
            }
        });
        TableButtonEditor buttonEditor = new TableButtonEditor();
        tableCurrentOrder.getColumnModel().getColumn(5).setCellRenderer(buttonEditor);
        tableCurrentOrder.getColumnModel().getColumn(5).setCellEditor(buttonEditor);

        if (jScrollPane1 != null) {
            jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        renderPriceOnQRCode(0.00);

        loadProductGrid();

        txtCashReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }
        });
    }

    private void renderPriceOnQRCode(double amount) {
        try {
            int size = 180;
            if (amount <= 0) {
                var qrURL = getClass().getResource("/images/QR.jpg");
                if (qrURL != null) {
                    java.awt.Image baseImg = new javax.swing.ImageIcon(qrURL).getImage();
                    java.awt.image.BufferedImage combinedImage = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2d = combinedImage.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, size, size, 20, 20));
                    g2d.drawImage(baseImg, 0, 0, size, size, null);
                    g2d.dispose();
                    lbQRCode.setIcon(new javax.swing.ImageIcon(combinedImage));
                    lbQRCode.setText("");
                }
                return;
            }

            String BANK_ID = "TPB";
            String ACCOUNT_NO = "05807910101";
            String ACCOUNT_NAME = "HOANG TRONG NGHIA";
            String DESCRIPTION = "Mon Staring Cat Shop";

            String vietQrUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-qr_only.jpg?amount=%.0f&addInfo=%s&accountName=%s",
                    BANK_ID, ACCOUNT_NO, amount,
                    java.net.URLEncoder.encode(DESCRIPTION, "UTF-8"),
                    java.net.URLEncoder.encode(ACCOUNT_NAME, "UTF-8")
            );

            new javax.swing.SwingWorker<java.awt.image.BufferedImage, Void>() {
                @Override
                protected java.awt.image.BufferedImage doInBackground() throws Exception {
                    java.net.URL url = new java.net.URL(vietQrUrl);
                    return javax.imageio.ImageIO.read(url);
                }

                @Override
                protected void done() {
                    try {
                        java.awt.image.BufferedImage rawQrImage = get();
                        if (rawQrImage != null) {
                            java.awt.image.BufferedImage styledQr = new java.awt.image.BufferedImage(
                                    rawQrImage.getWidth(), rawQrImage.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB
                            );

                            int brownRGB = new java.awt.Color(115, 61, 29).getRGB();
                            int whiteRGB = java.awt.Color.WHITE.getRGB();

                            for (int x = 0; x < rawQrImage.getWidth(); x++) {
                                for (int y = 0; y < rawQrImage.getHeight(); y++) {
                                    int pixel = rawQrImage.getRGB(x, y);
                                    if ((pixel & 0x00FFFFFF) < 0x007F7F7F) {
                                        styledQr.setRGB(x, y, brownRGB);
                                    } else {
                                        styledQr.setRGB(x, y, whiteRGB);
                                    }
                                }
                            }

                            java.awt.image.BufferedImage finalImage = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                            java.awt.Graphics2D g2d = finalImage.createGraphics();
                            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                            g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, size, size, 20, 20));
                            g2d.drawImage(styledQr, 0, 0, size, size, null);
                            g2d.dispose();

                            lbQRCode.setIcon(new javax.swing.ImageIcon(finalImage));
                            lbQRCode.setText("");
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("❌ Lỗi hiển thị ảnh QR ở luồng xử lý: " + e.getMessage());
                        lbQRCode.setText("Lỗi kết nối QR");
                    }
                }
            }.execute();

        } catch (UnsupportedEncodingException e) {
            System.out.println("❌ Lỗi khởi tạo cấu hình VietQR: " + e.getMessage());
        }
    }

    private void addProductToOrder(entity.Product product) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableCurrentOrder.getModel();
        int rowCount = model.getRowCount();
        boolean isExist = false;

        for (int i = 0; i < rowCount; i++) {
            String productNameInTable = model.getValueAt(i, 1).toString();
            if (productNameInTable.equals(product.getProductName())) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 2).toString());
                int newQty = currentQty + 1;
                BigDecimal unitPrice = product.getPrice();
                BigDecimal newSubTotal = unitPrice.multiply(BigDecimal.valueOf(newQty));
                model.setValueAt(newQty, i, 2);
                model.setValueAt(String.format("%,.0f đ", newSubTotal), i, 4);

                isExist = true;
                break;
            }
        }
        if (!isExist) {
            int stt = rowCount + 1;
            model.addRow(new Object[]{
                stt,
                product.getProductName(),
                1,
                String.format("%,.0f đ", product.getPrice()),
                String.format("%,.0f đ", product.getPrice())
            });
        }
        updateOrderSummaryTotals();
    }

    private void updateOrderSummaryTotals() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableCurrentOrder.getModel();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < model.getRowCount(); i++) {
            String totalStr = model.getValueAt(i, 4).toString().replace("đ", "").replace(",", "").trim();
            subtotal = subtotal.add(new BigDecimal(totalStr));
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal totalPay = subtotal.subtract(discount);

        if (lbSubtotal != null) {
            lbSubtotal.setText(String.format("%,.0f đ", totalPay));
        }
        if (lbTotalPay != null) {
            lbTotalPay.setText(String.format("%,.0f đ", totalPay));
        }

        renderPriceOnQRCode(totalPay.doubleValue());
        calculateChangeDue();
    }

    public void loadProductGrid() {
        panelProductGrid.removeAll();
        java.util.List<entity.Product> listProduct = productService.getPopularProducts();
        java.util.List<entity.Category> listCategory = categoryService.getCategoryForFilter();

        if (listProduct == null || listProduct.isEmpty()) {
            System.out.println("⚠️ [DEBUG GRID]: Không lấy được sản phẩm nào từ Database!");
            panelProductGrid.revalidate();
            panelProductGrid.repaint();
            return;
        } else {
            System.out.println("📊 [DEBUG GRID]: Tìm thấy " + listProduct.size() + " sản phẩm. Tiến hành đúc Card.");
        }

        for (entity.Product p : listProduct) {
            String catName = listCategory.stream()
                    .filter(c -> c.getId() == p.getCategoryId())
                    .map(entity.Category::getCategoryName)
                    .findFirst()
                    .orElse("Item");
            ProductCard card = new ProductCard();
            card.setProductData(p, catName);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    ProductCard sourceCard = (ProductCard) evt.getSource();
                    if (sourceCard.getProduct() != null) {
                        addProductToOrder(sourceCard.getProduct());
                    }
                }
            });
            panelProductGrid.add(card);
        }
        int totalItems = listProduct.size();
        int columns = 4;
        int rows = (int) Math.ceil((double) totalItems / columns);

        // Chiều cao lý tưởng cho mỗi hàng card
        int calculatedHeight = rows * 255 + 25;

        // 2. GIẢI PHÁP ĐẶC TRỊ: Cấu hình lại FlowLayout để ép nó nhận diện chiều cao thực tế
        if (panelProductGrid.getLayout() instanceof java.awt.FlowLayout layout) {
            layout.setAlignment(java.awt.FlowLayout.LEFT);
            layout.setHgap(15);
            layout.setVgap(15);
            panelProductGrid.setAutoscrolls(true);
        }

        java.awt.Dimension gridBounds = new java.awt.Dimension(510, calculatedHeight);
        panelProductGrid.setPreferredSize(gridBounds);
        panelProductGrid.setMinimumSize(gridBounds);
        panelProductGrid.setSize(gridBounds);

        if (scrollPopular != null) {
            scrollPopular.setViewportView(panelProductGrid); // Đảm bảo găm chặt ruột hiển thị
            scrollPopular.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPopular.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            scrollPopular.setWheelScrollingEnabled(true);
            scrollPopular.getVerticalScrollBar().setUnitIncrement(18);
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            panelProductGrid.revalidate();
            panelProductGrid.repaint();
            if (scrollPopular != null) {
                scrollPopular.getViewport().revalidate();
                scrollPopular.revalidate();
                scrollPopular.repaint();
            }
        });

        txtCashReceived.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calculateChangeDue();
            }
        });
    }

    private void removeOneProductFromOrder(int rowIndex) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableCurrentOrder.getModel();

        int currentQty = Integer.parseInt(model.getValueAt(rowIndex, 2).toString());

        if (currentQty > 1) {
            int newQty = currentQty - 1;
            model.setValueAt(newQty, rowIndex, 2);
            String unitPriceStr = model.getValueAt(rowIndex, 3).toString().replace("đ", "").replace(",", "").trim();
            double unitPrice = Double.parseDouble(unitPriceStr);
            double newSubTotal = newQty * unitPrice;

            model.setValueAt(String.format("%,.0f đ", newSubTotal), rowIndex, 4);

        } else {
            model.removeRow(rowIndex);

            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }
        }

        updateOrderSummaryTotals();
    }

    private void calculateChangeDue() {
        try {
            String totalPayStr = lbTotalPay.getText().replace("đ", "").replace(",", "").trim();
            double totalPay = Double.parseDouble(totalPayStr);

            if (totalPay <= 0) {
                lbChangeDue.setText("0 đ");
                lbChangeDue.setForeground(new java.awt.Color(46, 204, 113));
                return;
            }

            String cashReceivedStr = txtCashReceived.getText().trim();
            if (cashReceivedStr.isEmpty()) {
                lbChangeDue.setText(String.format("Thiếu: %,.0f đ", totalPay));
                lbChangeDue.setForeground(java.awt.Color.RED);
                return;
            }

            double cashReceived = Double.parseDouble(cashReceivedStr);
            double changeDue = cashReceived - totalPay;
            if (changeDue < 0) {
                lbChangeDue.setText(String.format("Thiếu: %,.0f đ", Math.abs(changeDue)));
                lbChangeDue.setForeground(java.awt.Color.RED);
            } else {
                lbChangeDue.setText(String.format("%,.0f đ", changeDue));
                lbChangeDue.setForeground(new java.awt.Color(46, 204, 113));
            }

        } catch (NumberFormatException e) {
            lbChangeDue.setText("Số tiền lỗi!");
            lbChangeDue.setForeground(java.awt.Color.RED);
        }
    }

    private java.awt.Image veLaiChatLuongCao(java.awt.Image nguon, int kichThuoc) {
        java.awt.image.BufferedImage hienTai = toBufferedImage(nguon);
        int w = hienTai.getWidth(), h = hienTai.getHeight();
        while (w / 2 >= kichThuoc && h / 2 >= kichThuoc) {
            w /= 2;
            h /= 2;
            hienTai = resizeStep(hienTai, w, h);
        }
        return resizeStep(hienTai, kichThuoc, kichThuoc);
    }

    private java.awt.image.BufferedImage resizeStep(java.awt.image.BufferedImage src, int w, int h) {
        var ketQua = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var g2 = ketQua.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return ketQua;
    }

    private java.awt.image.BufferedImage toBufferedImage(java.awt.Image img) {
        if (img instanceof java.awt.image.BufferedImage bufferedImage) {
            return bufferedImage;
        }
        var bimg = new java.awt.image.BufferedImage(img.getWidth(null), img.getHeight(null),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var g2 = bimg.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return bimg;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelBarcode = new javax.swing.JPanel();
        btnBarcode = new javax.swing.JButton();
        txtBarcodeSearch = new javax.swing.JTextField();
        panelCashier = new javax.swing.JPanel();
        btnScan = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        managerBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lbAvatarShop = new javax.swing.JLabel();
        panelCurrentOrder = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCurrentOrder = new javax.swing.JTable();
        panelOrderSummary = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        lbTotalPay = new javax.swing.JLabel();
        panelCardParent = new javax.swing.JPanel();
        panelCashView = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtCashReceived = new javax.swing.JTextField();
        panelChangeDue = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        lbChangeDue = new javax.swing.JLabel();
        panelQRView = new javax.swing.JPanel();
        lbQRCode = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnConfirmOrder = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        lbSubtotal = new javax.swing.JLabel();
        cashBtn = new javax.swing.JButton();
        qrBtn = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        panelPopular = new javax.swing.JPanel();
        lblPopular = new javax.swing.JLabel();
        scrollPopular = new javax.swing.JScrollPane();
        panelProductGrid = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(245, 245, 245));

        panelBarcode.setBackground(new java.awt.Color(255, 255, 255));
        panelBarcode.setPreferredSize(new java.awt.Dimension(600, 80));

        btnBarcode.setForeground(new java.awt.Color(226, 135, 67));
        btnBarcode.setText("||||||||||");
        btnBarcode.setBorder(null);
        btnBarcode.setPreferredSize(new java.awt.Dimension(32, 20));

        txtBarcodeSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtBarcodeSearch.setForeground(new java.awt.Color(153, 153, 153));
        txtBarcodeSearch.setText("Scan barcode or search product...");
        txtBarcodeSearch.setMinimumSize(new java.awt.Dimension(64, 20));
        txtBarcodeSearch.setPreferredSize(new java.awt.Dimension(73, 30));

        javax.swing.GroupLayout panelBarcodeLayout = new javax.swing.GroupLayout(panelBarcode);
        panelBarcode.setLayout(panelBarcodeLayout);
        panelBarcodeLayout.setHorizontalGroup(
            panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBarcodeLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(btnBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelBarcodeLayout.setVerticalGroup(
            panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBarcodeLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelCashier.setBackground(new java.awt.Color(255, 255, 255));

        btnScan.setPreferredSize(new java.awt.Dimension(35, 35));
        btnScan.addActionListener(this::btnScanActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(110, 58, 25));
        jLabel4.setText("STAFF CHECK-IN");

        labelStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        labelStatus.setForeground(new java.awt.Color(38, 205, 111));
        labelStatus.setText("Đang đợi quét thẻ...");
        labelStatus.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout panelCashierLayout = new javax.swing.GroupLayout(panelCashier);
        panelCashier.setLayout(panelCashierLayout);
        panelCashierLayout.setHorizontalGroup(
            panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCashierLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(labelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        panelCashierLayout.setVerticalGroup(
            panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashierLayout.createSequentialGroup()
                .addGroup(panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCashierLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCashierLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStatus)))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setPreferredSize(new java.awt.Dimension(410, 85));

        managerBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        managerBtn.setForeground(new java.awt.Color(226, 135, 67));
        managerBtn.setText("Manager Dashboard");

        jLabel2.setBackground(new java.awt.Color(122, 67, 29));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(122, 67, 29));
        jLabel2.setText("Mon Staring ");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Cat shop");

        lbAvatarShop.setBackground(new java.awt.Color(255, 255, 255));
        lbAvatarShop.setText("Logo");
        lbAvatarShop.setPreferredSize(new java.awt.Dimension(45, 45));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lbAvatarShop, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(managerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbAvatarShop, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(managerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(15, 15, 15))
        );

        panelCurrentOrder.setBackground(new java.awt.Color(248, 246, 242));
        panelCurrentOrder.setForeground(new java.awt.Color(248, 246, 242));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(110, 58, 25));
        jLabel1.setText("Current Order");

        tableCurrentOrder.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Title 1"
            }
        ));
        tableCurrentOrder.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(tableCurrentOrder);
        tableCurrentOrder.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        javax.swing.GroupLayout panelCurrentOrderLayout = new javax.swing.GroupLayout(panelCurrentOrder);
        panelCurrentOrder.setLayout(panelCurrentOrderLayout);
        panelCurrentOrderLayout.setHorizontalGroup(
            panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCurrentOrderLayout.setVerticalGroup(
            panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42))
        );

        panelOrderSummary.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(110, 58, 25));
        jLabel8.setText("Order Summary");

        jLabel5.setText("---------------------------------------------------------------------------");

        jPanel1.setBackground(new java.awt.Color(248, 246, 242));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(110, 58, 25));
        jLabel10.setText("TOTAL TO PAY");

        lbTotalPay.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbTotalPay.setForeground(new java.awt.Color(227, 138, 69));
        lbTotalPay.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbTotalPay.setText("0 đ");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbTotalPay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbTotalPay, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelCardParent.setPreferredSize(new java.awt.Dimension(300, 400));
        panelCardParent.setLayout(new java.awt.CardLayout());

        panelCashView.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 102, 102));
        jLabel12.setText("Cash Received");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(102, 102, 102));
        jLabel13.setText("$");

        txtCashReceived.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        panelChangeDue.setBackground(new java.awt.Color(248, 246, 242));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Change Due");

        lbChangeDue.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbChangeDue.setForeground(new java.awt.Color(38, 205, 111));
        lbChangeDue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbChangeDue.setText("0 đ ");

        javax.swing.GroupLayout panelChangeDueLayout = new javax.swing.GroupLayout(panelChangeDue);
        panelChangeDue.setLayout(panelChangeDueLayout);
        panelChangeDueLayout.setHorizontalGroup(
            panelChangeDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChangeDueLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbChangeDue, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelChangeDueLayout.setVerticalGroup(
            panelChangeDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelChangeDueLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelChangeDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbChangeDue, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout panelCashViewLayout = new javax.swing.GroupLayout(panelCashView);
        panelCashView.setLayout(panelCashViewLayout);
        panelCashViewLayout.setHorizontalGroup(
            panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashViewLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addGroup(panelCashViewLayout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelCashViewLayout.setVerticalGroup(
            panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashViewLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addGroup(panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );

        panelCardParent.add(panelCashView, "panelCashView");

        panelQRView.setBackground(new java.awt.Color(255, 255, 255));

        lbQRCode.setText("jLabel19");

        javax.swing.GroupLayout panelQRViewLayout = new javax.swing.GroupLayout(panelQRView);
        panelQRView.setLayout(panelQRViewLayout);
        panelQRViewLayout.setHorizontalGroup(
            panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelQRViewLayout.createSequentialGroup()
                .addContainerGap(106, Short.MAX_VALUE)
                .addComponent(lbQRCode, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(75, 75, 75))
        );
        panelQRViewLayout.setVerticalGroup(
            panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbQRCode, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
        );

        panelCardParent.add(panelQRView, "panelQRView");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(102, 102, 102));
        jLabel9.setText("Discount");

        btnConfirmOrder.setBackground(new java.awt.Color(30, 188, 97));
        btnConfirmOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnConfirmOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirmOrder.setText("CONFIRM PAYMENT & PRINT INVOICE");

        btnCancelOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancelOrder.setForeground(new java.awt.Color(225, 59, 53));
        btnCancelOrder.setText("Cancel Order");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(102, 102, 102));
        jLabel16.setText("Subtotal");

        lbSubtotal.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lbSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbSubtotal.setText("0 đ");

        cashBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cashBtn.setForeground(new java.awt.Color(102, 102, 102));
        cashBtn.setText("$ Cash");
        cashBtn.addActionListener(this::cashBtnActionPerformed);

        qrBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        qrBtn.setForeground(new java.awt.Color(102, 102, 102));
        qrBtn.setText("QR Pay");
        qrBtn.addActionListener(this::qrBtnActionPerformed);

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(102, 102, 102));
        jLabel19.setText("Payment Method");

        javax.swing.GroupLayout panelOrderSummaryLayout = new javax.swing.GroupLayout(panelOrderSummary);
        panelOrderSummary.setLayout(panelOrderSummaryLayout);
        panelOrderSummaryLayout.setHorizontalGroup(
            panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelCardParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel8)
                                .addComponent(jLabel9))
                            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lbSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(jLabel19)))
                .addGap(17, 17, 17))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSummaryLayout.createSequentialGroup()
                        .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSummaryLayout.createSequentialGroup()
                        .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnConfirmOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23))))
        );
        panelOrderSummaryLayout.setVerticalGroup(
            panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelCardParent, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConfirmOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPopular.setBackground(new java.awt.Color(255, 255, 255));
        panelPopular.setLayout(new java.awt.BorderLayout());

        lblPopular.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblPopular.setForeground(new java.awt.Color(110, 58, 25));
        lblPopular.setText("Quick Access - Popular Items");
        panelPopular.add(lblPopular, java.awt.BorderLayout.PAGE_START);

        panelProductGrid.setBackground(new java.awt.Color(255, 255, 255));
        scrollPopular.setViewportView(panelProductGrid);

        panelPopular.add(scrollPopular, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 744, Short.MAX_VALUE)
                    .addComponent(panelCurrentOrder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPopular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCashier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOrderSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 384, Short.MAX_VALUE))
                .addGap(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCashier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelCurrentOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelPopular, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelOrderSummary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnScanActionPerformed

    private void cashBtnActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.CardLayout cl = (java.awt.CardLayout) panelCardParent.getLayout();
        cl.show(panelCardParent, "panelCashView");

        cashBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #E28743;"
                + "foreground: #FFFFFF;"
                + "borderWidth: 0;"
                + "arc: 15;");

        qrBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #4A5568;"
                + "borderColor: #E2E8F0;"
                + "borderWidth: 1;"
                + "arc: 15;");

        panelCardParent.revalidate();
        panelCardParent.repaint();
    }

    private void qrBtnActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.CardLayout cl = (java.awt.CardLayout) panelCardParent.getLayout();
        cl.show(panelCardParent, "panelQRView");

        cashBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #4A5568;"
                + "borderColor: #E2E8F0;"
                + "borderWidth: 1;"
                + "arc: 15;");
        qrBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #6F3B1A;"
                + "foreground: #FFFFFF;"
                + "borderWidth: 0;"
                + "arc: 15;");

        panelCardParent.revalidate();
        panelCardParent.repaint();
    }

    public static void main(String args[]) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            SalesCounterFrame frame = new SalesCounterFrame();
            frame.setLocationRelativeTo(null); // Căn giữa màn hình
            frame.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBarcode;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnConfirmOrder;
    private javax.swing.JButton btnScan;
    private javax.swing.JButton cashBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JLabel lbAvatarShop;
    private javax.swing.JLabel lbChangeDue;
    private javax.swing.JLabel lbQRCode;
    private javax.swing.JLabel lbSubtotal;
    private javax.swing.JLabel lbTotalPay;
    private javax.swing.JLabel lblPopular;
    private javax.swing.JButton managerBtn;
    private javax.swing.JPanel panelBarcode;
    private javax.swing.JPanel panelCardParent;
    private javax.swing.JPanel panelCashView;
    private javax.swing.JPanel panelCashier;
    private javax.swing.JPanel panelChangeDue;
    private javax.swing.JPanel panelCurrentOrder;
    private javax.swing.JPanel panelOrderSummary;
    private javax.swing.JPanel panelPopular;
    private javax.swing.JPanel panelProductGrid;
    private javax.swing.JPanel panelQRView;
    private javax.swing.JButton qrBtn;
    private javax.swing.JScrollPane scrollPopular;
    private javax.swing.JTable tableCurrentOrder;
    private javax.swing.JTextField txtBarcodeSearch;
    private javax.swing.JTextField txtCashReceived;
    // End of variables declaration//GEN-END:variables
}

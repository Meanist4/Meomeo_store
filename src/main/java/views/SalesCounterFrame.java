package views;

import com.formdev.flatlaf.FlatClientProperties;
import controller.OrderCartController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import repository.OrderRepository;
import service.CategoryService;
import service.ProductService;
import service.impl.CategoryServiceImpl;
import service.impl.ProductServiceImpl;
import ui.MenuIcons;
import ui.ScannerButtonUI;
import util.ImageUtil;
import util.VietQrRenderer;

public final class SalesCounterFrame extends javax.swing.JFrame {

    private final CategoryService categoryService = new CategoryServiceImpl();
    private final ProductService productService = new ProductServiceImpl();
    private DashBoardFrame dashBoard;
    private final OrderRepository orderRepository = new repository.OrderRepository();
    private final List<Integer> sessionOrderIds = new ArrayList<>();

    private OrderCartController activeCart() {
        return orderSessions.get(activeIndex);
    }
    private final List<OrderCartController> orderSessions = new ArrayList<>();
    private int activeIndex = 0;
    private final List<JButton> tabButtons = new ArrayList<>();

    public SalesCounterFrame() {
        initComponents();
        this.dashBoard = new DashBoardFrame(this);
        panelOrderSplit.removeAll();
        panelOrderSplit.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        panelOrderSplit.setBackground(new java.awt.Color(248, 246, 242));
        panelOrderSplit.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #F8F6F2;");

        btnAddOrder.setText("＋");
        btnAddOrder.setPreferredSize(new java.awt.Dimension(36, 30));
        btnAddOrder.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 1; borderColor: #E28743; foreground: #E28743;");
        btnAddOrder.addActionListener(e -> createNewOrder());
        panelOrderSplit.add(btnAddOrder);

        String styleNormal = "background: #00000000; "
                + "foreground: #4A5568; "
                + "arc: 12; "
                + "borderWidth: 0; "
                + "focusWidth: 0; "
                + "innerFocusWidth: 0; "
                + "hoverBackground: #EDF2F7;";
        String styleActive = "background: #E38A45; "
                + "foreground: #FFFFFF; "
                + "arc: 12; "
                + "borderWidth: 0; "
                + "focusWidth: 0; "
                + "innerFocusWidth: 0;";
        javax.swing.JButton[] menuButtons = {btnMain, btnProductInventory,
            btnOrder, btnEmployee, btnCustomer};
        for (javax.swing.JButton btn : menuButtons) {
            btn.setContentAreaFilled(true);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setBorder(new com.formdev.flatlaf.ui.FlatButtonBorder());
            btn.setMargin(new java.awt.Insets(8, 14, 8, 14));
            btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            btn.setIconTextGap(12);
            btn.putClientProperty(FlatClientProperties.STYLE, styleNormal);
        }

        btnMain.putClientProperty(FlatClientProperties.STYLE, styleActive);
        btnMain.setIcon(ui.MenuIcons.dashboard());
        btnProductInventory.setIcon(ui.MenuIcons.inventory());
        btnOrder.setIcon(ui.MenuIcons.history());
        btnEmployee.setIcon(ui.MenuIcons.humanResources());
        btnCustomer.setIcon(ui.MenuIcons.customerManagement());
        btnMain.putClientProperty("cardName", "cardSaleCounter");
        btnProductInventory.putClientProperty("cardName", "cardProduct");
        btnOrder.putClientProperty("cardName", "cardOrder");
        btnEmployee.putClientProperty("cardName", "cardEmployee");
        btnCustomer.putClientProperty("cardName", "cardCustomer");

        java.awt.CardLayout cardLayout = (java.awt.CardLayout) panelMenu.getLayout();
        cardLayout.show(panelMenu, "cardSaleCounter");
        for (javax.swing.JButton clickedBtn : menuButtons) {
            clickedBtn.addActionListener(e -> {
                for (javax.swing.JButton btn : menuButtons) {
                    if (btn == clickedBtn) {
                        btn.putClientProperty(FlatClientProperties.STYLE, styleActive);
                    } else {
                        btn.putClientProperty(FlatClientProperties.STYLE, styleNormal);
                    }
                }
                String targetCard = (String) clickedBtn.getClientProperty("cardName");
                if (targetCard != null) {
                    cardLayout.show(panelMenu, targetCard);
                }
                if (clickedBtn.getParent() != null) {
                    clickedBtn.getParent().revalidate();
                    clickedBtn.getParent().repaint();
                }
            });
        }

        createNewOrder();
        applyTableStyling();

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

        qrBtn.setIcon(MenuIcons.paymentQr());

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

            var imgLogic = ImageUtil.scale(imgGoc, kichThuocLogic);
            var imgThuc = (kichThuocThuc == kichThuocLogic) ? imgLogic : ImageUtil.scale(imgGoc, kichThuocThuc);

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

            var imgLogicQR = ImageUtil.scale(imgGocQR, sizeLogic);
            var imgThucQR = (sizeThuc == sizeLogic) ? imgLogicQR : ImageUtil.scale(imgGocQR, sizeThuc);

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
                    activeCart().removeOne(currentRow);
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

        reapplyButtonEditor();

        btnScan.setContentAreaFilled(false);
        btnScan.setFocusPainted(false);
        btnScan.setBorderPainted(false);
        btnScan.setText("");
        btnScan.setUI(new ScannerButtonUI());
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
                activeCart().calculateChangeDue();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                activeCart().calculateChangeDue();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                activeCart().calculateChangeDue();
            }
        });
    }

    private void switchToOrder(int index) {
        if (index < 0 || index >= orderSessions.size()) {
            return;
        }
        activeIndex = index;
        orderSessions.get(index).rebindTo(tableCurrentOrder, lbSubtotal, lbTotalPay,
                lbChangeDue, txtCashReceived, this::handleCartTotalChanged);
        applyTableStyling();
        reapplyButtonEditor();
        refreshTabUI();
    }

    private void createNewOrder() {
        OrderCartController newCart = new OrderCartController(
                tableCurrentOrder, lbSubtotal, lbTotalPay,
                lbChangeDue, txtCashReceived, this::handleCartTotalChanged
        );
        orderSessions.add(newCart);
        activeIndex = orderSessions.size() - 1;
        applyTableStyling();
        reapplyButtonEditor();

        addTabButton("HD #" + orderSessions.size());
        refreshTabUI();
        renderPriceOnQRCode(0.0);
        int pendingId = -1;
        sessionOrderIds.add(pendingId);

        if (dashBoard != null) {
            dashBoard.refreshAfterNewOrder();
        }

    }

    private void ensureOrderPersisted(int index) {
        if (sessionOrderIds.get(index) == -1) {
            int pendingId = orderRepository.createPendingOrder(1);
            sessionOrderIds.set(index, pendingId);
        }
    }

    private void refreshTabUI() {
        for (int i = 0; i < tabButtons.size(); i++) {
            JButton btn = tabButtons.get(i);
            String base = "HD #" + (i + 1);
            btn.setText(i == activeIndex ? base + " ●" : base);

            if (i == activeIndex) {
                btn.putClientProperty(FlatClientProperties.STYLE,
                        "background: #E28743; foreground: #FFFFFF; arc: 20; borderWidth: 0;");
            } else {
                btn.putClientProperty(FlatClientProperties.STYLE,
                        "background: #FFFFFF; foreground: #555555; arc: 20; borderWidth: 1; borderColor: #E28743;");
            }
        }
        panelOrderSplit.revalidate();
        panelOrderSplit.repaint();
    }

    private void applyTableStyling() {
        tableCurrentOrder.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableCurrentOrder.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableCurrentOrder.getColumnModel().getColumn(2).setPreferredWidth(55);
        tableCurrentOrder.getColumnModel().getColumn(3).setPreferredWidth(75);
        tableCurrentOrder.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableCurrentOrder.getColumnModel().getColumn(5).setPreferredWidth(65);

        tableCurrentOrder.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        tableCurrentOrder.setRowHeight(38);
        tableCurrentOrder.setShowHorizontalLines(true);
        tableCurrentOrder.setShowVerticalLines(false);
        tableCurrentOrder.setGridColor(new java.awt.Color(230, 235, 240));

        javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        tableCurrentOrder.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableCurrentOrder.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableCurrentOrder.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableCurrentOrder.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
    }

    private void reapplyButtonEditor() {
        class TableButtonEditor extends javax.swing.AbstractCellEditor
                implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {

            private final javax.swing.JButton button;
            private int currentRow;

            public TableButtonEditor() {
                button = new javax.swing.JButton("-");
                button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                button.setFocusable(false);
                button.putClientProperty(FlatClientProperties.STYLE, ""
                        + "background: #E57373;"
                        + "foreground: #FFFFFF;"
                        + "arc: 8;"
                        + "borderWidth: 0;");
                button.addActionListener(e -> {
                    activeCart().removeOne(currentRow);
                    fireEditingStopped();
                });
            }

            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table,
                    Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return button;
            }

            @Override
            public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table,
                    Object value, boolean isSelected, int row, int column) {
                this.currentRow = row;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                return "-";
            }
        }

        TableButtonEditor editor = new TableButtonEditor();
        tableCurrentOrder.getColumnModel().getColumn(5).setCellRenderer(editor);
        tableCurrentOrder.getColumnModel().getColumn(5).setCellEditor(editor);
    }

    private void addTabButton(String label) {
        int index = tabButtons.size();
        JButton tab = new JButton(label);
        tab.setFocusPainted(false);
        tab.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 1; borderColor: #E28743;");
        tab.addActionListener(e -> switchToOrder(index));

        tabButtons.add(tab);
        panelOrderSplit.remove(btnAddOrder);
        panelOrderSplit.add(tab);
        panelOrderSplit.add(btnAddOrder);

        panelOrderSplit.revalidate();
        panelOrderSplit.repaint();
    }

    private void renderPriceOnQRCode(double amount) {
        int size = 180;

        if (amount <= 0) {
            var qrURL = getClass().getResource("/images/QR.jpg");
            if (qrURL != null) {
                java.awt.Image baseImg = new javax.swing.ImageIcon(qrURL).getImage();
                lbQRCode.setIcon(VietQrRenderer.staticQr(baseImg, size));
                lbQRCode.setText("");
            }
            return;
        }

        VietQrRenderer.renderAsync(amount, size, new java.awt.Color(115, 61, 29),
                icon -> {
                    lbQRCode.setIcon(icon);
                    lbQRCode.setText("");
                },
                errorMessage -> {
                    System.out.println("❌ " + errorMessage);
                    lbQRCode.setText("Lỗi kết nối QR");
                });
    }

    private void handleCartTotalChanged(double amount) {
        renderPriceOnQRCode(amount);

        int orderId = sessionOrderIds.get(activeIndex);
        if (orderId == -1) {
            return;
        }

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                orderRepository.updateOrderTotal(orderId, amount);
                return null;
            }

            @Override
            protected void done() {
                if (dashBoard != null) {
                    dashBoard.refreshOrderTableOnly();
                }
            }
        }.execute();
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
                        ensureOrderPersisted(activeIndex);
                        activeCart().addProduct(sourceCard.getProduct());
                    }
                }
            });
            panelProductGrid.add(card);
        }
        int totalItems = listProduct.size();
        int columns = 4;
        int rows = (int) Math.ceil((double) totalItems / columns);
        int calculatedHeight = rows * 255 + 25;
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
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        managerBtn = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lbAvatarShop = new javax.swing.JLabel();
        panelNav = new javax.swing.JPanel();
        btnMain = new javax.swing.JButton();
        btnProductInventory = new javax.swing.JButton();
        btnOrder = new javax.swing.JButton();
        btnEmployee = new javax.swing.JButton();
        btnCustomer = new javax.swing.JButton();
        panelMenu = new javax.swing.JPanel();
        panelSaleCounter = new javax.swing.JPanel();
        panelBarcode = new javax.swing.JPanel();
        btnBarcode = new javax.swing.JButton();
        txtBarcodeSearch = new javax.swing.JTextField();
        panelOrderSplit = new javax.swing.JPanel();
        btnAddOrder = new javax.swing.JButton();
        panelCurrentOrder = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableCurrentOrder = new javax.swing.JTable();
        panelPopular = new javax.swing.JPanel();
        lblPopular = new javax.swing.JLabel();
        scrollPopular = new javax.swing.JScrollPane();
        panelProductGrid = new javax.swing.JPanel();
        panelCashier = new javax.swing.JPanel();
        btnScan = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        panelOrderSummary = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
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
        btnConfirmOrder = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        lbSubtotal = new javax.swing.JLabel();
        cashBtn = new javax.swing.JButton();
        qrBtn = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        panelProduct = new javax.swing.JPanel();
        panelOrder = new javax.swing.JPanel();
        panelEmployee = new javax.swing.JPanel();
        panelCustomer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(245, 245, 245));

        panelHeader.setBackground(new java.awt.Color(255, 255, 255));
        panelHeader.setPreferredSize(new java.awt.Dimension(410, 85));

        managerBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        managerBtn.setForeground(new java.awt.Color(226, 135, 67));
        managerBtn.setText("Manager Dashboard");
        managerBtn.addActionListener(this::managerBtnActionPerformed);

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

        javax.swing.GroupLayout panelHeaderLayout = new javax.swing.GroupLayout(panelHeader);
        panelHeader.setLayout(panelHeaderLayout);
        panelHeaderLayout.setHorizontalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHeaderLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lbAvatarShop, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 768, Short.MAX_VALUE)
                .addComponent(managerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        panelHeaderLayout.setVerticalGroup(
            panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeaderLayout.createSequentialGroup()
                .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbAvatarShop, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                            .addGroup(panelHeaderLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panelHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(managerBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(15, 15, 15))
        );

        panelNav.setBackground(new java.awt.Color(255, 255, 255));

        btnMain.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnMain.setText("Payment");
        btnMain.setBorder(null);
        btnMain.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        btnProductInventory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnProductInventory.setText("Product");
        btnProductInventory.setBorder(null);
        btnProductInventory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        btnOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnOrder.setText("Invoice");
        btnOrder.setBorder(null);
        btnOrder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnOrder.addActionListener(this::btnOrderActionPerformed);

        btnEmployee.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEmployee.setText("Employee");
        btnEmployee.setBorder(null);
        btnEmployee.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEmployee.addActionListener(this::btnEmployeeActionPerformed);

        btnCustomer.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCustomer.setText("Customer");
        btnCustomer.setBorder(null);
        btnCustomer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCustomer.addActionListener(this::btnCustomerActionPerformed);

        javax.swing.GroupLayout panelNavLayout = new javax.swing.GroupLayout(panelNav);
        panelNav.setLayout(panelNavLayout);
        panelNavLayout.setHorizontalGroup(
            panelNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNavLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMain, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelNavLayout.setVerticalGroup(
            panelNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNavLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(btnMain, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(363, Short.MAX_VALUE))
        );

        panelMenu.setPreferredSize(new java.awt.Dimension(682, 100));
        panelMenu.setLayout(new java.awt.CardLayout());

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
                .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBarcodeLayout.setVerticalGroup(
            panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBarcodeLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        panelOrderSplit.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelOrderSplitLayout = new javax.swing.GroupLayout(panelOrderSplit);
        panelOrderSplit.setLayout(panelOrderSplitLayout);
        panelOrderSplitLayout.setHorizontalGroup(
            panelOrderSplitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSplitLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAddOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelOrderSplitLayout.setVerticalGroup(
            panelOrderSplitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderSplitLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addContainerGap())
        );

        panelCurrentOrder.setBackground(new java.awt.Color(248, 246, 242));
        panelCurrentOrder.setForeground(new java.awt.Color(248, 246, 242));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
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
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCurrentOrderLayout.setVerticalGroup(
            panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        panelPopular.setBackground(new java.awt.Color(248, 246, 242));

        lblPopular.setBackground(new java.awt.Color(248, 246, 242));
        lblPopular.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblPopular.setForeground(new java.awt.Color(110, 58, 25));
        lblPopular.setText("Quick Access - Popular Items");

        panelProductGrid.setBackground(new java.awt.Color(255, 255, 255));
        scrollPopular.setViewportView(panelProductGrid);

        javax.swing.GroupLayout panelPopularLayout = new javax.swing.GroupLayout(panelPopular);
        panelPopular.setLayout(panelPopularLayout);
        panelPopularLayout.setHorizontalGroup(
            panelPopularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPopularLayout.createSequentialGroup()
                .addComponent(lblPopular, javax.swing.GroupLayout.PREFERRED_SIZE, 618, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(panelPopularLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPopular)
                .addContainerGap())
        );
        panelPopularLayout.setVerticalGroup(
            panelPopularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPopularLayout.createSequentialGroup()
                .addComponent(lblPopular)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPopular)
                .addContainerGap())
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
                    .addComponent(labelStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        panelCashierLayout.setVerticalGroup(
            panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashierLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCashierLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStatus)))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        panelOrderSummary.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(110, 58, 25));
        jLabel8.setText("Order Summary");

        jPanel1.setBackground(new java.awt.Color(248, 246, 242));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
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
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbTotalPay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lbTotalPay, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
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
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(lbChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGroup(panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCashViewLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel12))
                    .addGroup(panelCashViewLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelCashViewLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        panelCashViewLayout.setVerticalGroup(
            panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashViewLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62, Short.MAX_VALUE)
                .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        panelCardParent.add(panelCashView, "panelCashView");

        panelQRView.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelQRViewLayout = new javax.swing.GroupLayout(panelQRView);
        panelQRView.setLayout(panelQRViewLayout);
        panelQRViewLayout.setHorizontalGroup(
            panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelQRViewLayout.createSequentialGroup()
                .addContainerGap(73, Short.MAX_VALUE)
                .addComponent(lbQRCode, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        panelQRViewLayout.setVerticalGroup(
            panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbQRCode, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
        );

        panelCardParent.add(panelQRView, "panelQRView");

        btnConfirmOrder.setBackground(new java.awt.Color(30, 188, 97));
        btnConfirmOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnConfirmOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirmOrder.setText("Confirm");
        btnConfirmOrder.addActionListener(this::btnConfirmOrderActionPerformed);

        btnCancelOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancelOrder.setForeground(new java.awt.Color(225, 59, 53));
        btnCancelOrder.setText("Cancel");
        btnCancelOrder.addActionListener(this::btnCancelOrderActionPerformed);

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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lbSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(17, 17, 17))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(27, 27, 27))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderSummaryLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnConfirmOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
            .addComponent(panelCardParent, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel16))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOrderSummaryLayout.setVerticalGroup(
            panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(4, 4, 4)
                .addComponent(lbSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
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
                .addContainerGap(8, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelSaleCounterLayout = new javax.swing.GroupLayout(panelSaleCounter);
        panelSaleCounter.setLayout(panelSaleCounterLayout);
        panelSaleCounterLayout.setHorizontalGroup(
            panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSaleCounterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelCurrentOrder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOrderSplit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBarcode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                    .addComponent(panelPopular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCashier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOrderSummary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelSaleCounterLayout.setVerticalGroup(
            panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSaleCounterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addComponent(panelBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(panelOrderSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelCurrentOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelPopular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addComponent(panelCashier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelOrderSummary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        panelMenu.add(panelSaleCounter, "cardSaleCounter");

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        panelProductLayout.setVerticalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        panelMenu.add(panelProduct, "cardProduct");

        javax.swing.GroupLayout panelOrderLayout = new javax.swing.GroupLayout(panelOrder);
        panelOrder.setLayout(panelOrderLayout);
        panelOrderLayout.setHorizontalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        panelOrderLayout.setVerticalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        panelMenu.add(panelOrder, "cardOrder");

        javax.swing.GroupLayout panelEmployeeLayout = new javax.swing.GroupLayout(panelEmployee);
        panelEmployee.setLayout(panelEmployeeLayout);
        panelEmployeeLayout.setHorizontalGroup(
            panelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        panelEmployeeLayout.setVerticalGroup(
            panelEmployeeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        panelMenu.add(panelEmployee, "cardEmployee");

        javax.swing.GroupLayout panelCustomerLayout = new javax.swing.GroupLayout(panelCustomer);
        panelCustomer.setLayout(panelCustomerLayout);
        panelCustomerLayout.setHorizontalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 953, Short.MAX_VALUE)
        );
        panelCustomerLayout.setVerticalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );

        panelMenu.add(panelCustomer, "cardCustomer");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 1200, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelNav, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 953, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelNav, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnScanActionPerformed

    private void managerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_managerBtnActionPerformed
        dashBoard.setVisible(true); // Bật dashboard lên
        this.setVisible(false);
    }//GEN-LAST:event_managerBtnActionPerformed

    private void btnConfirmOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmOrderActionPerformed
        OrderCartController cart = activeCart();
        if (cart.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giỏ hàng trống.",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        int orderId = sessionOrderIds.get(activeIndex);
        String paymentMethod = "Cash".equals(cashBtn.getClientProperty("_payMode"))
                || cashBtn.getClientProperty("_payMode") == null ? "Cash" : "Bank Transfer";
        List<repository.OrderRepository.NewOrderItem> items = new java.util.ArrayList<>();
        for (OrderCartController.CartItem ci : cart.getCartItems()) {
            items.add(new repository.OrderRepository.NewOrderItem(ci.productId, ci.quantity, ci.unitPrice));
        }
        double totalAmount = cart.getTotalAmount();
        final int finishedIndex = activeIndex;

        btnConfirmOrder.setEnabled(false); // chặn bấm lại trong lúc xử lý nền

        new javax.swing.SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                // Chỉ phần DB thuần (finalizeOrder) chạy ở luồng nền.
                return orderRepository.finalizeOrder(orderId, paymentMethod, totalAmount, "PAID", items);
            }

            @Override
            protected void done() {
                btnConfirmOrder.setEnabled(true);
                boolean ok;
                try {
                    ok = get();
                } catch (Exception e) {
                    ok = false;
                }
                if (!ok) {
                    javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this, "Lưu hóa đơn thất bại.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // dashBoard.refreshAfterNewOrder() đụng vào component Swing -> phải chạy ở EDT (done() đã là EDT)
                if (dashBoard != null) {
                    dashBoard.refreshAfterNewOrder();
                }
                loadProductGrid(); // Tải lại lưới sản phẩm để cập nhật số lượng tồn kho vừa trừ
                orderSessions.remove(finishedIndex);
                sessionOrderIds.remove(finishedIndex);
                panelOrderSplit.remove(tabButtons.get(finishedIndex));
                tabButtons.remove(finishedIndex);
                if (orderSessions.isEmpty()) {
                    createNewOrder();
                } else {
                    activeIndex = Math.max(0, finishedIndex - 1);
                    for (int i = 0; i < tabButtons.size(); i++) {
                        tabButtons.get(i).setText("HD #" + (i + 1));
                    }
                    switchToOrder(activeIndex);
                }
            }
        }.execute();
    }//GEN-LAST:event_btnConfirmOrderActionPerformed

    private void btnOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOrderActionPerformed

    private void btnEmployeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmployeeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEmployeeActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCustomerActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelOrderActionPerformed
        int orderId = sessionOrderIds.get(activeIndex);
        if (orderId != -1) {
            orderRepository.finalizeOrder(orderId, null, 0, "CANCELLED", null);
        }

        if (dashBoard != null) {
            dashBoard.refreshAfterNewOrder();
        }
        orderSessions.remove(activeIndex);
        sessionOrderIds.remove(activeIndex);
        panelOrderSplit.remove(tabButtons.get(activeIndex));
        tabButtons.remove(activeIndex);

        if (orderSessions.isEmpty()) {
            createNewOrder();
        } else {
            activeIndex = Math.max(0, activeIndex - 1);
            for (int i = 0; i < tabButtons.size(); i++) {
                tabButtons.get(i).setText("HD #" + (i + 1));
            }
            switchToOrder(activeIndex);
        }
    }//GEN-LAST:event_btnCancelOrderActionPerformed

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
        cashBtn.putClientProperty("_payMode", "Cash");
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

        cashBtn.putClientProperty("_payMode", "Bank Transfer");
        panelCardParent.revalidate();
        panelCardParent.repaint();
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup(); // Nên thêm FlatLaf vào đây nữa cho đồng bộ giao diện
        java.awt.EventQueue.invokeLater(() -> {
            SalesCounterFrame sales = new SalesCounterFrame();
            sales.setVisible(true); // Chỉ duy nhất quầy bán hàng hiện lên lúc mở app!
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnBarcode;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnConfirmOrder;
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnEmployee;
    private javax.swing.JButton btnMain;
    private javax.swing.JButton btnOrder;
    private javax.swing.JButton btnProductInventory;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
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
    private javax.swing.JPanel panelCustomer;
    private javax.swing.JPanel panelEmployee;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelNav;
    private javax.swing.JPanel panelOrder;
    private javax.swing.JPanel panelOrderSplit;
    private javax.swing.JPanel panelOrderSummary;
    private javax.swing.JPanel panelPopular;
    private javax.swing.JPanel panelProduct;
    private javax.swing.JPanel panelProductGrid;
    private javax.swing.JPanel panelQRView;
    private javax.swing.JPanel panelSaleCounter;
    private javax.swing.JButton qrBtn;
    private javax.swing.JScrollPane scrollPopular;
    private javax.swing.JTable tableCurrentOrder;
    private javax.swing.JTextField txtBarcodeSearch;
    private javax.swing.JTextField txtCashReceived;
    // End of variables declaration//GEN-END:variables
}

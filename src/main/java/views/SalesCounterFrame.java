package views;

import com.formdev.flatlaf.FlatClientProperties;
import controller.OrderCartController;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

import repository.OrderRepository;
import service.CategoryService;
import service.ProductService;
import service.impl.CategoryServiceImpl;
import service.impl.CustomerServiceImpl;
import service.impl.ProductServiceImpl;
import ui.InventoryTableRenderer;
import ui.MenuIcons;
import ui.ScannerButtonUI;
import util.ImageUtil;
import util.VietQrRenderer;

public final class SalesCounterFrame extends javax.swing.JFrame {

    private final CategoryService categoryService = new CategoryServiceImpl();
    private final ProductService productService = new ProductServiceImpl();
    private final CustomerServiceImpl customerService = new CustomerServiceImpl();
    private final repository.CustomerRepository customerRepository = new repository.CustomerRepository();
    private entity.Customer selectedCustomer;
    private javax.swing.JPopupMenu customerSuggestPopup;
    private javax.swing.Timer customerSearchTimer;
    private DashBoardFrame dashBoard;
    private final OrderRepository orderRepository = new repository.OrderRepository();
    private final repository.AttendanceRepository attendanceRepository = new repository.AttendanceRepository();
    private javax.swing.JPanel checkInListPanel;
    private javax.swing.JButton btnLoginQuick;
    private final List<Integer> sessionOrderIds = new ArrayList<>();
    private java.util.List<entity.Product> cachedProductList = new ArrayList<>();
    private java.util.List<entity.Category> cachedCategoryList = new ArrayList<>();
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> productTableSorter;
    private final java.util.Map<String, javax.swing.ImageIcon> productImageCache = new java.util.HashMap<>();
    private final repository.OrderRepository orderRepositoryHistory = orderRepository; // dùng lại orderRepository đã có sẵn

    private OrderCartController activeCart() {
        return orderSessions.get(activeIndex);
    }
    private final List<OrderCartController> orderSessions = new ArrayList<>();
    private int activeIndex = 0;
    private final List<JButton> tabButtons = new ArrayList<>();

    public SalesCounterFrame() {
        initComponents();
        entity.Employee user = util.UserSession.getInstance().getCurrentUser();
        if (user != null && user.getRoleId() == 1) {
            this.dashBoard = new DashBoardFrame(this);
        } else {
            this.dashBoard = null;
        }

        if (user != null && user.getRoleId() != 1) {
            if (!attendanceRepository.hasActiveCheckIn(user.getId()) && user.getId() > 0) {
                attendanceRepository.checkIn(user.getId());
            }
        }

        // Cấu hình cbb (jComboBox1) để hiển thị Tên + Role và chức năng đăng xuất
        if (user != null) {
            String roleName = "Staff";
            try {
                java.util.List<entity.Role> roles = new repository.EmployeeRepository().getAllRole();
                for (entity.Role r : roles) {
                    if (r.getId() == user.getRoleId()) {
                        roleName = r.getRoleName();
                        break;
                    }
                }
            } catch (Exception e) {
                roleName = user.getRoleId() == 1 ? "Manager" : "Staff";
            }
            String displayInfo = user.getFullName() + " (" + roleName + ")";
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { displayInfo, "Đăng xuất" }));
        } else {
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chưa đăng nhập", "Đăng nhập" }));
        }

        // Thiết lập phong cách hiển thị (Styling) cho jComboBox1 đồng bộ với giao diện chung
        jComboBox1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jComboBox1.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #6F3B1A;"
                + "borderColor: #D2B48C;"
                + "borderWidth: 1;"
                + "arc: 12;"
                + "buttonBackground: #FFFFFF;"
                + "buttonArrowColor: #6F3B1A;"
                + "focusWidth: 0;");
        jComboBox1.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                label.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
                if (isSelected) {
                    label.setBackground(new java.awt.Color(226, 135, 67)); // #E28743
                    label.setForeground(java.awt.Color.WHITE);
                } else {
                    label.setBackground(java.awt.Color.WHITE);
                    label.setForeground(new java.awt.Color(111, 59, 26)); // #6F3B1A
                }
                return label;
            }
        });

        panelOrderSplit.removeAll();
        panelOrderSplit.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        panelOrderSplit.setBackground(new java.awt.Color(248, 246, 242));
        panelOrderSplit.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #F8F6F2;");

        btnAddOrder.setText("＋");
        btnAddOrder.setPreferredSize(new java.awt.Dimension(36, 30));
        btnAddOrder.putClientProperty(FlatClientProperties.STYLE,
                "arc: 20; borderWidth: 1; borderColor: #E28743; foreground: #E28743;");
        btnAddOrder.addActionListener(e -> { if (checkLoginAndWarn()) createNewOrder(); });
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
            btnOrder, btnCustomer};
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
//        btnEmployee.setIcon(ui.MenuIcons.humanResources());
        btnCustomer.setIcon(ui.MenuIcons.customerManagement());
        btnMain.putClientProperty("cardName", "cardSaleCounter");
        btnProductInventory.putClientProperty("cardName", "cardProduct");
        btnOrder.putClientProperty("cardName", "cardOrder");
//        btnEmployee.putClientProperty("cardName", "cardEmployee");
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
        btnBarcode.addActionListener(e -> {
            if (!checkLoginAndWarn()) return;
            util.BarcodeScannerUtil.startScan(this, rawBarcode -> {
                if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
                    return;
                }
                entity.Product p = new repository.ProductRepository().findByBarcode(rawBarcode.trim());
                if (p != null) {
                    ensureOrderPersisted(activeIndex);
                    activeCart().addProduct(p);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Không tìm thấy sản phẩm với mã vạch: " + rawBarcode,
                            "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            });
        });
//        managerBtn.putClientProperty(FlatClientProperties.STYLE,
//                "arc: 30; borderWidth: 1; borderColor: #E28743;");

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
        tableCurrentOrder.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableCurrentOrder.getColumnModel().getColumn(0).setPreferredWidth(35);  // STT
        tableCurrentOrder.getColumnModel().getColumn(1).setPreferredWidth(160); // Tên sản phẩm
        tableCurrentOrder.getColumnModel().getColumn(2).setPreferredWidth(60);  // Còn lại
        tableCurrentOrder.getColumnModel().getColumn(3).setPreferredWidth(55);  // Số lượng
        tableCurrentOrder.getColumnModel().getColumn(4).setPreferredWidth(75);  // Đơn giá
        tableCurrentOrder.getColumnModel().getColumn(5).setPreferredWidth(85);  // Thành tiền

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
        tableCurrentOrder.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Còn lại -> Giữa
        tableCurrentOrder.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Số lượng -> Giữa
        tableCurrentOrder.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);  // Đơn giá -> Phải
        tableCurrentOrder.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);  // Thành tiền -> Phải

        btnScan.setContentAreaFilled(false);
        btnScan.setFocusPainted(false);
        btnScan.setBorderPainted(false);
        btnScan.setText("");
        btnScan.setUI(new ScannerButtonUI());

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

        initCustomerManagementInSale();
        initProductManagementInSale();
        customProductTableAppearance();
        loadCategoryComboBoxForTable();
        loadProductGrid();          // đã có sẵn, sẽ nạp cachedProductList/cachedCategoryList
        loadProductTableData();     // nạp dữ liệu cho bảng dựa trên cache vừa nạp
        initProductTableFilterEvents();
        customTransactionHistoryAppearance();
        initTransactionStatusFilter();
        initSearchInvoiceFilterInSale();
        initTransactionDateFilters();
        refreshTransactionHistory();
        loadCustomerTableData();
        initCustomerFilterEvents();
        loadCheckedInEmployees();
        updateCashierPanel();
    }

    private void customTransactionHistoryAppearance() {
        tableTransactionHistory.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableTransactionHistory.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"ORDER ID", "DATE & TIME", "CASHIER", "CUSTOMER", "PAYMENT", "TOTAL", "STATUS"}
        ) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false; // chỉ xem, không cho sửa/hủy trực tiếp
            }
        });

        tableTransactionHistory.setRowHeight(52);
        tableTransactionHistory.setShowHorizontalLines(true);
        tableTransactionHistory.setShowVerticalLines(false);
        tableTransactionHistory.setFillsViewportHeight(true);
        tableTransactionHistory.setSelectionBackground(new java.awt.Color(248, 246, 242));
        tableTransactionHistory.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tableTransactionHistory.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; rowSelectionForeground: #0F172A; lineColor: #F1F5F9;");

        javax.swing.table.JTableHeader h = tableTransactionHistory.getTableHeader();
        h.setPreferredSize(new java.awt.Dimension(h.getPreferredSize().width, 38));
        h.setDefaultRenderer(new ui.StandardTableHeaderRenderer(javax.swing.SwingConstants.LEFT, 12));

        ui.HistoryTableRenderer renderer = new ui.HistoryTableRenderer();
        for (int i = 0; i < tableTransactionHistory.getColumnCount(); i++) {
            tableTransactionHistory.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tableTransactionHistory.getColumnModel().getColumn(0).setPreferredWidth(135); // ORDER ID
        tableTransactionHistory.getColumnModel().getColumn(1).setPreferredWidth(155); // DATE & TIME
        tableTransactionHistory.getColumnModel().getColumn(2).setPreferredWidth(145); // CASHIER
        tableTransactionHistory.getColumnModel().getColumn(3).setPreferredWidth(175); // CUSTOMER
        tableTransactionHistory.getColumnModel().getColumn(4).setPreferredWidth(95);  // PAYMENT
        tableTransactionHistory.getColumnModel().getColumn(5).setPreferredWidth(120); // TOTAL
        tableTransactionHistory.getColumnModel().getColumn(6).setPreferredWidth(110); // STATUS
        java.awt.Container parent = tableTransactionHistory.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableTransactionHistory, "No transactions found"));
            scrollPane.setViewportView(tableTransactionHistory);
        }
    }

    private void loadTransactionHistoryLog() {
        java.util.List<repository.OrderRepository.OrderHistoryRow> rows = getFilteredTransactionHistoryRows();

        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableTransactionHistory.getModel();
        model.setRowCount(0);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (repository.OrderRepository.OrderHistoryRow row : rows) {
            String paymentValue = normalizeTransactionPayment(row.paymentMethod);
            String statusValue = row.status != null
                    ? row.status.trim().toUpperCase(java.util.Locale.ROOT)
                    : "PENDING";
            String orderIdStr = String.format("ORD-2026-%04d", row.id);
            String dateStr = (row.orderDate != null) ? sdf.format(row.orderDate) : "";
            String amountStr = String.format("%,.0f đ", row.totalAmount);
            String cashier = row.cashierName != null ? row.cashierName : "";
            String customer = (row.customerName != null && !row.customerName.isBlank())
                    ? row.customerName
                    : "Walk-in customer";

            model.addRow(new Object[]{
                orderIdStr,
                dateStr,
                cashier,
                customer,
                paymentValue,
                amountStr,
                statusValue
            });
        }

        lblRecordLog.setText("(" + rows.size() + " records)");
        tableTransactionHistory.revalidate();
        java.awt.Container vp = tableTransactionHistory.getParent();
        if (vp != null) {
            vp.revalidate();
            vp.repaint();
        }

    }

    private java.util.List<repository.OrderRepository.OrderHistoryRow> getFilteredTransactionHistoryRows() {
        String selectedStatus = cbStatus.getSelectedItem() != null
                ? cbStatus.getSelectedItem().toString() : "All";
        java.util.Date startDate = dateFrom.getDate();
        java.util.Date endDate = dateTo.getDate();

        java.util.List<repository.OrderRepository.OrderHistoryRow> rows
                = orderRepository.findOrderHistory(selectedStatus, startDate, endDate);
        String paymentFilter = (cbPaymentMethod != null && cbPaymentMethod.getSelectedItem() != null)
                ? cbPaymentMethod.getSelectedItem().toString() : "All";
        String searchText = (txtSearchInvoice != null)
                ? txtSearchInvoice.getText().trim().toLowerCase() : "";

        java.util.List<repository.OrderRepository.OrderHistoryRow> filteredRows = new java.util.ArrayList<>();
        for (repository.OrderRepository.OrderHistoryRow row : rows) {
            String paymentValue = normalizeTransactionPayment(row.paymentMethod);
            String orderIdStr = String.format("ORD-2026-%04d", row.id);
            String cashier = row.cashierName != null ? row.cashierName : "";
            String customer = (row.customerName != null && !row.customerName.isBlank())
                    ? row.customerName
                    : "Walk-in customer";

            if (!"All".equalsIgnoreCase(paymentFilter)
                    && !paymentFilter.equalsIgnoreCase(paymentValue)) {
                continue;
            }

            if (!searchText.isEmpty()) {
                boolean matchId = orderIdStr.toLowerCase().contains(searchText)
                        || String.valueOf(row.id).contains(searchText);
                boolean matchCashier = cashier.toLowerCase().contains(searchText);
                boolean matchCustomer = customer.toLowerCase().contains(searchText);
                if (!matchId && !matchCashier && !matchCustomer) {
                    continue;
                }
            }

            filteredRows.add(row);
        }

        return filteredRows;
    }

    private String normalizeTransactionPayment(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "";
        }
        String normalized = paymentMethod.trim().toUpperCase(java.util.Locale.ROOT);
        if (normalized.contains("BANK") && normalized.contains("TRANSFER")) {
            return "TRANSFER";
        }
        if (normalized.contains("CASH") || normalized.contains("TIỀN MẶT") || normalized.contains("TIEN MAT")) {
            return "CASH";
        }
        if (normalized.contains("TRANSFER") || normalized.contains("CHUYỂN KHOẢN") || normalized.contains("CHUYEN KHOAN")) {
            return "TRANSFER";
        }
        if (normalized.contains("CARD") || normalized.contains("THẺ") || normalized.contains("THE")) {
            return "CARD";
        }
        if (normalized.contains("E-WALLET") || normalized.contains("EWALLET") || normalized.contains("VÍ ĐIỆN TỬ") || normalized.contains("VI DIEN TU")) {
            return "E-WALLET";
        }
        return normalized;
    }

    private void initTransactionStatusFilter() {
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All", "PAID", "PENDING", "CANCELLED"}));
        cbStatus.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF;");
        cbStatus.addActionListener(e -> refreshTransactionHistory());

        initTransactionPaymentMethodFilter();
    }

    private void initTransactionPaymentMethodFilter() {
        java.util.LinkedHashSet<String> paymentOptions = new java.util.LinkedHashSet<>();
        paymentOptions.add("All");
        for (String paymentMethod : orderRepository.findDistinctPaymentMethods()) {
            String normalized = normalizeTransactionPayment(paymentMethod);
            if (!normalized.isBlank()) {
                paymentOptions.add(normalized);
            }
        }

        cbPaymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(
                paymentOptions.toArray(String[]::new)));
        cbPaymentMethod.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF;");
        for (java.awt.event.ActionListener listener : cbPaymentMethod.getActionListeners()) {
            cbPaymentMethod.removeActionListener(listener);
        }
        cbPaymentMethod.addActionListener(e -> refreshTransactionHistory());
    }

    private void initSearchInvoiceFilterInSale() {
        if (txtSearchInvoice == null) {
            return;
        }
        txtSearchInvoice.putClientProperty("JTextField.placeholder", "Search Order ID, customer, cashier...");
        txtSearchInvoice.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,10,0,10;");
        txtSearchInvoice.getDocument().addDocumentListener(onDocumentChange(this::refreshTransactionHistory));
    }

    public void refreshTransactionHistory() {
        loadTransactionHistoryLog();
    }

    private void initTransactionDateFilters() {
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateTo.setDateFormatString("dd/MM/yyyy");

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        dateFrom.setDate(cal.getTime());
        dateTo.setDate(new java.util.Date());

        styleTransactionDateChooser(dateFrom);
        styleTransactionDateChooser(dateTo);

        panelDate.setOpaque(false);
        panelDate.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; arc: 12; border: 2,6,2,6; borderWidth: 1; borderColor: #E2E8F0;");

        java.beans.PropertyChangeListener dateChangeListener = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadTransactionHistoryLog();
            }
        };
        dateFrom.addPropertyChangeListener(dateChangeListener);
        dateTo.addPropertyChangeListener(dateChangeListener);
    }

    private void styleTransactionDateChooser(com.toedter.calendar.JDateChooser choser) {
        choser.setOpaque(false);
        choser.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        if (choser.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor editor) {
            editor.setOpaque(false);
            editor.setEditable(false);
            editor.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
            editor.putClientProperty(FlatClientProperties.STYLE, "foreground: #334155; font: 11pt;");
        }
        for (java.awt.Component comp : choser.getComponents()) {
            if (comp instanceof javax.swing.JButton btn) {
                btn.setContentAreaFilled(false);
                btn.setFocusable(false);
                btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
                btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, "toolBarButton");
                btn.setIcon(ui.MenuIcons.calendar());
            }
        }
    }

    private javax.swing.event.DocumentListener onDocumentChange(Runnable action) {
        return new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                action.run();
            }
        };
    }

    private void customProductTableAppearance() {
        txtSearchProduct.putClientProperty("JTextField.placeholder", "Search products...");
        txtSearchProduct.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,10,0,10;");

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"PRODUCT ID", "IMAGE", "PRODUCT NAME", "CATEGORY", "PRICE", "STOCK"}
        ) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false; // chỉ xem, không cho sửa trực tiếp trên bảng
            }
        });

        tableProduct.setRowHeight(52);
        tableProduct.setShowHorizontalLines(true);
        tableProduct.setShowVerticalLines(false);
        tableProduct.setSelectionBackground(new java.awt.Color(248, 246, 242));
        tableProduct.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tableProduct.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; rowSelectionForeground: #0F172A; lineColor: #F1F5F9;");

        javax.swing.table.JTableHeader header = tableProduct.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 38));
        header.setDefaultRenderer(new ui.StandardTableHeaderRenderer());

        InventoryTableRenderer renderer = new ui.InventoryTableRenderer(this::getCachedProductIcon);
        for (int i = 0; i < tableProduct.getColumnCount(); i++) {
            tableProduct.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(70);   // PRODUCT ID
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(70);   // IMAGE
        tableProduct.getColumnModel().getColumn(1).setMaxWidth(70);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(200);  // PRODUCT NAME
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(100);  // CATEGORY
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(90);   // PRICE
        tableProduct.getColumnModel().getColumn(5).setPreferredWidth(50);   // STOCK

        java.awt.Container parent = tableProduct.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableProduct, "Nothing found!"));
            scrollPane.setViewportView(tableProduct);
        }
    }

    private void loadCategoryComboBoxForTable() {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement("All");
        categoryService.getCategoryForFilter().forEach(cat -> model.addElement(cat.getCategoryName()));
        cbAll.setModel(model);
    }

    private javax.swing.ImageIcon getCachedProductIcon(String imgPath) {
        if (imgPath == null || imgPath.isEmpty() || "null".equals(imgPath)) {
            return null;
        }
        return productImageCache.computeIfAbsent(imgPath, path -> {
            try {
                String fileName = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
                java.net.URL imgUrl = getClass().getResource("/images/" + fileName);
                if (imgUrl == null) {
                    return null;
                }
                java.awt.Image raw = new javax.swing.ImageIcon(imgUrl).getImage();
                java.awt.Image scaled = util.ImageUtil.scale(raw, 26);
                return new javax.swing.ImageIcon(scaled);
            } catch (Exception ex) {
                return null;
            }
        });
    }

    private void loadProductTableData() {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableProduct.getModel();
        model.setRowCount(0);

        for (entity.Product p : cachedProductList) {
            String catName = cachedCategoryList.stream()
                    .filter(c -> c.getId() == p.getCategoryId())
                    .map(entity.Category::getCategoryName)
                    .findFirst()
                    .orElse("Item");

            model.addRow(new Object[]{
                String.format("PRD-%04d", p.getId()),
                p.getImagePath(),
                p.getProductName(),
                catName,
                String.format("%,.0f đ", p.getPrice()),
                p.getQuantity()
            });
        }

        lblTotalItem.setText("(" + cachedProductList.size() + " items)");

        productTableSorter = new javax.swing.table.TableRowSorter<>(model);
        tableProduct.setRowSorter(productTableSorter);
        model.fireTableDataChanged();
    }

    private void switchToOrder(int index) {
        if (index < 0 || index >= orderSessions.size()) {
            return;
        }
        activeIndex = index;
        OrderCartController cart = orderSessions.get(index);
        cart.rebindTo(tableCurrentOrder, lbSubtotal, lbChangeDue, txtCashReceived, this::handleCartTotalChanged);
        txtSearchCustomer.setText(cart.getCustomerName() != null ? cart.getCustomerName() : "");
        lblCurrentOrder.setText(cart.getCustomerName() != null ? "Current Order - " + cart.getCustomerName() : "Current Order");
        applyTableStyling();
        refreshTabUI();
    }

    private void performProductTableFilter() {
        if (productTableSorter == null) {
            return;
        }
        String text = txtSearchProduct.getText().trim();
        productTableSorter.setRowFilter(text.isEmpty() ? null
                : javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 2));
        tableProduct.revalidate();
        java.awt.Container vp = tableProduct.getParent();
        if (vp != null) {
            vp.revalidate();
            vp.repaint();
        }
    }

    private void initProductTableFilterEvents() {
        txtSearchProduct.getDocument().addDocumentListener(onDocumentChange(this::performProductTableFilter));

        cbAll.addActionListener(e -> {
            if (productTableSorter == null) {
                return;
            }
            String selectedCategory = String.valueOf(cbAll.getSelectedItem()).trim();
            if (selectedCategory.equalsIgnoreCase("All") || selectedCategory.contains("All")) {
                productTableSorter.setRowFilter(null);
            } else {
                productTableSorter.setRowFilter(
                        javax.swing.RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(selectedCategory) + "$", 3));
            }
            tableProduct.revalidate();
            java.awt.Container vp = tableProduct.getParent();
            if (vp != null) {
                vp.revalidate();
                vp.repaint();
            }
        });
    }

    private void createNewOrder() {
        if (orderSessions.size() >= 6) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Chỉ được tạo tối đa 6 hóa đơn chờ.",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrderCartController newCart = new OrderCartController(
                tableCurrentOrder, lbSubtotal,
                lbChangeDue, txtCashReceived, this::handleCartTotalChanged
        );
        orderSessions.add(newCart);
        txtSearchCustomer.setText("");
        lblCurrentOrder.setText("Current Order");
        activeIndex = orderSessions.size() - 1;
        applyTableStyling();

        addTabButton("HD #" + orderSessions.size());
        refreshTabUI();
        renderPriceOnQRCode(0.0);
        int pendingId = -1;
        sessionOrderIds.add(pendingId);

        if (dashBoard != null) {
            dashBoard.refreshAfterNewOrder();
        }

        if (txtCashReceived != null) {
            txtCashReceived.setText("");
        }
        newCart.updateOrderSummaryTotals();
    }

    private void ensureOrderPersisted(int index) {
        if (sessionOrderIds.get(index) == -1) {
            entity.Employee user = util.UserSession.getInstance().getCurrentUser();
            int employeeId = (user != null) ? user.getId() : 1;
            Integer customerId = orderSessions.get(index).getCustomerId();
            int pendingId = orderRepository.createPendingOrder(employeeId, customerId);
            sessionOrderIds.set(index, pendingId);
        }
    }

    private void refreshTabUI() {
        for (int i = 0; i < tabButtons.size(); i++) {
            JButton btn = tabButtons.get(i);
            String customerName = orderSessions.get(i).getCustomerName();
            String base = (customerName != null) ? customerName : "HD #" + (i + 1);
            btn.setText(i == activeIndex ? base + " ●" : base);
            btn.setToolTipText(customerName != null ? customerName : "Hóa đơn " + (i + 1));
            btn.setPreferredSize(new java.awt.Dimension(85, 30));

            for (java.awt.event.ActionListener al : btn.getActionListeners()) {
                btn.removeActionListener(al);
            }
            final int index = i;
            btn.addActionListener(e -> switchToOrder(index));

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
        tableCurrentOrder.getColumnModel().getColumn(2).setPreferredWidth(60); // Còn lại
        tableCurrentOrder.getColumnModel().getColumn(3).setPreferredWidth(55); // Số lượng
        tableCurrentOrder.getColumnModel().getColumn(4).setPreferredWidth(75); // Đơn giá
        tableCurrentOrder.getColumnModel().getColumn(5).setPreferredWidth(85); // Thành tiền

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
        tableCurrentOrder.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Còn lại
        tableCurrentOrder.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Số lượng
        tableCurrentOrder.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);  // Đơn giá
        tableCurrentOrder.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);  // Thành tiền
    }

    private void addTabButton(String label) {
        int index = tabButtons.size();
        JButton tab = new JButton(label);
        tab.setFocusPainted(false);
        tab.setPreferredSize(new java.awt.Dimension(85, 30));
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

        List<OrderCartController.CartItem> cartItems = activeCart().getCartItems();
        List<repository.OrderRepository.NewOrderItem> items = new java.util.ArrayList<>();
        for (OrderCartController.CartItem ci : cartItems) {
            items.add(new repository.OrderRepository.NewOrderItem(ci.productId, ci.quantity, ci.unitPrice));
        }

        new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                orderRepository.updateOrderTotal(orderId, amount);
                orderRepository.syncPendingOrderDetails(orderId, items);
                return null;
            }
        }.execute();
    }

    private void initCustomerManagementInSale() {
        customerSuggestPopup = new javax.swing.JPopupMenu();
        customerSuggestPopup.setFocusable(false);

        txtSearchCustomer.getDocument().addDocumentListener(onDocumentChange(() -> {
            String keyword = txtSearchCustomer.getText().trim();
            if (customerSearchTimer != null && customerSearchTimer.isRunning()) {
                customerSearchTimer.stop();
            }
            if (keyword.isEmpty()) {
                customerSuggestPopup.setVisible(false);
                return;
            }
            customerSearchTimer = new javax.swing.Timer(300, e -> showCustomerSuggestions(keyword));
            customerSearchTimer.setRepeats(false);
            customerSearchTimer.start();
        }));

        btnAddCustomer.addActionListener(e -> {
            if (!checkLoginAndWarn()) return;
            views.AddCustomerFrame addFrame = new views.AddCustomerFrame(() -> {
                String phone = txtSearchCustomer.getText().trim();
                if (!phone.isEmpty()) {
                    java.util.List<entity.Customer> found = customerRepository.search(phone);
                    if (!found.isEmpty()) {
                        selectCustomerForOrder(found.get(0));
                    }
                }
            });
            addFrame.setVisible(true);
        });
    }

    private void showCustomerSuggestions(String keyword) {
        java.util.List<entity.Customer> results = customerRepository.search(keyword);
        customerSuggestPopup.removeAll();
        if (results.isEmpty()) {
            customerSuggestPopup.setVisible(false);
            return;
        }
        for (entity.Customer c : results) {
            javax.swing.JMenuItem item = new javax.swing.JMenuItem(c.getFullName() + " - " + c.getPhone());
            item.addActionListener(ev -> selectCustomerForOrder(c));
            customerSuggestPopup.add(item);
        }
        customerSuggestPopup.show(txtSearchCustomer, 0, txtSearchCustomer.getHeight());
    }

    private void selectCustomerForOrder(entity.Customer c) {
        activeCart().setCustomerId(c.getId());
        activeCart().setCustomerName(c.getFullName());
        txtSearchCustomer.setText(c.getFullName());
        customerSuggestPopup.setVisible(false);
        lblCurrentOrder.setText("Current Order - " + c.getFullName());
        refreshTabUI();

        int orderId = sessionOrderIds.get(activeIndex);
        if (orderId != -1) {
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    orderRepository.updateOrderCustomer(orderId, c.getId());
                    return null;
                }
            }.execute();
        }
    }

    private void initProductManagementInSale() {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement("All");
        categoryService.getCategoryForFilter().forEach(cat -> model.addElement(cat.getCategoryName()));
        cbCategory.setModel(model);

        cbCategory.addActionListener(e -> {
            String selectedCategory = String.valueOf(cbCategory.getSelectedItem()).trim();
            if (selectedCategory.equalsIgnoreCase("All") || selectedCategory.contains("All")) {
                renderProductGrid(cachedProductList);
            } else {
                java.util.List<entity.Product> filtered = cachedProductList.stream()
                        .filter(p -> cachedCategoryList.stream()
                        .anyMatch(c -> c.getId() == p.getCategoryId()
                        && c.getCategoryName().equalsIgnoreCase(selectedCategory)))
                        .collect(java.util.stream.Collectors.toList());
                renderProductGrid(filtered);
            }
        });
    }

    public void loadProductGrid() {
        java.util.List<entity.Product> listProduct = productService.getPopularProducts();
        java.util.List<entity.Category> listCategory = categoryService.getCategoryForFilter();
        cachedProductList = listProduct != null ? listProduct : new ArrayList<>();
        cachedCategoryList = listCategory != null ? listCategory : new ArrayList<>();
        renderProductGrid(cachedProductList);
    }

    private void renderProductGrid(java.util.List<entity.Product> listProduct) {
        panelProductGrid.removeAll();

        if (listProduct == null || listProduct.isEmpty()) {
            System.out.println("⚠️ [DEBUG GRID]: Không lấy được sản phẩm nào từ Database!");
            panelProductGrid.revalidate();
            panelProductGrid.repaint();
            return;
        } else {
            System.out.println("📊 [DEBUG GRID]: Tìm thấy " + listProduct.size() + " sản phẩm. Tiến hành đúc Card.");
        }

        for (entity.Product p : listProduct) {
            String catName = cachedCategoryList.stream()
                    .filter(c -> c.getId() == p.getCategoryId())
                    .map(entity.Category::getCategoryName)
                    .findFirst()
                    .orElse("Item");
            ProductCard card = new ProductCard();
            card.setProductData(p, catName);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (!checkLoginAndWarn()) return;
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

    private void loadCustomerTableData() {
        String keyword = (txtPhone != null) ? txtPhone.getText().trim() : "";

        new javax.swing.SwingWorker<java.util.List<entity.Customer>, Void>() {
            @Override
            protected java.util.List<entity.Customer> doInBackground() {
                return customerService.searchCustomers(keyword);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<entity.Customer> customers = get();
                    javax.swing.table.DefaultTableModel model
                            = (javax.swing.table.DefaultTableModel) jTable1.getModel();
                    
                    model.setRowCount(0);

                    int stt = 1;
                    for (var rec : customers) {
                        model.addRow(new Object[]{
                            stt++,
                            String.format("CTM-%04d", rec.getId()),
                            rec.getFullName(),
                            rec.getPhone()
                        });
                    }
                    jTable1.revalidate();
                    jTable1.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void initCustomerFilterEvents() {
        txtPhone.getDocument().addDocumentListener(
                onDocumentChange(() -> {
                    if (customerSearchTimer != null && customerSearchTimer.isRunning()) {
                        customerSearchTimer.stop();
                    }
                    customerSearchTimer = new javax.swing.Timer(300, event -> {
                        loadCustomerTableData();
                    });
                    customerSearchTimer.setRepeats(false);
                    customerSearchTimer.start();
                })
        );

        btnAddCustomer.addActionListener(e -> {
            if (!checkLoginAndWarn()) return;
            AddCustomerFrame addFrame = new AddCustomerFrame(this::loadCustomerTableData);
            addFrame.setVisible(true);
        });
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelHeader = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lbAvatarShop = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        panelNav = new javax.swing.JPanel();
        btnMain = new javax.swing.JButton();
        btnProductInventory = new javax.swing.JButton();
        btnOrder = new javax.swing.JButton();
        btnCustomer = new javax.swing.JButton();
        panelMenu = new javax.swing.JPanel();
        panelSaleCounter = new javax.swing.JPanel();
        panelBarcode = new javax.swing.JPanel();
        btnBarcode = new javax.swing.JButton();
        txtBarcodeSearch = new javax.swing.JTextField();
        panelOrderSplit = new javax.swing.JPanel();
        btnAddOrder = new javax.swing.JButton();
        panelCurrentOrder = new javax.swing.JPanel();
        lblCurrentOrder = new javax.swing.JLabel();
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
        jLabel16 = new javax.swing.JLabel();
        lbSubtotal = new javax.swing.JLabel();
        cashBtn = new javax.swing.JButton();
        qrBtn = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnConfirmOrder = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        cbCategory = new javax.swing.JComboBox<>();
        btnAddCustomer = new javax.swing.JButton();
        txtSearchCustomer = new javax.swing.JTextField();
        panelEmployeeCheckIn = new javax.swing.JScrollPane();
        panelProduct = new javax.swing.JPanel();
        txtSearchProduct = new javax.swing.JTextField();
        panelOrderManagement1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableProduct = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        lblTotalItem = new javax.swing.JLabel();
        cbAll = new javax.swing.JComboBox<>();
        panelOrder = new javax.swing.JPanel();
        panelBelowHeader = new javax.swing.JPanel();
        panelDate = new javax.swing.JPanel();
        dateTo = new com.toedter.calendar.JDateChooser();
        dateFrom = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        lblPaidOrder1 = new javax.swing.JLabel();
        lblPaidOrder2 = new javax.swing.JLabel();
        lblPaidOrder3 = new javax.swing.JLabel();
        cbPaymentMethod = new javax.swing.JComboBox<>();
        lblPaidOrder4 = new javax.swing.JLabel();
        txtSearchInvoice = new javax.swing.JTextField();
        panelOrderManagement2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableTransactionHistory = new javax.swing.JTable();
        jLabel27 = new javax.swing.JLabel();
        lblRecordLog = new javax.swing.JLabel();
        panelCustomer = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        txtPhone = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(245, 245, 245));

        panelHeader.setBackground(new java.awt.Color(255, 255, 255));
        panelHeader.setPreferredSize(new java.awt.Dimension(410, 85));

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

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
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
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelHeaderLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(jComboBox1))))
                    .addGroup(panelHeaderLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel3)))
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
                    .addComponent(btnOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMain, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
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
                .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(432, Short.MAX_VALUE))
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

        lblCurrentOrder.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblCurrentOrder.setForeground(new java.awt.Color(110, 58, 25));
        lblCurrentOrder.setText("Current Order");

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
                        .addComponent(lblCurrentOrder)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCurrentOrderLayout.setVerticalGroup(
            panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCurrentOrder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        panelPopular.setBackground(new java.awt.Color(248, 246, 242));

        lblPopular.setBackground(new java.awt.Color(248, 246, 242));
        lblPopular.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblPopular.setForeground(new java.awt.Color(110, 58, 25));
        lblPopular.setText("Product - Items");

        panelProductGrid.setBackground(new java.awt.Color(255, 255, 255));
        scrollPopular.setViewportView(panelProductGrid);

        javax.swing.GroupLayout panelPopularLayout = new javax.swing.GroupLayout(panelPopular);
        panelPopular.setLayout(panelPopularLayout);
        panelPopularLayout.setHorizontalGroup(
            panelPopularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPopularLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPopularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPopular, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                    .addGroup(panelPopularLayout.createSequentialGroup()
                        .addComponent(lblPopular)
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addContainerGap(69, Short.MAX_VALUE)
                .addComponent(lbQRCode, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        panelQRViewLayout.setVerticalGroup(
            panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbQRCode, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
        );

        panelCardParent.add(panelQRView, "panelQRView");

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

        btnConfirmOrder.setBackground(new java.awt.Color(30, 188, 97));
        btnConfirmOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnConfirmOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnConfirmOrder.setText("Confirm");
        btnConfirmOrder.addActionListener(this::btnConfirmOrderActionPerformed);

        btnCancelOrder.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancelOrder.setForeground(new java.awt.Color(225, 59, 53));
        btnCancelOrder.setText("Cancel");
        btnCancelOrder.addActionListener(this::btnCancelOrderActionPerformed);

        javax.swing.GroupLayout panelOrderSummaryLayout = new javax.swing.GroupLayout(panelOrderSummary);
        panelOrderSummary.setLayout(panelOrderSummaryLayout);
        panelOrderSummaryLayout.setHorizontalGroup(
            panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCardParent, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel16)
                    .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jSeparator1)
                            .addComponent(lbSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(15, 15, 15))
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(btnConfirmOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelOrderSummaryLayout.setVerticalGroup(
            panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelCardParent, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConfirmOrder)
                    .addComponent(btnCancelOrder))
                .addGap(0, 18, Short.MAX_VALUE))
        );

        cbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAddCustomer.setText("Add customer");
        btnAddCustomer.addActionListener(this::btnAddCustomerActionPerformed);

        txtSearchCustomer.addActionListener(this::txtSearchCustomerActionPerformed);

        javax.swing.GroupLayout panelSaleCounterLayout = new javax.swing.GroupLayout(panelSaleCounter);
        panelSaleCounter.setLayout(panelSaleCounterLayout);
        panelSaleCounterLayout.setHorizontalGroup(
            panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSaleCounterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(panelCurrentOrder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelOrderSplit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelBarcode, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 618, Short.MAX_VALUE)
                            .addComponent(panelPopular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSaleCounterLayout.createSequentialGroup()
                        .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 118, Short.MAX_VALUE)
                        .addComponent(txtSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnAddCustomer)
                        .addGap(14, 14, 14)))
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCashier, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOrderSummary, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(panelEmployeeCheckIn)))
                .addContainerGap())
        );
        panelSaleCounterLayout.setVerticalGroup(
            panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSaleCounterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelCashier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addComponent(panelOrderSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelCurrentOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnAddCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .addComponent(txtSearchCustomer)
                            .addComponent(cbCategory))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelPopular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelSaleCounterLayout.createSequentialGroup()
                        .addComponent(panelEmployeeCheckIn, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelOrderSummary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        panelMenu.add(panelSaleCounter, "cardSaleCounter");

        panelOrderManagement1.setBackground(new java.awt.Color(247, 246, 242));

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tableProduct);

        jLabel20.setBackground(new java.awt.Color(122, 67, 29));
        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 19)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(122, 67, 29));
        jLabel20.setText("All Products");

        lblTotalItem.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTotalItem.setForeground(new java.awt.Color(102, 102, 102));
        lblTotalItem.setText("(0 items)");

        javax.swing.GroupLayout panelOrderManagement1Layout = new javax.swing.GroupLayout(panelOrderManagement1);
        panelOrderManagement1.setLayout(panelOrderManagement1Layout);
        panelOrderManagement1Layout.setHorizontalGroup(
            panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderManagement1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalItem, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 933, Short.MAX_VALUE)
        );
        panelOrderManagement1Layout.setVerticalGroup(
            panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagement1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(lblTotalItem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cbAll.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbAll.setForeground(new java.awt.Color(102, 102, 102));
        cbAll.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(477, Short.MAX_VALUE))
            .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelProductLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panelOrderManagement1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelProductLayout.setVerticalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(605, Short.MAX_VALUE))
            .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelProductLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOrderManagement1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        panelMenu.add(panelProduct, "cardProduct");

        panelDate.setBackground(new java.awt.Color(255, 255, 255));

        dateTo.setPreferredSize(new java.awt.Dimension(80, 22));

        dateFrom.setPreferredSize(new java.awt.Dimension(80, 22));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(102, 102, 102));
        jLabel1.setText("-");

        javax.swing.GroupLayout panelDateLayout = new javax.swing.GroupLayout(panelDate);
        panelDate.setLayout(panelDateLayout);
        panelDateLayout.setHorizontalGroup(
            panelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDateLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        panelDateLayout.setVerticalGroup(
            panelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDateLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(panelDateLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        cbStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbStatus.setForeground(new java.awt.Color(102, 102, 102));
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbStatus.setPreferredSize(new java.awt.Dimension(74, 38));

        lblPaidOrder1.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder1.setText("From");

        lblPaidOrder2.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder2.setText("To");

        lblPaidOrder3.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder3.setText("Status");

        cbPaymentMethod.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbPaymentMethod.setForeground(new java.awt.Color(102, 102, 102));
        cbPaymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPaymentMethod.setPreferredSize(new java.awt.Dimension(74, 38));

        lblPaidOrder4.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder4.setText("Payment");

        txtSearchInvoice.addActionListener(this::txtSearchInvoiceActionPerformed);

        javax.swing.GroupLayout panelBelowHeaderLayout = new javax.swing.GroupLayout(panelBelowHeader);
        panelBelowHeader.setLayout(panelBelowHeaderLayout);
        panelBelowHeaderLayout.setHorizontalGroup(
            panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(panelDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(lblPaidOrder1)
                        .addGap(151, 151, 151)
                        .addComponent(lblPaidOrder2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPaidOrder3)
                    .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                        .addComponent(cbPaymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblPaidOrder4))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        panelBelowHeaderLayout.setVerticalGroup(
            panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                .addContainerGap(7, Short.MAX_VALUE)
                .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPaidOrder1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblPaidOrder2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPaidOrder3)
                        .addComponent(lblPaidOrder4)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbPaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );

        panelOrderManagement2.setBackground(new java.awt.Color(247, 246, 242));

        tableTransactionHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tableTransactionHistory);

        jLabel27.setBackground(new java.awt.Color(122, 67, 29));
        jLabel27.setFont(new java.awt.Font("Segoe UI", 0, 19)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(122, 67, 29));
        jLabel27.setText("Transaction Log ");

        lblRecordLog.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRecordLog.setForeground(new java.awt.Color(102, 102, 102));
        lblRecordLog.setText("(0 records)");

        javax.swing.GroupLayout panelOrderManagement2Layout = new javax.swing.GroupLayout(panelOrderManagement2);
        panelOrderManagement2.setLayout(panelOrderManagement2Layout);
        panelOrderManagement2Layout.setHorizontalGroup(
            panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderManagement2Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblRecordLog)
                .addContainerGap(684, Short.MAX_VALUE))
            .addGroup(panelOrderManagement2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        panelOrderManagement2Layout.setVerticalGroup(
            panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagement2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(lblRecordLog))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout panelOrderLayout = new javax.swing.GroupLayout(panelOrder);
        panelOrder.setLayout(panelOrderLayout);
        panelOrderLayout.setHorizontalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelBelowHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelOrderLayout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        panelOrderLayout.setVerticalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderLayout.createSequentialGroup()
                .addComponent(panelBelowHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 609, Short.MAX_VALUE))
            .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelMenu.add(panelOrder, "cardOrder");

        panelCustomer.setPreferredSize(new java.awt.Dimension(949, 590));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "No", "Code", "Name", "Phone"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
        }

        txtPhone.addActionListener(this::txtPhoneActionPerformed);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Phone");

        javax.swing.GroupLayout panelCustomerLayout = new javax.swing.GroupLayout(panelCustomer);
        panelCustomer.setLayout(panelCustomerLayout);
        panelCustomerLayout.setHorizontalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 941, Short.MAX_VALUE)
                    .addGroup(panelCustomerLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel5)
                        .addGap(29, 29, 29)
                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelCustomerLayout.setVerticalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomerLayout.createSequentialGroup()
                .addGap(162, 162, 162)
                .addGroup(panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)
                .addContainerGap())
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
        util.BarcodeScannerUtil.startScan(this, rawBarcode -> {
            if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
                return;
            }
            String empCode = util.BarcodeHashUtil.isEmpCode(rawBarcode)
                    ? rawBarcode.trim()
                    : util.BarcodeHashUtil.toEmpCode(rawBarcode);
            
            repository.EmployeeRepository empRepo = new repository.EmployeeRepository();
            entity.Employee emp = empRepo.findByBarcode(empCode);
            if (emp == null) {
                emp = empRepo.findByBarcode(rawBarcode.trim());
            }
            
            if (emp != null) {
                if (emp.getStatus() == 0) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Tài khoản nhân viên này đang bị tạm dừng hoạt động!",
                            "Lỗi đăng nhập", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (emp.getRoleId() == 1) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Không cho phép đăng nhập hoặc thao tác tài khoản Quản lý bằng mã vạch!",
                            "Lỗi đăng nhập", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean alreadyLoggedIn = util.UserSession.getInstance().isLoggedIn();
                if (alreadyLoggedIn) {
                    if (emp.getRoleId() != 1) {
                        if (!attendanceRepository.hasActiveCheckIn(emp.getId())) {
                            attendanceRepository.checkIn(emp.getId());
                        }
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Điểm danh (Check-in) thành công cho nhân viên " + emp.getFullName() + ".",
                                "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        loadCheckedInEmployees();
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Tài khoản Quản lý không cần điểm danh!",
                                "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    util.UserSession.getInstance().setCurrentUser(emp);
                    util.UserSession.getInstance().setToken(java.util.UUID.randomUUID().toString());
                    
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Đăng nhập thành công! Chào mừng " + emp.getFullName() + ".",
                            "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    
                    if (emp.getRoleId() == 1) {
                        java.awt.EventQueue.invokeLater(() -> {
                            DashBoardFrame db = new DashBoardFrame(this);
                            db.setVisible(true);
                            this.setVisible(false);
                        });
                    } else {
                        if (!attendanceRepository.hasActiveCheckIn(emp.getId())) {
                            attendanceRepository.checkIn(emp.getId());
                        }
                        loadCheckedInEmployees();
                        refreshUserDropdown();
                    }
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Mã thẻ không hợp lệ hoặc không tìm thấy nhân viên!",
                        "Lỗi đăng nhập", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }//GEN-LAST:event_btnScanActionPerformed

//    public void setManagerButtonVisible(boolean visible) {
////        managerBtn.setVisible(visible);
//    }

//    public void hideNonManagerMenus() {
//        managerBtn.setVisible(false);
//        managerBtn.setEnabled(false);
////        btnEmployee.setVisible(false);
////        btnOrder.setVisible(false);
//    }

    private void btnConfirmOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfirmOrderActionPerformed
        entity.Employee user = util.UserSession.getInstance().getCurrentUser();
        if (user == null || (user.getRoleId() != 1 && user.getRoleId() != 2)) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Bạn không có quyền thực hiện thanh toán trên POS!",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
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
        if ("Cash".equals(paymentMethod)) {
            String cashStr = txtCashReceived.getText().trim();
            if (cashStr.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập số tiền khách hàng trả!",
                        "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String cleanCashStr = cashStr.replace(",", "").replace(".", "").replace("đ", "").trim();
                double cashReceived = Double.parseDouble(cleanCashStr);
                if (cashReceived < totalAmount) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            String.format("Số tiền khách trả (%,.0f đ) nhỏ hơn tổng tiền phải thanh toán (%,.0f đ)!", cashReceived, totalAmount),
                            "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Số tiền khách hàng trả không hợp lệ!",
                        "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        final int finishedIndex = activeIndex;
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                String.format("Xác nhận thanh toán%nPhương thức: %s%nTổng tiền: %,.0f đ",
                        paymentMethod, totalAmount),
                "Xác nhận đơn hàng",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        btnConfirmOrder.setEnabled(false); // chặn bấm lại trong lúc xử lý nền

        new javax.swing.SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                // 1. Kiểm tra số lượng tồn kho thực tế trước khi lưu
                repository.ProductRepository prodRepo = new repository.ProductRepository();
                for (OrderCartController.CartItem ci : cart.getCartItems()) {
                    entity.Product p = prodRepo.findById(ci.productId);
                    if (p == null) {
                        return "NOT_FOUND:" + ci.productName;
                    }
                    if (p.getQuantity() < ci.quantity) {
                        return "INSUFFICIENT_STOCK:" + ci.productName + ":" + p.getQuantity() + ":" + ci.quantity;
                    }
                }

                boolean ok = orderRepository.finalizeOrder(orderId, cart.getCustomerId(), paymentMethod, totalAmount, "PAID", items);
                return ok ? "OK" : "DATABASE_ERROR";
            }

            @Override
            protected void done() {
                btnConfirmOrder.setEnabled(true);
                String result;
                try {
                    result = get();
                } catch (Exception e) {
                    result = "DATABASE_ERROR";
                }

                if (result.startsWith("NOT_FOUND:")) {
                    String name = result.substring("NOT_FOUND:".length());
                    javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this,
                            "Sản phẩm \"" + name + "\" không còn tồn tại trên hệ thống.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (result.startsWith("INSUFFICIENT_STOCK:")) {
                    String[] parts = result.split(":");
                    String name = parts[1];
                    String stock = parts[2];
                    String req = parts[3];
                    javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this,
                            "Không đủ số lượng trong kho cho sản phẩm: " + name
                            + "\nSố lượng yêu cầu: " + req
                            + "\nSố lượng hiện có: " + stock,
                            "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!"OK".equals(result)) {
                    javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this, "Lưu hóa đơn thất bại.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // dashBoard.refreshAfterNewOrder() đụng vào component Swing -> phải chạy ở EDT (done() đã là EDT)
                if (dashBoard != null) {
                    dashBoard.refreshAfterNewOrder();
                }
                loadProductGrid(); // Tải lại lưới sản phẩm để cập nhật số lượng tồn kho vừa trừ
                refreshTransactionHistory();
                if (txtCashReceived != null) {
                    txtCashReceived.setText("");
                }
                askAndExportInvoice(orderId);
                orderSessions.remove(finishedIndex);
                sessionOrderIds.remove(finishedIndex);
                panelOrderSplit.remove(tabButtons.get(finishedIndex));
                tabButtons.remove(finishedIndex);
                if (orderSessions.isEmpty()) {
                    createNewOrder();
                } else {
                    activeIndex = Math.max(0, finishedIndex - 1);
                    switchToOrder(activeIndex);
                }
            }
        }.execute();
    }//GEN-LAST:event_btnConfirmOrderActionPerformed

    private void btnOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrderActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOrderActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCustomerActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelOrderActionPerformed
        if (!checkLoginAndWarn()) return;
        int orderId = sessionOrderIds.get(activeIndex);
        if (orderId != -1) {
            orderRepository.finalizeOrder(orderId, null, null, 0, "CANCELLED", null);
        }

        if (dashBoard != null) {
            dashBoard.refreshAfterNewOrder();
        }
        loadProductGrid();
        refreshTransactionHistory();
        if (txtCashReceived != null) {
            txtCashReceived.setText("");
        }
        orderSessions.remove(activeIndex);
        sessionOrderIds.remove(activeIndex);
        panelOrderSplit.remove(tabButtons.get(activeIndex));
        tabButtons.remove(activeIndex);

        if (orderSessions.isEmpty()) {
            createNewOrder();
        } else {
            activeIndex = Math.max(0, activeIndex - 1);
            switchToOrder(activeIndex);
        }
    }//GEN-LAST:event_btnCancelOrderActionPerformed

    private void txtSearchInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchInvoiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchInvoiceActionPerformed

    private void txtPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPhoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPhoneActionPerformed

    private void txtSearchCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchCustomerActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        String selected = (String) jComboBox1.getSelectedItem();
        if ("Đăng xuất".equals(selected)) {
            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                util.UserSession.getInstance().cleanUserSession();
                refreshUserDropdown();
            }
            jComboBox1.setSelectedIndex(0);
        } else if ("Đăng nhập".equals(selected)) {
            LoginFrame loginFrame = new LoginFrame(this);
            loginFrame.setVisible(true);
            jComboBox1.setSelectedIndex(0);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddCustomerActionPerformed

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

    public boolean checkLoginAndWarn() {
        if (!util.UserSession.getInstance().isLoggedIn()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Bạn chưa đăng nhập! Vui lòng đăng nhập để thực hiện thao tác này.",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public void refreshUserDropdown() {
        entity.Employee user = util.UserSession.getInstance().getCurrentUser();
        if (user != null && user.getRoleId() != 1) {
            if (!attendanceRepository.hasActiveCheckIn(user.getId()) && user.getId() > 0) {
                attendanceRepository.checkIn(user.getId());
            }
        }
        
        if (user != null) {
            String roleName = "Staff";
            try {
                java.util.List<entity.Role> roles = new repository.EmployeeRepository().getAllRole();
                for (entity.Role r : roles) {
                    if (r.getId() == user.getRoleId()) {
                        roleName = r.getRoleName();
                        break;
                    }
                }
            } catch (Exception e) {
                roleName = user.getRoleId() == 1 ? "Manager" : "Staff";
            }
            String displayInfo = user.getFullName() + " (" + roleName + ")";
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { displayInfo, "Đăng xuất" }));
        } else {
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chưa đăng nhập", "Đăng nhập" }));
        }
        updateCashierPanel();
        loadCheckedInEmployees();
    }

    public void updateCashierPanel() {
        panelCashier.removeAll();
        
        jLabel4.setText("STAFF CHECK-IN");
        labelStatus.setText("Đang đợi quét thẻ...");
        
        if (btnLoginQuick == null) {
            btnLoginQuick = new javax.swing.JButton("Đăng nhập");
            btnLoginQuick.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btnLoginQuick.setBackground(new java.awt.Color(111, 59, 26)); // Meomeo theme brown
            btnLoginQuick.setForeground(java.awt.Color.WHITE);
            btnLoginQuick.setPreferredSize(new java.awt.Dimension(100, 35));
            btnLoginQuick.setFocusPainted(false);
            btnLoginQuick.setBorderPainted(false);
            btnLoginQuick.putClientProperty(FlatClientProperties.STYLE, "background: #6F3B1A; foreground: #FFFFFF; arc: 10;");
            btnLoginQuick.addActionListener(e -> {
                LoginFrame loginFrame = new LoginFrame(this);
                loginFrame.setVisible(true);
            });
        }
        
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
                .addComponent(btnLoginQuick, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        panelCashierLayout.setVerticalGroup(
            panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCashierLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLoginQuick, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelCashierLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStatus)))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        
        panelCashier.revalidate();
        panelCashier.repaint();
    }


    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    private void initCheckedInEmployeesPanel() {
        panelEmployeeCheckIn.setPreferredSize(new java.awt.Dimension(250, 200));
        panelEmployeeCheckIn.setBorder(null);
        panelEmployeeCheckIn.setOpaque(false);
        panelEmployeeCheckIn.getViewport().setOpaque(false);
        
        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Nhân viên đã check-in", javax.swing.SwingConstants.LEFT);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        titleLabel.setForeground(new java.awt.Color(111, 59, 26)); // #6F3B1A
        titleLabel.setIcon(ui.MenuIcons.humanResources());
        titleLabel.setIconTextGap(8);
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new javax.swing.BoxLayout(listPanel, javax.swing.BoxLayout.Y_AXIS));
        listPanel.setBackground(java.awt.Color.WHITE);
        
        JPanel mainPanel = new JPanel(new java.awt.BorderLayout(0, 10));
        mainPanel.setBackground(java.awt.Color.WHITE);
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        mainPanel.add(titleLabel, java.awt.BorderLayout.NORTH);
        mainPanel.add(listPanel, java.awt.BorderLayout.CENTER);
        
        panelEmployeeCheckIn.setViewportView(mainPanel);
        checkInListPanel = listPanel;
        
        panelEmployeeCheckIn.revalidate();
        panelEmployeeCheckIn.repaint();
        if (panelEmployeeCheckIn.getParent() != null) {
            panelEmployeeCheckIn.getParent().revalidate();
            panelEmployeeCheckIn.getParent().repaint();
        }
    }

    public void loadCheckedInEmployees() {
        if (checkInListPanel == null) {
            initCheckedInEmployeesPanel();
        }
        
        checkInListPanel.removeAll();
        java.util.List<repository.AttendanceRepository.CheckedInEmployee> list = attendanceRepository.getTodayCheckedInEmployees();
        
        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel(new java.awt.GridBagLayout());
            emptyPanel.setOpaque(false);
            
            javax.swing.JLabel noEmpLabel = new javax.swing.JLabel("Không có nhân viên check-in");
            noEmpLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 12));
            noEmpLabel.setForeground(new java.awt.Color(148, 163, 184)); // #94A3B8
            emptyPanel.add(noEmpLabel);
            
            checkInListPanel.add(emptyPanel);
        } else {
            for (repository.AttendanceRepository.CheckedInEmployee emp : list) {
                JPanel card = createEmployeeCard(emp);
                checkInListPanel.add(card);
                checkInListPanel.add(javax.swing.Box.createVerticalStrut(8));
            }
            checkInListPanel.add(javax.swing.Box.createVerticalGlue());
        }
        
        checkInListPanel.revalidate();
        checkInListPanel.repaint();
        panelEmployeeCheckIn.revalidate();
        panelEmployeeCheckIn.repaint();
        if (panelEmployeeCheckIn.getParent() != null) {
            panelEmployeeCheckIn.getParent().revalidate();
            panelEmployeeCheckIn.getParent().repaint();
        }

        // Synchronize with dashboard if active
        if (dashBoard != null) {
            dashBoard.loadCheckedInEmployees();
            dashBoard.loadAttendanceTableData();
            dashBoard.loadOverviewCardsData();
        }
    }

    private JPanel createEmployeeCard(repository.AttendanceRepository.CheckedInEmployee emp) {
        JPanel card = new JPanel(new java.awt.BorderLayout(8, 0));
        card.setBackground(java.awt.Color.WHITE);
        card.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 56));
        card.setPreferredSize(new java.awt.Dimension(250, 56));
        card.setMinimumSize(new java.awt.Dimension(200, 56));
        card.putClientProperty(FlatClientProperties.STYLE,
            "arc: 12; border: 1,1,1,1,#E2E8F0; background: #FFFFFF;");
        card.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 10));

        // --- Màu role ---
        java.awt.Color roleColor = new java.awt.Color(160, 174, 192);
        if (emp.roleColorHex != null) {
            try { roleColor = java.awt.Color.decode(emp.roleColorHex); } catch (Exception ignored) {}
        }
        final java.awt.Color finalRoleColor = roleColor;

        // --- Tên nhân viên ---
        javax.swing.JLabel lblName = new javax.swing.JLabel(emp.fullName);
        lblName.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblName.setForeground(new java.awt.Color(30, 41, 59));
        lblName.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        // --- Role badge (text có màu) ---
        javax.swing.JLabel lblRole = new javax.swing.JLabel(emp.roleName != null ? emp.roleName : "");
        lblRole.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        lblRole.setForeground(finalRoleColor);
        lblRole.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new javax.swing.BoxLayout(infoPanel, javax.swing.BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(lblName);
        infoPanel.add(javax.swing.Box.createVerticalStrut(2));
        infoPanel.add(lblRole);

        // --- Giờ vào ---
        String timeStr = "—";
        if (emp.checkInTime != null) {
            timeStr = new java.text.SimpleDateFormat("HH:mm").format(emp.checkInTime);
        }
        javax.swing.JLabel lblTime = new javax.swing.JLabel(timeStr);
        lblTime.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        lblTime.setForeground(new java.awt.Color(100, 116, 139));

        // --- Panel bên phải: giờ + nút kết thúc ---
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new javax.swing.BoxLayout(eastPanel, javax.swing.BoxLayout.Y_AXIS));
        eastPanel.setOpaque(false);

        lblTime.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);
        eastPanel.add(lblTime);

        if (emp.id >= 2) {
            eastPanel.add(javax.swing.Box.createVerticalStrut(2));
            javax.swing.JButton btnEndShift = new javax.swing.JButton("Kết thúc");
            btnEndShift.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 9));
            btnEndShift.setBackground(new java.awt.Color(239, 68, 68));
            btnEndShift.setForeground(java.awt.Color.WHITE);
            btnEndShift.setFocusPainted(false);
            btnEndShift.setBorderPainted(false);
            btnEndShift.setMargin(new java.awt.Insets(1, 6, 1, 6));
            btnEndShift.putClientProperty(FlatClientProperties.STYLE, "background: #EF4444; foreground: #FFFFFF; arc: 8;");
            btnEndShift.setAlignmentX(java.awt.Component.RIGHT_ALIGNMENT);
            btnEndShift.addActionListener(e -> {
                int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Xác nhận kết thúc phiên làm việc cho nhân viên " + emp.fullName + "?",
                    "Kết thúc phiên",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                    attendanceRepository.checkOut(emp.id);
                    entity.Employee currentUser = util.UserSession.getInstance().getCurrentUser();
                    if (currentUser != null && currentUser.getId() == emp.id) {
                        util.UserSession.getInstance().cleanUserSession();
                    }
                    loadCheckedInEmployees();
                    refreshUserDropdown();
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "Đã kết thúc phiên làm việc và đăng xuất thành công!",
                        "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            });
            eastPanel.add(btnEndShift);
        }

        card.add(infoPanel, java.awt.BorderLayout.CENTER);
        card.add(eastPanel, java.awt.BorderLayout.EAST);

        final String colorHex = emp.roleColorHex != null ? emp.roleColorHex : "#E28743";
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 12; border: 1,1,1,1," + colorHex + "; background: #F8FAFC;");
                card.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.putClientProperty(FlatClientProperties.STYLE,
                    "arc: 12; border: 1,1,1,1,#E2E8F0; background: #FFFFFF;");
                card.repaint();
            }
        });

        return card;
    }

    private void askAndExportInvoice(int orderId) {
        int getInvoiceConfirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có muốn lấy hóa đơn không?",
                "Xác nhận hóa đơn",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        
        if (getInvoiceConfirm == javax.swing.JOptionPane.YES_OPTION) {
            int downloadConfirm = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn tải hóa đơn dạng PDF về máy không?",
                    "Tải hóa đơn",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE);
            
            if (downloadConfirm == javax.swing.JOptionPane.YES_OPTION) {
                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                fileChooser.setDialogTitle("Lưu hóa đơn PDF");
                fileChooser.setSelectedFile(new java.io.File("hoadon_ORD-2026-" + String.format("%04d", orderId) + ".pdf"));
                
                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                    java.io.File fileToSave = fileChooser.getSelectedFile();
                    if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                        fileToSave = new java.io.File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                    }
                    
                    final java.io.File targetFile = fileToSave;
                    new javax.swing.SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            util.InvoicePdfExporter.exportInvoice(orderId, targetFile);
                            return null;
                        }
                        
                        @Override
                        protected void done() {
                            try {
                                get();
                                javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this,
                                        "Xuất hóa đơn PDF thành công tại:\n" + targetFile.getAbsolutePath(),
                                        "Thành công",
                                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                javax.swing.JOptionPane.showMessageDialog(SalesCounterFrame.this,
                                        "Có lỗi xảy ra khi xuất file PDF:\n" + e.getMessage(),
                                        "Lỗi",
                                        javax.swing.JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.execute();
                }
            }
        }
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup(); // Nên thêm FlatLaf vào đây nữa cho đồng bộ giao diện
        java.awt.EventQueue.invokeLater(() -> {
            SalesCounterFrame sales = new SalesCounterFrame();
            sales.setVisible(true); // Chỉ duy nhất quầy bán hàng hiện lên lúc mở app!
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnBarcode;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnConfirmOrder;
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnMain;
    private javax.swing.JButton btnOrder;
    private javax.swing.JButton btnProductInventory;
    private javax.swing.JButton btnScan;
    private javax.swing.JButton cashBtn;
    private javax.swing.JComboBox<String> cbAll;
    private javax.swing.JComboBox<String> cbCategory;
    private javax.swing.JComboBox<String> cbPaymentMethod;
    private javax.swing.JComboBox<String> cbStatus;
    private com.toedter.calendar.JDateChooser dateFrom;
    private com.toedter.calendar.JDateChooser dateTo;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JLabel lbAvatarShop;
    private javax.swing.JLabel lbChangeDue;
    private javax.swing.JLabel lbQRCode;
    private javax.swing.JLabel lbSubtotal;
    private javax.swing.JLabel lblCurrentOrder;
    private javax.swing.JLabel lblPaidOrder1;
    private javax.swing.JLabel lblPaidOrder2;
    private javax.swing.JLabel lblPaidOrder3;
    private javax.swing.JLabel lblPaidOrder4;
    private javax.swing.JLabel lblPopular;
    private javax.swing.JLabel lblRecordLog;
    private javax.swing.JLabel lblTotalItem;
    private javax.swing.JPanel panelBarcode;
    private javax.swing.JPanel panelBelowHeader;
    private javax.swing.JPanel panelCardParent;
    private javax.swing.JPanel panelCashView;
    private javax.swing.JPanel panelCashier;
    private javax.swing.JPanel panelChangeDue;
    private javax.swing.JPanel panelCurrentOrder;
    private javax.swing.JPanel panelCustomer;
    private javax.swing.JPanel panelDate;
    private javax.swing.JScrollPane panelEmployeeCheckIn;
    private javax.swing.JPanel panelHeader;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelNav;
    private javax.swing.JPanel panelOrder;
    private javax.swing.JPanel panelOrderManagement1;
    private javax.swing.JPanel panelOrderManagement2;
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
    private javax.swing.JTable tableProduct;
    private javax.swing.JTable tableTransactionHistory;
    private javax.swing.JTextField txtBarcodeSearch;
    private javax.swing.JTextField txtCashReceived;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSearchCustomer;
    private javax.swing.JTextField txtSearchInvoice;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
}

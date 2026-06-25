package views;

import ui.BarChartPanel;
import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import repository.AttendanceRepository;
import repository.CategoryRepository;
import repository.ScheduleRepository.EmployeeScheduleRow;
import repository.ProductRepository;
import repository.ScheduleRepository;
import ui.AttendanceTableRenderer;
import ui.HistoryTableRenderer;
import ui.HistoryActionCellEditor;
import ui.InventoryTableRenderer;
import ui.ScheduleTableRenderer;
import ui.BarChartPanel;

public class DashBoardFrame extends javax.swing.JFrame {

    private final repository.EmployeeRepository employeeRepository = new repository.EmployeeRepository();
    private final repository.OrderRepository orderRepository = new repository.OrderRepository();
    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private ui.BarChartPanel chartTuan;
    private ui.BarChartPanel chartThang;
    private java.awt.CardLayout cardRevenue;
    private ui.TopSalesPanel topSalesNgay;
    private ui.TopSalesPanel topSalesThang;
    private java.awt.CardLayout cardTopSales;

    private final java.util.Map<String, javax.swing.ImageIcon> imageCache = new java.util.HashMap<>();
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> inventorySorter;
    private boolean isInitializingAttendance = true;
    private java.time.LocalDate currentWeekStart;

    public DashBoardFrame() {
        initComponents();
        panelRevenueDate.setPreferredSize(new java.awt.Dimension(500, 320));
        panelTopSales.setPreferredSize(new java.awt.Dimension(400, 320));
        setupScrollArea();
        setupTopSales();
        initHRTabEvents();
        loadOverviewCardsData();
        initCancelOrderEvent();
        customTableAppearance();
        customInventoryAppearance();
        loadInventoryTableData();
        loadCategoryComboBox();
        loadOrderTableData();
        initInventoryFilterEvents();
        initStatusFilter();
        initSearchInvoiceFilter();
        initDateFilters();
        customHistoryTableAppearance();
        refreshOrderHistory();
        customDashboardComponentsStyle();
        customHistoryComponentsStyle();
        ui.CancelButtonStyler.apply(btnCancelOrder);
        updateAttendanceTitle();
        initAttendanceDateFilter();
        customAttendanceTableAppearance();
        loadAttendanceTableData();
        loadRoleComboBox();
        customEmployeeManagementTableAppearance();
        initEmployeeManagementFilterEvents();
        loadEmployeeManagementTableData();
        this.getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        this.getContentPane().add(panelMenu, java.awt.BorderLayout.WEST);
        this.getContentPane().add(panelContent, java.awt.BorderLayout.CENTER);

        this.setSize(1240, 800);
        this.setLocationRelativeTo(null);
        this.currentWeekStart = java.time.LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        initWeekNavigationEvents();
        updateWeekRangeLabel();
        loadWeeklyScheduleData();

        String styleActive = "background: #E38A45; "
                + "foreground: #FFFFFF; "
                + "arc: 12; "
                + "borderWidth: 0; "
                + "focusWidth: 0; "
                + "innerFocusWidth: 0;";
        String styleNormal = "background: #00000000; "
                + "foreground: #4A5568; "
                + "arc: 12; "
                + "borderWidth: 0; "
                + "focusWidth: 0; "
                + "innerFocusWidth: 0; "
                + "hoverBackground: #EDF2F7;";

        javax.swing.JButton[] menuButtons = {btnDashBoard, btnProductInventory,
            btnOrderHistory, btnHumanResources, btnBackToSaleCounter, btnCustomerManagement};
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
        btnDashBoard.putClientProperty(FlatClientProperties.STYLE, styleActive);
        btnDashBoard.setIcon(ui.MenuIcons.dashboard());
        btnProductInventory.setIcon(ui.MenuIcons.inventory());
        btnOrderHistory.setIcon(ui.MenuIcons.history());
        btnHumanResources.setIcon(ui.MenuIcons.humanResources());
        btnBackToSaleCounter.setIcon(ui.MenuIcons.backToSaleCounter());
        btnCustomerManagement.setIcon(ui.MenuIcons.customerManagement());
        btnDashBoard.putClientProperty("cardName", "CardDashboard");
        btnProductInventory.putClientProperty("cardName", "CardProduct");
        btnOrderHistory.putClientProperty("cardName", "CardOrder");
        btnHumanResources.putClientProperty("cardName", "CardAttendance");
        btnCustomerManagement.putClientProperty("cardName", "CardCustomer");

        java.awt.CardLayout cardLayout = (java.awt.CardLayout) panelContent.getLayout();
        cardLayout.show(panelContent, "CardDashboard");
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
                    cardLayout.show(panelContent, targetCard);
                }
                if (clickedBtn.getParent() != null) {
                    clickedBtn.getParent().revalidate();
                    clickedBtn.getParent().repaint();
                }
            });
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

            var imgLogic = util.ImageUtil.scale(imgGoc, kichThuocLogic);
            var imgThuc = (kichThuocThuc == kichThuocLogic) ? imgLogic : util.ImageUtil.scale(imgGoc, kichThuocThuc);

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
    }

    private void setupScrollArea() {
        panelRevenueDate.removeAll();
        panelRevenueDate.setLayout(new BorderLayout());
        panelRevenueDate.setBackground(Color.WHITE);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 12));

        javax.swing.JLabel lblChartTitle = new javax.swing.JLabel("Doanh thu theo ngày");
        lblChartTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblChartTitle.setForeground(new java.awt.Color(30, 41, 59));

        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(btnTuan);
        btnRow.add(btnThang);

        headerRow.add(lblChartTitle, BorderLayout.WEST);
        headerRow.add(btnRow, BorderLayout.EAST);
        panelRevenueDate.add(headerRow, BorderLayout.NORTH);

        JPanel chartCard = new JPanel();
        cardRevenue = new CardLayout();
        chartCard.setLayout(cardRevenue);
        chartCard.setBackground(Color.WHITE);

        String[] labelsTuan = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        String[] labelsThang = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};

        chartTuan = new BarChartPanel();
        chartThang = new BarChartPanel();
        chartTuan.setData(labelsTuan, orderRepository.getRevenueByWeek());
        chartThang.setData(labelsThang, orderRepository.getRevenueByMonth());

        chartCard.add(chartTuan, "TUAN");
        chartCard.add(chartThang, "THANG");
        cardRevenue.show(chartCard, "TUAN");

        panelRevenueDate.add(chartCard, BorderLayout.CENTER);

        btnTuan.addActionListener(e -> cardRevenue.show(chartCard, "TUAN"));
        btnThang.addActionListener(e -> cardRevenue.show(chartCard, "THANG"));

        panelRevenueDate.setPreferredSize(new Dimension(panelRevenueDate.getWidth(), 320));
        panelTopSales.setPreferredSize(new Dimension(panelTopSales.getWidth(), 320));

        panelRevenueDate.revalidate();
        panelRevenueDate.repaint();
    }

    private void setupTopSales() {
        panelTopSales.removeAll();
        panelTopSales.setLayout(new BorderLayout());
        panelTopSales.setBackground(Color.WHITE);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 12));

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Sản phẩm bán chạy");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(30, 41, 59));

        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(btnTopNgay);
        btnRow.add(btnTopThang);

        headerRow.add(lblTitle, BorderLayout.WEST);
        headerRow.add(btnRow, BorderLayout.EAST);
        panelTopSales.add(headerRow, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel();
        cardTopSales = new CardLayout();
        cardPanel.setLayout(cardTopSales);
        cardPanel.setBackground(Color.WHITE);

        topSalesNgay = new ui.TopSalesPanel();
        topSalesThang = new ui.TopSalesPanel();

        topSalesNgay.setData(orderRepository.getTopProductsToday(5));
        topSalesThang.setData(orderRepository.getTopProductsThisMonth(5));

        cardPanel.add(topSalesNgay, "NGAY");
        cardPanel.add(topSalesThang, "THANG");
        cardTopSales.show(cardPanel, "NGAY");

        panelTopSales.add(cardPanel, BorderLayout.CENTER);

        btnTopNgay.addActionListener(e -> {
            topSalesNgay.setData(orderRepository.getTopProductsToday(5));
            cardTopSales.show(cardPanel, "NGAY");
        });

        btnTopThang.addActionListener(e -> {
            topSalesThang.setData(orderRepository.getTopProductsThisMonth(5));
            cardTopSales.show(cardPanel, "THANG");
        });

        panelTopSales.revalidate();
        panelTopSales.repaint();
    }

    private void customDashboardComponentsStyle() {
        panelRevenueToday.setOpaque(false); //
        panelActiveEmployees.setOpaque(false); //
        panelActiveOrders.setOpaque(false); //
        panelCanceledOrders.setOpaque(false); //

        panelRevenueToday.putClientProperty(FlatClientProperties.STYLE, "background: #FFFFFF;");
        panelActiveEmployees.putClientProperty(FlatClientProperties.STYLE, "background: #FFFFFF;");
        panelActiveOrders.putClientProperty(FlatClientProperties.STYLE, "background: #FFFFFF;");
        panelCanceledOrders.putClientProperty(FlatClientProperties.STYLE, "background: #FFFFFF;");

        java.awt.Color cardBorderColor = java.awt.Color.decode("#F1F5F9");
        panelRevenueToday.setBorder(new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelActiveEmployees.setBorder(new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelActiveOrders.setBorder(new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelCanceledOrders.setBorder(new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));

        this.repaint(); //
    }

    private void customScheduleTableAppearance() {
        tableSchedule.setRowHeight(64);
        tableSchedule.setShowHorizontalLines(true);
        tableSchedule.setShowVerticalLines(false);
        tableSchedule.setIntercellSpacing(new java.awt.Dimension(0, 0));
        tableSchedule.setSelectionBackground(tableSchedule.getBackground());
        tableSchedule.setSelectionForeground(java.awt.Color.BLACK);
        tableSchedule.setRowSelectionAllowed(false);

        // --- Header 2 dòng: "MON 16" -> dòng 1 "MON", dòng 2 "16" ---
        javax.swing.table.JTableHeader header = tableSchedule.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 46));
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                String text = value != null ? value.toString() : "";
                javax.swing.JPanel panel = new javax.swing.JPanel();
                panel.setLayout(new java.awt.GridBagLayout());
                panel.setBackground(new java.awt.Color(248, 250, 252));
                panel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));

                if (column == 0) {
                    javax.swing.JLabel lbl = new javax.swing.JLabel(text);
                    lbl.setFont(table.getFont().deriveFont(java.awt.Font.BOLD, 11f));
                    lbl.setForeground(new java.awt.Color(100, 116, 139));
                    panel.add(lbl);
                    return panel;
                }

                String[] parts = text.split(" ");
                String dayName = parts.length > 0 ? parts[0] : text;
                String dayNumber = parts.length > 1 ? parts[1] : "";

                javax.swing.JPanel inner = new javax.swing.JPanel();
                inner.setOpaque(false);
                inner.setLayout(new javax.swing.BoxLayout(inner, javax.swing.BoxLayout.Y_AXIS));

                javax.swing.JLabel lblDay = new javax.swing.JLabel(dayName, javax.swing.SwingConstants.CENTER);
                lblDay.setFont(table.getFont().deriveFont(java.awt.Font.BOLD, 10f));
                lblDay.setForeground(new java.awt.Color(100, 116, 139));
                lblDay.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

                javax.swing.JLabel lblNum = new javax.swing.JLabel(dayNumber, javax.swing.SwingConstants.CENTER);
                lblNum.setFont(table.getFont().deriveFont(java.awt.Font.BOLD, 13f));
                lblNum.setForeground(new java.awt.Color(30, 41, 59));
                lblNum.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

                inner.add(lblDay);
                inner.add(lblNum);
                panel.add(inner);
                return panel;
            }
        });

        ScheduleTableRenderer renderer = new ScheduleTableRenderer();
        for (int i = 0; i < tableSchedule.getColumnCount(); i++) {
            tableSchedule.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tableSchedule.getColumnModel().getColumn(0).setPreferredWidth(150);
    }

    private void updateWeekRangeLabel() {
        java.time.LocalDate weekEnd = currentWeekStart.plusDays(6);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d");

        String text = currentWeekStart.format(fmt) + " – " + weekEnd.format(fmt);
        lblWeekRange.setText(text);
    }

    private void initWeekNavigationEvents() {
        btnPrevWeek.addActionListener(e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeekRangeLabel();
            loadWeeklyScheduleData();
        });

        btnNextWeek.addActionListener(e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeekRangeLabel();
            loadWeeklyScheduleData();
        });
    }

    private void customHistoryComponentsStyle() {
        int CARD_HEIGHT = 108;
        java.awt.Dimension cardSize;

        cardSize = new java.awt.Dimension(225, CARD_HEIGHT);
        panelTotalRevenue.setPreferredSize(cardSize);
        panelTotalRevenue.setMinimumSize(cardSize);
        panelTotalRevenue.setMaximumSize(new java.awt.Dimension(300, CARD_HEIGHT));

        cardSize = new java.awt.Dimension(225, CARD_HEIGHT);
        jPanel1.setPreferredSize(cardSize);
        jPanel1.setMinimumSize(cardSize);
        jPanel1.setMaximumSize(new java.awt.Dimension(300, CARD_HEIGHT));

        cardSize = new java.awt.Dimension(225, CARD_HEIGHT);
        panelPaidOrders.setPreferredSize(cardSize);
        panelPaidOrders.setMinimumSize(cardSize);
        panelPaidOrders.setMaximumSize(new java.awt.Dimension(300, CARD_HEIGHT));

        cardSize = new java.awt.Dimension(225, CARD_HEIGHT);
        panelCanceledOrder.setPreferredSize(cardSize);
        panelCanceledOrder.setMinimumSize(cardSize);
        panelCanceledOrder.setMaximumSize(new java.awt.Dimension(300, CARD_HEIGHT));

        java.awt.Color borderColor = java.awt.Color.decode("#F1F5F9");
        com.formdev.flatlaf.ui.FlatLineBorder cardBorder
                = new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), borderColor, 1, 14);

        for (javax.swing.JPanel p : new javax.swing.JPanel[]{
            panelTotalRevenue, jPanel1, panelPaidOrders, panelCanceledOrder}) {
            p.setOpaque(true);
            p.setBackground(java.awt.Color.WHITE);
            p.setBorder(cardBorder);
        }

        int PAD = 18;
        setCardPadding(panelTotalRevenue, PAD);
        setCardPadding(jPanel1, PAD);
        setCardPadding(panelPaidOrders, PAD);
        setCardPadding(panelCanceledOrder, PAD);

        panelBelowHeader.setBackground(java.awt.Color.WHITE);
        panelBelowHeader.setBorder(new com.formdev.flatlaf.ui.FlatLineBorder(
                new java.awt.Insets(0, 0, 0, 0), borderColor, 1, 14));
        panelBelowHeader.setPreferredSize(new java.awt.Dimension(panelBelowHeader.getPreferredSize().width, 80));
        panelBelowHeader.setMinimumSize(new java.awt.Dimension(100, 80));
        panelBelowHeader.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, 80));
    }

    private void setCardPadding(javax.swing.JPanel panel, int leftPad) {
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(
                        new java.awt.Insets(0, 0, 0, 0),
                        java.awt.Color.decode("#F1F5F9"), 1, 14),
                javax.swing.BorderFactory.createEmptyBorder(14, leftPad, 14, 12)
        ));
    }

    private void loadWeeklyScheduleData() {
        Map<Integer, EmployeeScheduleRow> rowsByEmployeeId = scheduleRepository.findWeeklySchedule(currentWeekStart);

        buildScheduleTableModel(rowsByEmployeeId.values());
        customScheduleTableAppearance();
        updateScheduleHeaderLabels(rowsByEmployeeId.size());
    }

    private void buildScheduleTableModel(java.util.Collection<EmployeeScheduleRow> rows) {
        java.lang.String[] columnNames = new String[8];
        columnNames[0] = "EMPLOYEE";
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate d = currentWeekStart.plusDays(i);
            columnNames[i + 1] = d.getDayOfWeek().toString().substring(0, 3) + " " + d.getDayOfMonth();
            // ví dụ: "MON 16" -> renderer header sẽ tách lại để hiển thị 2 dòng
        }

        Object[][] data = new Object[rows.size()][8];
        int r = 0;
        for (EmployeeScheduleRow row : rows) {
            data[r][0] = row.fullName + "\n" + String.format("EMP%03d", row.employeeId);
            for (int i = 0; i < 7; i++) {
                java.time.LocalDate d = currentWeekStart.plusDays(i);
                data[r][i + 1] = row.shiftsByDate.get(d); // ShiftCell hoặc null nếu không có ca
            }
            r++;
        }

        tableSchedule.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
    }

    private void updateScheduleHeaderLabels(int employeeCount) {
        java.time.LocalDate weekEnd = currentWeekStart.plusDays(6);
        java.time.format.DateTimeFormatter monthDayFmt = java.time.format.DateTimeFormatter.ofPattern("MMMM d");
        String text;
        if (currentWeekStart.getMonth() == weekEnd.getMonth()) {
            text = currentWeekStart.format(monthDayFmt) + " – " + weekEnd.getDayOfMonth() + ", " + weekEnd.getYear();
        } else {
            text = currentWeekStart.format(monthDayFmt) + " – " + weekEnd.format(monthDayFmt) + ", " + weekEnd.getYear();
        }

        lblMonthDayYear.setText(text);
        lblEmployeeSchedule.setText(employeeCount + (employeeCount == 1 ? " employee scheduled" : " employees scheduled"));
    }

    private void initCancelOrderEvent() {
        btnCancelOrder.addActionListener(e -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableDashboard.getModel();
            int[] selectedRows = tableDashboard.getSelectedRows();

            if (selectedRows.length == 0) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn ít nhất một hóa đơn 'PAID' để hủy.",
                        "Chưa chọn hóa đơn",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.List<Integer> selectedOrderIds = new java.util.ArrayList<>();
            for (int i : selectedRows) {
                String status = model.getValueAt(i, 5).toString(); // cột TRẠNG THÁI
                if ("PAID".equals(status)) {
                    String maHD = model.getValueAt(i, 1).toString(); // "HD001"
                    try {
                        int id = Integer.parseInt(maHD.substring(2)); // bỏ "HD"
                        selectedOrderIds.add(id);
                    } catch (Exception ex) {
                        System.err.println("Lỗi parse ID: " + ex.getMessage());
                    }
                }
            }

            if (selectedOrderIds.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Chỉ có thể hủy hóa đơn có trạng thái 'PAID'.",
                        "Không hợp lệ",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn hủy " + selectedOrderIds.size() + " hóa đơn đã chọn?",
                    "Xác nhận hủy",
                    javax.swing.JOptionPane.YES_NO_OPTION);

            if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                updateOrdersToCanceled(selectedOrderIds);
            }
        });
    }

    private void updateOrdersToCanceled(java.util.List<Integer> orderIds) {
        int updatedRows = orderRepository.cancelOrders(orderIds);
        if (updatedRows > 0) {
            loadOverviewCardsData();
            loadOrderTableData();
            javax.swing.JOptionPane.showMessageDialog(this, "Successfully canceled " + updatedRows + " order(s).");
        }
    }

    private void customEmployeeManagementTableAppearance() {
        tableEmployeeManagement.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Employee ID", "Full Name", "Role", "Phone", "Barcode", "Status", "Actions"}
        ) {
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableEmployeeManagement.setRowHeight(48);
        tableEmployeeManagement.setShowHorizontalLines(true);
        tableEmployeeManagement.setShowVerticalLines(false);

        tableEmployeeManagement.setSelectionBackground(new java.awt.Color(248, 246, 242));
        tableEmployeeManagement.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tableEmployeeManagement.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; rowSelectionForeground: #0F172A; lineColor: #F1F5F9;");

        javax.swing.table.JTableHeader header = tableEmployeeManagement.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 38));
        header.setDefaultRenderer(new ui.StandardTableHeaderRenderer(javax.swing.SwingConstants.LEFT, 12));

        ui.EmployeeTableRenderer renderer = new ui.EmployeeTableRenderer();
        for (int i = 0; i < tableEmployeeManagement.getColumnCount(); i++) {
            tableEmployeeManagement.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        java.awt.Container parent = tableEmployeeManagement.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableEmployeeManagement, "Nothing found!"));
            scrollPane.setViewportView(tableEmployeeManagement);
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

    private void loadEmployeeManagementTableData() {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableEmployeeManagement.getModel();
        model.setRowCount(0);

        // Đọc filter trên EDT trước khi vào background thread
        String role = (cbRole != null && cbRole.getSelectedItem() != null)
                ? cbRole.getSelectedItem().toString() : "All";
        String keyword = (txtSearchEmployee != null) ? txtSearchEmployee.getText() : "";

        new javax.swing.SwingWorker<java.util.List<repository.EmployeeRepository.EmployeeRow>, Void>() {
            @Override
            protected java.util.List<repository.EmployeeRepository.EmployeeRow> doInBackground() {
                return employeeRepository.findEmployees(role, keyword);
            }

            @Override
            protected void done() {
                try {
                    for (var rec : get()) {
                        String empIdStr = String.format("EMP%03d", rec.id);
                        String statusStr = (rec.status == 1) ? "Active" : "Inactive";
                        model.addRow(new Object[]{
                            empIdStr, rec.fullName, rec.roleName, rec.phone, rec.barcode, statusStr, ""
                        });
                    }
                    tableEmployeeManagement.revalidate();
                    tableEmployeeManagement.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadRoleComboBox() {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement("All");

        java.util.List<String> roleNames = employeeRepository.findAllRoleNames();
        for (String roleName : roleNames) {
            model.addElement(roleName);
        }

        cbRole.setModel(model);
    }

    private void initEmployeeManagementFilterEvents() {
        cbRole.addActionListener(e -> loadEmployeeManagementTableData());

        txtSearchEmployee.getDocument().addDocumentListener(
                onDocumentChange(this::loadEmployeeManagementTableData));

    }

    private void loadOrderTableData() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableDashboard.getModel();
        model.setRowCount(0);

        java.util.List<repository.OrderRepository.OrderRow> rows = orderRepository.findTodayOrders();

        if (rows.isEmpty()) {
            System.out.println("⚠️ Kết nối DB thành công nhưng hôm nay chưa có đơn hàng nào trong bảng orders!");
        }

        int stt = 1; // Khởi tạo số thứ tự bắt đầu từ 1

        for (repository.OrderRepository.OrderRow row : rows) {
            model.addRow(new Object[]{
                stt++,
                String.format("HD%03d", row.id), // Định dạng hóa đơn mới: HD001, HD002...
                row.customerName, // Tên khách hàng
                row.customerPhone, // Số điện thoại khách hàng
                String.format("%,.0f đ", row.totalAmount), // Định dạng tiền tệ
                row.status // Trạng thái chuỗi (PAID, CANCELED...)
            });
        }

        tableDashboard.repaint();
        tableDashboard.revalidate();
    }

    private void loadInventoryTableData() {

        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

        model.setRowCount(0);

        new SwingWorker<java.util.List<ProductRepository.InventoryRow>, Void>() {

            @Override
            protected java.util.List<ProductRepository.InventoryRow> doInBackground() {
                return productRepository.findAllForInventory();
            }

            @Override
            protected void done() {
                try {

                    var rows = get();

                    for (ProductRepository.InventoryRow row : rows) {

                        model.addRow(new Object[]{
                            String.format("PRD-%04d", row.id),
                            row.imagePath,
                            row.productName,
                            row.categoryName,
                            String.format("%,.0f đ", row.price),
                            row.quantity
                        });
                    }

                    lblTotalItem.setText("(" + rows.size() + " items)");

                    inventorySorter
                            = new javax.swing.table.TableRowSorter<>(model);

                    tableProduct.setRowSorter(inventorySorter);

                    model.fireTableDataChanged();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }.execute();
    }

    private void customHistoryTableAppearance() {
        tableTransactionHistory.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableTransactionHistory.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"ORDER ID", "DATE & TIME", "CASHIER", "CUSTOMER", "PAYMENT", "TOTAL", "STATUS", "ACTION"}
        ) {
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false, true};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
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

        HistoryTableRenderer renderer = new HistoryTableRenderer();
        for (int i = 0; i < tableTransactionHistory.getColumnCount(); i++) {
            tableTransactionHistory.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        HistoryActionCellEditor actionEditor = new HistoryActionCellEditor(
                this::onViewHistoryOrder,
                this::onCancelHistoryOrder
        );
        tableTransactionHistory.getColumnModel().getColumn(7).setCellEditor(actionEditor);

        tableTransactionHistory.getColumnModel().getColumn(0).setPreferredWidth(135); // ORDER ID
        tableTransactionHistory.getColumnModel().getColumn(1).setPreferredWidth(155); // DATE & TIME
        tableTransactionHistory.getColumnModel().getColumn(2).setPreferredWidth(145); // CASHIER
        tableTransactionHistory.getColumnModel().getColumn(3).setPreferredWidth(175); // CUSTOMER
        tableTransactionHistory.getColumnModel().getColumn(4).setPreferredWidth(95);  // PAYMENT
        tableTransactionHistory.getColumnModel().getColumn(5).setPreferredWidth(120); // TOTAL
        tableTransactionHistory.getColumnModel().getColumn(6).setPreferredWidth(110); // STATUS
        tableTransactionHistory.getColumnModel().getColumn(7).setPreferredWidth(220);
        tableTransactionHistory.getColumnModel().getColumn(7).setMinWidth(210);

        java.awt.Container parent = tableTransactionHistory.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableTransactionHistory, "No transactions found"));
            scrollPane.setViewportView(tableTransactionHistory);
        }
    }

    private void loadOrderHistoryLog() {
        // Thay thế đoạn lọc cũ bằng bộ lọc chung
        java.util.List<repository.OrderRepository.OrderHistoryRow> rows = getFilteredOrderHistoryRows();

        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableTransactionHistory.getModel();
        model.setRowCount(0);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (repository.OrderRepository.OrderHistoryRow row : rows) {
            String paymentValue = normalizeHistoryPayment(row.paymentMethod);
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

            // Hai đoạn check bộ lọc cũ (paymentFilter và searchText) đã được xóa bỏ tại đây
            model.addRow(new Object[]{
                orderIdStr,
                dateStr,
                cashier,
                customer,
                paymentValue,
                amountStr,
                statusValue,
                "VIEW"
            });
        }

        // Cập nhật hiển thị số lượng bản ghi bằng rows.size() thay vì biến đếm matchCount
        lblRecordLog.setText("(" + rows.size() + " records)");
    }

    private java.util.List<repository.OrderRepository.OrderHistoryRow> getFilteredOrderHistoryRows() {
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
            String paymentValue = normalizeHistoryPayment(row.paymentMethod);
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

    private String normalizeHistoryPayment(String paymentMethod) {
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

    private int extractHistoryOrderId(int rowIndex) {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableTransactionHistory.getModel();
        String orderCode = String.valueOf(model.getValueAt(rowIndex, 0));
        int lastHyphen = orderCode.lastIndexOf('-');
        String numericPart = lastHyphen >= 0 ? orderCode.substring(lastHyphen + 1) : orderCode;
        return Integer.parseInt(numericPart);
    }

    private void onViewHistoryOrder(int rowIndex) {
        javax.swing.JOptionPane.showMessageDialog(this,
                "Frame chi tiết order_details sẽ nối sau.\nHiện tại cột ACTION của tableTransactionHistory đã sẵn sàng để gắn tiếp.",
                "Order details",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void onCancelHistoryOrder(int rowIndex) {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableTransactionHistory.getModel();

        String status = String.valueOf(model.getValueAt(rowIndex, 6));
        if (!"PENDING".equalsIgnoreCase(status)) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Chỉ có thể hủy hóa đơn ở trạng thái PENDING.",
                    "Không thể hủy",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderCode = String.valueOf(model.getValueAt(rowIndex, 0));
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn hủy hóa đơn " + orderCode + "?",
                "Xác nhận hủy hóa đơn",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        int updatedRows = orderRepository.cancelOrders(java.util.Collections.singletonList(extractHistoryOrderId(rowIndex)));
        if (updatedRows > 0) {
            loadOverviewCardsData();
            loadOrderTableData();
            refreshOrderHistory();
            initPaymentMethodFilter();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Đã hủy hóa đơn " + orderCode + " thành công.",
                    "Thành công",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Hủy hóa đơn thất bại. Vui lòng thử lại.",
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrderHistoryOverview() {
        java.util.List<repository.OrderRepository.OrderHistoryRow> rows = getFilteredOrderHistoryRows();

        double totalRevenue = 0;
        int paidCount = 0;
        int canceledCount = 0;

        for (repository.OrderRepository.OrderHistoryRow row : rows) {
            totalRevenue += row.totalAmount;

            String statusValue = row.status != null
                    ? row.status.trim().toUpperCase(java.util.Locale.ROOT)
                    : "PENDING";

            if ("PAID".equals(statusValue)) {
                paidCount++;
            } else if ("CANCELLED".equals(statusValue) || "CANCELED".equals(statusValue)) {
                canceledCount++;
            }
        }

        lblTotalRevenue.setText(String.format("%,.0f đ", totalRevenue));
        lblPaidOrders.setText(String.valueOf(paidCount));
        lblOrdersCanceled.setText(String.valueOf(canceledCount));

        int total = paidCount + canceledCount;

        if (lblPaidOrder != null) {
            lblPaidOrder.setText(paidCount + (paidCount == 1 ? " paid order" : " paid orders"));
        }

        if (lblPercentTotal != null) {
            double pct = total > 0 ? (paidCount * 100.0) / total : 0;
            lblPercentTotal.setText(String.format("%.1f%% of total", pct));
        }

        if (lblPercentCanceledOrder != null) {
            double pct = total > 0 ? (canceledCount * 100.0) / total : 0;
            lblPercentCanceledOrder.setText(String.format("%.1f%% of total", pct));
        }
    }

    private void performInventoryFilter() {
        if (inventorySorter == null) {
            return;
        }
        String text = txtSearchProduct.getText().trim();
        inventorySorter.setRowFilter(text.isEmpty() ? null
                : javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 2));
        tableProduct.revalidate();
        java.awt.Container vp = tableProduct.getParent();
        if (vp != null) {
            vp.revalidate();
            vp.repaint();
        }
    }

    private void initInventoryFilterEvents() {
        txtSearchProduct.getDocument().addDocumentListener(
                onDocumentChange(this::performInventoryFilter));
        cbAll.addActionListener(e -> {
            if (inventorySorter == null) {
                return;
            }
            String selectedCategory = String.valueOf(cbAll.getSelectedItem()).trim();
            if (selectedCategory.equalsIgnoreCase("All") || selectedCategory.contains("All")) {
                inventorySorter.setRowFilter(null);
            } else {
                inventorySorter.setRowFilter(
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

    private void customInventoryAppearance() {
        txtSearchProduct.putClientProperty("JTextField.placeholder", "Search products...");
        txtSearchProduct.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,10,0,10;");

        btnAddProduct.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 12; borderWidth: 0; focusWidth: 0; font: bold;");

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"PRODUCT ID", "IMAGE", "PRODUCT NAME", "CATEGORY", "PRICE", "STOCK", "ACTION"}
        ) {
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, true};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
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

        InventoryTableRenderer renderer = new InventoryTableRenderer(this::getCachedProductIcon);
        for (int i = 0; i < tableProduct.getColumnCount() - 1; i++) {
            tableProduct.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(70);   // PRODUCT ID
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(70);   // IMAGE
        tableProduct.getColumnModel().getColumn(1).setMaxWidth(70);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(200);  // PRODUCT NAME
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(100);  // CATEGORY
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(90);  // PRICE
        tableProduct.getColumnModel().getColumn(5).setPreferredWidth(50);

        ui.ProductActionCellRenderer actionRenderer = new ui.ProductActionCellRenderer();
        ui.ProductActionCellEditor actionEditor = new ui.ProductActionCellEditor(
                this::onEditProduct,
                this::onDeleteProduct
        );
        tableProduct.getColumnModel().getColumn(6).setCellRenderer(actionRenderer);
        tableProduct.getColumnModel().getColumn(6).setCellEditor(actionEditor);
        tableProduct.getColumnModel().getColumn(6).setPreferredWidth(130);
        tableProduct.getColumnModel().getColumn(6).setMaxWidth(150);

        java.awt.Container parent = tableProduct.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableProduct, "Nothing found!"));
            scrollPane.setViewportView(tableProduct);
        }
    }

    private void onEditProduct(int rowIndex) {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

        String productIdStr = model.getValueAt(rowIndex, 0).toString();
        int productId = Integer.parseInt(productIdStr.replace("PRD-", ""));

        entity.Product product = productRepository.findById(productId);
        if (product == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không tìm thấy sản phẩm!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        views.AddProductFrame editFrame = new views.AddProductFrame(product, () -> loadInventoryTableData());
        editFrame.setVisible(true);
    }

    private void onDeleteProduct(int rowIndex) {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

        String productIdStr = model.getValueAt(rowIndex, 0).toString();
        String productName = model.getValueAt(rowIndex, 2).toString();
        int productId = Integer.parseInt(productIdStr.replace("PRD-", ""));

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa sản phẩm \"" + productName + "\"?\n(Sản phẩm sẽ bị ẩn, không mất dữ liệu)",
                "Xác nhận xóa",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE);

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            boolean deleted = productRepository.softDelete(productId);
            if (deleted) {
                loadInventoryTableData();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Xóa thất bại! Vui lòng thử lại.", "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private javax.swing.ImageIcon getCachedProductIcon(String imgPath) {
        if (imgPath == null || imgPath.isEmpty() || "null".equals(imgPath)) {
            return null;
        }
        return imageCache.computeIfAbsent(imgPath, path -> {
            try {
                String fileName = path.contains("/")
                        ? path.substring(path.lastIndexOf('/') + 1)
                        : path;
                String resourcePath = "/images/" + fileName;

                java.net.URL imgUrl = getClass().getResource(resourcePath);
                if (imgUrl == null) {
                    System.err.println("⚠️ Không tìm thấy ảnh: " + resourcePath);
                    return null;
                }

                java.awt.Image raw = new javax.swing.ImageIcon(imgUrl).getImage();

                int kichThuocLogic = 26;
                double scale = 1.0;
                var gc = getGraphicsConfiguration();
                if (gc != null) {
                    scale = gc.getDefaultTransform().getScaleX();
                }
                int kichThuocThuc = Math.max(kichThuocLogic, (int) Math.round(kichThuocLogic * scale));

                var imgLogic = util.ImageUtil.scale(raw, kichThuocLogic);
                var imgThuc = (kichThuocThuc == kichThuocLogic)
                        ? imgLogic
                        : util.ImageUtil.scale(raw, kichThuocThuc);

                return new javax.swing.ImageIcon(
                        new java.awt.image.BaseMultiResolutionImage(imgLogic, imgThuc));
            } catch (Exception ex) {
                return null;
            }
        });
    }

    private void customTableAppearance() {
        tableDashboard.setSelectionBackground(new java.awt.Color(248, 246, 242));
        tableDashboard.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tableDashboard.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"No.", "Order ID", "Customer name", "Phone", "Total", "Status"}
        ) {
            Class[] types = new Class[]{java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class};
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, true};

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        tableDashboard.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableDashboard.getColumnModel().getColumn(0).setMaxWidth(60);
        tableDashboard.getColumnModel().getColumn(1).setPreferredWidth(110);
        tableDashboard.getColumnModel().getColumn(2).setPreferredWidth(200);
        tableDashboard.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableDashboard.getColumnModel().getColumn(4).setPreferredWidth(130);
        tableDashboard.getColumnModel().getColumn(5).setPreferredWidth(120);

        tableDashboard.setRowHeight(42);
        tableDashboard.setShowHorizontalLines(true);
        tableDashboard.setShowVerticalLines(false);
        tableDashboard.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; "
                + "rowSelectionForeground: #0F172A; "
                + "lineColor: #F1F5F9;");
        javax.swing.table.JTableHeader header = tableDashboard.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 38));
        header.setDefaultRenderer(new ui.StandardTableHeaderRenderer());

        ui.OrderTableRenderer orderRenderer = new ui.OrderTableRenderer();
        for (int i = 0; i < tableDashboard.getColumnCount(); i++) {
            tableDashboard.getColumnModel().getColumn(i).setCellRenderer(orderRenderer);
        }
    }

    private void loadCategoryComboBox() {
        javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>();
        model.addElement("All");
        categoryRepository.getAll().forEach(cat -> model.addElement(cat.getCategoryName()));
        cbAll.setModel(model);
    }

    private void customAttendanceTableAppearance() {
        tableAttendance.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Employee ID", "Name", "Role", "Date", "Clock-In", "Clock-Out", "Working Hours", "Status"}
        ) {
            boolean[] canEdit = new boolean[]{false, false, false, false, false, false, false, false};

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tableAttendance.setRowHeight(42);
        tableAttendance.setShowHorizontalLines(true);
        tableAttendance.setShowVerticalLines(false);

        tableAttendance.setSelectionBackground(new java.awt.Color(248, 246, 242));
        tableAttendance.setSelectionForeground(new java.awt.Color(15, 23, 42));
        tableAttendance.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; rowSelectionForeground: #0F172A; lineColor: #F1F5F9;");

        javax.swing.table.JTableHeader header = tableAttendance.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 38));
        header.setDefaultRenderer(new ui.StandardTableHeaderRenderer());

        AttendanceTableRenderer renderer = new AttendanceTableRenderer();
        for (int i = 0; i < tableAttendance.getColumnCount(); i++) {
            tableAttendance.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void loadAttendanceTableData() {
        javax.swing.table.DefaultTableModel model
                = (javax.swing.table.DefaultTableModel) tableAttendance.getModel();
        model.setRowCount(0);
        int targetMonth = java.time.LocalDate.now().getMonthValue();
        int targetYear = java.time.LocalDate.now().getYear();

        if (cbMonth != null && cbMonth.getSelectedItem() != null
                && cbYear != null && cbYear.getSelectedItem() != null) {
            targetYear = Integer.parseInt(cbYear.getSelectedItem().toString());
            String selectedMonthStr = cbMonth.getSelectedItem().toString();
            for (int i = 0; i < util.DateConstants.MONTH_NAMES.length; i++) {
                if (util.DateConstants.MONTH_NAMES[i].equalsIgnoreCase(selectedMonthStr)) {
                    targetMonth = i + 1;
                    break;
                }
            }
        }

        final int month = targetMonth;
        final int year = targetYear;

        new javax.swing.SwingWorker<java.util.List<repository.AttendanceRepository.AttendanceRow>, Void>() {
            @Override
            protected java.util.List<repository.AttendanceRepository.AttendanceRow> doInBackground() {
                return attendanceRepository.findByMonthYear(month, year);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<repository.AttendanceRepository.AttendanceRow> rows = get();

                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss");
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");

                    int onTimeCount = 0, lateCount = 0, absentCount = 0, onLeaveCount = 0;

                    for (var row : rows) {
                        switch (row.finalStatus.toUpperCase()) {
                            case "ON TIME" ->
                                onTimeCount++;
                            case "LATE" ->
                                lateCount++;
                            case "ABSENT" ->
                                absentCount++;
                            case "ON LEAVE" ->
                                onLeaveCount++;
                        }

                        String empIdStr = String.format("EMP-%04d", row.employeeId);
                        String dateStr = (row.workDate != null) ? dateFormat.format(row.workDate) : "—";
                        String clockInStr = (row.checkIn != null) ? timeFormat.format(row.checkIn) : "—";
                        String clockOutStr = (row.checkOut != null) ? timeFormat.format(row.checkOut) : "—";
                        String workingHoursStr = "—";
                        if (row.checkIn != null && row.checkOut != null) {
                            long diffMin = Math.abs(row.checkOut.getTime() - row.checkIn.getTime()) / (60 * 1000);
                            workingHoursStr = String.format("%dh %02dm", diffMin / 60, diffMin % 60);
                        }

                        model.addRow(new Object[]{
                            empIdStr, row.fullName, row.displayRole,
                            dateStr, clockInStr, clockOutStr, workingHoursStr, row.finalStatus
                        });
                    }

                    if (lblOnTimeCount != null) {
                        lblOnTimeCount.setText("· " + onTimeCount + " on time");
                    }
                    if (lblLateCount != null) {
                        lblLateCount.setText("· " + lateCount + " late");
                    }
                    if (lblAbsentCount != null) {
                        lblAbsentCount.setText("· " + absentCount + " absent");
                    }
                    if (lblOnLeaveCount != null) {
                        lblOnLeaveCount.setText("· " + onLeaveCount + " on leave");
                    }

                    updateAttendanceTitle();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void initStatusFilter() {
        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All", "PAID", "PENDING", "CANCELLED"}));
        cbStatus.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "arc: 12; background: #FFFFFF;");
        cbStatus.addActionListener(e -> refreshOrderHistory());

        initPaymentMethodFilter();
    }

    private void initPaymentMethodFilter() {
        java.util.LinkedHashSet<String> paymentOptions = new java.util.LinkedHashSet<>();
        paymentOptions.add("All");
        for (String paymentMethod : orderRepository.findDistinctPaymentMethods()) {
            String normalized = normalizeHistoryPayment(paymentMethod);
            if (!normalized.isBlank()) {
                paymentOptions.add(normalized);
            }
        }

        cbPaymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(
                paymentOptions.toArray(String[]::new)));
        cbPaymentMethod.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 12; background: #FFFFFF;");
        for (java.awt.event.ActionListener listener : cbPaymentMethod.getActionListeners()) {
            cbPaymentMethod.removeActionListener(listener);
        }
        cbPaymentMethod.addActionListener(e -> refreshOrderHistory());
    }

    private void initSearchInvoiceFilter() {
        if (txtSearchInvoice == null) {
            return;
        }
        txtSearchInvoice.putClientProperty("JTextField.placeholder", "Search Order ID, customer, cashier...");
        txtSearchInvoice.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 12; margin: 0,10,0,10;");
        txtSearchInvoice.getDocument().addDocumentListener(onDocumentChange(this::refreshOrderHistory));
    }

    private void refreshOrderHistory() {
        loadOrderHistoryOverview();
        loadOrderHistoryLog();
    }

    private void initAttendanceDateFilter() {
        isInitializingAttendance = true;

        if (cbMonth == null || cbYear == null) {
            return;
        }

        int currentYear = java.time.LocalDate.now().getYear();
        int openingYear = attendanceRepository.findEarliestActiveYear(); // ← thay cả khối try-catch SQL

        cbYear.removeAllItems();
        for (int y = currentYear; y >= openingYear; y--) {
            cbYear.addItem(String.valueOf(y));
        }
        cbYear.setSelectedItem(String.valueOf(currentYear));

        populateAttendanceMonthComboBox(currentYear);
        cbMonth.setSelectedIndex(cbMonth.getItemCount() - 1);

        cbMonth.addActionListener(e -> {
            if (!isInitializingAttendance) {
                loadAttendanceTableData();
            }
        });
        cbYear.addActionListener(e -> {
            if (!isInitializingAttendance) {
                isInitializingAttendance = true;
                int selectedYear = Integer.parseInt(cbYear.getSelectedItem().toString());
                populateAttendanceMonthComboBox(selectedYear);
                cbMonth.setSelectedIndex(cbMonth.getItemCount() - 1);
                isInitializingAttendance = false;
                loadAttendanceTableData();
            }
        });

        isInitializingAttendance = false;
    }

    private void populateAttendanceMonthComboBox(int year) {
        String[] allMonths = util.DateConstants.MONTH_NAMES;
        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        int limit = (year == currentYear) ? currentMonth : 12;

        cbMonth.removeAllItems();
        for (int i = 0; i < limit; i++) {
            cbMonth.addItem(allMonths[i]);
        }
    }

    private void updateAttendanceTitle() {
        if (cbMonth != null && cbYear != null && cbMonth.getSelectedItem() != null && cbYear.getSelectedItem() != null) {
            String month = cbMonth.getSelectedItem().toString();
            String year = cbYear.getSelectedItem().toString();
            lblAttendance.setText("Attendance Log — " + month + " " + year);
        }
    }

    private void initDateFilters() {
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateTo.setDateFormatString("dd/MM/yyyy");

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        dateFrom.setDate(cal.getTime());
        dateTo.setDate(new java.util.Date());

        styleJDateChooser(dateFrom);
        styleJDateChooser(dateTo);

        panelDate.setOpaque(false);
        panelDate.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; "
                + "arc: 12; "
                + "border: 2,6,2,6; "
                + "borderWidth: 1; "
                + "borderColor: #E2E8F0;");

        java.beans.PropertyChangeListener dateChangeListener = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadOrderHistoryOverview();
                loadOrderHistoryLog();
            }
        };
        dateFrom.addPropertyChangeListener(dateChangeListener);
        dateTo.addPropertyChangeListener(dateChangeListener);
    }

    private void styleJDateChooser(com.toedter.calendar.JDateChooser choser) {
        choser.setOpaque(false);
        choser.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        if (choser.getDateEditor() instanceof com.toedter.calendar.JTextFieldDateEditor editor) {
            editor.setOpaque(false);
            editor.setEditable(false);
            editor.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 4, 0, 4));
            editor.putClientProperty(FlatClientProperties.STYLE,
                    "foreground: #334155; "
                    + "font: 11pt;");
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

    private void initHRTabEvents() {
        java.awt.CardLayout hrCardLayout = (java.awt.CardLayout) panelEmployeeMain.getLayout();

        btnAttendance.addActionListener(e -> {
            hrCardLayout.show(panelEmployeeMain, "cardAttendance");
            setActiveHRTabStyle(btnAttendance);
        });

        btnEmployeeManagement.addActionListener(e -> {
            hrCardLayout.show(panelEmployeeMain, "cardEmployeeManagement");
            setActiveHRTabStyle(btnEmployeeManagement);
        });

        btnSchedule.addActionListener(e -> {
            hrCardLayout.show(panelEmployeeMain, "cardSchedule");
            setActiveHRTabStyle(btnSchedule);
        });

        hrCardLayout.show(panelEmployeeMain, "cardAttendance");
        setActiveHRTabStyle(btnAttendance);
    }

    private void setActiveHRTabStyle(javax.swing.JButton activeBtn) {
        javax.swing.JButton[] tabs = {btnAttendance, btnEmployeeManagement, btnSchedule};
        for (javax.swing.JButton btn : tabs) {
            if (btn == activeBtn) {
                btn.putClientProperty(FlatClientProperties.STYLE,
                        "foreground: #E38A45; borderWidth: 0; focusWidth: 0;");
            } else {
                btn.putClientProperty(FlatClientProperties.STYLE,
                        "foreground: #4A5568; borderWidth: 0; focusWidth: 0;");
            }
        }
    }

    private void loadOverviewCardsData() {
        repository.OrderRepository.DailyOrderStats stats = orderRepository.getDailyOrderStats();
        int presentEmployees = attendanceRepository.countTodayPresentEmployees();

        lblRevenueToday.setText(String.format("%,.0f đ", orderRepository.getDailyRevenue()));
        lblRevenueMonth.setText(String.format("%,.0f đ", orderRepository.getMonthlyRevenue()));
        lblActiveEmployees.setText(String.valueOf(presentEmployees));

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMenu = new javax.swing.JPanel();
        panelLogo = new javax.swing.JPanel();
        lbAvatarShop = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnDashBoard = new javax.swing.JButton();
        btnProductInventory = new javax.swing.JButton();
        btnOrderHistory = new javax.swing.JButton();
        btnHumanResources = new javax.swing.JButton();
        btnBackToSaleCounter = new javax.swing.JButton();
        btnCustomerManagement = new javax.swing.JButton();
        panelContent = new javax.swing.JPanel();
        panelDashboard = new javax.swing.JPanel();
        panelDashboardHeader = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        panelRevenueToday = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        lblRevenueToday = new javax.swing.JLabel();
        panelActiveOrders = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        lblPendingInvoice = new javax.swing.JLabel();
        panelCanceledOrders = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        lblRevenueMonth = new javax.swing.JLabel();
        panelOrderManagement = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableDashboard = new javax.swing.JTable();
        jLabel19 = new javax.swing.JLabel();
        btnCancelOrder = new javax.swing.JButton();
        panelActiveEmployees = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        lblActiveEmployees = new javax.swing.JLabel();
        panelRevenueDate = new javax.swing.JPanel();
        btnTuan = new javax.swing.JButton();
        btnThang = new javax.swing.JButton();
        panelTopSales = new javax.swing.JPanel();
        btnTopNgay = new javax.swing.JButton();
        btnTopThang = new javax.swing.JButton();
        panelProduct = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        panelOrderManagement1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableProduct = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        lblTotalItem = new javax.swing.JLabel();
        txtSearchProduct = new javax.swing.JTextField();
        cbAll = new javax.swing.JComboBox<>();
        btnAddProduct = new javax.swing.JButton();
        panelOrder = new javax.swing.JPanel();
        panelOrderManagement2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableTransactionHistory = new javax.swing.JTable();
        jLabel27 = new javax.swing.JLabel();
        lblRecordLog = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        panelTotalRevenue = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        lblTotalRevenue = new javax.swing.JLabel();
        lblPaidOrder = new javax.swing.JLabel();
        panelPaidOrders = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        lblPaidOrders = new javax.swing.JLabel();
        lblPercentTotal = new javax.swing.JLabel();
        panelCanceledOrder = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        lblOrdersCanceled = new javax.swing.JLabel();
        lblPercentCanceledOrder = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        lblAvgOrderValue = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
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
        panelHRMain = new javax.swing.JPanel();
        panelHRHeader = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        panelSelection = new javax.swing.JPanel();
        btnAttendance = new javax.swing.JButton();
        btnEmployeeManagement = new javax.swing.JButton();
        btnSchedule = new javax.swing.JButton();
        panelEmployeeMain = new javax.swing.JPanel();
        panelAttendance = new javax.swing.JPanel();
        lblOnTimeCount = new javax.swing.JLabel();
        lblLateCount = new javax.swing.JLabel();
        lblAbsentCount = new javax.swing.JLabel();
        lblRecordAttendance = new javax.swing.JLabel();
        lblAttendance = new javax.swing.JLabel();
        lblOnLeaveCount = new javax.swing.JLabel();
        cbMonth = new javax.swing.JComboBox<>();
        cbYear = new javax.swing.JComboBox<>();
        panelOrderManagement4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableAttendance = new javax.swing.JTable();
        panelEmployeeManagement = new javax.swing.JPanel();
        txtSearchEmployee = new javax.swing.JTextField();
        cbRole = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableEmployeeManagement = new javax.swing.JTable();
        panelSchedule = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnPrevWeek = new javax.swing.JButton();
        btnNextWeek = new javax.swing.JButton();
        lblWeekRange = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblMonthDayYear = new javax.swing.JLabel();
        lblEmployeeSchedule = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableSchedule = new javax.swing.JTable();
        panelCustomer = new javax.swing.JPanel();
        panelCustomerHeader = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(244, 246, 248));

        panelMenu.setBackground(new java.awt.Color(255, 255, 255));

        panelLogo.setBackground(new java.awt.Color(255, 255, 255));

        lbAvatarShop.setBackground(new java.awt.Color(255, 255, 255));
        lbAvatarShop.setText("Logo");
        lbAvatarShop.setPreferredSize(new java.awt.Dimension(45, 45));

        jLabel2.setBackground(new java.awt.Color(122, 67, 29));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(122, 67, 29));
        jLabel2.setText("Mon Staring ");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Cat shop");

        javax.swing.GroupLayout panelLogoLayout = new javax.swing.GroupLayout(panelLogo);
        panelLogo.setLayout(panelLogoLayout);
        panelLogoLayout.setHorizontalGroup(
            panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lbAvatarShop, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelLogoLayout.setVerticalGroup(
            panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLogoLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLogoLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3))
                    .addComponent(lbAvatarShop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );

        btnDashBoard.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDashBoard.setText("Statistical");
        btnDashBoard.setBorder(null);
        btnDashBoard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        btnProductInventory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnProductInventory.setText("Product");
        btnProductInventory.setBorder(null);
        btnProductInventory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        btnOrderHistory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnOrderHistory.setText("Invoice");
        btnOrderHistory.setBorder(null);
        btnOrderHistory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnOrderHistory.addActionListener(this::btnOrderHistoryActionPerformed);

        btnHumanResources.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnHumanResources.setText("Employee");
        btnHumanResources.setBorder(null);
        btnHumanResources.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnHumanResources.addActionListener(this::btnHumanResourcesActionPerformed);

        btnBackToSaleCounter.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBackToSaleCounter.setText("Back to POS");
        btnBackToSaleCounter.setBorder(null);
        btnBackToSaleCounter.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBackToSaleCounter.addActionListener(this::btnBackToSaleCounterActionPerformed);

        btnCustomerManagement.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCustomerManagement.setText("Customer");
        btnCustomerManagement.setBorder(null);
        btnCustomerManagement.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCustomerManagement.addActionListener(this::btnCustomerManagementActionPerformed);

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLogo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnCustomerManagement, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackToSaleCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnHumanResources, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnOrderHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addComponent(panelLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnOrderHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnHumanResources, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCustomerManagement, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackToSaleCounter, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );

        panelContent.setBackground(new java.awt.Color(244, 246, 248));
        panelContent.setPreferredSize(new java.awt.Dimension(100, 500));
        panelContent.setLayout(new java.awt.CardLayout());

        panelDashboard.setBackground(new java.awt.Color(244, 246, 248));

        panelDashboardHeader.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setBackground(new java.awt.Color(122, 67, 29));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(122, 67, 29));
        jLabel7.setText("Dashboard Overview");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Monitor your business performance");

        javax.swing.GroupLayout panelDashboardHeaderLayout = new javax.swing.GroupLayout(panelDashboardHeader);
        panelDashboardHeader.setLayout(panelDashboardHeaderLayout);
        panelDashboardHeaderLayout.setHorizontalGroup(
            panelDashboardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(panelDashboardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelDashboardHeaderLayout.setVerticalGroup(
            panelDashboardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        panelRevenueToday.setBackground(new java.awt.Color(255, 255, 255));
        panelRevenueToday.setPreferredSize(new java.awt.Dimension(220, 90));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(102, 102, 102));
        jLabel15.setText("Total Revenue Today");

        lblRevenueToday.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblRevenueToday.setForeground(new java.awt.Color(38, 205, 111));
        lblRevenueToday.setText("0đ");

        javax.swing.GroupLayout panelRevenueTodayLayout = new javax.swing.GroupLayout(panelRevenueToday);
        panelRevenueToday.setLayout(panelRevenueTodayLayout);
        panelRevenueTodayLayout.setHorizontalGroup(
            panelRevenueTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRevenueTodayLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelRevenueTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(lblRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        panelRevenueTodayLayout.setVerticalGroup(
            panelRevenueTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRevenueTodayLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        panelActiveOrders.setBackground(new java.awt.Color(255, 255, 255));
        panelActiveOrders.setPreferredSize(new java.awt.Dimension(220, 90));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(102, 102, 102));
        jLabel17.setText("Pending invoice");

        lblPendingInvoice.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblPendingInvoice.setForeground(new java.awt.Color(47, 116, 255));
        lblPendingInvoice.setText("0");

        javax.swing.GroupLayout panelActiveOrdersLayout = new javax.swing.GroupLayout(panelActiveOrders);
        panelActiveOrders.setLayout(panelActiveOrdersLayout);
        panelActiveOrdersLayout.setHorizontalGroup(
            panelActiveOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActiveOrdersLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelActiveOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPendingInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        panelActiveOrdersLayout.setVerticalGroup(
            panelActiveOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActiveOrdersLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPendingInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelCanceledOrders.setBackground(new java.awt.Color(255, 255, 255));
        panelCanceledOrders.setPreferredSize(new java.awt.Dimension(220, 90));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(102, 102, 102));
        jLabel18.setText("Revenue this month");

        lblRevenueMonth.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblRevenueMonth.setForeground(new java.awt.Color(38, 205, 111));
        lblRevenueMonth.setText("0đ");

        javax.swing.GroupLayout panelCanceledOrdersLayout = new javax.swing.GroupLayout(panelCanceledOrders);
        panelCanceledOrders.setLayout(panelCanceledOrdersLayout);
        panelCanceledOrdersLayout.setHorizontalGroup(
            panelCanceledOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCanceledOrdersLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panelCanceledOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRevenueMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        panelCanceledOrdersLayout.setVerticalGroup(
            panelCanceledOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCanceledOrdersLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblRevenueMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelOrderManagement.setBackground(new java.awt.Color(247, 246, 242));

        tableDashboard.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tableDashboard);

        jLabel19.setBackground(new java.awt.Color(122, 67, 29));
        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 19)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(122, 67, 29));
        jLabel19.setText("Today's bill");

        btnCancelOrder.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnCancelOrder.setForeground(new java.awt.Color(255, 77, 77));
        btnCancelOrder.setText("Cancel Selected Invoice");
        btnCancelOrder.setPreferredSize(new java.awt.Dimension(155, 35));

        javax.swing.GroupLayout panelOrderManagementLayout = new javax.swing.GroupLayout(panelOrderManagement);
        panelOrderManagement.setLayout(panelOrderManagementLayout);
        panelOrderManagementLayout.setHorizontalGroup(
            panelOrderManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderManagementLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagementLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        panelOrderManagementLayout.setVerticalGroup(
            panelOrderManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagementLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(panelOrderManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(87, Short.MAX_VALUE))
        );

        panelActiveEmployees.setBackground(new java.awt.Color(255, 255, 255));
        panelActiveEmployees.setPreferredSize(new java.awt.Dimension(220, 90));

        jLabel16.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(102, 102, 102));
        jLabel16.setText("Active Employees");

        lblActiveEmployees.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblActiveEmployees.setForeground(new java.awt.Color(227, 138, 69));
        lblActiveEmployees.setText("0");

        javax.swing.GroupLayout panelActiveEmployeesLayout = new javax.swing.GroupLayout(panelActiveEmployees);
        panelActiveEmployees.setLayout(panelActiveEmployeesLayout);
        panelActiveEmployeesLayout.setHorizontalGroup(
            panelActiveEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActiveEmployeesLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelActiveEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblActiveEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        panelActiveEmployeesLayout.setVerticalGroup(
            panelActiveEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelActiveEmployeesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblActiveEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelRevenueDate.setBackground(new java.awt.Color(255, 255, 255));
        panelRevenueDate.setForeground(new java.awt.Color(255, 255, 255));

        btnTuan.setText("Tuần");

        btnThang.setText("Tháng");

        javax.swing.GroupLayout panelRevenueDateLayout = new javax.swing.GroupLayout(panelRevenueDate);
        panelRevenueDate.setLayout(panelRevenueDateLayout);
        panelRevenueDateLayout.setHorizontalGroup(
            panelRevenueDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelRevenueDateLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTuan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnThang)
                .addGap(19, 19, 19))
        );
        panelRevenueDateLayout.setVerticalGroup(
            panelRevenueDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelRevenueDateLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelRevenueDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThang, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTuan, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(240, Short.MAX_VALUE))
        );

        panelTopSales.setBackground(new java.awt.Color(255, 255, 255));

        btnTopNgay.setText("Ngày");

        btnTopThang.setText("Tháng");

        javax.swing.GroupLayout panelTopSalesLayout = new javax.swing.GroupLayout(panelTopSales);
        panelTopSales.setLayout(panelTopSalesLayout);
        panelTopSalesLayout.setHorizontalGroup(
            panelTopSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelTopSalesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnTopNgay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTopThang)
                .addGap(14, 14, 14))
        );
        panelTopSalesLayout.setVerticalGroup(
            panelTopSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTopSalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTopSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTopNgay, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTopThang, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelDashboardLayout = new javax.swing.GroupLayout(panelDashboard);
        panelDashboard.setLayout(panelDashboardLayout);
        panelDashboardLayout.setHorizontalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDashboardHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDashboardLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelOrderManagement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelDashboardLayout.createSequentialGroup()
                        .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelDashboardLayout.createSequentialGroup()
                                .addComponent(panelRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelCanceledOrders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(panelRevenueDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDashboardLayout.createSequentialGroup()
                                .addComponent(panelActiveOrders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                                .addComponent(panelActiveEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(panelTopSales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(24, 24, 24))
        );
        panelDashboardLayout.setVerticalGroup(
            panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDashboardLayout.createSequentialGroup()
                .addComponent(panelDashboardHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelRevenueToday, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCanceledOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelActiveOrders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelActiveEmployees, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelRevenueDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelTopSales, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(panelOrderManagement, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelContent.add(panelDashboard, "CardDashboard");

        panelProduct.setBackground(new java.awt.Color(244, 246, 248));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setBackground(new java.awt.Color(122, 67, 29));
        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(122, 67, 29));
        jLabel9.setText("Product Inventory");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(102, 102, 102));
        jLabel10.setText("Manage products and stock levels");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addGap(16, 16, 16))
        );

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
            .addComponent(jScrollPane2)
        );
        panelOrderManagement1Layout.setVerticalGroup(
            panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagement1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(lblTotalItem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cbAll.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbAll.setForeground(new java.awt.Color(102, 102, 102));
        cbAll.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAddProduct.setText("+ Add Product");
        btnAddProduct.addActionListener(this::btnAddProductActionPerformed);

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelProductLayout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(81, 81, 81)
                .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAddProduct)
                .addGap(346, 346, 346))
            .addGroup(panelProductLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelOrderManagement1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelProductLayout.setVerticalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(panelOrderManagement1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        panelContent.add(panelProduct, "CardProduct");

        panelOrder.setBackground(new java.awt.Color(244, 246, 248));

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 933, Short.MAX_VALUE)
        );
        panelOrderManagement2Layout.setVerticalGroup(
            panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderManagement2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(lblRecordLog))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setBackground(new java.awt.Color(122, 67, 29));
        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(122, 67, 29));
        jLabel11.setText("Order History");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(102, 102, 102));
        jLabel12.setText("View and manage all transactions");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addGap(16, 16, 16))
        );

        panelTotalRevenue.setBackground(new java.awt.Color(255, 255, 255));
        panelTotalRevenue.setPreferredSize(new java.awt.Dimension(300, 150));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(102, 102, 102));
        jLabel23.setText("Total Revenue");

        lblTotalRevenue.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTotalRevenue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalRevenue.setText("0đ");

        lblPaidOrder.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder.setText("0 paid orders");

        javax.swing.GroupLayout panelTotalRevenueLayout = new javax.swing.GroupLayout(panelTotalRevenue);
        panelTotalRevenue.setLayout(panelTotalRevenueLayout);
        panelTotalRevenueLayout.setHorizontalGroup(
            panelTotalRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalRevenueLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(panelTotalRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPaidOrder)
                    .addComponent(jLabel23)
                    .addComponent(lblTotalRevenue, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        panelTotalRevenueLayout.setVerticalGroup(
            panelTotalRevenueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTotalRevenueLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalRevenue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPaidOrder)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelPaidOrders.setBackground(new java.awt.Color(255, 255, 255));
        panelPaidOrders.setPreferredSize(new java.awt.Dimension(300, 150));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Paid Orders");

        lblPaidOrders.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblPaidOrders.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPaidOrders.setText("0");

        lblPercentTotal.setForeground(new java.awt.Color(102, 102, 102));
        lblPercentTotal.setText("0% of total");

        javax.swing.GroupLayout panelPaidOrdersLayout = new javax.swing.GroupLayout(panelPaidOrders);
        panelPaidOrders.setLayout(panelPaidOrdersLayout);
        panelPaidOrdersLayout.setHorizontalGroup(
            panelPaidOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaidOrdersLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(panelPaidOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPercentTotal)
                    .addComponent(jLabel22)
                    .addComponent(lblPaidOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        panelPaidOrdersLayout.setVerticalGroup(
            panelPaidOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPaidOrdersLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPaidOrders)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPercentTotal)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCanceledOrder.setBackground(new java.awt.Color(255, 255, 255));
        panelCanceledOrder.setPreferredSize(new java.awt.Dimension(300, 150));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(102, 102, 102));
        jLabel21.setText("Canceled Orders");

        lblOrdersCanceled.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblOrdersCanceled.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblOrdersCanceled.setText("0");

        lblPercentCanceledOrder.setForeground(new java.awt.Color(102, 102, 102));
        lblPercentCanceledOrder.setText("0% of total");

        javax.swing.GroupLayout panelCanceledOrderLayout = new javax.swing.GroupLayout(panelCanceledOrder);
        panelCanceledOrder.setLayout(panelCanceledOrderLayout);
        panelCanceledOrderLayout.setHorizontalGroup(
            panelCanceledOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCanceledOrderLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(panelCanceledOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPercentCanceledOrder)
                    .addComponent(jLabel21)
                    .addComponent(lblOrdersCanceled, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        panelCanceledOrderLayout.setVerticalGroup(
            panelCanceledOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCanceledOrderLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblOrdersCanceled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPercentCanceledOrder)
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(102, 102, 102));
        jLabel26.setText("Avg. Order Value");

        lblAvgOrderValue.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblAvgOrderValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAvgOrderValue.setText("0đ");

        jLabel29.setForeground(new java.awt.Color(102, 102, 102));
        jLabel29.setText("Per paid order");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addComponent(lblAvgOrderValue, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAvgOrderValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel29)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblPaidOrder4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addGroup(panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbPaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout panelOrderLayout = new javax.swing.GroupLayout(panelOrder);
        panelOrder.setLayout(panelOrderLayout);
        panelOrderLayout.setHorizontalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelOrderLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelOrderLayout.createSequentialGroup()
                        .addComponent(panelTotalRevenue, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelPaidOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelCanceledOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelBelowHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(22, Short.MAX_VALUE))
            .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(panelOrderLayout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(25, Short.MAX_VALUE)))
        );
        panelOrderLayout.setVerticalGroup(
            panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOrderLayout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelPaidOrders, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addComponent(panelTotalRevenue, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addComponent(panelCanceledOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24)
                .addComponent(panelBelowHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(378, Short.MAX_VALUE))
            .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderLayout.createSequentialGroup()
                    .addContainerGap(367, Short.MAX_VALUE)
                    .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(17, 17, 17)))
        );

        panelContent.add(panelOrder, "CardOrder");

        panelHRMain.setBackground(new java.awt.Color(244, 246, 248));

        panelHRHeader.setBackground(new java.awt.Color(255, 255, 255));
        panelHRHeader.setPreferredSize(new java.awt.Dimension(992, 125));

        jLabel13.setBackground(new java.awt.Color(122, 67, 29));
        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(122, 67, 29));
        jLabel13.setText("Human Resources");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(102, 102, 102));
        jLabel14.setText("Track employee attendance and performance");

        panelSelection.setBackground(new java.awt.Color(255, 255, 255));

        btnAttendance.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnAttendance.setText("Attendance Log");
        btnAttendance.setBorderPainted(false);

        btnEmployeeManagement.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        btnEmployeeManagement.setText("Employee Management");
        btnEmployeeManagement.setBorderPainted(false);

        btnSchedule.setText("Scheduling");
        btnSchedule.setBorderPainted(false);

        javax.swing.GroupLayout panelSelectionLayout = new javax.swing.GroupLayout(panelSelection);
        panelSelection.setLayout(panelSelectionLayout);
        panelSelectionLayout.setHorizontalGroup(
            panelSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSelectionLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(btnAttendance, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEmployeeManagement, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(500, Short.MAX_VALUE))
        );
        panelSelectionLayout.setVerticalGroup(
            panelSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSelectionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAttendance, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                    .addComponent(btnSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnEmployeeManagement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout panelHRHeaderLayout = new javax.swing.GroupLayout(panelHRHeader);
        panelHRHeader.setLayout(panelHRHeaderLayout);
        panelHRHeaderLayout.setHorizontalGroup(
            panelHRHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHRHeaderLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(panelHRHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panelSelection, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        panelHRHeaderLayout.setVerticalGroup(
            panelHRHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHRHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelSelection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelEmployeeMain.setLayout(new java.awt.CardLayout());

        panelAttendance.setBackground(new java.awt.Color(244, 246, 248));

        lblOnTimeCount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblOnTimeCount.setForeground(new java.awt.Color(38, 205, 111));
        lblOnTimeCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblOnTimeCount.setText("0");

        lblLateCount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblLateCount.setForeground(new java.awt.Color(227, 138, 69));
        lblLateCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblLateCount.setText("0");

        lblAbsentCount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblAbsentCount.setForeground(new java.awt.Color(225, 59, 53));
        lblAbsentCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblAbsentCount.setText("0");

        lblRecordAttendance.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblRecordAttendance.setForeground(new java.awt.Color(102, 102, 102));
        lblRecordAttendance.setText("(0 records)");

        lblAttendance.setBackground(new java.awt.Color(122, 67, 29));
        lblAttendance.setFont(new java.awt.Font("Segoe UI", 0, 19)); // NOI18N
        lblAttendance.setForeground(new java.awt.Color(122, 67, 29));
        lblAttendance.setText("Attendance Log");

        lblOnLeaveCount.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblOnLeaveCount.setForeground(new java.awt.Color(47, 116, 255));
        lblOnLeaveCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblOnLeaveCount.setText("0");

        cbMonth.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbMonth.setForeground(new java.awt.Color(102, 102, 102));
        cbMonth.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMonth.addActionListener(this::cbMonthActionPerformed);

        cbYear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbYear.setForeground(new java.awt.Color(102, 102, 102));
        cbYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        panelOrderManagement4.setBackground(new java.awt.Color(247, 246, 242));

        tableAttendance.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(tableAttendance);

        javax.swing.GroupLayout panelOrderManagement4Layout = new javax.swing.GroupLayout(panelOrderManagement4);
        panelOrderManagement4.setLayout(panelOrderManagement4Layout);
        panelOrderManagement4Layout.setHorizontalGroup(
            panelOrderManagement4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 942, Short.MAX_VALUE)
        );
        panelOrderManagement4Layout.setVerticalGroup(
            panelOrderManagement4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelAttendanceLayout = new javax.swing.GroupLayout(panelAttendance);
        panelAttendance.setLayout(panelAttendanceLayout);
        panelAttendanceLayout.setHorizontalGroup(
            panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttendanceLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelOrderManagement4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelAttendanceLayout.createSequentialGroup()
                        .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAttendance, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelAttendanceLayout.createSequentialGroup()
                                .addComponent(lblRecordAttendance)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblOnTimeCount, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblLateCount, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAbsentCount, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblOnLeaveCount, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(317, 317, 317)
                        .addComponent(cbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbYear, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        panelAttendanceLayout.setVerticalGroup(
            panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttendanceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAttendance)
                    .addGroup(panelAttendanceLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                                .addComponent(lblRecordAttendance, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblOnTimeCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblLateCount, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblAbsentCount, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblOnLeaveCount))
                            .addGroup(panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(cbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbYear, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelOrderManagement4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelEmployeeMain.add(panelAttendance, "cardAttendance");

        panelEmployeeManagement.setBackground(new java.awt.Color(244, 246, 248));

        cbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton4.setBackground(new java.awt.Color(227, 138, 69));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("+ Add Employee");

        jPanel4.setBackground(new java.awt.Color(247, 246, 242));

        tableEmployeeManagement.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(tableEmployeeManagement);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelEmployeeManagementLayout = new javax.swing.GroupLayout(panelEmployeeManagement);
        panelEmployeeManagement.setLayout(panelEmployeeManagementLayout);
        panelEmployeeManagementLayout.setHorizontalGroup(
            panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEmployeeManagementLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelEmployeeManagementLayout.createSequentialGroup()
                        .addComponent(txtSearchEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 326, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        panelEmployeeManagementLayout.setVerticalGroup(
            panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEmployeeManagementLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearchEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        panelEmployeeMain.add(panelEmployeeManagement, "cardEmployeeManagement");

        panelSchedule.setBackground(new java.awt.Color(244, 246, 248));

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        btnPrevWeek.setText("<");
        btnPrevWeek.setBorderPainted(false);

        btnNextWeek.setText(">");
        btnNextWeek.setBorderPainted(false);

        lblWeekRange.setForeground(new java.awt.Color(102, 102, 102));
        lblWeekRange.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWeekRange.setText("Date");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWeekRange, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblWeekRange, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel4.setText("Weekly Schedule");

        lblMonthDayYear.setForeground(new java.awt.Color(102, 102, 102));
        lblMonthDayYear.setText("Month day - day, Year");

        lblEmployeeSchedule.setForeground(new java.awt.Color(102, 102, 102));
        lblEmployeeSchedule.setText("0 employees scheduled");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(102, 102, 102));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("·");

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        tableSchedule.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(tableSchedule);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelScheduleLayout = new javax.swing.GroupLayout(panelSchedule);
        panelSchedule.setLayout(panelScheduleLayout);
        panelScheduleLayout.setHorizontalGroup(
            panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScheduleLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelScheduleLayout.createSequentialGroup()
                        .addGroup(panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(panelScheduleLayout.createSequentialGroup()
                                .addComponent(lblMonthDayYear)
                                .addGap(2, 2, 2)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblEmployeeSchedule)))
                        .addGap(473, 473, 473)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelScheduleLayout.setVerticalGroup(
            panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScheduleLayout.createSequentialGroup()
                .addGroup(panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelScheduleLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblMonthDayYear)
                            .addComponent(lblEmployeeSchedule)
                            .addComponent(jLabel6)))
                    .addGroup(panelScheduleLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        panelEmployeeMain.add(panelSchedule, "cardSchedule");

        javax.swing.GroupLayout panelHRMainLayout = new javax.swing.GroupLayout(panelHRMain);
        panelHRMain.setLayout(panelHRMainLayout);
        panelHRMainLayout.setHorizontalGroup(
            panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelHRHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE)
            .addGroup(panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(panelEmployeeMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelHRMainLayout.setVerticalGroup(
            panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHRMainLayout.createSequentialGroup()
                .addComponent(panelHRHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 598, Short.MAX_VALUE))
            .addGroup(panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHRMainLayout.createSequentialGroup()
                    .addGap(0, 131, Short.MAX_VALUE)
                    .addComponent(panelEmployeeMain, javax.swing.GroupLayout.PREFERRED_SIZE, 592, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelContent.add(panelHRMain, "CardAttendance");

        panelCustomer.setBackground(new java.awt.Color(244, 246, 248));

        panelCustomerHeader.setBackground(new java.awt.Color(255, 255, 255));

        jLabel24.setBackground(new java.awt.Color(122, 67, 29));
        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(122, 67, 29));
        jLabel24.setText("Customer Management");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(102, 102, 102));
        jLabel25.setText("Management the customer information");

        javax.swing.GroupLayout panelCustomerHeaderLayout = new javax.swing.GroupLayout(panelCustomerHeader);
        panelCustomerHeader.setLayout(panelCustomerHeaderLayout);
        panelCustomerHeaderLayout.setHorizontalGroup(
            panelCustomerHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomerHeaderLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(panelCustomerHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel25)
                    .addComponent(jLabel24))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCustomerHeaderLayout.setVerticalGroup(
            panelCustomerHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomerHeaderLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setForeground(new java.awt.Color(255, 255, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane7.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 951, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelCustomerLayout = new javax.swing.GroupLayout(panelCustomer);
        panelCustomer.setLayout(panelCustomerLayout);
        panelCustomerLayout.setHorizontalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCustomerLayout.createSequentialGroup()
                .addComponent(panelCustomerHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panelCustomerLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );
        panelCustomerLayout.setVerticalGroup(
            panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCustomerLayout.createSequentialGroup()
                .addComponent(panelCustomerHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(100, 100, 100)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 30, Short.MAX_VALUE))
        );

        panelContent.add(panelCustomer, "CardCustomer");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, 982, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelContent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 723, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHumanResourcesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHumanResourcesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnHumanResourcesActionPerformed

    private void btnOrderHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrderHistoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnOrderHistoryActionPerformed

    private void cbMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMonthActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbMonthActionPerformed

    private void btnBackToSaleCounterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToSaleCounterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBackToSaleCounterActionPerformed

    private void btnCustomerManagementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomerManagementActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCustomerManagementActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        views.AddProductFrame addFrame = new views.AddProductFrame(() -> {
            loadInventoryTableData();
        });
        addFrame.setVisible(true);
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void txtSearchInvoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchInvoiceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchInvoiceActionPerformed

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            new DashBoardFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnAttendance;
    private javax.swing.JButton btnBackToSaleCounter;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnCustomerManagement;
    private javax.swing.JButton btnDashBoard;
    private javax.swing.JButton btnEmployeeManagement;
    private javax.swing.JButton btnHumanResources;
    private javax.swing.JButton btnNextWeek;
    private javax.swing.JButton btnOrderHistory;
    private javax.swing.JButton btnPrevWeek;
    private javax.swing.JButton btnProductInventory;
    private javax.swing.JButton btnSchedule;
    private javax.swing.JButton btnThang;
    private javax.swing.JButton btnTopNgay;
    private javax.swing.JButton btnTopThang;
    private javax.swing.JButton btnTuan;
    private javax.swing.JComboBox<String> cbAll;
    private javax.swing.JComboBox<String> cbMonth;
    private javax.swing.JComboBox<String> cbPaymentMethod;
    private javax.swing.JComboBox<String> cbRole;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JComboBox<String> cbYear;
    private com.toedter.calendar.JDateChooser dateFrom;
    private com.toedter.calendar.JDateChooser dateTo;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lbAvatarShop;
    private javax.swing.JLabel lblAbsentCount;
    private javax.swing.JLabel lblActiveEmployees;
    private javax.swing.JLabel lblAttendance;
    private javax.swing.JLabel lblAvgOrderValue;
    private javax.swing.JLabel lblEmployeeSchedule;
    private javax.swing.JLabel lblLateCount;
    private javax.swing.JLabel lblMonthDayYear;
    private javax.swing.JLabel lblOnLeaveCount;
    private javax.swing.JLabel lblOnTimeCount;
    private javax.swing.JLabel lblOrdersCanceled;
    private javax.swing.JLabel lblPaidOrder;
    private javax.swing.JLabel lblPaidOrder1;
    private javax.swing.JLabel lblPaidOrder2;
    private javax.swing.JLabel lblPaidOrder3;
    private javax.swing.JLabel lblPaidOrder4;
    private javax.swing.JLabel lblPaidOrders;
    private javax.swing.JLabel lblPendingInvoice;
    private javax.swing.JLabel lblPercentCanceledOrder;
    private javax.swing.JLabel lblPercentTotal;
    private javax.swing.JLabel lblRecordAttendance;
    private javax.swing.JLabel lblRecordLog;
    private javax.swing.JLabel lblRevenueMonth;
    private javax.swing.JLabel lblRevenueToday;
    private javax.swing.JLabel lblTotalItem;
    private javax.swing.JLabel lblTotalRevenue;
    private javax.swing.JLabel lblWeekRange;
    private javax.swing.JPanel panelActiveEmployees;
    private javax.swing.JPanel panelActiveOrders;
    private javax.swing.JPanel panelAttendance;
    private javax.swing.JPanel panelBelowHeader;
    private javax.swing.JPanel panelCanceledOrder;
    private javax.swing.JPanel panelCanceledOrders;
    private javax.swing.JPanel panelContent;
    private javax.swing.JPanel panelCustomer;
    private javax.swing.JPanel panelCustomerHeader;
    private javax.swing.JPanel panelDashboard;
    private javax.swing.JPanel panelDashboardHeader;
    private javax.swing.JPanel panelDate;
    private javax.swing.JPanel panelEmployeeMain;
    private javax.swing.JPanel panelEmployeeManagement;
    private javax.swing.JPanel panelHRHeader;
    private javax.swing.JPanel panelHRMain;
    private javax.swing.JPanel panelLogo;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelOrder;
    private javax.swing.JPanel panelOrderManagement;
    private javax.swing.JPanel panelOrderManagement1;
    private javax.swing.JPanel panelOrderManagement2;
    private javax.swing.JPanel panelOrderManagement4;
    private javax.swing.JPanel panelPaidOrders;
    private javax.swing.JPanel panelProduct;
    private javax.swing.JPanel panelRevenueDate;
    private javax.swing.JPanel panelRevenueToday;
    private javax.swing.JPanel panelSchedule;
    private javax.swing.JPanel panelSelection;
    private javax.swing.JPanel panelTopSales;
    private javax.swing.JPanel panelTotalRevenue;
    private javax.swing.JTable tableAttendance;
    private javax.swing.JTable tableDashboard;
    private javax.swing.JTable tableEmployeeManagement;
    private javax.swing.JTable tableProduct;
    private javax.swing.JTable tableSchedule;
    private javax.swing.JTable tableTransactionHistory;
    private javax.swing.JTextField txtSearchEmployee;
    private javax.swing.JTextField txtSearchInvoice;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
}

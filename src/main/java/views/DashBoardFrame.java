package views;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
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

import controller.OrderCartController;
import service.CategoryService;
import service.ProductService;
import service.impl.CategoryServiceImpl;
import service.impl.ProductServiceImpl;
import ui.MenuIcons;
import ui.ScannerButtonUI;
import util.ImageUtil;
import util.VietQrRenderer;

public class DashBoardFrame extends javax.swing.JFrame {

    private final repository.EmployeeRepository employeeRepository = new repository.EmployeeRepository();
    private final repository.OrderRepository orderRepository = new repository.OrderRepository();
    private final service.CustomerService customerService = new service.impl.CustomerServiceImpl();
    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final ProductRepository productRepository = new ProductRepository();
    private final AttendanceRepository attendanceRepository = new AttendanceRepository();
    private ui.BarChartPanel chartTuan;
    private ui.BarChartPanel chartThang;
    private javax.swing.JComboBox<String> cbWeekFilter;
    private java.awt.CardLayout cardRevenue;
    private ui.TopSalesPanel topSalesNgay;
    private ui.TopSalesPanel topSalesThang;
    private java.awt.CardLayout cardTopSales;
    private SalesCounterFrame salesCounter;
    private javax.swing.JPanel checkInListPanel;
    private javax.swing.JButton btnLoginQuick;

    // Fields for Sales Counter Panel integration
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final ProductService productService = new ProductServiceImpl();
    private final service.impl.CustomerServiceImpl customerServiceImpl = new service.impl.CustomerServiceImpl();
    private final repository.CustomerRepository customerRepository = new repository.CustomerRepository();
    private entity.Customer selectedCustomer;
    private javax.swing.JPopupMenu customerSuggestPopup;
    private javax.swing.Timer customerSearchTimer;
    private final java.util.List<Integer> sessionOrderIds = new java.util.ArrayList<>();
    private java.util.List<entity.Product> cachedProductList = new java.util.ArrayList<>();
    private java.util.List<entity.Category> cachedCategoryList = new java.util.ArrayList<>();
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> productTableSorter;
    private final java.util.Map<String, javax.swing.ImageIcon> productImageCache = new java.util.HashMap<>();
    private final java.util.List<OrderCartController> orderSessions = new java.util.ArrayList<>();
    private int activeIndex = 0;
    private final java.util.List<javax.swing.JButton> tabButtons = new java.util.ArrayList<>();

    private final java.util.Map<String, javax.swing.ImageIcon> imageCache = new java.util.HashMap<>();
    private javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> inventorySorter;
    private boolean isInitializingAttendance = true;
    private java.time.LocalDate currentWeekStart;
    private java.util.List<java.time.LocalDate> weekStartOptions = new java.util.ArrayList<>();
    private javax.swing.JLabel lblMonthTitle;
    private javax.swing.JLabel lblMonthSalesTitle;
    private int selectedMonth = java.time.LocalDate.now().getMonthValue();
    private int selectedYear = java.time.LocalDate.now().getYear();

    public DashBoardFrame(SalesCounterFrame salesCounter) {
        initComponents();
        this.salesCounter = salesCounter;
        this.selectedMonth = java.time.LocalDate.now().getMonthValue();
        this.selectedYear = java.time.LocalDate.now().getYear();
        this.currentWeekStart = java.time.LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        panelRevenueDate.setPreferredSize(new java.awt.Dimension(500, 220));
        panelTopSales.setPreferredSize(new java.awt.Dimension(400, 220));
        setupScrollArea();
        setupTopSales();
        initHRTabEvents();
        loadOverviewCardsData();
        customInventoryAppearance();
        loadInventoryTableData();
        loadCategoryComboBox();
        initInventoryFilterEvents();
        initStatusFilter();
        initSearchInvoiceFilter();
        initDateFilters();
        customHistoryTableAppearance();
        refreshOrderHistory();
        customDashboardComponentsStyle();
        updateAttendanceTitle();
        initAttendanceDateFilter();
        customAttendanceTableAppearance();
        loadAttendanceTableData();
        loadCheckedInEmployees();
        loadRoleComboBox();
        customEmployeeManagementTableAppearance();
        initEmployeeManagementFilterEvents();
        loadEmployeeManagementTableData();

        customCustomerTableAppearance();
        initCustomerFilterEvents();
        loadCustomerTableData();

        this.getContentPane().setLayout(new java.awt.BorderLayout(0, 0));
        this.getContentPane().add(panelMenu, java.awt.BorderLayout.WEST);
        this.getContentPane().add(panelContent, java.awt.BorderLayout.CENTER);

        this.setSize(1240, 800);
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

        javax.swing.JButton[] menuButtons = { btnDashBoard, btnProductInventory,
                btnOrderHistory, btnHumanResources, btnCustomerManagement, btnPOSPayment };
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
        btnCustomerManagement.setIcon(ui.MenuIcons.customerManagement());
        btnPOSPayment.setIcon(ui.MenuIcons.backToSaleCounter());
        btnDashBoard.putClientProperty("cardName", "CardDashboard");
        btnProductInventory.putClientProperty("cardName", "CardProduct");
        btnOrderHistory.putClientProperty("cardName", "CardOrder");
        btnHumanResources.putClientProperty("cardName", "CardAttendance");
        btnCustomerManagement.putClientProperty("cardName", "CardCustomer");
        btnPOSPayment.putClientProperty("cardName", "cardSaleCounter");

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
        initSalesCounterPanel();

        // Cấu hình cbbLogOut hiển thị "Xin chào + Tên" và chức năng đăng xuất
        entity.Employee currentUser = util.UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayInfo = "Xin chào, " + currentUser.getFullName();
            cbbLogOut.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { displayInfo, "Đăng xuất" }));
        } else {
            cbbLogOut.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Chưa đăng nhập", "Đăng nhập" }));
        }

        // Thiết lập phong cách hiển thị (Styling) cho cbbLogOut đồng bộ
        cbbLogOut.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        cbbLogOut.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #6F3B1A;"
                + "borderColor: #D2B48C;"
                + "borderWidth: 1;"
                + "arc: 12;"
                + "buttonBackground: #FFFFFF;"
                + "buttonArrowColor: #6F3B1A;"
                + "focusWidth: 0;");

        cbbLogOut.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel label = (javax.swing.JLabel) super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
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

        cbbLogOut.addActionListener(e -> {
            String selected = (String) cbbLogOut.getSelectedItem();
            if ("Đăng xuất".equals(selected)) {
                int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn đăng xuất?",
                        "Xác nhận đăng xuất",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (confirm == javax.swing.JOptionPane.YES_OPTION) {
                    util.UserSession.getInstance().cleanUserSession();
                    util.AppRouter.showSalesCounterFrame();
                    this.dispose();
                } else {
                    cbbLogOut.setSelectedIndex(0);
                }
            } else if ("Đăng nhập".equals(selected)) {
                util.AppRouter.showSalesCounterFrame();
                this.dispose();
            }
        });
    }

    public void refreshAfterNewOrder() {
        loadOverviewCardsData();
        dateTo.setDate(new java.util.Date());
        refreshOrderHistory();
        loadInventoryTableData();

        // Refresh charts (panelRevenueDate)
        if (chartTuan != null) {
            if (cbWeekFilter != null && !weekStartOptions.isEmpty() && cbWeekFilter.getSelectedIndex() >= 0) {
                currentWeekStart = weekStartOptions.get(cbWeekFilter.getSelectedIndex());
                loadWeekChartData(currentWeekStart);
            } else {
                repository.OrderRepository.WeekRevenueResult weekData = orderRepository.getRevenueByWeek();
                chartTuan.setData(weekData.labels, weekData.values);
                chartTuan.repaint();
            }
        }
        if (chartThang != null) {
            String[] labelsThang = { "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12" };
            chartThang.setData(labelsThang, orderRepository.getRevenueByMonth());
            chartThang.repaint();
        }

        // Refresh top selling products (panelTopSales)
        if (topSalesNgay != null) {
            topSalesNgay.setData(orderRepository.getTopProductsToday(5));
            topSalesNgay.repaint();
        }
        if (topSalesThang != null) {
            topSalesThang.setData(orderRepository.getTopProductsThisMonth(5));
            topSalesThang.repaint();
        }

        panelRevenueDate.revalidate();
        panelRevenueDate.repaint();
        panelTopSales.revalidate();
        panelTopSales.repaint();
    }

    private void loadWeekChartData(java.time.LocalDate weekStart) {
        if (chartTuan == null) {
            return;
        }
        repository.OrderRepository.WeekRevenueResult weekData = orderRepository.getRevenueByWeek(weekStart);
        chartTuan.setData(weekData.labels, weekData.values);
        chartTuan.repaint();
    }

    private void updateMonthLabels(int month, int year) {
        String monthName = util.DateConstants.MONTH_NAMES[Math.max(0, Math.min(month - 1, 11))];
        String text = "This Month — " + monthName;
        if (year != java.time.LocalDate.now().getYear()) {
            text += " " + year;
        }
        if (lblMonthTitle != null) {
            lblMonthTitle.setText(text);
        }
        if (lblMonthSalesTitle != null) {
            lblMonthSalesTitle.setText(text);
        }
    }

    private void updateWeekFilterForMonth(int month, int year) {
        if (cbWeekFilter == null) {
            return;
        }

        weekStartOptions.clear();
        cbWeekFilter.removeAllItems();

        java.time.LocalDate firstDay = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate monthEnd = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        java.time.LocalDate cursor = firstDay
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        while (!cursor.isAfter(monthEnd)) {
            weekStartOptions.add(cursor);
            cursor = cursor.plusWeeks(1);
        }

        java.time.format.DateTimeFormatter rangeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        for (java.time.LocalDate weekStart : weekStartOptions) {
            java.time.LocalDate weekEnd = weekStart.plusDays(6);
            String itemText = weekStart.format(rangeFormatter) + " - " + weekEnd.format(rangeFormatter);
            cbWeekFilter.addItem(itemText);
        }

        int defaultIndex = 0;
        if (year == java.time.LocalDate.now().getYear() && month == java.time.LocalDate.now().getMonthValue()) {
            defaultIndex = weekStartOptions.indexOf(currentWeekStart);
            if (defaultIndex < 0) {
                defaultIndex = 0;
            }
        }
        if (defaultIndex >= 0 && defaultIndex < cbWeekFilter.getItemCount()) {
            cbWeekFilter.setSelectedIndex(defaultIndex);
            currentWeekStart = weekStartOptions.get(defaultIndex);
            loadWeekChartData(currentWeekStart);
        }
    }

    private void setupScrollArea() {
        panelRevenueDate.removeAll();
        panelRevenueDate.setLayout(new BorderLayout());
        panelRevenueDate.setBackground(Color.WHITE);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 8, 12));

        javax.swing.JLabel lblChartTitle = new javax.swing.JLabel("Revenue Overview");
        lblChartTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        lblChartTitle.setForeground(new java.awt.Color(30, 41, 59));

        headerRow.add(lblChartTitle, BorderLayout.WEST);
        panelRevenueDate.add(headerRow, BorderLayout.NORTH);

        JPanel chartCard = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        chartCard.setBackground(Color.WHITE);

        String[] labelsThang = { "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12" };

        this.currentWeekStart = java.time.LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        chartTuan = new BarChartPanel();
        chartThang = new BarChartPanel();
        // Gán listener click cột cho biểu đồ doanh thu tháng để lọc top products theo
        // tháng tương ứng.
        chartThang.setOnBarClickListener(index -> {
            if (index < 0 || index >= labelsThang.length) {
                return;
            }
            selectedMonth = index + 1;
            selectedYear = java.time.LocalDate.now().getYear();
            updateMonthLabels(selectedMonth, selectedYear);
            updateWeekFilterForMonth(selectedMonth, selectedYear);
            if (topSalesThang != null) {
                topSalesThang.setData(orderRepository.getTopProductsByMonth(selectedMonth, selectedYear, 5));
                topSalesThang.repaint();
            }
        });
        repository.OrderRepository.WeekRevenueResult weekData = orderRepository.getRevenueByWeek(this.currentWeekStart);
        chartTuan.setData(weekData.labels, weekData.values);
        chartThang.setData(labelsThang, orderRepository.getRevenueByMonth());

        JPanel weekPanel = new JPanel(new BorderLayout());
        weekPanel.setBackground(Color.WHITE);
        javax.swing.JLabel lblWeekTitle = new javax.swing.JLabel("This Week");
        lblWeekTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblWeekTitle.setForeground(new java.awt.Color(100, 116, 139));
        // 40px is a tighter match for the bar-chart Y-axis label start.
        lblWeekTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 40, 4, 0));

        JPanel weekHeader = new JPanel(new BorderLayout());
        weekHeader.setBackground(Color.WHITE);
        weekHeader.add(lblWeekTitle, BorderLayout.WEST);
        cbWeekFilter = new javax.swing.JComboBox<>();
        cbWeekFilter.addActionListener(e -> {
            if (weekStartOptions.isEmpty() || cbWeekFilter.getSelectedIndex() < 0) {
                return;
            }
            currentWeekStart = weekStartOptions.get(cbWeekFilter.getSelectedIndex());
            loadWeekChartData(currentWeekStart);
        });
        weekHeader.add(cbWeekFilter, BorderLayout.EAST);
        weekPanel.add(weekHeader, BorderLayout.NORTH);
        weekPanel.add(chartTuan, BorderLayout.CENTER);

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setBackground(Color.WHITE);
        lblMonthTitle = new javax.swing.JLabel("This Month");
        lblMonthTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblMonthTitle.setForeground(new java.awt.Color(100, 116, 139));
        // 40px is a tighter match for the bar-chart Y-axis label start.
        lblMonthTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 40, 4, 0));
        monthPanel.add(lblMonthTitle, BorderLayout.NORTH);
        monthPanel.add(chartThang, BorderLayout.CENTER);

        updateMonthLabels(selectedMonth, selectedYear);
        updateWeekFilterForMonth(selectedMonth, selectedYear);

        chartCard.add(weekPanel);
        chartCard.add(monthPanel);

        panelRevenueDate.add(chartCard, BorderLayout.CENTER);

        panelRevenueDate.setPreferredSize(new Dimension(panelRevenueDate.getWidth(), 220));
        panelTopSales.setPreferredSize(new Dimension(panelTopSales.getWidth(), 220));

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

        javax.swing.JLabel lblTitle = new javax.swing.JLabel("Best Selling Products");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(30, 41, 59));

        headerRow.add(lblTitle, BorderLayout.WEST);
        panelTopSales.add(headerRow, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new java.awt.GridLayout(1, 2, 16, 0));
        cardPanel.setBackground(Color.WHITE);

        topSalesNgay = new ui.TopSalesPanel();
        topSalesThang = new ui.TopSalesPanel();

        topSalesNgay.setData(orderRepository.getTopProductsToday(5));
        topSalesThang.setData(orderRepository.getTopProductsThisMonth(5));

        JPanel todayPanel = new JPanel(new BorderLayout());
        todayPanel.setBackground(Color.WHITE);
        javax.swing.JLabel lblTodayTitle = new javax.swing.JLabel("Today");
        lblTodayTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblTodayTitle.setForeground(new java.awt.Color(100, 116, 139));
        // 16px matches TopSalesPanel's paddingLeft so title aligns with list content.
        lblTodayTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 4, 0));
        todayPanel.add(lblTodayTitle, BorderLayout.NORTH);
        todayPanel.add(topSalesNgay, BorderLayout.CENTER);

        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setBackground(Color.WHITE);
        lblMonthSalesTitle = new javax.swing.JLabel("This Month");
        lblMonthSalesTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblMonthSalesTitle.setForeground(new java.awt.Color(100, 116, 139));
        // 16px matches TopSalesPanel's paddingLeft so title aligns with list content.
        lblMonthSalesTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 16, 4, 0));
        monthPanel.add(lblMonthSalesTitle, BorderLayout.NORTH);
        monthPanel.add(topSalesThang, BorderLayout.CENTER);

        updateMonthLabels(selectedMonth, selectedYear);

        cardPanel.add(todayPanel);
        cardPanel.add(monthPanel);

        panelTopSales.add(cardPanel, BorderLayout.CENTER);

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
        panelRevenueToday.setBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelActiveEmployees.setBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelActiveOrders.setBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));
        panelCanceledOrders.setBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(new java.awt.Insets(0, 0, 0, 0), cardBorderColor, 1, 16));

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
                panel.setBorder(
                        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(226, 232, 240)));

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

    private void setCardPadding(javax.swing.JPanel panel, int leftPad) {
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new com.formdev.flatlaf.ui.FlatLineBorder(
                        new java.awt.Insets(0, 0, 0, 0),
                        java.awt.Color.decode("#F1F5F9"), 1, 14),
                javax.swing.BorderFactory.createEmptyBorder(14, leftPad, 14, 12)));
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
            text = currentWeekStart.format(monthDayFmt) + " – " + weekEnd.format(monthDayFmt) + ", "
                    + weekEnd.getYear();
        }

        lblMonthDayYear.setText(text);
        lblEmployeeSchedule
                .setText(employeeCount + (employeeCount == 1 ? " employee scheduled" : " employees scheduled"));
    }

    private void customEmployeeManagementTableAppearance() {
        tableEmployeeManagement.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] { "Employee ID", "Full Name", "Role", "Phone", "Barcode", "Status", "Actions" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, true };

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
        int actionColumnIndex = 6;
        for (int i = 0; i < tableEmployeeManagement.getColumnCount(); i++) {
            if (i != actionColumnIndex) {
                tableEmployeeManagement.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }

        java.awt.Container parent = tableEmployeeManagement.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableEmployeeManagement, "Nothing found!"));
            scrollPane.setViewportView(tableEmployeeManagement);
        }

        ui.EmployeeActionCellRenderer actionRenderer = new ui.EmployeeActionCellRenderer();
        ui.EmployeeActionCellEditor actionEditor = new ui.EmployeeActionCellEditor(
                this::onEditEmployee,
                this::onDeleteEmployee,
                this::onShowBarcode);
        tableEmployeeManagement.setRowHeight(50);
        tableEmployeeManagement.getColumnModel().getColumn(actionColumnIndex).setCellRenderer(actionRenderer);
        tableEmployeeManagement.getColumnModel().getColumn(actionColumnIndex).setCellEditor(actionEditor);
        tableEmployeeManagement.getColumnModel().getColumn(actionColumnIndex).setPreferredWidth(210);
        tableEmployeeManagement.getColumnModel().getColumn(actionColumnIndex).setMaxWidth(230);
        tableEmployeeManagement.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableEmployeeManagement.rowAtPoint(e.getPoint());
                int col = tableEmployeeManagement.columnAtPoint(e.getPoint());
                if (row >= 0 && col == actionColumnIndex) {
                    tableEmployeeManagement.editCellAt(row, col, e);
                }
            }
        });
    }

    private void onEditEmployee(int rowIndex) {
        int modelRow = tableEmployeeManagement.convertRowIndexToModel(rowIndex);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableEmployeeManagement
                .getModel();
        String empIdStr = model.getValueAt(modelRow, 0).toString();
        String roleName = model.getValueAt(modelRow, 2).toString();
        try {
            int empId = Integer.parseInt(empIdStr.replace("EMP", ""));
            entity.Employee currentUser = util.UserSession.getInstance().getCurrentUser();

            // Validate: Không được phép sửa tài khoản Manager khác
            if ("Manager".equalsIgnoreCase(roleName) && (currentUser == null || currentUser.getId() != empId)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không được phép thay đổi thông tin tài khoản Manager khác!",
                        "Cảnh báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            openEditEmployeeFrame(empId);
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Mã nhân viên không hợp lệ.",
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowBarcode(int rowIndex) {
        int modelRow = tableEmployeeManagement.convertRowIndexToModel(rowIndex);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableEmployeeManagement
                .getModel();
        String empIdStr = model.getValueAt(modelRow, 0).toString();
        String fullName = model.getValueAt(modelRow, 1).toString();

        Object barcodeObj = model.getValueAt(modelRow, 4); // Column index 4 is Barcode
        if (barcodeObj == null || barcodeObj.toString().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Nhân viên này chưa có mã vạch (Barcode)!",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String barcodeVal = barcodeObj.toString().trim();

        try {
            // Ensure src/main/resources/barcode/emp folder exists
            java.io.File dir = new java.io.File("src/main/resources/barcode/emp");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            java.io.File outputFile = new java.io.File(dir, empIdStr + ".png");
            java.awt.image.BufferedImage img;

            if (outputFile.exists()) {
                img = javax.imageio.ImageIO.read(outputFile);
            } else {
                // Generate barcode image using ZXing Code 128
                int width = 300;
                int height = 100;
                com.google.zxing.oned.Code128Writer writer = new com.google.zxing.oned.Code128Writer();
                com.google.zxing.common.BitMatrix bitMatrix = writer.encode(
                        barcodeVal,
                        com.google.zxing.BarcodeFormat.CODE_128,
                        width,
                        height);

                // Write to a temporary file in src/main/resources/barcode
                com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputFile.toPath());

                // Convert BitMatrix to BufferedImage
                img = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
            }

            // Create custom panel to display the barcode & employee info
            JPanel panel = new JPanel(new java.awt.BorderLayout(10, 10));
            panel.setBackground(java.awt.Color.WHITE);
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

            javax.swing.JLabel lblInfo = new javax.swing.JLabel(
                    "<html><b>Nhân viên:</b> " + fullName + " (" + empIdStr + ")<br><b>Mã vạch:</b> " + barcodeVal
                            + "</html>",
                    javax.swing.SwingConstants.CENTER);
            lblInfo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            lblInfo.setForeground(new java.awt.Color(30, 41, 59));

            javax.swing.JLabel lblImage = new javax.swing.JLabel(new javax.swing.ImageIcon(img));
            lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            panel.add(lblInfo, java.awt.BorderLayout.NORTH);
            panel.add(lblImage, java.awt.BorderLayout.CENTER);

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    panel,
                    "Mã vạch nhân viên - " + empIdStr,
                    javax.swing.JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi khi tạo mã vạch: " + e.getMessage(),
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowBarcodeProduct(int rowIndex) {
        int modelRow = tableProduct.convertRowIndexToModel(rowIndex);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableProduct.getModel();
        String productIdStr = model.getValueAt(modelRow, 0).toString();
        String productName = model.getValueAt(modelRow, 2).toString();

        Object barcodeObj = model.getValueAt(modelRow, 4); // Column index 4 is Barcode
        if (barcodeObj == null || barcodeObj.toString().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Sản phẩm này chưa có mã vạch (Barcode)!",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        String barcodeVal = barcodeObj.toString().trim();

        try {
            // Ensure src/main/resources/barcode/pro folder exists
            java.io.File dir = new java.io.File("src/main/resources/barcode/pro");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            java.io.File outputFile = new java.io.File(dir, productIdStr + ".png");
            java.awt.image.BufferedImage img;

            if (outputFile.exists()) {
                img = javax.imageio.ImageIO.read(outputFile);
            } else {
                // Generate barcode image using ZXing Code 128
                int width = 300;
                int height = 100;
                com.google.zxing.oned.Code128Writer writer = new com.google.zxing.oned.Code128Writer();
                com.google.zxing.common.BitMatrix bitMatrix = writer.encode(
                        barcodeVal,
                        com.google.zxing.BarcodeFormat.CODE_128,
                        width,
                        height);

                // Write to a temporary file in src/main/resources/barcode/pro
                com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputFile.toPath());

                // Convert BitMatrix to BufferedImage
                img = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(bitMatrix);
            }

            // Create custom panel to display the barcode & product info
            JPanel panel = new JPanel(new java.awt.BorderLayout(10, 10));
            panel.setBackground(java.awt.Color.WHITE);
            panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

            javax.swing.JLabel lblInfo = new javax.swing.JLabel(
                    "<html><b>Sản phẩm:</b> " + productName + " (" + productIdStr + ")<br><b>Mã vạch:</b> " + barcodeVal
                            + "</html>",
                    javax.swing.SwingConstants.CENTER);
            lblInfo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            lblInfo.setForeground(new java.awt.Color(30, 41, 59));

            javax.swing.JLabel lblImage = new javax.swing.JLabel(new javax.swing.ImageIcon(img));
            lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            panel.add(lblInfo, java.awt.BorderLayout.NORTH);
            panel.add(lblImage, java.awt.BorderLayout.CENTER);

            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    panel,
                    "Mã vạch sản phẩm - " + productIdStr,
                    javax.swing.JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Lỗi khi tạo mã vạch: " + e.getMessage(),
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteEmployee(int rowIndex) {
        int modelRow = tableEmployeeManagement.convertRowIndexToModel(rowIndex);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableEmployeeManagement
                .getModel();
        String empIdStr = model.getValueAt(modelRow, 0).toString();
        String fullName = model.getValueAt(modelRow, 1).toString();
        String roleName = model.getValueAt(modelRow, 2).toString();

        try {
            int empId = Integer.parseInt(empIdStr.replace("EMP", ""));
            entity.Employee currentUser = util.UserSession.getInstance().getCurrentUser();

            // 1. Không được phép xóa tài khoản đang đăng nhập
            if (currentUser != null && currentUser.getId() == empId) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không được phép xóa tài khoản đang đăng nhập!",
                        "Cảnh báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Không được phép xóa tài khoản Manager khác
            if ("Manager".equalsIgnoreCase(roleName) && (currentUser == null || currentUser.getId() != empId)) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không được phép xóa tài khoản Manager khác!",
                        "Cảnh báo",
                        javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            // Ignore for initial check, will show error dialog below if parsed fails
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa nhân viên " + fullName + " (" + empIdStr + ")?",
                "Xác nhận xóa",
                javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int empId = Integer.parseInt(empIdStr.replace("EMP", ""));
            if (employeeRepository.deleteEmployee(empId)) {
                loadEmployeeManagementTableData();
                if (salesCounter != null) {
                    salesCounter.loadCheckedInEmployees();
                    salesCounter.refreshUserDropdown();
                }
                javax.swing.JOptionPane.showMessageDialog(this, "Đã xóa nhân viên thành công.");
            } else {
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Không thể xóa nhân viên. Vui lòng thử lại.",
                        "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Mã nhân viên không hợp lệ.",
                    "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
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

    // private void loadEmployeeManagementTableData() { lỗi khi gọi lại hàm liên tục
    // làm dữ liệu trùng lặp hoặc crash
    // javax.swing.table.DefaultTableModel model
    // = (javax.swing.table.DefaultTableModel) tableEmployeeManagement.getModel();
    // model.setRowCount(0);
    //
    // // Đọc filter trên EDT trước khi vào background thread
    // String role = (cbRole != null && cbRole.getSelectedItem() != null)
    // ? cbRole.getSelectedItem().toString() : "All";
    // String keyword = (txtSearchEmployee != null) ? txtSearchEmployee.getText() :
    // "";
    //
    // new
    // javax.swing.SwingWorker<java.util.List<repository.EmployeeRepository.EmployeeRow>,
    // Void>() {
    // @Override
    // protected java.util.List<repository.EmployeeRepository.EmployeeRow>
    // doInBackground() {
    // return employeeRepository.findEmployees(role, keyword);
    // }
    //
    // @Override
    // protected void done() {
    // try {
    // for (var rec : get()) {
    // String empIdStr = String.format("EMP%03d", rec.id);
    // String statusStr = (rec.status == 1) ? "Active" : "Inactive";
    // model.addRow(new Object[]{
    // empIdStr, rec.fullName, rec.roleName, rec.phone, rec.barcode, statusStr, ""
    // });
    // }
    // tableEmployeeManagement.revalidate();
    // tableEmployeeManagement.repaint();
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // }
    // }.execute();
    // }
    private void loadEmployeeManagementTableData() {
        // 1. Đọc filter trên EDT trước khi vào background thread
        String role = (cbRole != null && cbRole.getSelectedItem() != null)
                ? cbRole.getSelectedItem().toString()
                : "All";
        String keyword = (txtSearchEmployee != null) ? txtSearchEmployee.getText() : "";

        // 2. Chạy SwingWorker để lấy dữ liệu ngầm
        new javax.swing.SwingWorker<java.util.List<repository.EmployeeRepository.EmployeeRow>, Void>() {
            @Override
            protected java.util.List<repository.EmployeeRepository.EmployeeRow> doInBackground() {
                // Khối này chạy ngầm, không block Giao diện (UI)
                return employeeRepository.findEmployees(role, keyword);
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả từ doInBackground()
                    java.util.List<repository.EmployeeRepository.EmployeeRow> employees = get();

                    // Lấy model và xóa dữ liệu cũ NGAY TẠI ĐÂY (An toàn trên EDT)
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableEmployeeManagement
                            .getModel();
                    model.setRowCount(0);

                    // Đổ dữ liệu mới vào bảng
                    for (var rec : employees) {
                        String empIdStr = String.format("EMP%03d", rec.id);
                        String statusStr = (rec.status == 1) ? "Active" : "Inactive";
                        model.addRow(new Object[] {
                                empIdStr, rec.fullName, rec.roleName, rec.phone, rec.barcode, statusStr, ""
                        });
                    }

                    // Thông báo cập nhật giao diện
                    tableEmployeeManagement.revalidate();
                    tableEmployeeManagement.repaint();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Nên hiển thị thêm một thông báo lỗi nhỏ cho người dùng thấy nếu cần
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

    // private void initEmployeeManagementFilterEvents() { //Không thực hiện việc
    // gọi hàm load liên tục dễ lỗi
    // cbRole.addActionListener(e -> loadEmployeeManagementTableData());
    //
    // txtSearchEmployee.getDocument().addDocumentListener(
    // onDocumentChange(this::loadEmployeeManagementTableData));
    //
    // }
    // Timer
    private javax.swing.Timer searchTimer = null;

    private void initEmployeeManagementFilterEvents() {
        // Với ComboBox thì không cần debounce vì người dùng chỉ click 1 lần
        cbRole.addActionListener(e -> loadEmployeeManagementTableData());

        // Với Textfield tìm kiếm: Áp dụng Debounce
        txtSearchEmployee.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search employee by name/code...");
        txtSearchEmployee.getDocument().addDocumentListener(
                onDocumentChange(() -> {
                    // Nếu có timer cũ đang chạy, hủy nó đi
                    if (searchTimer != null && searchTimer.isRunning()) {
                        searchTimer.stop();
                    }

                    // Tạo timer mới, đợi người dùng dừng gõ 300 mili-giây rồi mới chạy
                    searchTimer = new javax.swing.Timer(300, event -> {
                        loadEmployeeManagementTableData();
                    });
                    searchTimer.setRepeats(false); // Chỉ chạy 1 lần duy nhất khi hết hạn
                    searchTimer.start();
                }));
    }

    private void customCustomerTableAppearance() {
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] { "STT", "Customer ID", "Full Name", "Phone Number" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jTable1.setRowHeight(48);
        jTable1.setShowHorizontalLines(true);
        jTable1.setShowVerticalLines(false);

        jTable1.setSelectionBackground(new java.awt.Color(248, 246, 242));
        jTable1.setSelectionForeground(new java.awt.Color(15, 23, 42));
        jTable1.putClientProperty(FlatClientProperties.STYLE,
                "rowSelectionBackground: #F8F6F2; rowSelectionForeground: #0F172A; lineColor: #F1F5F9;");

        javax.swing.table.JTableHeader header = jTable1.getTableHeader();
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 38));
        header.setDefaultRenderer(new ui.StandardTableHeaderRenderer(javax.swing.SwingConstants.LEFT, 12));

        java.awt.Container parent = jTable1.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(jTable1, "No customers found!"));
            scrollPane.setViewportView(jTable1);
        }

        // Apply FlatLaf styling to the Customer Tab elements
        jTextField1.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,10,0,10;");
        jTextField1.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by phone number...");

        btnAddCustomer.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 8; borderWidth: 0; focusWidth: 0;");
        btnAddCustomer.setBackground(new java.awt.Color(227, 138, 69));
        btnAddCustomer.setForeground(java.awt.Color.WHITE);
        btnAddCustomer.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        jPanel7.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
    }

    // private javax.swing.Timer customerSearchTimer = null;

    private void initCustomerFilterEvents() {
        jTextField1.getDocument().addDocumentListener(
                onDocumentChange(() -> {
                    if (customerSearchTimer != null && customerSearchTimer.isRunning()) {
                        customerSearchTimer.stop();
                    }
                    customerSearchTimer = new javax.swing.Timer(300, event -> {
                        loadCustomerTableData();
                    });
                    customerSearchTimer.setRepeats(false);
                    customerSearchTimer.start();
                }));

        btnAddCustomer.addActionListener(e -> {
            AddCustomerFrame addFrame = new AddCustomerFrame(() -> {
                loadCustomerTableData();
                if (salesCounter != null) {
                    salesCounter.loadCustomerTableData();
                }
            });
            addFrame.setVisible(true);
        });
    }

    public void loadCustomerTableData() {
        String keyword = (jTextField1 != null) ? jTextField1.getText().trim() : "";

        new javax.swing.SwingWorker<java.util.List<entity.Customer>, Void>() {
            @Override
            protected java.util.List<entity.Customer> doInBackground() {
                return customerService.searchCustomers(keyword);
            }

            @Override
            protected void done() {
                try {
                    java.util.List<entity.Customer> customers = get();
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1
                            .getModel();
                    model.setRowCount(0);

                    int stt = 1;
                    for (var rec : customers) {
                        model.addRow(new Object[] {
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

    private void loadInventoryTableData() {

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

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

                        model.addRow(new Object[] {
                                String.format("PRD-%04d", row.id),
                                row.imagePath,
                                row.productName,
                                row.categoryName,
                                row.barcode,
                                String.format("%,.0f đ", row.price),
                                row.quantity,
                                ""
                        });
                    }

                    lblTotalItem.setText("(" + rows.size() + " items)");

                    inventorySorter = new javax.swing.table.TableRowSorter<>(model);

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
                new Object[][] {},
                new String[] { "ORDER ID", "DATE & TIME", "CASHIER", "CUSTOMER", "PAYMENT", "TOTAL", "STATUS",
                        "ACTION" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, true };

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
                this::onExportHistoryOrder,
                this::onViewHistoryOrder,
                this::onCancelHistoryOrder);
        tableTransactionHistory.getColumnModel().getColumn(7).setCellEditor(actionEditor);

        tableTransactionHistory.getColumnModel().getColumn(0).setPreferredWidth(135); // ORDER ID
        tableTransactionHistory.getColumnModel().getColumn(1).setPreferredWidth(155); // DATE & TIME
        tableTransactionHistory.getColumnModel().getColumn(2).setPreferredWidth(145); // CASHIER
        tableTransactionHistory.getColumnModel().getColumn(3).setPreferredWidth(175); // CUSTOMER
        tableTransactionHistory.getColumnModel().getColumn(4).setPreferredWidth(95); // PAYMENT
        tableTransactionHistory.getColumnModel().getColumn(5).setPreferredWidth(120); // TOTAL
        tableTransactionHistory.getColumnModel().getColumn(6).setPreferredWidth(110); // STATUS
        tableTransactionHistory.getColumnModel().getColumn(7).setPreferredWidth(320);
        tableTransactionHistory.getColumnModel().getColumn(7).setMinWidth(300);

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

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableTransactionHistory
                .getModel();
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
            model.addRow(new Object[] {
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

        // Cập nhật hiển thị số lượng bản ghi bằng rows.size() thay vì biến đếm
        // matchCount
        lblRecordLog.setText("(" + rows.size() + " records)");
    }

    private java.util.List<repository.OrderRepository.OrderHistoryRow> getFilteredOrderHistoryRows() {
        String selectedStatus = cbStatus.getSelectedItem() != null
                ? cbStatus.getSelectedItem().toString()
                : "All";
        java.util.Date startDate = dateFrom.getDate();
        java.util.Date endDate = dateTo.getDate();

        java.util.List<repository.OrderRepository.OrderHistoryRow> rows = orderRepository
                .findOrderHistory(selectedStatus, startDate, endDate);
        String paymentFilter = (cbPaymentMethod != null && cbPaymentMethod.getSelectedItem() != null)
                ? cbPaymentMethod.getSelectedItem().toString()
                : "All";
        String searchText = (txtSearchInvoice != null)
                ? txtSearchInvoice.getText().trim().toLowerCase()
                : "";

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
        if (normalized.contains("TRANSFER") || normalized.contains("CHUYỂN KHOẢN")
                || normalized.contains("CHUYEN KHOAN")) {
            return "TRANSFER";
        }
        if (normalized.contains("CARD") || normalized.contains("THẺ") || normalized.contains("THE")) {
            return "CARD";
        }
        if (normalized.contains("E-WALLET") || normalized.contains("EWALLET") || normalized.contains("VÍ ĐIỆN TỬ")
                || normalized.contains("VI DIEN TU")) {
            return "E-WALLET";
        }
        return normalized;
    }

    private int extractHistoryOrderId(int rowIndex) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableTransactionHistory
                .getModel();
        String orderCode = String.valueOf(model.getValueAt(rowIndex, 0));
        int lastHyphen = orderCode.lastIndexOf('-');
        String numericPart = lastHyphen >= 0 ? orderCode.substring(lastHyphen + 1) : orderCode;
        return Integer.parseInt(numericPart);
    }

    private void onExportHistoryOrder(int rowIndex) {
        int orderId = extractHistoryOrderId(rowIndex);
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn có muốn xuất hóa đơn dạng PDF cho đơn hàng này không?",
                "Xuất hóa đơn PDF",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

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
                        javax.swing.JOptionPane.showMessageDialog(DashBoardFrame.this,
                                "Xuất hóa đơn PDF thành công tại:\n" + targetFile.getAbsolutePath(),
                                "Thành công",
                                javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(DashBoardFrame.this,
                                "Có lỗi xảy ra khi xuất file PDF:\n" + e.getMessage(),
                                "Lỗi",
                                javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private void onViewHistoryOrder(int rowIndex) {
        int orderId = extractHistoryOrderId(rowIndex);
        ViewOrderDetailFrame detailFrame = new ViewOrderDetailFrame(orderId);
        detailFrame.setLocationRelativeTo(this);
        detailFrame.setVisible(true);
    }

    private void onCancelHistoryOrder(int rowIndex) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableTransactionHistory
                .getModel();

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

        int updatedRows = orderRepository
                .cancelOrders(java.util.Collections.singletonList(extractHistoryOrderId(rowIndex)));
        if (updatedRows > 0) {
            loadOverviewCardsData();
            refreshOrderHistory();
            initPaymentMethodFilter();
            loadInventoryTableData();
            loadProductGrid();
            if (salesCounter != null) {
                salesCounter.refreshTransactionHistory();
                salesCounter.loadProductGrid();
            }
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
                        javax.swing.RowFilter
                                .regexFilter("(?i)^" + java.util.regex.Pattern.quote(selectedCategory) + "$", 3));
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
        txtSearchProduct.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search products...");
        txtSearchProduct.putClientProperty(FlatClientProperties.STYLE, "arc: 12; margin: 0,10,0,10;");

        btnAddProduct.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 12; borderWidth: 0; focusWidth: 0; font: bold;");

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {},
                new String[] { "PRODUCT ID", "IMAGE", "PRODUCT NAME", "CATEGORY", "BARCODE", "PRICE", "STOCK",
                        "ACTION" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, true, true };

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

        tableProduct.getColumnModel().getColumn(0).setPreferredWidth(70); // PRODUCT ID
        tableProduct.getColumnModel().getColumn(1).setPreferredWidth(70); // IMAGE
        tableProduct.getColumnModel().getColumn(1).setMaxWidth(70);
        tableProduct.getColumnModel().getColumn(2).setPreferredWidth(200); // PRODUCT NAME
        tableProduct.getColumnModel().getColumn(3).setPreferredWidth(100); // CATEGORY
        tableProduct.getColumnModel().getColumn(4).setPreferredWidth(100); // BARCODE
        tableProduct.getColumnModel().getColumn(5).setPreferredWidth(90); // PRICE
        tableProduct.getColumnModel().getColumn(6).setPreferredWidth(50); // STOCK

        ui.ProductActionCellRenderer actionRenderer = new ui.ProductActionCellRenderer();
        ui.ProductActionCellEditor actionEditor = new ui.ProductActionCellEditor(
                this::onEditProduct,
                this::onDeleteProduct,
                this::onShowBarcodeProduct);
        tableProduct.getColumnModel().getColumn(6).setCellEditor(new StockSpinnerCellEditor(productRepository));
        tableProduct.getColumnModel().getColumn(7).setCellRenderer(actionRenderer);
        tableProduct.getColumnModel().getColumn(7).setCellEditor(actionEditor);
        tableProduct.getColumnModel().getColumn(7).setPreferredWidth(210);
        tableProduct.getColumnModel().getColumn(7).setMaxWidth(230);

        java.awt.Container parent = tableProduct.getParent();
        if (parent instanceof javax.swing.JViewport viewport) {
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) viewport.getParent();
            scrollPane.setViewport(new ui.EmptyTableViewport(tableProduct, "Nothing found!"));
            scrollPane.setViewportView(tableProduct);
        }
    }

    private void onEditProduct(int rowIndex) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

        String productIdStr = model.getValueAt(rowIndex, 0).toString();
        int productId = Integer.parseInt(productIdStr.replace("PRD-", ""));

        entity.Product product = productRepository.findById(productId);
        if (product == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Không tìm thấy sản phẩm!", "Lỗi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        views.AddProductFrame editFrame = new views.AddProductFrame(product, () -> {
            loadInventoryTableData();
            if (salesCounter != null) {
                salesCounter.loadProductGrid();
            }
        });
        editFrame.setVisible(true);
    }

    private void onDeleteProduct(int rowIndex) {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableProduct.getModel();

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
                if (salesCounter != null) {
                    salesCounter.loadProductGrid();
                }
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

    private static class StockSpinnerCellEditor extends javax.swing.AbstractCellEditor
            implements javax.swing.table.TableCellEditor {
        private final javax.swing.JSpinner spinner = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        private final repository.ProductRepository productRepository;
        private javax.swing.JTable currentTable;
        private int currentRow = -1;
        private int originalValue;

        private StockSpinnerCellEditor(repository.ProductRepository productRepository) {
            this.productRepository = productRepository;
            spinner.setOpaque(true);
            spinner.setEditor(new javax.swing.JSpinner.NumberEditor(spinner, "#"));
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentTable = table;
            currentRow = row;
            originalValue = value instanceof Number ? ((Number) value).intValue() : 0;
            spinner.setValue(originalValue);
            return spinner;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public boolean stopCellEditing() {
            try {
                int newQuantity = ((Number) spinner.getValue()).intValue();
                if (newQuantity < 0) {
                    throw new NumberFormatException("Quantity cannot be negative");
                }

                int productId = Integer.parseInt(
                        currentTable.getValueAt(currentRow, 0).toString().replace("PRD-", ""));
                if (productRepository.updateQuantity(productId, newQuantity)) {
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) currentTable
                            .getModel();
                    model.setValueAt(newQuantity, currentRow, 6);
                    return super.stopCellEditing();
                }

                javax.swing.JOptionPane.showMessageDialog(
                        currentTable,
                        "Không thể cập nhật số lượng!",
                        "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                spinner.setValue(originalValue);
                return false;
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(
                        currentTable,
                        "Không thể cập nhật số lượng!",
                        "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                spinner.setValue(originalValue);
                return false;
            }
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
                new Object[][] {},
                new String[] { "Employee ID", "Name", "Role", "Date", "Clock-In", "Clock-Out", "Working Hours",
                        "Status" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false };

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

    public void loadAttendanceTableData() {
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableAttendance.getModel();
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

                    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");

                    int onTimeCount = 0, lateCount = 0, absentCount = 0, onLeaveCount = 0;

                    for (var row : rows) {
                        String status = (row.finalStatus != null) ? row.finalStatus.toUpperCase() : "ABSENT";
                        switch (status) {
                            case "ON TIME" -> onTimeCount++;
                            case "LATE" -> lateCount++;
                            case "ON LEAVE" -> onLeaveCount++;
                            default -> absentCount++; // ABSENT + trường hợp không xác định
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

                        model.addRow(new Object[] {
                                empIdStr, row.fullName, row.displayRole,
                                dateStr, clockInStr, clockOutStr, workingHoursStr, status
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
        cbStatus.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "PAID", "PENDING", "CANCELLED" }));
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
        txtSearchInvoice.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Search Order ID, customer, cashier...");
        txtSearchInvoice.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE,
                "arc: 12; margin: 0,10,0,10;");
        txtSearchInvoice.getDocument().addDocumentListener(onDocumentChange(this::refreshOrderHistory));
    }

    private void refreshOrderHistory() {
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
        if (cbMonth != null && cbYear != null && cbMonth.getSelectedItem() != null
                && cbYear.getSelectedItem() != null) {
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
        javax.swing.JButton[] tabs = { btnAttendance, btnEmployeeManagement, btnSchedule };
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

    public void loadOverviewCardsData() {
        repository.OrderRepository.DailyOrderStats stats = orderRepository.getDailyOrderStats();
        int presentEmployees = attendanceRepository.countTodayPresentEmployees();

        lblRevenueToday.setText(String.format("%,.0f đ", orderRepository.getDailyRevenue()));
        lblRevenueMonth.setText(String.format("%,.0f đ", orderRepository.getMonthlyRevenue()));
        lblActiveEmployees.setText(String.valueOf(presentEmployees));

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
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
        btnCustomerManagement = new javax.swing.JButton();
        btnPOSPayment = new javax.swing.JButton();
        panelContent = new javax.swing.JPanel();
        panelDashboard = new javax.swing.JPanel();
        panelDashboardHeader = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cbbLogOut = new javax.swing.JComboBox<>();
        panelRevenueToday = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        lblRevenueToday = new javax.swing.JLabel();
        panelActiveOrders = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        lblPendingInvoice = new javax.swing.JLabel();
        panelCanceledOrders = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        lblRevenueMonth = new javax.swing.JLabel();
        panelActiveEmployees = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        lblActiveEmployees = new javax.swing.JLabel();
        panelRevenueDate = new javax.swing.JPanel();
        panelTopSales = new javax.swing.JPanel();
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
        jPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
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
        btnAddEmployee = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableEmployeeManagement = new javax.swing.JTable();
        findByBarcode = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
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
        btnAddSchedule = new javax.swing.JButton();
        panelCustomer = new javax.swing.JPanel();
        btnAddCustomer = new javax.swing.JButton();
        panelCustomerHeader = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
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
        jLabel19 = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        panelOrderSummary = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        panelCardParent = new javax.swing.JPanel();
        panelCashView = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        txtCashReceived = new javax.swing.JTextField();
        panelChangeDue = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        lbChangeDue = new javax.swing.JLabel();
        panelQRView = new javax.swing.JPanel();
        lbQRCode = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        lbSubtotal = new javax.swing.JLabel();
        cashBtn = new javax.swing.JButton();
        qrBtn = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnConfirmOrder = new javax.swing.JButton();
        btnCancelOrder = new javax.swing.JButton();
        panelEmployeeCheckIn = new javax.swing.JPanel();
        cbCategory = new javax.swing.JComboBox<>();
        btnAddCustomer1 = new javax.swing.JButton();
        txtSearchCustomer = new javax.swing.JTextField();

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
                                .addComponent(lbAvatarShop, javax.swing.GroupLayout.PREFERRED_SIZE, 67,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel3))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelLogoLayout.setVerticalGroup(
                panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelLogoLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(panelLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelLogoLayout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel3))
                                        .addComponent(lbAvatarShop, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)));

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

        btnCustomerManagement.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCustomerManagement.setText("Customer");
        btnCustomerManagement.setBorder(null);
        btnCustomerManagement.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCustomerManagement.addActionListener(this::btnCustomerManagementActionPerformed);

        btnPOSPayment.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnPOSPayment.setText("Payment");
        btnPOSPayment.setBorder(null);
        btnPOSPayment.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPOSPayment.addActionListener(this::btnPOSPaymentActionPerformed);

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
                panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMenuLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(panelLogo, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(panelMenuLayout.createSequentialGroup()
                                                .addGroup(panelMenuLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(btnPOSPayment,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnCustomerManagement,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnHumanResources,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnOrderHistory,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnProductInventory,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(btnDashBoard,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 212,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        panelMenuLayout.setVerticalGroup(
                panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMenuLayout.createSequentialGroup()
                                .addComponent(panelLogo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnProductInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnOrderHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnHumanResources, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnCustomerManagement, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnPOSPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 47,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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

        cbbLogOut.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout panelDashboardHeaderLayout = new javax.swing.GroupLayout(panelDashboardHeader);
        panelDashboardHeader.setLayout(panelDashboardHeaderLayout);
        panelDashboardHeaderLayout.setHorizontalGroup(
                panelDashboardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addGroup(panelDashboardHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                                                .addComponent(jLabel8)
                                                .addContainerGap(726, Short.MAX_VALUE))
                                        .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(cbbLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 228,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(57, 57, 57)))));
        panelDashboardHeaderLayout.setVerticalGroup(
                panelDashboardHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelDashboardHeaderLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(panelDashboardHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(cbbLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8)
                                .addContainerGap(20, Short.MAX_VALUE)));

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
                                .addGroup(panelRevenueTodayLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel15)
                                        .addComponent(lblRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE, 192,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(14, Short.MAX_VALUE)));
        panelRevenueTodayLayout.setVerticalGroup(
                panelRevenueTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelRevenueTodayLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
                                .addGroup(panelActiveOrdersLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblPendingInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 192,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel17))
                                .addContainerGap(12, Short.MAX_VALUE)));
        panelActiveOrdersLayout.setVerticalGroup(
                panelActiveOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelActiveOrdersLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblPendingInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
                                .addGroup(panelCanceledOrdersLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblRevenueMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 192,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel18))
                                .addContainerGap(12, Short.MAX_VALUE)));
        panelCanceledOrdersLayout.setVerticalGroup(
                panelCanceledOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCanceledOrdersLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblRevenueMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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
                                .addGroup(panelActiveEmployeesLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblActiveEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, 192,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel16))
                                .addContainerGap(13, Short.MAX_VALUE)));
        panelActiveEmployeesLayout.setVerticalGroup(
                panelActiveEmployeesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelActiveEmployeesLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblActiveEmployees, javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        panelRevenueDate.setBackground(new java.awt.Color(255, 255, 255));
        panelRevenueDate.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelRevenueDateLayout = new javax.swing.GroupLayout(panelRevenueDate);
        panelRevenueDate.setLayout(panelRevenueDateLayout);
        panelRevenueDateLayout.setHorizontalGroup(
                panelRevenueDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 938, Short.MAX_VALUE));
        panelRevenueDateLayout.setVerticalGroup(
                panelRevenueDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 220, Short.MAX_VALUE));

        panelTopSales.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelTopSalesLayout = new javax.swing.GroupLayout(panelTopSales);
        panelTopSales.setLayout(panelTopSalesLayout);
        panelTopSalesLayout.setHorizontalGroup(
                panelTopSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 938, Short.MAX_VALUE));
        panelTopSalesLayout.setVerticalGroup(
                panelTopSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 226, Short.MAX_VALUE));

        javax.swing.GroupLayout panelDashboardLayout = new javax.swing.GroupLayout(panelDashboard);
        panelDashboard.setLayout(panelDashboardLayout);
        panelDashboardLayout.setHorizontalGroup(
                panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelDashboardHeader, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDashboardLayout
                                .createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(panelDashboardLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(panelRevenueDate, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(panelDashboardLayout.createSequentialGroup()
                                                .addComponent(panelRevenueToday, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(panelCanceledOrders,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(panelActiveOrders, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22,
                                                        Short.MAX_VALUE)
                                                .addComponent(panelActiveEmployees,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(panelTopSales, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(24, 24, 24)));
        panelDashboardLayout.setVerticalGroup(
                panelDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelDashboardLayout.createSequentialGroup()
                                .addComponent(panelDashboardHeader, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addGroup(panelDashboardLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(panelCanceledOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 75,
                                                Short.MAX_VALUE)
                                        .addComponent(panelRevenueToday, javax.swing.GroupLayout.DEFAULT_SIZE, 75,
                                                Short.MAX_VALUE)
                                        .addComponent(panelActiveOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 75,
                                                Short.MAX_VALUE)
                                        .addComponent(panelActiveEmployees, javax.swing.GroupLayout.DEFAULT_SIZE, 75,
                                                Short.MAX_VALUE))
                                .addGap(33, 33, 33)
                                .addComponent(panelRevenueDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelTopSales, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(20, Short.MAX_VALUE)));

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
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap(26, Short.MAX_VALUE)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10)
                                .addGap(16, 16, 16)));

        panelOrderManagement1.setBackground(new java.awt.Color(247, 246, 242));

        tableProduct.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
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
                                .addComponent(lblTotalItem, javax.swing.GroupLayout.PREFERRED_SIZE, 80,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jScrollPane2));
        panelOrderManagement1Layout.setVerticalGroup(
                panelOrderManagement1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                panelOrderManagement1Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(panelOrderManagement1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel20)
                                                .addComponent(lblTotalItem))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8,
                                                Short.MAX_VALUE)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 455,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap()));

        cbAll.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbAll.setForeground(new java.awt.Color(102, 102, 102));
        cbAll.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAddProduct.setText("+ Add Product");
        btnAddProduct.addActionListener(this::btnAddProductActionPerformed);

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
                panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelProductLayout.createSequentialGroup()
                                .addContainerGap(25, Short.MAX_VALUE)
                                .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 305,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(81, 81, 81)
                                .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 106,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnAddProduct)
                                .addGap(346, 346, 346))
                        .addGroup(panelProductLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(panelOrderManagement1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap()));
        panelProductLayout.setVerticalGroup(
                panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelProductLayout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12,
                                        Short.MAX_VALUE)
                                .addGroup(panelProductLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbAll, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnAddProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(42, 42, 42)
                                .addComponent(panelOrderManagement1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)));

        panelContent.add(panelProduct, "CardProduct");

        panelOrder.setBackground(new java.awt.Color(244, 246, 248));

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
                                .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 128,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21,
                                        Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 18,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, 129,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)));
        panelDateLayout.setVerticalGroup(
                panelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelDateLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(panelDateLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap()));

        cbStatus.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbStatus.setForeground(new java.awt.Color(102, 102, 102));
        cbStatus.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbStatus.setPreferredSize(new java.awt.Dimension(74, 38));

        lblPaidOrder1.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder1.setText("From");

        lblPaidOrder2.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder2.setText("To");

        lblPaidOrder3.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder3.setText("Status");

        cbPaymentMethod.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbPaymentMethod.setForeground(new java.awt.Color(102, 102, 102));
        cbPaymentMethod.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPaymentMethod.setPreferredSize(new java.awt.Dimension(74, 38));

        lblPaidOrder4.setForeground(new java.awt.Color(102, 102, 102));
        lblPaidOrder4.setText("Payment");

        txtSearchInvoice.addActionListener(this::txtSearchInvoiceActionPerformed);

        javax.swing.GroupLayout panelBelowHeaderLayout = new javax.swing.GroupLayout(panelBelowHeader);
        panelBelowHeader.setLayout(panelBelowHeaderLayout);
        panelBelowHeaderLayout.setHorizontalGroup(
                panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                                .addGroup(panelBelowHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(panelDate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                                                .addGap(36, 36, 36)
                                                .addComponent(lblPaidOrder1)
                                                .addGap(151, 151, 151)
                                                .addComponent(lblPaidOrder2)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelBelowHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblPaidOrder3)
                                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 113,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelBelowHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                                                .addComponent(cbPaymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        301, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(lblPaidOrder4))
                                .addContainerGap(15, Short.MAX_VALUE)));
        panelBelowHeaderLayout.setVerticalGroup(
                panelBelowHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBelowHeaderLayout.createSequentialGroup()
                                .addContainerGap(7, Short.MAX_VALUE)
                                .addGroup(panelBelowHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblPaidOrder1, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblPaidOrder2, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                panelBelowHeaderLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblPaidOrder3)
                                                        .addComponent(lblPaidOrder4)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelBelowHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(panelDate, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cbStatus, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(panelBelowHeaderLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(cbPaymentMethod, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(14, 14, 14)));

        panelOrderManagement2.setBackground(new java.awt.Color(247, 246, 242));

        tableTransactionHistory.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
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
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 937, Short.MAX_VALUE));
        panelOrderManagement2Layout.setVerticalGroup(
                panelOrderManagement2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                panelOrderManagement2Layout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addGroup(panelOrderManagement2Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel27)
                                                .addComponent(lblRecordLog))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 437,
                                                Short.MAX_VALUE)));

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
                                .addContainerGap(735, Short.MAX_VALUE)));
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap(26, Short.MAX_VALUE)
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel12)
                                .addGap(16, 16, 16)));

        javax.swing.GroupLayout panelOrderLayout = new javax.swing.GroupLayout(panelOrder);
        panelOrder.setLayout(panelOrderLayout);
        panelOrderLayout.setHorizontalGroup(
                panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelOrderLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(panelBelowHeader, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelOrderLayout.createSequentialGroup()
                                        .addGap(20, 20, 20)
                                        .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(26, Short.MAX_VALUE))));
        panelOrderLayout.setVerticalGroup(
                panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelOrderLayout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(panelBelowHeader, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(524, Short.MAX_VALUE))
                        .addGroup(panelOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOrderLayout
                                        .createSequentialGroup()
                                        .addContainerGap(211, Short.MAX_VALUE)
                                        .addComponent(panelOrderManagement2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(17, 17, 17))));

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
                                .addComponent(btnAttendance, javax.swing.GroupLayout.PREFERRED_SIZE, 131,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEmployeeManagement, javax.swing.GroupLayout.PREFERRED_SIZE, 178,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 131,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(501, Short.MAX_VALUE)));
        panelSelectionLayout.setVerticalGroup(
                panelSelectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelSelectionLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelSelectionLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(btnAttendance, javax.swing.GroupLayout.DEFAULT_SIZE, 43,
                                                Short.MAX_VALUE)
                                        .addComponent(btnSchedule, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnEmployeeManagement, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));

        javax.swing.GroupLayout panelHRHeaderLayout = new javax.swing.GroupLayout(panelHRHeader);
        panelHRHeader.setLayout(panelHRHeaderLayout);
        panelHRHeaderLayout.setHorizontalGroup(
                panelHRHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelHRHeaderLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(panelHRHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel13)
                                        .addComponent(jLabel14))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(panelSelection, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE));
        panelHRHeaderLayout.setVerticalGroup(
                panelHRHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelHRHeaderLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panelSelection, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));

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
        cbMonth.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMonth.addActionListener(this::cbMonthActionPerformed);

        cbYear.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        cbYear.setForeground(new java.awt.Color(102, 102, 102));
        cbYear.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbYear.addActionListener(this::cbYearActionPerformed);

        panelOrderManagement4.setBackground(new java.awt.Color(247, 246, 242));

        tableAttendance.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane5.setViewportView(tableAttendance);

        javax.swing.GroupLayout panelOrderManagement4Layout = new javax.swing.GroupLayout(panelOrderManagement4);
        panelOrderManagement4.setLayout(panelOrderManagement4Layout);
        panelOrderManagement4Layout.setHorizontalGroup(
                panelOrderManagement4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 942, Short.MAX_VALUE));
        panelOrderManagement4Layout.setVerticalGroup(
                panelOrderManagement4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE));

        javax.swing.GroupLayout panelAttendanceLayout = new javax.swing.GroupLayout(panelAttendance);
        panelAttendance.setLayout(panelAttendanceLayout);
        panelAttendanceLayout.setHorizontalGroup(
                panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelAttendanceLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(panelAttendanceLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(panelOrderManagement4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(panelAttendanceLayout.createSequentialGroup()
                                                .addGroup(panelAttendanceLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(lblAttendance,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 334,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(panelAttendanceLayout.createSequentialGroup()
                                                                .addComponent(lblRecordAttendance)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblOnTimeCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 70,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblLateCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 46,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblAbsentCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 63,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblOnLeaveCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(317, 317, 317)
                                                .addComponent(cbMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 117,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbYear, javax.swing.GroupLayout.PREFERRED_SIZE, 117,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(21, Short.MAX_VALUE)));
        panelAttendanceLayout.setVerticalGroup(
                panelAttendanceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelAttendanceLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelAttendanceLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblAttendance)
                                        .addGroup(panelAttendanceLayout.createSequentialGroup()
                                                .addGap(25, 25, 25)
                                                .addGroup(panelAttendanceLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(panelAttendanceLayout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE,
                                                                        false)
                                                                .addComponent(lblRecordAttendance,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(lblOnTimeCount,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(lblLateCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblAbsentCount,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 27,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(lblOnLeaveCount))
                                                        .addGroup(panelAttendanceLayout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(cbMonth,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(cbYear,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panelOrderManagement4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(21, Short.MAX_VALUE)));

        panelEmployeeMain.add(panelAttendance, "cardAttendance");

        panelEmployeeManagement.setBackground(new java.awt.Color(244, 246, 248));

        txtSearchEmployee.addActionListener(this::txtSearchEmployeeActionPerformed);

        cbRole.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAddEmployee.setBackground(new java.awt.Color(227, 138, 69));
        btnAddEmployee.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnAddEmployee.setForeground(new java.awt.Color(255, 255, 255));
        btnAddEmployee.setText("+ Add Employee");
        btnAddEmployee.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddEmployeeMouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(247, 246, 242));

        tableEmployeeManagement.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane4.setViewportView(tableEmployeeManagement);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane4));
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE));

        findByBarcode.setBackground(new java.awt.Color(227, 138, 69));
        findByBarcode.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        findByBarcode.setForeground(new java.awt.Color(255, 255, 255));
        findByBarcode.setText("Find By Barcode");
        findByBarcode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                findByBarcodeMouseClicked(evt);
            }
        });

        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRefresh.setText("↻");
        btnRefresh.addActionListener(this::btnRefreshActionPerformed);

        javax.swing.GroupLayout panelEmployeeManagementLayout = new javax.swing.GroupLayout(panelEmployeeManagement);
        panelEmployeeManagement.setLayout(panelEmployeeManagementLayout);
        panelEmployeeManagementLayout.setHorizontalGroup(
                panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEmployeeManagementLayout
                                .createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelEmployeeManagementLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(panelEmployeeManagementLayout.createSequentialGroup()
                                                .addComponent(txtSearchEmployee, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        317, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 135,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnRefresh)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(findByBarcode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnAddEmployee, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(25, 25, 25)));
        panelEmployeeManagementLayout.setVerticalGroup(
                panelEmployeeManagementLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelEmployeeManagementLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(panelEmployeeManagementLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtSearchEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnAddEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(findByBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 37,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnRefresh))
                                .addGap(27, 27, 27)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(7, Short.MAX_VALUE)));

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
                                .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblWeekRange, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 38,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnPrevWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnNextWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblWeekRange, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)));

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
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane6.setViewportView(tableSchedule);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, 957, Short.MAX_VALUE));
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE));

        btnAddSchedule.setBackground(new java.awt.Color(227, 138, 69));
        btnAddSchedule.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnAddSchedule.setForeground(new java.awt.Color(255, 255, 255));
        btnAddSchedule.setText("+ Make schedule");
        btnAddSchedule.addActionListener(this::btnAddScheduleActionPerformed);

        javax.swing.GroupLayout panelScheduleLayout = new javax.swing.GroupLayout(panelSchedule);
        panelSchedule.setLayout(panelScheduleLayout);
        panelScheduleLayout.setHorizontalGroup(
                panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelScheduleLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                                .addGroup(panelScheduleLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel4)
                                                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                                                .addComponent(lblMonthDayYear)
                                                                .addGap(2, 2, 2)
                                                                .addComponent(jLabel6,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 22,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(lblEmployeeSchedule)))
                                                .addGap(308, 308, 308)
                                                .addComponent(btnAddSchedule, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(28, 28, 28)
                                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelScheduleLayout.setVerticalGroup(
                panelScheduleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                .addGroup(panelScheduleLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 27,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(panelScheduleLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(lblMonthDayYear)
                                                        .addComponent(lblEmployeeSchedule)
                                                        .addComponent(jLabel6)))
                                        .addGroup(panelScheduleLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(panelScheduleLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnAddSchedule,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE))))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(18, Short.MAX_VALUE)));

        panelEmployeeMain.add(panelSchedule, "cardSchedule");

        javax.swing.GroupLayout panelHRMainLayout = new javax.swing.GroupLayout(panelHRMain);
        panelHRMain.setLayout(panelHRMainLayout);
        panelHRMainLayout.setHorizontalGroup(
                panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelHRHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 983, Short.MAX_VALUE)
                        .addGroup(panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(panelEmployeeMain, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelHRMainLayout.setVerticalGroup(
                panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelHRMainLayout.createSequentialGroup()
                                .addComponent(panelHRHeader, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 598, Short.MAX_VALUE))
                        .addGroup(panelHRMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                        panelHRMainLayout.createSequentialGroup()
                                                .addGap(0, 131, Short.MAX_VALUE)
                                                .addComponent(panelEmployeeMain, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        592, javax.swing.GroupLayout.PREFERRED_SIZE))));

        panelContent.add(panelHRMain, "CardAttendance");

        panelCustomer.setBackground(new java.awt.Color(244, 246, 248));

        btnAddCustomer.setText("Add Customer");
        btnAddCustomer.addActionListener(this::btnAddCustomerActionPerformed);

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
                                .addGroup(panelCustomerHeaderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel25)
                                        .addComponent(jLabel24))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelCustomerHeaderLayout.setVerticalGroup(
                panelCustomerHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCustomerHeaderLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel25)
                                .addContainerGap(24, Short.MAX_VALUE)));

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setForeground(new java.awt.Color(255, 255, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null },
                        { null, null, null, null }
                },
                new String[] {
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }));
        jScrollPane7.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 932,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 510,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Phone");

        javax.swing.GroupLayout panelCustomerLayout = new javax.swing.GroupLayout(panelCustomer);
        panelCustomer.setLayout(panelCustomerLayout);
        panelCustomerLayout.setHorizontalGroup(
                panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelCustomerHeader, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelCustomerLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelCustomerLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(panelCustomerLayout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addGap(18, 18, 18)
                                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 264,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(492, 492, 492)
                                                .addComponent(btnAddCustomer))
                                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelCustomerLayout.setVerticalGroup(
                panelCustomerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCustomerLayout.createSequentialGroup()
                                .addComponent(panelCustomerHeader, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(51, 51, 51)
                                .addGroup(panelCustomerLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnAddCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 36,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 36,
                                                Short.MAX_VALUE)
                                        .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 487,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 30, Short.MAX_VALUE)));

        panelContent.add(panelCustomer, "CardCustomer");

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
                                .addComponent(btnBarcode, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 547,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelBarcodeLayout.setVerticalGroup(
                panelBarcodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBarcodeLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(panelBarcodeLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtBarcodeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(16, Short.MAX_VALUE)));

        panelOrderSplit.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelOrderSplitLayout = new javax.swing.GroupLayout(panelOrderSplit);
        panelOrderSplit.setLayout(panelOrderSplitLayout);
        panelOrderSplitLayout.setHorizontalGroup(
                panelOrderSplitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                panelOrderSplitLayout.createSequentialGroup()
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnAddOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 29,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap()));
        panelOrderSplitLayout.setVerticalGroup(
                panelOrderSplitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelOrderSplitLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(btnAddOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                                .addContainerGap()));

        panelCurrentOrder.setBackground(new java.awt.Color(248, 246, 242));
        panelCurrentOrder.setForeground(new java.awt.Color(248, 246, 242));

        lblCurrentOrder.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblCurrentOrder.setForeground(new java.awt.Color(110, 58, 25));
        lblCurrentOrder.setText("Current Order");

        tableCurrentOrder.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        { null },
                        { null },
                        { null },
                        { null }
                },
                new String[] {
                        "Title 1"
                }));
        tableCurrentOrder.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(tableCurrentOrder);

        javax.swing.GroupLayout panelCurrentOrderLayout = new javax.swing.GroupLayout(panelCurrentOrder);
        panelCurrentOrder.setLayout(panelCurrentOrderLayout);
        panelCurrentOrderLayout.setHorizontalGroup(
                panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelCurrentOrderLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1)
                                        .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                                                .addComponent(lblCurrentOrder)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        panelCurrentOrderLayout.setVerticalGroup(
                panelCurrentOrderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCurrentOrderLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblCurrentOrder)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)));

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
                                .addGroup(panelPopularLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(scrollPopular, javax.swing.GroupLayout.DEFAULT_SIZE, 606,
                                                Short.MAX_VALUE)
                                        .addGroup(panelPopularLayout.createSequentialGroup()
                                                .addComponent(lblPopular)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap()));
        panelPopularLayout.setVerticalGroup(
                panelPopularLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelPopularLayout.createSequentialGroup()
                                .addComponent(lblPopular)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPopular)
                                .addContainerGap()));

        panelCashier.setBackground(new java.awt.Color(255, 255, 255));

        btnScan.setPreferredSize(new java.awt.Dimension(35, 35));
        btnScan.addActionListener(this::btnScanActionPerformed);

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(110, 58, 25));
        jLabel19.setText("STAFF CHECK-IN");

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
                                .addGroup(panelCashierLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel19)
                                        .addComponent(labelStatus))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)));
        panelCashierLayout.setVerticalGroup(
                panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCashierLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(panelCashierLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(panelCashierLayout.createSequentialGroup()
                                                .addComponent(jLabel19)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(labelStatus)))
                                .addContainerGap(15, Short.MAX_VALUE)));

        panelOrderSummary.setBackground(new java.awt.Color(255, 255, 255));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(110, 58, 25));
        jLabel21.setText("Order Summary");

        panelCardParent.setPreferredSize(new java.awt.Dimension(300, 400));
        panelCardParent.setLayout(new java.awt.CardLayout());

        panelCashView.setBackground(new java.awt.Color(255, 255, 255));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(102, 102, 102));
        jLabel22.setText("Cash Received");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(102, 102, 102));
        jLabel23.setText("$");

        txtCashReceived.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        panelChangeDue.setBackground(new java.awt.Color(248, 246, 242));

        jLabel26.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel26.setText("Change Due");

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
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30,
                                        Short.MAX_VALUE)
                                .addComponent(lbChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE, 159,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()));
        panelChangeDueLayout.setVerticalGroup(
                panelChangeDueLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                panelChangeDueLayout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(panelChangeDueLayout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(lbChangeDue, javax.swing.GroupLayout.DEFAULT_SIZE, 31,
                                                        Short.MAX_VALUE))
                                        .addContainerGap()));

        javax.swing.GroupLayout panelCashViewLayout = new javax.swing.GroupLayout(panelCashView);
        panelCashView.setLayout(panelCashViewLayout);
        panelCashViewLayout.setHorizontalGroup(
                panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCashViewLayout.createSequentialGroup()
                                .addGroup(panelCashViewLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelCashViewLayout.createSequentialGroup()
                                                .addGap(27, 27, 27)
                                                .addComponent(jLabel22))
                                        .addGroup(panelCashViewLayout.createSequentialGroup()
                                                .addGap(26, 26, 26)
                                                .addComponent(jLabel23)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        249, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panelCashViewLayout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelCashViewLayout.setVerticalGroup(
                panelCashViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCashViewLayout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelCashViewLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtCashReceived, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 62,
                                        Short.MAX_VALUE)
                                .addComponent(panelChangeDue, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)));

        panelCardParent.add(panelCashView, "panelCashView");

        panelQRView.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelQRViewLayout = new javax.swing.GroupLayout(panelQRView);
        panelQRView.setLayout(panelQRViewLayout);
        panelQRViewLayout.setHorizontalGroup(
                panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelQRViewLayout.createSequentialGroup()
                                .addContainerGap(103, Short.MAX_VALUE)
                                .addComponent(lbQRCode, javax.swing.GroupLayout.PREFERRED_SIZE, 210,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)));
        panelQRViewLayout.setVerticalGroup(
                panelQRViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lbQRCode, javax.swing.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE));

        panelCardParent.add(panelQRView, "panelQRView");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(102, 102, 102));
        jLabel28.setText("Subtotal");

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

        jLabel29.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(102, 102, 102));
        jLabel29.setText("Payment Method");

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
                        .addComponent(panelCardParent, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelOrderSummaryLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel21)
                                        .addComponent(jLabel28)
                                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addGroup(panelOrderSummaryLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                                                .addComponent(jLabel29)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                                                .addComponent(cashBtn,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 114,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        Short.MAX_VALUE)
                                                                .addComponent(qrBtn,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 118,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addComponent(jSeparator1)
                                                        .addComponent(lbSubtotal, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                Short.MAX_VALUE))))
                                .addGap(15, 15, 15))
                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(btnConfirmOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 130,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnCancelOrder, javax.swing.GroupLayout.PREFERRED_SIZE, 130,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        panelOrderSummaryLayout.setVerticalGroup(
                panelOrderSummaryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelOrderSummaryLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lbSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelOrderSummaryLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cashBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(qrBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panelCardParent, javax.swing.GroupLayout.PREFERRED_SIZE, 217,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelOrderSummaryLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnConfirmOrder)
                                        .addComponent(btnCancelOrder))
                                .addGap(0, 18, Short.MAX_VALUE)));

        panelEmployeeCheckIn.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelEmployeeCheckInLayout = new javax.swing.GroupLayout(panelEmployeeCheckIn);
        panelEmployeeCheckIn.setLayout(panelEmployeeCheckInLayout);
        panelEmployeeCheckInLayout.setHorizontalGroup(
                panelEmployeeCheckInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));
        panelEmployeeCheckInLayout.setVerticalGroup(
                panelEmployeeCheckInLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE));

        cbCategory.setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnAddCustomer1.setText("Add customer");

        txtSearchCustomer.addActionListener(this::txtSearchCustomerActionPerformed);

        javax.swing.GroupLayout panelSaleCounterLayout = new javax.swing.GroupLayout(panelSaleCounter);
        panelSaleCounter.setLayout(panelSaleCounterLayout);
        panelSaleCounterLayout.setHorizontalGroup(
                panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelSaleCounterLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                                .addGroup(panelSaleCounterLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(panelCurrentOrder,
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(panelOrderSplit,
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(panelBarcode,
                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 618,
                                                                Short.MAX_VALUE)
                                                        .addComponent(panelPopular,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                                .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 120,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(txtSearchCustomer, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnAddCustomer1)
                                                .addGap(14, 14, 14)))
                                .addGroup(panelSaleCounterLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(panelCashier, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(panelOrderSummary, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(panelEmployeeCheckIn, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap()));
        panelSaleCounterLayout.setVerticalGroup(
                panelSaleCounterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(panelSaleCounterLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(panelBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 67,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(panelCashier, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelSaleCounterLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                                .addComponent(panelOrderSplit, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(panelCurrentOrder, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addGroup(panelSaleCounterLayout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                                false)
                                                        .addComponent(btnAddCustomer1,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 34,
                                                                Short.MAX_VALUE)
                                                        .addComponent(txtSearchCustomer)
                                                        .addComponent(cbCategory))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(panelPopular, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(panelSaleCounterLayout.createSequentialGroup()
                                                .addComponent(panelEmployeeCheckIn,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(panelOrderSummary, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap()));

        panelContent.add(panelSaleCounter, "cardSaleCounter");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panelMenu, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, 983,
                                        Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelContent, javax.swing.GroupLayout.Alignment.TRAILING,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 723, javax.swing.GroupLayout.PREFERRED_SIZE));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnHumanResourcesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnHumanResourcesActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnHumanResourcesActionPerformed

    private void btnOrderHistoryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnOrderHistoryActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnOrderHistoryActionPerformed

    private void cbMonthActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbMonthActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_cbMonthActionPerformed

    private void btnCustomerManagementActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCustomerManagementActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnCustomerManagementActionPerformed

    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddProductActionPerformed
        views.AddProductFrame addFrame = new views.AddProductFrame(() -> {
            loadInventoryTableData();
            if (salesCounter != null) {
                salesCounter.loadProductGrid();
            }
        });
        addFrame.setVisible(true);
    }// GEN-LAST:event_btnAddProductActionPerformed

    private void txtSearchInvoiceActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtSearchInvoiceActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txtSearchInvoiceActionPerformed

    private void btnAddEmployeeMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_btnAddEmployeeMouseClicked
        openAddEmployeeFrame();
    }// GEN-LAST:event_btnAddEmployeeMouseClicked

    private void cbYearActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbYearActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_cbYearActionPerformed

    private void txtSearchEmployeeActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtSearchEmployeeActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txtSearchEmployeeActionPerformed

    private void findByBarcodeMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_findByBarcodeMouseClicked
        util.BarcodeScannerUtil.startScan(this, rawBarcode -> {
            if (rawBarcode != null && !rawBarcode.trim().isEmpty()) {
                String empCode = util.BarcodeHashUtil.isEmpCode(rawBarcode)
                        ? rawBarcode.trim()
                        : util.BarcodeHashUtil.toEmpCode(rawBarcode);
                if (cbRole != null) {
                    cbRole.setSelectedItem("All");
                }
                if (txtSearchEmployee != null) {
                    txtSearchEmployee.setText(empCode);
                }
                loadEmployeeManagementTableData();
            }
        });
    }// GEN-LAST:event_findByBarcodeMouseClicked

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {
        if (txtSearchEmployee != null) {
            txtSearchEmployee.setText("");
        }
        if (cbRole != null) {
            cbRole.setSelectedItem("All");
        }
        loadEmployeeManagementTableData();
    }

    private void openAddEmployeeFrame() {
        AddEmployeeFrame addFrame = new AddEmployeeFrame(() -> showEmployeeManagementAndReload());
        addFrame.setVisible(true);
    }

    private void openEditEmployeeFrame(int employeeId) {
        AddEmployeeFrame editFrame = new AddEmployeeFrame(employeeId, () -> showEmployeeManagementAndReload());
        editFrame.setVisible(true);
    }

    public void showEmployeeManagementAndReload() {
        btnHumanResources.doClick();
        btnEmployeeManagement.doClick();
        loadEmployeeManagementTableData();
        if (salesCounter != null) {
            salesCounter.loadCheckedInEmployees();
            salesCounter.refreshUserDropdown();
        }
    }

    private void btnAddScheduleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddScheduleActionPerformed
        AddScheduleFrame addScheduleFrame = new AddScheduleFrame(this::loadWeeklyScheduleData);
        addScheduleFrame.setLocationRelativeTo(this);
        addScheduleFrame.setVisible(true);
    }// GEN-LAST:event_btnAddScheduleActionPerformed

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAddCustomerActionPerformed
        // btnAddCustomer1 is used for sales counter.
    }// GEN-LAST:event_btnAddCustomerActionPerformed

    private void btnPOSPaymentActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPOSPaymentActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_btnPOSPaymentActionPerformed

    private void cashBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cashBtnActionPerformed
        java.awt.CardLayout cl = (java.awt.CardLayout) panelCardParent.getLayout();
        cl.show(panelCardParent, "panelCashView");
        cashBtn.putClientProperty("_payMode", "Cash");
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
        panelCardParent.revalidate();
        panelCardParent.repaint();
    }// GEN-LAST:event_cashBtnActionPerformed

    private void qrBtnActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_qrBtnActionPerformed
        java.awt.CardLayout cl = (java.awt.CardLayout) panelCardParent.getLayout();
        cl.show(panelCardParent, "panelQRView");
        cashBtn.putClientProperty("_payMode", "Bank Transfer");
        cashBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #FFFFFF;"
                + "foreground: #4A5568;"
                + "border: 1,#E2E8F0;"
                + "arc: 15;");
        qrBtn.putClientProperty(FlatClientProperties.STYLE, ""
                + "background: #E28743;"
                + "foreground: #FFFFFF;"
                + "borderWidth: 0;"
                + "arc: 15;");
        panelCardParent.revalidate();
        panelCardParent.repaint();
    }// GEN-LAST:event_qrBtnActionPerformed

    private void btnConfirmOrderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConfirmOrderActionPerformed
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
                            String.format("Số tiền khách trả (%,.0f đ) nhỏ hơn tổng tiền phải thanh toán (%,.0f đ)!",
                                    cashReceived, totalAmount),
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

                boolean ok = orderRepository.finalizeOrder(orderId, cart.getCustomerId(), paymentMethod, totalAmount,
                        "PAID", items);
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
                    javax.swing.JOptionPane.showMessageDialog(DashBoardFrame.this,
                            "Sản phẩm \"" + name + "\" không còn tồn tại trên hệ thống.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (result.startsWith("INSUFFICIENT_STOCK:")) {
                    String[] parts = result.split(":");
                    String name = parts[1];
                    String stock = parts[2];
                    String req = parts[3];
                    javax.swing.JOptionPane.showMessageDialog(DashBoardFrame.this,
                            "Không đủ số lượng trong kho cho sản phẩm: " + name
                                    + "\nSố lượng yêu cầu: " + req
                                    + "\nSố lượng hiện có: " + stock,
                            "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (!"OK".equals(result)) {
                    javax.swing.JOptionPane.showMessageDialog(DashBoardFrame.this, "Lưu hóa đơn thất bại.",
                            "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                refreshAfterNewOrder();
                loadProductGrid(); // Tải lại lưới sản phẩm để cập nhật số lượng tồn kho vừa trừ
                if (txtCashReceived != null) {
                    txtCashReceived.setText("");
                }
                if (salesCounter != null) {
                    salesCounter.refreshTransactionHistory();
                    salesCounter.loadProductGrid();
                }
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
    }// GEN-LAST:event_btnConfirmOrderActionPerformed

    private void btnCancelOrderActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCancelOrderActionPerformed
        int orderId = sessionOrderIds.get(activeIndex);
        if (orderId != -1) {
            orderRepository.finalizeOrder(orderId, null, null, 0, "CANCELLED", null);
        }

        refreshAfterNewOrder();
        if (txtCashReceived != null) {
            txtCashReceived.setText("");
        }
        if (salesCounter != null) {
            salesCounter.refreshTransactionHistory();
            salesCounter.loadProductGrid();
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
    }// GEN-LAST:event_btnCancelOrderActionPerformed

    private void txtSearchCustomerActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_txtSearchCustomerActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_txtSearchCustomerActionPerformed

    private void btnScanActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnScanActionPerformed
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

                if (emp.getRoleId() != 1) {
                    if (!attendanceRepository.hasActiveCheckIn(emp.getId())) {
                        attendanceRepository.checkIn(emp.getId());
                    }
                    // Thêm emp vào UserSession để SalesCounter cũng đồng bộ được
                    util.UserSession.getInstance().addActiveEmployee(emp);

                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Điểm danh (Check-in) thành công cho nhân viên " + emp.getFullName() + ".",
                            "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);

                    // Reload local attendance in Dashboard (on EDT để đảm bảo Swing cập nhật)
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        loadCheckedInEmployees();
                        loadAttendanceTableData();
                        loadOverviewCardsData();
                    });

                    // Synchronize POS sales counter attendance
                    if (salesCounter != null) {
                        salesCounter.loadCheckedInEmployees();
                        salesCounter.refreshUserDropdown();
                    }
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Tài khoản Quản lý không cần điểm danh!",
                            "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Mã thẻ không hợp lệ hoặc không tìm thấy nhân viên!",
                        "Lỗi đăng nhập", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }// GEN-LAST:event_btnScanActionPerformed

    // --- INTEGRATED SALES COUNTER LOGIC ---

    private controller.OrderCartController activeCart() {
        return orderSessions.get(activeIndex);
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

    private void createNewOrder() {
        if (orderSessions.size() >= 6) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Chỉ được tạo tối đa 6 hóa đơn chờ.",
                    "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrderCartController newCart = new OrderCartController(
                tableCurrentOrder, lbSubtotal,
                lbChangeDue, txtCashReceived, this::handleCartTotalChanged);
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
            javax.swing.JButton btn = tabButtons.get(i);
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
        tableCurrentOrder.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Đơn giá
        tableCurrentOrder.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Thành tiền
    }

    private void addTabButton(String label) {
        int index = tabButtons.size();
        javax.swing.JButton tab = new javax.swing.JButton(label);
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

        txtSearchCustomer.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search customer by name/phone...");
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
        cachedProductList = listProduct != null ? listProduct : new java.util.ArrayList<>();
        cachedCategoryList = listCategory != null ? listCategory : new java.util.ArrayList<>();
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
            scrollPopular.setViewportView(panelProductGrid);
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

    private void switchToOrder(int index) {
        if (index < 0 || index >= orderSessions.size()) {
            return;
        }
        activeIndex = index;
        OrderCartController cart = orderSessions.get(index);
        cart.rebindTo(tableCurrentOrder, lbSubtotal, lbChangeDue, txtCashReceived, this::handleCartTotalChanged);
        txtSearchCustomer.setText(cart.getCustomerName() != null ? cart.getCustomerName() : "");
        lblCurrentOrder.setText(
                cart.getCustomerName() != null ? "Current Order - " + cart.getCustomerName() : "Current Order");
        applyTableStyling();
        refreshTabUI();
    }

    private void initSalesCounterPanel() {
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

        String panelStyle = "arc: 20; background: #FFFFFF;";
        panelBarcode.putClientProperty(FlatClientProperties.STYLE, panelStyle);
        panelCashier.putClientProperty(FlatClientProperties.STYLE, panelStyle);
        updateCashierPanel();
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
            util.BarcodeScannerUtil.startScan(this, rawBarcode -> {
                if (rawBarcode == null || rawBarcode.trim().isEmpty()) {
                    return;
                }
                entity.Product p = productRepository.findByBarcode(rawBarcode.trim());
                if (p != null) {
                    if (!util.UserSession.getInstance().isLoggedIn()) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Vui lòng đăng nhập trước!", "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    ensureOrderPersisted(activeIndex);
                    activeCart().addProduct(p);
                } else {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Không tìm thấy sản phẩm với mã vạch: " + rawBarcode,
                            "Thông báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            });
        });

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

        btnScan.setContentAreaFilled(false);
        btnScan.setFocusPainted(false);
        btnScan.setBorderPainted(false);
        btnScan.setText("");
        btnScan.setUI(new ScannerButtonUI());

        if (jScrollPane1 != null) {
            jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        }
        renderPriceOnQRCode(0.00);

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

        // Add Customer Action Listener
        btnAddCustomer1.addActionListener(e -> {
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

        initCustomerManagementInSale();
        initProductManagementInSale();
        createNewOrder();
        loadProductGrid();
    }

    public void updateCashierPanel() {
        panelCashier.removeAll();

        jLabel19.setText("STAFF CHECK-IN");
        labelStatus.setText("Đang đợi quét thẻ...");

        if (btnLoginQuick == null) {
            btnLoginQuick = new javax.swing.JButton("Đăng nhập");
            btnLoginQuick.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btnLoginQuick.setBackground(new java.awt.Color(111, 59, 26)); // Meomeo theme brown
            btnLoginQuick.setForeground(java.awt.Color.WHITE);
            btnLoginQuick.setPreferredSize(new java.awt.Dimension(100, 35));
            btnLoginQuick.setFocusPainted(false);
            btnLoginQuick.setBorderPainted(false);
            btnLoginQuick.putClientProperty(FlatClientProperties.STYLE,
                    "background: #6F3B1A; foreground: #FFFFFF; arc: 10;");
            btnLoginQuick.addActionListener(e -> {
                // Truyền this (DashBoardFrame) để AppRouter không tạo thêm Dashboard mới sau login
                LoginFrame loginFrame = new LoginFrame(DashBoardFrame.this, salesCounter);
                loginFrame.setVisible(true);
            });
        }

        javax.swing.GroupLayout panelCashierLayout = new javax.swing.GroupLayout(panelCashier);
        panelCashier.setLayout(panelCashierLayout);
        panelCashierLayout.setHorizontalGroup(
                panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCashierLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelCashierLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel19)
                                        .addComponent(labelStatus))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnLoginQuick, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)));
        panelCashierLayout.setVerticalGroup(
                panelCashierLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelCashierLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(panelCashierLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(btnScan, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnLoginQuick, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(panelCashierLayout.createSequentialGroup()
                                                .addComponent(jLabel19)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(labelStatus)))
                                .addContainerGap(15, Short.MAX_VALUE)));

        panelCashier.revalidate();
        panelCashier.repaint();
    }

    private void initCheckedInEmployeesPanel() {
        panelEmployeeCheckIn.removeAll();
        panelEmployeeCheckIn.setLayout(new java.awt.BorderLayout());
        panelEmployeeCheckIn.setPreferredSize(new java.awt.Dimension(250, 200));
        panelEmployeeCheckIn.setMinimumSize(new java.awt.Dimension(200, 136));
        panelEmployeeCheckIn.setOpaque(false);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("Nhân viên đã check-in",
                javax.swing.SwingConstants.LEFT);
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
        mainPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 20; background: #FFFFFF;");

        mainPanel.add(titleLabel, java.awt.BorderLayout.NORTH);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        panelEmployeeCheckIn.add(mainPanel, java.awt.BorderLayout.CENTER);
        checkInListPanel = listPanel;
    }

    public void loadCheckedInEmployees() {
        if (checkInListPanel == null) {
            initCheckedInEmployeesPanel();
        }

        checkInListPanel.removeAll();
        java.util.List<repository.AttendanceRepository.CheckedInEmployee> list = attendanceRepository
                .getTodayCheckedInEmployees();

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

        // Revalidate từ trong ra ngoài để GroupLayout cha tính lại kích thước
        checkInListPanel.revalidate();
        checkInListPanel.repaint();
        panelEmployeeCheckIn.revalidate();
        panelEmployeeCheckIn.repaint();
        if (panelEmployeeCheckIn.getParent() != null) {
            panelEmployeeCheckIn.getParent().revalidate();
            panelEmployeeCheckIn.getParent().repaint();
        }
        // Force repaint toàn frame để đảm bảo hiển thị đúng
        this.revalidate();
        this.repaint();
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
            try {
                roleColor = java.awt.Color.decode(emp.roleColorHex);
            } catch (Exception ignored) {
            }
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
            btnEndShift.putClientProperty(FlatClientProperties.STYLE,
                    "background: #EF4444; foreground: #FFFFFF; arc: 8;");
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

                    // Reload local attendance in Dashboard
                    loadCheckedInEmployees();
                    loadAttendanceTableData();
                    loadOverviewCardsData();

                    // Synchronize POS sales counter attendance
                    if (salesCounter != null) {
                        salesCounter.loadCheckedInEmployees();
                        salesCounter.refreshUserDropdown();
                    }

                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Đã kết thúc phiên làm việc thành công!",
                            "Thông báo", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            });
            eastPanel.add(btnEndShift);
        }

        card.add(infoPanel, java.awt.BorderLayout.CENTER);
        card.add(eastPanel, java.awt.BorderLayout.EAST);

        return card;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (!util.UserSession.getInstance().isLoggedIn()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Bạn chưa đăng nhập! Vui lòng đăng nhập để tiếp tục.",
                        "Lỗi truy cập", javax.swing.JOptionPane.ERROR_MESSAGE);
                util.AppRouter.showSalesCounterFrame();
                this.dispose();
                return;
            }
            if (util.UserSession.getInstance().getCurrentUser().getRoleId() != 1) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Chỉ Manager mới được phép truy cập trang quản lý!",
                        "Từ chối truy cập", javax.swing.JOptionPane.ERROR_MESSAGE);
                this.dispose();
                return;
            }
        }
        super.setVisible(b);
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            // Tạo một frame rỗng tạm thời hoặc truyền null để test giao diện Dashboard
            // (Nếu code constructor của bạn bắt buộc phải có SalesCounterFrame, hãy giữ
            // nguyên nhưng kiểm tra Constructor của SalesCounter xem có bị gọi
            // setVisible(true) bên trong không nhé)
            DashBoardFrame db = new DashBoardFrame(new SalesCounterFrame());
            db.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddCustomer1;
    private javax.swing.JButton btnAddEmployee;
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnAddSchedule;
    private javax.swing.JButton btnAttendance;
    private javax.swing.JButton btnBarcode;
    private javax.swing.JButton btnCancelOrder;
    private javax.swing.JButton btnConfirmOrder;
    private javax.swing.JButton btnCustomerManagement;
    private javax.swing.JButton btnDashBoard;
    private javax.swing.JButton btnEmployeeManagement;
    private javax.swing.JButton btnHumanResources;
    private javax.swing.JButton btnNextWeek;
    private javax.swing.JButton btnOrderHistory;
    private javax.swing.JButton btnPOSPayment;
    private javax.swing.JButton btnPrevWeek;
    private javax.swing.JButton btnProductInventory;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnScan;
    private javax.swing.JButton btnSchedule;
    private javax.swing.JButton cashBtn;
    private javax.swing.JComboBox<String> cbAll;
    private javax.swing.JComboBox<String> cbCategory;
    private javax.swing.JComboBox<String> cbMonth;
    private javax.swing.JComboBox<String> cbPaymentMethod;
    private javax.swing.JComboBox<String> cbRole;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JComboBox<String> cbYear;
    private javax.swing.JComboBox<String> cbbLogOut;
    private com.toedter.calendar.JDateChooser dateFrom;
    private com.toedter.calendar.JDateChooser dateTo;
    private javax.swing.JButton findByBarcode;
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
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JLabel lbAvatarShop;
    private javax.swing.JLabel lbChangeDue;
    private javax.swing.JLabel lbQRCode;
    private javax.swing.JLabel lbSubtotal;
    private javax.swing.JLabel lblAbsentCount;
    private javax.swing.JLabel lblActiveEmployees;
    private javax.swing.JLabel lblAttendance;
    private javax.swing.JLabel lblCurrentOrder;
    private javax.swing.JLabel lblEmployeeSchedule;
    private javax.swing.JLabel lblLateCount;
    private javax.swing.JLabel lblMonthDayYear;
    private javax.swing.JLabel lblOnLeaveCount;
    private javax.swing.JLabel lblOnTimeCount;
    private javax.swing.JLabel lblPaidOrder1;
    private javax.swing.JLabel lblPaidOrder2;
    private javax.swing.JLabel lblPaidOrder3;
    private javax.swing.JLabel lblPaidOrder4;
    private javax.swing.JLabel lblPendingInvoice;
    private javax.swing.JLabel lblPopular;
    private javax.swing.JLabel lblRecordAttendance;
    private javax.swing.JLabel lblRecordLog;
    private javax.swing.JLabel lblRevenueMonth;
    private javax.swing.JLabel lblRevenueToday;
    private javax.swing.JLabel lblTotalItem;
    private javax.swing.JLabel lblWeekRange;
    private javax.swing.JPanel panelActiveEmployees;
    private javax.swing.JPanel panelActiveOrders;
    private javax.swing.JPanel panelAttendance;
    private javax.swing.JPanel panelBarcode;
    private javax.swing.JPanel panelBelowHeader;
    private javax.swing.JPanel panelCanceledOrders;
    private javax.swing.JPanel panelCardParent;
    private javax.swing.JPanel panelCashView;
    private javax.swing.JPanel panelCashier;
    private javax.swing.JPanel panelChangeDue;
    private javax.swing.JPanel panelContent;
    private javax.swing.JPanel panelCurrentOrder;
    private javax.swing.JPanel panelCustomer;
    private javax.swing.JPanel panelCustomerHeader;
    private javax.swing.JPanel panelDashboard;
    private javax.swing.JPanel panelDashboardHeader;
    private javax.swing.JPanel panelDate;
    private javax.swing.JPanel panelEmployeeCheckIn;
    private javax.swing.JPanel panelEmployeeMain;
    private javax.swing.JPanel panelEmployeeManagement;
    private javax.swing.JPanel panelHRHeader;
    private javax.swing.JPanel panelHRMain;
    private javax.swing.JPanel panelLogo;
    private javax.swing.JPanel panelMenu;
    private javax.swing.JPanel panelOrder;
    private javax.swing.JPanel panelOrderManagement1;
    private javax.swing.JPanel panelOrderManagement2;
    private javax.swing.JPanel panelOrderManagement4;
    private javax.swing.JPanel panelOrderSplit;
    private javax.swing.JPanel panelOrderSummary;
    private javax.swing.JPanel panelPopular;
    private javax.swing.JPanel panelProduct;
    private javax.swing.JPanel panelProductGrid;
    private javax.swing.JPanel panelQRView;
    private javax.swing.JPanel panelRevenueDate;
    private javax.swing.JPanel panelRevenueToday;
    private javax.swing.JPanel panelSaleCounter;
    private javax.swing.JPanel panelSchedule;
    private javax.swing.JPanel panelSelection;
    private javax.swing.JPanel panelTopSales;
    private javax.swing.JButton qrBtn;
    private javax.swing.JScrollPane scrollPopular;
    private javax.swing.JTable tableAttendance;
    private javax.swing.JTable tableCurrentOrder;
    private javax.swing.JTable tableEmployeeManagement;
    private javax.swing.JTable tableProduct;
    private javax.swing.JTable tableSchedule;
    private javax.swing.JTable tableTransactionHistory;
    private javax.swing.JTextField txtBarcodeSearch;
    private javax.swing.JTextField txtCashReceived;
    private javax.swing.JTextField txtSearchCustomer;
    private javax.swing.JTextField txtSearchEmployee;
    private javax.swing.JTextField txtSearchInvoice;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
}

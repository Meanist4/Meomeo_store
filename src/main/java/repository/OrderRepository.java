package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class OrderRepository {

    public static class OrderRow {

        public int id;
        public Timestamp orderDate;
        public String cashierName;
        public String customerName;
        public String customerPhone;
        public double totalAmount;
        public String status;
    }

    public List<OrderRow> findTodayOrders() {
        List<OrderRow> result = new ArrayList<>();
        String sql = "SELECT o.id, o.order_date, c.full_name AS customer_name, c.phone, o.total_amount, o.status "
                + "FROM orders o "
                + "LEFT JOIN customers c ON o.customer_id = c.id "
                + "WHERE DATE(o.order_date) = CURDATE() "
                + "ORDER BY o.order_date DESC"; // Giữ ORDER BY để đơn hàng mới nhất lên đầu

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                OrderRow row = new OrderRow();
                row.id = rs.getInt("id");
                row.orderDate = rs.getTimestamp("order_date");
                row.customerName = rs.getString("customer_name") != null ? rs.getString("customer_name") : "Khách vãng lai";
                row.customerPhone = rs.getString("phone") != null ? rs.getString("phone") : "";

                row.totalAmount = rs.getDouble("total_amount");
                row.status = rs.getString("status");

                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách đơn hàng hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public int cancelOrders(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return 0;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orderIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE id IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < orderIds.size(); i++) {
                ps.setInt(i + 1, orderIds.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi hủy đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static class OrderHistoryRow {

        public int id;
        public Timestamp orderDate;
        public String cashierName;
        public String customerName;
        public String paymentMethod;
        public double totalAmount;
        public String status;
        public int isDeleted;
    }

    public List<OrderHistoryRow> findOrderHistory(String status, java.util.Date startDate, java.util.Date endDate) {
        List<OrderHistoryRow> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.order_date, e.full_name AS cashier_name, "
                + "COALESCE(c.full_name, 'Walk-in customer') AS customer_name, "
                + "o.payment_method, o.total_amount, o.status AS order_status "
                + "FROM orders o "
                + "JOIN employees e ON o.employee_id = e.id "
                + "LEFT JOIN customers c ON o.customer_id = c.id "
                + "WHERE 1=1 ");

        String normalizedStatus = normalizeFilterStatus(status);
        if ("PAID".equals(normalizedStatus)) {
            sql.append("AND UPPER(COALESCE(o.status, 'PENDING')) = 'PAID' ");
        }
        if ("PENDING".equals(normalizedStatus)) {
            sql.append("AND UPPER(COALESCE(o.status, 'PENDING')) = 'PENDING' ");
        }
        if ("CANCELLED".equals(normalizedStatus)) {
            sql.append("AND UPPER(COALESCE(o.status, 'PENDING')) = 'CANCELLED' ");
        }
        if (startDate != null) {
            sql.append("AND o.order_date >= ? ");
        }
        if (endDate != null) {
            sql.append("AND o.order_date <= ? ");
        }
        sql.append("ORDER BY o.order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (startDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                ps.setTimestamp(idx++, new Timestamp(cal.getTimeInMillis()));
            }
            if (endDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                ps.setTimestamp(idx++, new Timestamp(cal.getTimeInMillis()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderHistoryRow row = new OrderHistoryRow();
                    row.id = rs.getInt("id");
                    row.orderDate = rs.getTimestamp("order_date");
                    row.cashierName = rs.getString("cashier_name");
                    row.customerName = rs.getString("customer_name");
                    row.paymentMethod = rs.getString("payment_method");
                    row.totalAmount = rs.getDouble("total_amount");
                    row.status = normalizeHistoryStatus(rs.getString("order_status"));
                    row.isDeleted = "CANCELLED".equals(row.status) ? 1 : 0;
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp lịch sử đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    private String normalizeFilterStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "ALL";
        }

        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CANCELED", "CANCELLED" -> "CANCELLED";
            case "PENDING", "PAID", "ALL" -> normalized;
            default -> normalized;
        };
    }

    private String normalizeHistoryStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "PENDING";
        }

        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CANCELED", "CANCELLED" -> "CANCELLED";
            case "PENDING", "PAID" -> normalized;
            default -> normalized;
        };
    }

    public static class OrderOverview {

        public double totalRevenue;
        public int paidCount;
        public int canceledCount;
    }

    public OrderOverview getOrderOverview(java.util.Date startDate, java.util.Date endDate) {
        OrderOverview overview = new OrderOverview();
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) = 'PAID' THEN total_amount ELSE 0 END), 0) AS total_revenue, "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) = 'PAID' THEN 1 ELSE 0 END), 0) AS paid_count, "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) IN ('CANCELED', 'CANCELLED') THEN 1 ELSE 0 END), 0) AS canceled_count "
                + "FROM orders WHERE order_date >= ? AND order_date <= ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate != null ? startDate : new java.util.Date(0));
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            ps.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            cal.setTime(endDate != null ? endDate : new java.util.Date());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            ps.setTimestamp(2, new Timestamp(cal.getTimeInMillis()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    overview.totalRevenue = rs.getDouble("total_revenue");
                    overview.paidCount = rs.getInt("paid_count");
                    overview.canceledCount = rs.getInt("canceled_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp tổng quan đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return overview;
    }

    public static class DailyOrderStats {

        public double revenue;
        public int activeOrders;
        public int canceledOrders;
    }

    public double getDailyRevenue() {
        String sql = """
        SELECT COALESCE(SUM(total_amount), 0)
        FROM orders
        WHERE status = 'PAID'
          AND DATE(order_date) = CURRENT_DATE
        """;
        try (var conn = DatabaseConnection.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public double getMonthlyRevenue() {
        String sql = """
        SELECT COALESCE(SUM(total_amount), 0)
        FROM orders
        WHERE status = 'PAID'
          AND MONTH(order_date) = MONTH(CURRENT_DATE)
          AND YEAR(order_date)  = YEAR(CURRENT_DATE)
        """;
        try (var conn = DatabaseConnection.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public DailyOrderStats getDailyOrderStats() {
        DailyOrderStats stats = new DailyOrderStats();
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) = 'PAID' THEN total_amount ELSE 0 END), 0) AS revenue, "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) IN ('PAID', 'PENDING') THEN 1 ELSE 0 END), 0) AS active_orders, "
                + "COALESCE(SUM(CASE WHEN UPPER(COALESCE(status, 'PENDING')) IN ('CANCELED', 'CANCELLED') THEN 1 ELSE 0 END), 0) AS canceled_orders "
                + "FROM orders WHERE DATE(order_date) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                stats.revenue = rs.getDouble("revenue");
                stats.activeOrders = rs.getInt("active_orders");
                stats.canceledOrders = rs.getInt("canceled_orders");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tải thống kê đơn hàng hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    public List<String> findDistinctPaymentMethods() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT DISTINCT payment_method "
                + "FROM orders "
                + "WHERE payment_method IS NOT NULL AND TRIM(payment_method) <> '' "
                + "ORDER BY payment_method ASC";

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getString("payment_method"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách phương thức thanh toán: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public long[] getRevenueByWeek() {
        java.util.LinkedHashMap<java.time.LocalDate, Long> map = new java.util.LinkedHashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            map.put(today.minusDays(i), 0L);
        }

        String sqlWeek = "SELECT DATE(order_date) AS ngay, "
                + "COALESCE(SUM(total_amount), 0) AS doanh_thu "
                + "FROM orders "
                + "WHERE status = 'PAID' " // ← sửa ở đây
                + "  AND DATE(order_date) >= CURDATE() - INTERVAL 6 DAY "
                + "GROUP BY DATE(order_date) "
                + "ORDER BY ngay ASC";

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlWeek)) {
            while (rs.next()) {
                java.time.LocalDate d = rs.getDate("ngay").toLocalDate();
                if (map.containsKey(d)) {
                    map.put(d, rs.getLong("doanh_thu"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getRevenueByWeek: " + e.getMessage());
            e.printStackTrace();
        }

        return map.values().stream().mapToLong(Long::longValue).toArray();
    }

    public long[] getRevenueByMonth() {
        long[] result = new long[12]; // index 0 = tháng 1

        String sql = "SELECT MONTH(order_date) AS thang, "
                + "COALESCE(SUM(total_amount), 0) AS doanh_thu "
                + "FROM orders "
                + "WHERE status = 'PAID' "
                + "  AND YEAR(order_date) = YEAR(CURDATE()) "
                + "GROUP BY MONTH(order_date) "
                + "ORDER BY thang ASC";

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int thang = rs.getInt("thang");
                if (thang >= 1 && thang <= 12) {
                    result[thang - 1] = rs.getLong("doanh_thu");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getRevenueByMonth: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static class TopProductRow {

        public String productName;
        public int quantitySold;
        public int stock; // quantity còn trong kho
    }

    public List<TopProductRow> getTopProductsToday(int limit) {
        List<TopProductRow> result = new ArrayList<>();
        String sql = "SELECT p.product_name, "
                + "  SUM(od.quantity) AS qty_sold, "
                + "  p.quantity AS stock "
                + "FROM order_details od "
                + "JOIN orders o  ON od.order_id  = o.id "
                + "JOIN products p ON od.product_id = p.id "
                + "WHERE o.status = 'PAID' "
                + "  AND DATE(o.order_date) = CURDATE() "
                + "GROUP BY p.id, p.product_name, p.quantity "
                + "ORDER BY qty_sold DESC "
                + "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductRow row = new TopProductRow();
                    row.productName = rs.getString("product_name");
                    row.quantitySold = rs.getInt("qty_sold");
                    row.stock = rs.getInt("stock");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTopProductsToday: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<TopProductRow> getTopProductsThisMonth(int limit) {
        List<TopProductRow> result = new ArrayList<>();
        String sql = "SELECT p.product_name, "
                + "  SUM(od.quantity) AS qty_sold, "
                + "  p.quantity AS stock "
                + "FROM order_details od "
                + "JOIN orders o  ON od.order_id  = o.id "
                + "JOIN products p ON od.product_id = p.id "
                + "WHERE o.status = 'PAID' "
                + "  AND MONTH(o.order_date) = MONTH(CURDATE()) "
                + "  AND YEAR(o.order_date)  = YEAR(CURDATE()) "
                + "GROUP BY p.id, p.product_name, p.quantity "
                + "ORDER BY qty_sold DESC "
                + "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductRow row = new TopProductRow();
                    row.productName = rs.getString("product_name");
                    row.quantitySold = rs.getInt("qty_sold");
                    row.stock = rs.getInt("stock");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTopProductsThisMonth: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}

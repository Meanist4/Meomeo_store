package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class OrderRepository {
    public static class OrderRow {
        public int id;
        public Timestamp orderDate;
        public String cashierName;
        public double totalAmount;
        public int isDeleted;   // 0: PAID, 1: CANCELED
    }

    public List<OrderRow> findTodayOrders() {
        List<OrderRow> result = new ArrayList<>();
        String sql = "SELECT o.id, o.order_date, e.full_name, o.total_amount, o.is_deleted "
                + "FROM orders o JOIN employees e ON o.employee_id = e.id "
                + "WHERE DATE(o.order_date) = CURDATE() ORDER BY o.order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                OrderRow row = new OrderRow();
                row.id = rs.getInt("id");
                row.orderDate = rs.getTimestamp("order_date");
                row.cashierName = rs.getString("full_name");
                row.totalAmount = rs.getDouble("total_amount");
                row.isDeleted = rs.getInt("is_deleted");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách đơn hàng hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    public int cancelOrders(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return 0;
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orderIds.size(); i++) {
            placeholders.append(i == 0 ? "?" : ",?");
        }
        String sql = "UPDATE orders SET is_deleted = 1 WHERE id IN (" + placeholders + ")";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
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
        public String paymentMethod;
        public double totalAmount;
        public int isDeleted;
    }

    public List<OrderHistoryRow> findOrderHistory(String status, java.util.Date startDate, java.util.Date endDate) {
        List<OrderHistoryRow> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.id, o.order_date, e.full_name, o.payment_method, o.total_amount, o.is_deleted "
                + "FROM orders o JOIN employees e ON o.employee_id = e.id WHERE 1=1 ");
        if ("PAID".equals(status))     sql.append("AND o.is_deleted = 0 ");
        if ("CANCELED".equals(status)) sql.append("AND o.is_deleted = 1 ");
        if (startDate != null) sql.append("AND o.order_date >= ? ");
        if (endDate   != null) sql.append("AND o.order_date <= ? ");
        sql.append("ORDER BY o.order_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (startDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
                ps.setTimestamp(idx++, new Timestamp(cal.getTimeInMillis()));
            }
            if (endDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
                ps.setTimestamp(idx++, new Timestamp(cal.getTimeInMillis()));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderHistoryRow row = new OrderHistoryRow();
                    row.id = rs.getInt("id");
                    row.orderDate = rs.getTimestamp("order_date");
                    row.cashierName = rs.getString("full_name");
                    row.paymentMethod = rs.getString("payment_method");
                    row.totalAmount = rs.getDouble("total_amount");
                    row.isDeleted = rs.getInt("is_deleted");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp lịch sử đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static class OrderOverview {
        public double totalRevenue;
        public int paidCount;
        public int canceledCount;
    }

    public OrderOverview getOrderOverview(java.util.Date startDate, java.util.Date endDate) {
        OrderOverview overview = new OrderOverview();
        String sql = "SELECT "
                + "SUM(CASE WHEN is_deleted = 0 THEN total_amount ELSE 0 END) AS total_revenue, "
                + "COUNT(CASE WHEN is_deleted = 0 THEN 1 END) AS paid_count, "
                + "COUNT(CASE WHEN is_deleted = 1 THEN 1 END) AS canceled_count "
                + "FROM orders WHERE order_date >= ? AND order_date <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate != null ? startDate : new java.util.Date(0));
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            ps.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));
            cal.setTime(endDate != null ? endDate : new java.util.Date());
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
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

    public DailyOrderStats getDailyOrderStats() {
        DailyOrderStats stats = new DailyOrderStats();
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN is_deleted = 0 THEN total_amount ELSE 0 END), 0) AS revenue, "
                + "COUNT(CASE WHEN is_deleted = 0 THEN 1 END) AS active_orders, "
                + "COUNT(CASE WHEN is_deleted = 1 THEN 1 END) AS canceled_orders "
                + "FROM orders WHERE DATE(order_date) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
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
}

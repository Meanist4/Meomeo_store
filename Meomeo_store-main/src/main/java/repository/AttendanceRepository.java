package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class AttendanceRepository {
    public static class AttendanceRow {

        public int employeeId;
        public String fullName;
        public String displayRole;
        public java.sql.Date workDate;
        public Timestamp checkIn;    // null nếu ABSENT hoặc ON LEAVE
        public Timestamp checkOut;   // null nếu chưa checkout
        public String finalStatus;   // ON TIME | LATE | ABSENT | ON LEAVE
    }

    public List<AttendanceRow> findByMonthYear(int month, int year) {
        List<AttendanceRow> result = new ArrayList<>();
        String sql = "SELECT e.id, e.full_name, "
                + "       r.role_name AS display_role, "
                + "       s.shift_date AS work_date, "
                + "       a.check_in, a.check_out, "
                + "       CASE "
                + "           WHEN a.check_in IS NOT NULL THEN a.status "
                + "           WHEN el.id IS NOT NULL THEN 'ON LEAVE' "
                + "           ELSE 'ABSENT' "
                + "       END AS final_status "
                + "FROM schedules s "
                + "JOIN employees e ON s.employee_id = e.id "
                + "JOIN roles r ON e.role_id = r.id "
                + "LEFT JOIN attendance a ON s.employee_id = a.employee_id AND s.shift_date = a.work_date "
                + "LEFT JOIN employee_leaves el ON s.employee_id = el.employee_id "
                + "       AND s.shift_date = el.leave_date AND el.status = 'APPROVED' "
                + "WHERE MONTH(s.shift_date) = ? AND YEAR(s.shift_date) = ? "
                + "  AND e.is_deleted = 0 "
                + "ORDER BY s.shift_date DESC, e.full_name ASC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRow row = new AttendanceRow();
                    row.employeeId = rs.getInt("id");
                    row.fullName = rs.getString("full_name");
                    row.displayRole = rs.getString("display_role");
                    row.workDate = rs.getDate("work_date");
                    row.checkIn = rs.getTimestamp("check_in");
                    row.checkOut = rs.getTimestamp("check_out");
                    row.finalStatus = rs.getString("final_status");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp bảng điểm danh: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public int countTodayPresentEmployees() {
        String sql = "SELECT COUNT(DISTINCT employee_id) AS employees "
                + "FROM attendance WHERE work_date = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("employees");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đếm nhân viên đi làm hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int findEarliestActiveYear() {
        int currentYear = java.time.LocalDate.now().getYear();
        String sql = "SELECT MIN(y) AS earliest_year FROM ("
                + "  SELECT YEAR(shift_date) AS y FROM schedules"
                + "  UNION SELECT YEAR(order_date) AS y FROM orders"
                + "  UNION SELECT YEAR(work_date) AS y FROM attendance"
                + ") t";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getObject("earliest_year") != null) {
                return Math.min(rs.getInt("earliest_year"), currentYear);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm năm bắt đầu hoạt động: " + e.getMessage());
        }
        return currentYear;
    }
}

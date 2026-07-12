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
        // Phần 1: nhân viên có lịch (có thể đã check-in hoặc chưa)
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
                + "UNION "
                // Phần 2: nhân viên check-in không có lịch (ngoài giờ)
                + "SELECT e.id, e.full_name, "
                + "       r.role_name AS display_role, "
                + "       a.work_date, "
                + "       a.check_in, a.check_out, "
                + "       a.status AS final_status "
                + "FROM attendance a "
                + "JOIN employees e ON a.employee_id = e.id "
                + "JOIN roles r ON e.role_id = r.id "
                + "WHERE MONTH(a.work_date) = ? AND YEAR(a.work_date) = ? "
                + "  AND e.is_deleted = 0 "
                + "  AND NOT EXISTS ("
                + "      SELECT 1 FROM schedules s2 "
                + "      WHERE s2.employee_id = a.employee_id AND s2.shift_date = a.work_date"
                + "  ) "
                + "ORDER BY work_date DESC, full_name ASC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ps.setInt(3, month);
            ps.setInt(4, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceRow row = new AttendanceRow();
                    row.employeeId = rs.getInt("id");
                    row.fullName = rs.getString("full_name");
                    row.displayRole = rs.getString("display_role");
                    row.workDate = rs.getDate("work_date");
                    row.checkIn = rs.getTimestamp("check_in");
                    row.checkOut = rs.getTimestamp("check_out");
                    String status = rs.getString("final_status");
                    row.finalStatus = (status != null) ? status : "ABSENT";
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

    public static class CheckedInEmployee {
        public int id;
        public String fullName;
        public String roleName;
        public String roleColorHex;
        public Timestamp checkInTime;
    }

    public List<CheckedInEmployee> getTodayCheckedInEmployees() {
        List<CheckedInEmployee> list = new ArrayList<>();
        String sql = "SELECT e.id, e.full_name, r.role_name, r.color_hex, a.check_in "
                   + "FROM attendance a "
                   + "JOIN employees e ON a.employee_id = e.id "
                   + "JOIN roles r ON e.role_id = r.id "
                   + "WHERE a.work_date = CURDATE() AND a.check_out IS NULL "
                   + "ORDER BY a.check_in ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CheckedInEmployee emp = new CheckedInEmployee();
                emp.id = rs.getInt("id");
                emp.fullName = rs.getString("full_name");
                emp.roleName = rs.getString("role_name");
                emp.roleColorHex = rs.getString("color_hex");
                emp.checkInTime = rs.getTimestamp("check_in");
                list.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách nhân viên checkin hôm nay: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean hasScheduleToday(int employeeId) {
        String sql = "SELECT 1 FROM schedules WHERE employee_id = ? AND shift_date = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra lịch làm hôm nay: " + e.getMessage());
        }
        return false;
    }

    public boolean hasActiveCheckIn(int employeeId) {
        String sql = "SELECT 1 FROM attendance WHERE employee_id = ? AND work_date = CURDATE() AND check_out IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra check-in hoạt động: " + e.getMessage());
        }
        return false;
    }

    public boolean checkIn(int employeeId) {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalTime nowTime = java.time.LocalTime.now();
        String status = "ON TIME";
        
        String schedSql = "SELECT start_time FROM schedules WHERE employee_id = ? AND shift_date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(schedSql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Time startTime = rs.getTime("start_time");
                    if (startTime != null) {
                        java.time.LocalTime schedStart = startTime.toLocalTime();
                        if (nowTime.isAfter(schedStart)) {
                            status = "LATE";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra lịch làm việc khi check-in: " + e.getMessage());
        }

        String sql = "INSERT INTO attendance (employee_id, work_date, check_in, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(today));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(4, status);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm check-in: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkOut(int employeeId) {
        String sql = "UPDATE attendance SET check_out = ? WHERE employee_id = ? AND work_date = CURDATE() AND check_out IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi check-out: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

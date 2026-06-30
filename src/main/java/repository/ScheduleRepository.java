package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ScheduleRepository {

    public static class ShiftCell {

        public LocalTime start;
        public LocalTime end;
    }

    public static class EmployeeScheduleRow {

        public int employeeId;
        public String fullName;
        public String barcode;
        public Map<LocalDate, ShiftCell> shiftsByDate = new LinkedHashMap<>();
    }

    public Map<Integer, EmployeeScheduleRow> findWeeklySchedule(LocalDate weekStart) {
        Map<Integer, EmployeeScheduleRow> rowsByEmployeeId = new LinkedHashMap<>();
        LocalDate weekEnd = weekStart.plusDays(6);

        String sql = "SELECT e.id, e.full_name, e.barcode, s.shift_date, s.start_time, s.end_time "
                + "FROM employees e "
                + "JOIN schedules s ON s.employee_id = e.id "
                + "WHERE e.is_deleted = 0 AND s.shift_date BETWEEN ? AND ? "
                + "ORDER BY e.full_name ASC, s.shift_date ASC";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(weekStart));
            ps.setDate(2, java.sql.Date.valueOf(weekEnd));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int empId = rs.getInt("id");
                    EmployeeScheduleRow row = rowsByEmployeeId.computeIfAbsent(empId, k -> {
                        EmployeeScheduleRow r = new EmployeeScheduleRow();
                        r.employeeId = empId;
                        try {
                            r.fullName = rs.getString("full_name");
                            r.barcode = rs.getString("barcode");
                        } catch (SQLException ex) {
                            r.fullName = "";
                            r.barcode = "";
                        }
                        return r;
                    });
                    ShiftCell cell = new ShiftCell();
                    cell.start = rs.getTime("start_time").toLocalTime();
                    cell.end = rs.getTime("end_time").toLocalTime();
                    row.shiftsByDate.put(rs.getDate("shift_date").toLocalDate(), cell);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp lịch làm việc tuần: " + e.getMessage());
            e.printStackTrace();
        }
        return rowsByEmployeeId;
    }

    public boolean existsByEmployeeAndDate(int employeeId, LocalDate date) {
        String sql = "SELECT 1 FROM schedules WHERE employee_id = ? AND shift_date = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra trùng lịch: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean addSchedule(int employeeId, LocalDate date, LocalTime start, LocalTime end) {
        String sql = "INSERT INTO schedules(employee_id, shift_date, start_time, end_time) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ps.setTime(3, java.sql.Time.valueOf(start));
            ps.setTime(4, java.sql.Time.valueOf(end));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm lịch làm việc: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}

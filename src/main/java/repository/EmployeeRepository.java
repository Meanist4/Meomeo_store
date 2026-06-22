package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class EmployeeRepository {

    public static class EmployeeRow {
        public int id;
        public String fullName;
        public String roleName;
        public String phone;
        public String barcode;
        public int status;   // 1: Active, 0: Inactive
    }

    public List<EmployeeRow> findEmployees(String roleName, String keyword) {
        List<EmployeeRow> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.full_name, r.role_name, e.phone, e.barcode, e.status "
                + "FROM employees e JOIN roles r ON e.role_id = r.id "
                + "WHERE e.is_deleted = 0 ");
        if (roleName != null && !"All".equalsIgnoreCase(roleName)) {
            sql.append("AND r.role_name = ? ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (e.full_name LIKE ? OR e.barcode LIKE ?) ");
        }
        sql.append("ORDER BY e.id ASC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (roleName != null && !"All".equalsIgnoreCase(roleName)) {
                ps.setString(idx++, roleName);
            }
            if (keyword != null && !keyword.isBlank()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmployeeRow row = new EmployeeRow();
                    row.id = rs.getInt("id");
                    row.fullName = rs.getString("full_name");
                    row.roleName = rs.getString("role_name");
                    row.phone = rs.getString("phone");
                    row.barcode = rs.getString("barcode");
                    row.status = rs.getInt("status");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<String> findAllRoleNames() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT role_name FROM roles ORDER BY role_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(rs.getString("role_name"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách vai trò: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }
}

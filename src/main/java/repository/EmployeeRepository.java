package repository;

import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

import com.mysql.cj.protocol.a.authentication.Sha256PasswordPlugin;

import entity.Employee;
import entity.Role;

public class EmployeeRepository {

    public static class EmployeeRow {

        public int id;
        public String fullName;
        public String roleName;
        public String roleColorHex;
        public String phone;
        public String barcode;
        public int status;
    }

    public List<EmployeeRow> findEmployees(String roleName, String keyword) {
        List<EmployeeRow> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.full_name, r.role_name, r.color_hex, e.phone, e.barcode, e.status "
                + "FROM employees e JOIN roles r ON e.role_id = r.id "
                + "WHERE e.is_deleted = 0 ");
        if (roleName != null && !"All".equalsIgnoreCase(roleName)) {
            sql.append("AND r.role_name = ? ");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (e.full_name LIKE ? OR e.barcode LIKE ?) ");
        }
        sql.append("ORDER BY e.id ASC");

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
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
                    row.roleColorHex = rs.getString("color_hex");
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
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(rs.getString("role_name"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách vai trò: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }

    public List<Role> getAllRole() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_name ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement st = conn.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("id"));
                role.setRoleName(rs.getString("role_name"));
                roles.add(role);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp danh sách vai trò: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }

    public Employee findById(int id) {
        String sql = """
                SELECT id, role_id, username, full_name, phone, barcode, status, is_deleted
                FROM employees WHERE id = ? AND is_deleted = 0
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Employee emp = new Employee();
                    emp.setId(rs.getInt("id"));
                    emp.setRoleId(rs.getInt("role_id"));
                    emp.setUsername(rs.getString("username"));
                    emp.setFullName(rs.getString("full_name"));
                    emp.setPhone(rs.getString("phone"));
                    emp.setBarcode(rs.getString("barcode"));
                    emp.setStatus(rs.getInt("status"));
                    emp.setIsDeleted(rs.getInt("is_deleted"));
                    return emp;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean addEmployee(Employee emp) {
        String sql = """
                INSERT INTO employees(full_name,username,password,role_id,phone,barcode) VALUES(?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getUsername());
            ps.setString(3, util.PasswordEncryptionPlugin.hashPassword(emp.getPassword()));
            ps.setInt(4, emp.getRoleId());
            ps.setString(5, emp.getPhone());
            ps.setString(6, emp.getBarcode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateEmployee(Employee emp) {
        String sql = """
                UPDATE employees SET full_name=?, role_id=?, phone=?, barcode=?, status=? WHERE id=?
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.getFullName());
            ps.setInt(2, emp.getRoleId());
            ps.setString(3, emp.getPhone());
            ps.setString(4, emp.getBarcode());
            ps.setInt(5, emp.getStatus());
            ps.setInt(6, emp.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteEmployee(int empId) {
        String sql = "UPDATE employees SET is_deleted=1 WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, empId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa nhân viên: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    // CREATE TABLE IF NOT EXISTS employees (
    // id INT AUTO_INCREMENT PRIMARY KEY,
    // role_id INT NOT NULL,
    // username VARCHAR(50) NOT NULL,
    // password VARCHAR(255) NOT NULL,
    // full_name VARCHAR(100) NOT NULL,
    // phone VARCHAR(15),
    // barcode VARCHAR(50) NOT NULL,
    // status TINYINT NOT NULL DEFAULT 1, -- 1: Đang làm, 0: Nghỉ tạm thời
    // is_deleted TINYINT(1) NOT NULL DEFAULT 0, -- SOFT DELETE: 1 là đã xóa tài
    // khoản (nghỉ hẳn)
    // FOREIGN KEY (role_id) REFERENCES roles(id)
    // );
}

package repository;

import entity.Product;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    public static class InventoryRow {

        public int id;
        public String productName;
        public String categoryName;
        public double price;
        public int quantity;
        public int status;
        public String imagePath;
    }

    public List<Product> getActiveProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 1 AND is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setProductName(rs.getString("product_name"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setQuantity(rs.getInt("quantity"));
                p.setStatus(rs.getInt("status"));
                p.setIsDeleted(rs.getInt("is_deleted"));
                p.setImagePath(rs.getString("image_path"));

                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    public List<InventoryRow> findAllForInventory() {
        List<InventoryRow> result = new ArrayList<>();
        String sql = "SELECT p.id, p.product_name, c.category_name, p.price, "
                + "p.quantity, p.status, p.image_path "
                + "FROM products p JOIN categories c ON p.category_id = c.id "
                + "WHERE p.is_deleted = 0 ORDER BY p.id ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                InventoryRow row = new InventoryRow();
                row.id = rs.getInt("id");
                row.productName = rs.getString("product_name");
                row.categoryName = rs.getString("category_name");
                row.price = rs.getDouble("price");
                row.quantity = rs.getInt("quantity");
                row.status = rs.getInt("status");
                row.imagePath = rs.getString("image_path");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp bảng tồn kho: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE (product_name LIKE ? OR id = ?) AND status = 1 AND is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            int idSearch = -1;
            try {
                idSearch = Integer.parseInt(keyword);
            } catch (NumberFormatException ignored) {
            }
            ps.setInt(2, idSearch);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setProductName(rs.getString("product_name"));
                    p.setPrice(rs.getBigDecimal("price"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setStatus(rs.getInt("status"));
                    p.setIsDeleted(rs.getInt("is_deleted"));
                    p.setImagePath(rs.getString("image_path"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }
}

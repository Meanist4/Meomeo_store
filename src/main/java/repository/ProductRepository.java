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
        public String unit;
        public double importPrice;
        public double price;
        public String barcode;
        public int quantity;
        public String imagePath;
    }

    public List<Product> getActiveProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 1 AND is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    public List<InventoryRow> findAllForInventory() {
        List<InventoryRow> result = new ArrayList<>();
        String sql = "SELECT p.id, p.product_name, c.category_name, p.unit, "
                + "p.import_price, p.price, p.barcode, p.quantity, p.image_path "
                + "FROM products p JOIN categories c ON p.category_id = c.id "
                + "WHERE p.is_deleted = 0 AND p.status = 1 "
                + "ORDER BY p.id ASC";

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                InventoryRow row = new InventoryRow();
                row.id = rs.getInt("id");
                row.productName = rs.getString("product_name");
                row.categoryName = rs.getString("category_name");
                row.unit = rs.getString("unit");
                row.importPrice = rs.getDouble("import_price");
                row.price = rs.getDouble("price");
                row.barcode = rs.getString("barcode");
                row.quantity = rs.getInt("quantity");
                row.imagePath = rs.getString("image_path");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi nạp bảng tồn kho: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public Product findById(int id) {
        String sql = "SELECT * FROM products WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean softDelete(int id) {
        String sql = "UPDATE products SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addQuantityByBarcode(String barcode, int addQty) {
        String sql = "UPDATE products SET quantity = quantity + ? "
                + "WHERE barcode = ? AND is_deleted = 0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, addQty);
            ps.setString(2, barcode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products "
                + "WHERE (product_name LIKE ? OR barcode LIKE ? OR id = ?) "
                + "AND status = 1 AND is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);          // tìm theo barcode luôn

            int idSearch = -1;
            try {
                idSearch = Integer.parseInt(keyword);
            } catch (NumberFormatException ignored) {
            }
            ps.setInt(3, idSearch);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return list;
    }

    public boolean add(Product product) {
        String sql = "INSERT INTO products "
                + "(category_id, product_name, description, unit, import_price, price, barcode, quantity, status, is_deleted, image_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getUnit());
            ps.setBigDecimal(5, product.getImportPrice());
            ps.setBigDecimal(6, product.getPrice());
            ps.setString(7, product.getBarcode());
            ps.setInt(8, product.getQuantity());
            ps.setInt(9, product.getStatus());
            ps.setInt(10, product.getIsDeleted());
            ps.setString(11, product.getImagePath());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Product findByBarcode(String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return null;
        }
        String sql = "SELECT * FROM products WHERE barcode = ? AND is_deleted = 0 LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isBarcodeExists(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM products WHERE barcode = ? AND is_deleted = 0";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Product product) {
        String sql = "UPDATE products SET category_id=?, product_name=?, description=?, unit=?, "
                + "import_price=?, price=?, quantity=?, image_path=? "
                + "WHERE id=? AND is_deleted=0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getCategoryId());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getDescription());
            ps.setString(4, product.getUnit());
            ps.setBigDecimal(5, product.getImportPrice());
            ps.setBigDecimal(6, product.getPrice());
            ps.setInt(7, product.getQuantity());
            ps.setString(8, product.getImagePath());
            ps.setInt(9, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setUnit(rs.getString("unit"));
        p.setImportPrice(rs.getBigDecimal("import_price"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setBarcode(rs.getString("barcode"));
        p.setQuantity(rs.getInt("quantity"));
        p.setStatus(rs.getInt("status"));
        p.setIsDeleted(rs.getInt("is_deleted"));
        p.setImagePath(rs.getString("image_path"));
        return p;
    }
}

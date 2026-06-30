package entity;

import java.math.BigDecimal;

public class Product {

    private int id;
    private int categoryId;
    private String productName;
    private String description;    // Cập nhật mới
    private String unit;           // Cập nhật mới
    private BigDecimal importPrice;// Cập nhật mới
    private BigDecimal price;      // Giá bán
    private String barcode;        // Cập nhật mới
    private int quantity;
    private int status;            // 1: Đang bán, 0: Ngừng bán
    private int isDeleted;         // 0: Bình thường, 1: Đã xóa (Soft delete)
    private String imagePath;      // Nullable

    public Product() {
    }

    public Product(int id, int categoryId, String productName, String description, String unit,
            BigDecimal importPrice, BigDecimal price, String barcode, int quantity,
            int status, int isDeleted, String imagePath) {
        this.id = id;
        this.categoryId = categoryId;
        this.productName = productName;
        this.description = description;
        this.unit = unit;
        this.importPrice = importPrice;
        this.price = price;
        this.barcode = barcode;
        this.quantity = quantity;
        this.status = status;
        this.isDeleted = isDeleted;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}

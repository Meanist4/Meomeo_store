package entity;

import java.math.BigDecimal;

public class Product {
    private int id;
    private int categoryId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private int status;       // 1: Đang bán, 0: Ngừng bán
    private int isDeleted;    // 0: bình thường, 1: đã xóa (soft delete)
    private String imagePath; // nullable

    public Product() {}

    public Product(int id, int categoryId, String productName, BigDecimal price,
                   int quantity, int status, int isDeleted, String imagePath) {
        this.id = id;
        this.categoryId = categoryId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.isDeleted = isDeleted;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", productName='" + productName
                + "', price=" + price + ", quantity=" + quantity + "}";
    }
}

package entity;

import java.math.BigDecimal;

public class OrderDetail {
    private int orderId;
    private int productId;
    private int quantity;
    private BigDecimal price;   // Giá tại thời điểm bán (snapshot, không đổi khi product.price thay đổi)

    public OrderDetail() {}

    public OrderDetail(int orderId, int productId, int quantity, BigDecimal price) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "OrderDetail{orderId=" + orderId + ", productId=" + productId
                + ", quantity=" + quantity + ", price=" + price + "}";
    }
}

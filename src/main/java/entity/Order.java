package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int id;
    private int employeeId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String paymentMethod;  // Cash, Card...
    private int isDeleted;         // 0: bình thường, 1: đã hủy (soft delete)

    public Order() {}

    public Order(int id, int employeeId, LocalDateTime orderDate,
                 BigDecimal totalAmount, String paymentMethod, int isDeleted) {
        this.id = id;
        this.employeeId = employeeId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.isDeleted = isDeleted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", employeeId=" + employeeId
                + ", orderDate=" + orderDate + ", totalAmount=" + totalAmount
                + ", paymentMethod='" + paymentMethod + "'}";
    }
}

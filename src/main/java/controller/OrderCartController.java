package controller;

import entity.Product;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class OrderCartController {

    private final DefaultTableModel ownModel;

    private JTable table;
    private JLabel lbSubtotal;
    private JLabel lbChangeDue;
    private JTextField txtCashReceived;
    private DoubleConsumer onTotalChanged;
    private Integer customerId;
    private String customerName;

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // CONSTRUCTOR: Chỉ nhận đúng các tham số bạn đang gọi ở View
    public OrderCartController(JTable table, JLabel lbSubtotal,
            JLabel lbChangeDue, JTextField txtCashReceived, DoubleConsumer onTotalChanged) {

        this.ownModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"STT", "Tên sản phẩm", "Còn lại", "Số lượng", "Đơn giá", "Thành tiền", "Thao tác", "ProductID"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        this.table = table;
        this.lbSubtotal = lbSubtotal;
        this.lbChangeDue = lbChangeDue;
        this.txtCashReceived = txtCashReceived;
        this.onTotalChanged = onTotalChanged;

        if (table != null) {
            table.setModel(this.ownModel);
            hideProductIdColumn(table);
        }
    }

    private void hideProductIdColumn(JTable t) {
        if (t.getColumnModel().getColumnCount() > 7) {
            t.getColumnModel().removeColumn(t.getColumnModel().getColumn(7));
        }
    }

    private DefaultTableModel model() {
        return ownModel;
    }

    public void addProduct(Product product) {
        DefaultTableModel model = model();
        int rowCount = model.getRowCount();
        boolean isExist = false;

        repository.ProductRepository productRepo = new repository.ProductRepository();
        Product latestProduct = productRepo.findById(product.getId());
        if (latestProduct == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Sản phẩm không tồn tại trên hệ thống.",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        int stockQty = latestProduct.getQuantity();

        for (int i = 0; i < rowCount; i++) {
            String productNameInTable = model.getValueAt(i, 1).toString();
            if (productNameInTable.equals(product.getProductName())) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 3).toString());
                int newQty = currentQty + 1;
                if (newQty > stockQty) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Sản phẩm " + product.getProductName() + " chỉ còn " + stockQty + " trong kho.",
                            "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BigDecimal unitPrice = product.getPrice();
                BigDecimal newSubTotal = unitPrice.multiply(BigDecimal.valueOf(newQty));
                model.setValueAt(stockQty, i, 2);
                model.setValueAt(newQty, i, 3);
                model.setValueAt(formatMoney(newSubTotal), i, 5);
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            if (stockQty < 1) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Sản phẩm " + product.getProductName() + " đã hết hàng.",
                        "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }
            int stt = rowCount + 1;
            model.addRow(new Object[]{
                stt,
                product.getProductName(),
                stockQty,
                1,
                formatMoney(product.getPrice()),
                formatMoney(product.getPrice()),
                "",
                product.getId()
            });
        }

        updateOrderSummaryTotals();
    }

    public static class CartItem {

        public int productId;
        public String productName;
        public int quantity;
        public double unitPrice;
    }

    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        DefaultTableModel model = model();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object pid = model.getValueAt(i, 7);
            if (pid == null) {
                continue;
            }
            CartItem item = new CartItem();
            item.productId = Integer.parseInt(pid.toString());
            item.productName = model.getValueAt(i, 1).toString();
            item.quantity = Integer.parseInt(model.getValueAt(i, 3).toString());
            item.unitPrice = parseMoney(model.getValueAt(i, 4).toString());
            items.add(item);
        }
        return items;
    }

    public boolean isEmpty() {
        return model().getRowCount() == 0;
    }

    public double getTotalAmount() {
        if (lbSubtotal != null) {
            try {
                return parseMoney(lbSubtotal.getText());
            } catch (NumberFormatException ignored) {
            }
        }
        double total = 0;
        for (CartItem item : getCartItems()) {
            total += item.unitPrice * item.quantity;
        }
        return total;
    }

    public void removeOne(int rowIndex) {
        DefaultTableModel model = model();
        int currentQty = Integer.parseInt(model.getValueAt(rowIndex, 3).toString());

        if (currentQty > 1) {
            int newQty = currentQty - 1;
            model.setValueAt(newQty, rowIndex, 3);

            double unitPrice = parseMoney(model.getValueAt(rowIndex, 4).toString());
            double newSubTotal = newQty * unitPrice;
            model.setValueAt(formatMoney(newSubTotal), rowIndex, 5);

            try {
                int productId = Integer.parseInt(model.getValueAt(rowIndex, 7).toString());
                repository.ProductRepository productRepo = new repository.ProductRepository();
                Product p = productRepo.findById(productId);
                if (p != null) {
                    model.setValueAt(p.getQuantity(), rowIndex, 2);
                }
            } catch (Exception ignored) {
            }
        } else {
            model.removeRow(rowIndex);
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0);
            }
        }

        updateOrderSummaryTotals();
    }

    public void updateOrderSummaryTotals() {
        DefaultTableModel model = model();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < model.getRowCount(); i++) {
            subtotal = subtotal.add(BigDecimal.valueOf(parseMoney(model.getValueAt(i, 5).toString())));
        }

        if (lbSubtotal != null) {
            lbSubtotal.setText(formatMoney(subtotal));
        }

        calculateChangeDue();

        if (onTotalChanged != null) {
            onTotalChanged.accept(subtotal.doubleValue());
        }
    }

    // REBIND: Đã loại bỏ hoàn toàn lbTotalPay thừa, khớp với switchToOrder() ở View
    public void rebindTo(JTable table, JLabel lbSubtotal,
            JLabel lbChangeDue, JTextField txtCashReceived, DoubleConsumer onTotalChanged) {
        this.table = table;
        this.lbSubtotal = lbSubtotal;
        this.lbChangeDue = lbChangeDue;
        this.txtCashReceived = txtCashReceived;
        this.onTotalChanged = onTotalChanged;

        if (table != null) {
            table.setModel(this.ownModel);
            hideProductIdColumn(table);
        }
        updateOrderSummaryTotals();
    }

    public void calculateChangeDue() {
        try {
            if (lbSubtotal == null) {
                return;
            }
            double totalPay = parseMoney(lbSubtotal.getText());

            if (totalPay <= 0) {
                if (lbChangeDue != null) {
                    lbChangeDue.setText("0 đ");
                    lbChangeDue.setForeground(new Color(46, 204, 113));
                }
                return;
            }

            if (txtCashReceived == null) {
                return;
            }
            String cashReceivedStr = txtCashReceived.getText().trim();
            if (cashReceivedStr.isEmpty()) {
                if (lbChangeDue != null) {
                    lbChangeDue.setText(String.format("Thiếu: %,.0f đ", totalPay));
                    lbChangeDue.setForeground(Color.RED);
                }
                return;
            }

            double cashReceived = Double.parseDouble(cashReceivedStr);
            double changeDue = cashReceived - totalPay;

            if (lbChangeDue != null) {
                if (changeDue < 0) {
                    lbChangeDue.setText(String.format("Thiếu: %,.0f đ", Math.abs(changeDue)));
                    lbChangeDue.setForeground(Color.RED);
                } else {
                    lbChangeDue.setText(formatMoney(changeDue));
                    lbChangeDue.setForeground(new Color(46, 204, 113));
                }
            }
        } catch (NumberFormatException e) {
            if (lbChangeDue != null) {
                boxErrorDue();
            }
        }
    }

    private void boxErrorDue() {
        lbChangeDue.setText("Số tiền lỗi!");
        lbChangeDue.setForeground(Color.RED);
    }

    private static String formatMoney(BigDecimal amount) {
        return String.format("%,.0f đ", amount);
    }

    private static String formatMoney(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private static double parseMoney(String text) {
        String clean = text.replace("đ", "").replace(",", "").trim();
        if (clean.contains("Thiếu:")) {
            clean = clean.replace("Thiếu:", "").trim();
        }
        return Double.parseDouble(clean);
    }
}

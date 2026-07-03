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
    private boolean isUpdating = false;

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
                new String[]{"STT", "Tên sản phẩm", "Còn lại", "Số lượng", "Đơn giá", "Thành tiền", "ProductID"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
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
            setupCellEditor(table);
        }

        this.ownModel.addTableModelListener(e -> {
            if (isUpdating) {
                return;
            }
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 3) {
                    handleQuantityChanged(row);
                }
            }
        });
    }

    private void hideProductIdColumn(JTable t) {
        if (t.getColumnModel().getColumnCount() > 6) {
            t.getColumnModel().removeColumn(t.getColumnModel().getColumn(6));
        }
    }

    private void setupCellEditor(JTable t) {
        if (t == null) {
            return;
        }
        JTextField qtyField = new JTextField();
        qtyField.addActionListener(e -> {
            if (t.isEditing()) {
                t.getCellEditor().stopCellEditing();
            }
            t.requestFocusInWindow();
        });
        t.getColumnModel().getColumn(3).setCellEditor(new javax.swing.DefaultCellEditor(qtyField));
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
                isUpdating = true;
                try {
                    model.setValueAt(stockQty, i, 2);
                    model.setValueAt(newQty, i, 3);
                    model.setValueAt(formatMoney(newSubTotal), i, 5);
                } finally {
                    isUpdating = false;
                }
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
            isUpdating = true;
            try {
                model.addRow(new Object[]{
                    stt,
                    product.getProductName(),
                    stockQty,
                    1,
                    formatMoney(product.getPrice()),
                    formatMoney(product.getPrice()),
                    product.getId()
                });
            } finally {
                isUpdating = false;
            }
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
            Object pid = model.getValueAt(i, 6);
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

    private void handleQuantityChanged(int row) {
        if (row < 0 || row >= ownModel.getRowCount()) {
            return;
        }

        Object qtyObj = ownModel.getValueAt(row, 3);
        int newQty = 0;
        boolean valid = true;
        if (qtyObj != null) {
            try {
                newQty = Integer.parseInt(qtyObj.toString().trim());
            } catch (NumberFormatException ex) {
                valid = false;
            }
        } else {
            valid = false;
        }

        // Calculate previous quantity from subtotal and unit price to restore if invalid
        double unitPrice = parseMoney(ownModel.getValueAt(row, 4).toString());
        double subtotal = parseMoney(ownModel.getValueAt(row, 5).toString());
        int previousQty = (int) Math.round(subtotal / unitPrice);
        if (previousQty <= 0) {
            previousQty = 1;
        }

        if (!valid) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Số lượng phải là số nguyên hợp lệ.",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            isUpdating = true;
            try {
                ownModel.setValueAt(previousQty, row, 3);
            } finally {
                isUpdating = false;
            }
            return;
        }

        if (newQty <= 0) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Số lượng phải lớn hơn 0!",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            isUpdating = true;
            try {
                ownModel.setValueAt(previousQty, row, 3);
            } finally {
                isUpdating = false;
            }
            return;
        }

        int productId = Integer.parseInt(ownModel.getValueAt(row, 6).toString());
        repository.ProductRepository productRepo = new repository.ProductRepository();
        Product latestProduct = productRepo.findById(productId);
        if (latestProduct == null) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Sản phẩm không tồn tại trên hệ thống.",
                    "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            isUpdating = true;
            try {
                ownModel.setValueAt(previousQty, row, 3);
            } finally {
                isUpdating = false;
            }
            return;
        }

        int stockQty = latestProduct.getQuantity();
        if (newQty > stockQty) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Sản phẩm " + latestProduct.getProductName() + " chỉ còn " + stockQty + " trong kho.",
                    "Cảnh báo", javax.swing.JOptionPane.WARNING_MESSAGE);
            isUpdating = true;
            try {
                ownModel.setValueAt(previousQty, row, 3);
            } finally {
                isUpdating = false;
            }
            return;
        }

        BigDecimal newSubtotal = latestProduct.getPrice().multiply(BigDecimal.valueOf(newQty));
        isUpdating = true;
        try {
            ownModel.setValueAt(stockQty, row, 2);
            ownModel.setValueAt(formatMoney(newSubtotal), row, 5);
        } finally {
            isUpdating = false;
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
            setupCellEditor(table);
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
        String clean = text.replace("đ", "")
                           .replace("Thiếu:", "")
                           .replace(",", "")
                           .replace(".", "")
                           .trim();
        return Double.parseDouble(clean);
    }
}

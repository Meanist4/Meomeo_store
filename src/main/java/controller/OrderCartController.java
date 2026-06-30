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
    private JLabel lbTotalPay;
    private JLabel lbChangeDue;
    private JTextField txtCashReceived;
    private DoubleConsumer onTotalChanged;

    public OrderCartController(JTable table, JLabel lbSubtotal, JLabel lbTotalPay,
            JLabel lbChangeDue, JTextField txtCashReceived, DoubleConsumer onTotalChanged) {

        this.ownModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"STT", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền", "Thao tác", "ProductID"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        this.table = table;
        this.lbSubtotal = lbSubtotal;
        this.lbTotalPay = lbTotalPay;
        this.lbChangeDue = lbChangeDue;
        this.txtCashReceived = txtCashReceived;
        this.onTotalChanged = onTotalChanged;
        if (table != null) {
            table.setModel(this.ownModel);
            hideProductIdColumn(table);
        }
    }

    private void hideProductIdColumn(JTable t) {
        if (t.getColumnModel().getColumnCount() > 6) {
            t.getColumnModel().removeColumn(t.getColumnModel().getColumn(6));
        }
    }

    private DefaultTableModel model() {
        return ownModel;
    }

    public void addProduct(Product product) {
        DefaultTableModel model = model();
        int rowCount = model.getRowCount();
        boolean isExist = false;

        for (int i = 0; i < rowCount; i++) {
            String productNameInTable = model.getValueAt(i, 1).toString();
            if (productNameInTable.equals(product.getProductName())) {
                int currentQty = Integer.parseInt(model.getValueAt(i, 2).toString());
                int newQty = currentQty + 1;
                BigDecimal unitPrice = product.getPrice();
                BigDecimal newSubTotal = unitPrice.multiply(BigDecimal.valueOf(newQty));
                model.setValueAt(newQty, i, 2);
                model.setValueAt(formatMoney(newSubTotal), i, 4);
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            int stt = rowCount + 1;
            model.addRow(new Object[]{
                stt,
                product.getProductName(),
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
            Object pid = model.getValueAt(i, 6);
            if (pid == null) {
                continue;
            }
            CartItem item = new CartItem();
            item.productId = Integer.parseInt(pid.toString());
            item.productName = model.getValueAt(i, 1).toString();
            item.quantity = Integer.parseInt(model.getValueAt(i, 2).toString());
            item.unitPrice = parseMoney(model.getValueAt(i, 3).toString());
            items.add(item);
        }
        return items;
    }

    public boolean isEmpty() {
        return model().getRowCount() == 0;
    }

    public double getTotalAmount() {
        if (lbTotalPay != null) {
            try {
                return parseMoney(lbTotalPay.getText());
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
        int currentQty = Integer.parseInt(model.getValueAt(rowIndex, 2).toString());

        if (currentQty > 1) {
            int newQty = currentQty - 1;
            model.setValueAt(newQty, rowIndex, 2);

            double unitPrice = parseMoney(model.getValueAt(rowIndex, 3).toString());
            double newSubTotal = newQty * unitPrice;
            model.setValueAt(formatMoney(newSubTotal), rowIndex, 4);
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
            subtotal = subtotal.add(BigDecimal.valueOf(parseMoney(model.getValueAt(i, 4).toString())));
        }

        BigDecimal discount = BigDecimal.ZERO; // chỗ này để mở rộng khuyến mãi sau
        BigDecimal totalPay = subtotal.subtract(discount);

        if (lbSubtotal != null) {
            lbSubtotal.setText(formatMoney(totalPay));
        }
        if (lbTotalPay != null) {
            lbTotalPay.setText(formatMoney(totalPay));
        }

        calculateChangeDue();

        if (onTotalChanged != null) {
            onTotalChanged.accept(totalPay.doubleValue());
        }
    }

    public void rebindTo(JTable table,
            JLabel lbSubtotal, JLabel lbTotalPay,
            JLabel lbChangeDue, JTextField txtCashReceived,
            DoubleConsumer onTotalChanged) {
        this.table = table;
        this.lbSubtotal = lbSubtotal;
        this.lbTotalPay = lbTotalPay;
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
            if (lbTotalPay == null) {
                return;
            }
            double totalPay = parseMoney(lbTotalPay.getText());

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
                lbChangeDue.setText("Số tiền lỗi!");
                lbChangeDue.setForeground(Color.RED);
            }
        }
    }

    private static String formatMoney(BigDecimal amount) {
        return String.format("%,.0f đ", amount);
    }

    private static String formatMoney(double amount) {
        return String.format("%,.0f đ", amount);
    }

    private static double parseMoney(String text) {
        String clean = text.replace("đ", "").replace(",", "").trim();
        return Double.parseDouble(clean);
    }
}

package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.*;

public class HistoryActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    public interface ViewAction {

        void onView(int modelRow);
    }

    public interface CancelAction {

        void onCancel(int modelRow);
    }

    public interface ExportAction {

        void onExport(int modelRow);
    }

    private final ViewAction onView;
    private final CancelAction onCancel;
    private final ExportAction onExport;

    // Loại bỏ biến instance currentModelRow để tránh lỗi đồng bộ index
    private JTable currentTable;

    private static final Color BTN_VIEW_FG = new Color(0xC47529);
    private static final Color BTN_VIEW_BG = new Color(0xFFF7ED);
    private static final Color BTN_VIEW_BORDER = new Color(0xFBBF79);
    private static final Color BTN_CANCEL_FG = new Color(0xDC2626);
    private static final Color BTN_CANCEL_BG = new Color(0xFFF1F1);
    private static final Color BTN_CANCEL_BORDER = new Color(0xFCA5A5);
    private static final Color BTN_EXPORT_FG = new Color(0x0F766E);
    private static final Color BTN_EXPORT_BG = new Color(0xF0FDFA);
    private static final Color BTN_EXPORT_BORDER = new Color(0x99F6E4);

    // Khởi tạo cố định các thành phần giao diện để dùng lại (Reusability)
    private final JPanel panel;
    private final JButton btnView;
    private final JButton btnCancel;
    private final JButton btnExport;
    private final Component horizontalStrut;

    public HistoryActionCellEditor(ExportAction onExport, ViewAction onView, CancelAction onCancel) {
        this.onExport = onExport;
        this.onView = onView;
        this.onCancel = onCancel;

        // 1. Khởi tạo cấu trúc Panel cố định một lần duy nhất
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        panel.setOpaque(true);

        // 2. Khởi tạo các nút và khoảng cách tay (Strut)
        btnExport = makeBtn("⤓ Export", BTN_EXPORT_FG, BTN_EXPORT_BG, BTN_EXPORT_BORDER);
        btnView = makeBtn("⊙ View", BTN_VIEW_FG, BTN_VIEW_BG, BTN_VIEW_BORDER);
        btnCancel = makeBtn("✕ Cancel", BTN_CANCEL_FG, BTN_CANCEL_BG, BTN_CANCEL_BORDER);
        horizontalStrut = Box.createHorizontalStrut(8);

        // 3. Gắn Listener cố định - Lấy chỉ số dòng thời gian thực bằng getEditingRow()
        btnExport.addActionListener(e -> {
            if (currentTable != null) {
                int editingRow = currentTable.getEditingRow();
                stopCellEditing();
                if (editingRow != -1 && onExport != null) {
                    int modelRow = currentTable.convertRowIndexToModel(editingRow);
                    onExport.onExport(modelRow);
                }
            }
        });

        btnView.addActionListener(e -> {
            if (currentTable != null) {
                int editingRow = currentTable.getEditingRow();
                stopCellEditing();
                if (editingRow != -1 && onView != null) {
                    int modelRow = currentTable.convertRowIndexToModel(editingRow);
                    onView.onView(modelRow);
                }
            }
        });

        btnCancel.addActionListener(e -> {
            if (currentTable != null) {
                int editingRow = currentTable.getEditingRow();
                stopCellEditing();
                if (editingRow != -1 && onCancel != null) {
                    int modelRow = currentTable.convertRowIndexToModel(editingRow);
                    onCancel.onCancel(modelRow);
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value,
            boolean isSelected, int row, int column) {

        this.currentTable = table; // Lưu tham chiếu bàn đang tương tác hiện tại

        // Xác định dòng thực tế trong Model để lấy dữ liệu cột trạng thái (Status)
        int modelRow = table.convertRowIndexToModel(row);
        String status = "";
        int statusCol = 6;
        if (table.getModel().getColumnCount() > statusCol) {
            Object sv = table.getModel().getValueAt(modelRow, statusCol);
            if (sv != null) {
                status = sv.toString().trim(); // Thêm .trim() để phòng lỗi khoảng trắng dữ liệu
            }
        }

        // Đồng bộ màu nền của Editor theo trạng thái hàng
        panel.setBackground(isSelected ? new Color(0xF8F6F2) : table.getBackground());

        // Xóa các component cũ ra khỏi layout động và nạp lại trạng thái mới phù hợp
        panel.removeAll();
        panel.add(btnExport);
        panel.add(Box.createHorizontalStrut(8));
        panel.add(btnView);

        // Kiểm tra điều kiện trạng thái để hiển thị nút Cancel và khoảng cách tay
        if ("PENDING".equalsIgnoreCase(status)) {
            panel.add(horizontalStrut);
            panel.add(btnCancel);
        }

        // Yêu cầu Swing tính toán và vẽ lại chính xác cấu trúc nút
        panel.revalidate();
        panel.repaint();

        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    private JButton makeBtn(String text, Color fg, Color bg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 12, 4, 12));

        Dimension size = new Dimension(Math.max(86, btn.getPreferredSize().width), 30);
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);

        return btn;
    }
}

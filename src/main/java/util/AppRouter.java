package util;

import entity.Employee;
import views.DashBoardFrame;
import views.LoginFrame;
import views.SalesCounterFrame;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AppRouter {

    public static void showSalesCounterFrame() {
        java.awt.EventQueue.invokeLater(() -> {
            new SalesCounterFrame().setVisible(true);
        });
    }

    public static void showActualLogin() {
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }

    public static void route(JFrame currentFrame) {
        
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "Vui lòng đăng nhập trước!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            if (currentFrame != null) {
                currentFrame.dispose();
            }
            showSalesCounterFrame();
            return;
        }
        
        Employee user = session.getCurrentUser();

        if (currentFrame instanceof LoginFrame loginFrame && loginFrame.getCallerFrame() != null) {
            SalesCounterFrame salesFrame = loginFrame.getCallerFrame();

            // Trường hợp: Login từ nút "Đăng nhập" bên TRONG Dashboard (quick-login)
            // → KHÔNG tạo thêm DashBoardFrame mới, chỉ dispose LoginFrame và reload Dashboard
            views.DashBoardFrame existingDash = loginFrame.getCallerDashBoard();
            if (existingDash != null) {
                loginFrame.dispose();
                java.awt.EventQueue.invokeLater(() -> {
                    existingDash.loadCheckedInEmployees();
                    existingDash.loadAttendanceTableData();
                    existingDash.updateCashierPanel();
                    if (salesFrame != null) {
                        salesFrame.loadCheckedInEmployees();
                        salesFrame.refreshUserDropdown();
                    }
                });
                return;
            }

            // Trường hợp: Login từ SalesCounterFrame (không có Dashboard hiện tại)
            loginFrame.dispose();
            if (user != null && user.getRoleId() == 1) {
                java.awt.EventQueue.invokeLater(() -> {
                    DashBoardFrame db = new DashBoardFrame(salesFrame);
                    db.setVisible(true);
                    salesFrame.setVisible(false);
                });
            } else {
                java.awt.EventQueue.invokeLater(() -> {
                    salesFrame.loadCheckedInEmployees();
                    salesFrame.refreshUserDropdown();
                    salesFrame.setVisible(true);
                });
            }
            return;
        }

        if (currentFrame != null) {
            currentFrame.dispose();
        }

        java.awt.EventQueue.invokeLater(() -> {
            SalesCounterFrame salesFrame = new SalesCounterFrame();
            if (user != null && user.getRoleId() == 1) {
                DashBoardFrame db = new DashBoardFrame(salesFrame);
                db.setVisible(true);
            } else {
                salesFrame.loadCheckedInEmployees();
                salesFrame.refreshUserDropdown();
                salesFrame.setVisible(true);
            }
        });
    }

    public static boolean checkAccess(JFrame frame) {
        
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(frame, "Bạn chưa đăng nhập! Vui lòng đăng nhập để tiếp tục.", "Lỗi truy cập", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            showSalesCounterFrame();
            return false;
        }

        Employee user = session.getCurrentUser();
        if (frame instanceof DashBoardFrame) {
            if (user.getRoleId() != 1) {
                JOptionPane.showMessageDialog(frame, "Chỉ Manager mới được phép truy cập trang quản lý!", "Từ chối truy cập", JOptionPane.ERROR_MESSAGE);
                frame.dispose();
                java.awt.EventQueue.invokeLater(() -> {
                    SalesCounterFrame salesFrame = new SalesCounterFrame();
//                    salesFrame.setManagerButtonVisible(false);
//                    salesFrame.hideNonManagerMenus();
                    salesFrame.setVisible(true);
                });
                return false;
            }
        }
        return true; 
    }
}
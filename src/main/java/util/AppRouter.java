package util;

import entity.Employee;
import views.DashBoardFrame;
import views.LoginFrame;
import views.SalesCounterFrame;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class AppRouter {

    public static void showLogin() {
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
            showLogin();
            return;
        }
        

        if (currentFrame != null) {
            currentFrame.dispose();
        }

        Employee user = session.getCurrentUser();
        java.awt.EventQueue.invokeLater(() -> {
            SalesCounterFrame salesFrame = new SalesCounterFrame();
            if (user != null && user.getRoleId() == 1) {
                DashBoardFrame db = new DashBoardFrame(salesFrame);
                db.setVisible(true);
            } else {
                salesFrame.setVisible(true);
            }
        });
    }

    public static boolean checkAccess(JFrame frame) {
        
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            JOptionPane.showMessageDialog(frame, "Bạn chưa đăng nhập! Vui lòng đăng nhập để tiếp tục.", "Lỗi truy cập", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            showLogin();
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
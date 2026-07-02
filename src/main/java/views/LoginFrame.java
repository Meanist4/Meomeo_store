/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package views;

import com.formdev.flatlaf.FlatClientProperties;

/**
 *
 * @author admin
 */
public class LoginFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginFrame.class.getName());
    private final service.EmployeeService employeeService = new service.impl.EmployeeServiceImpl();

    private enum ScreenState {
        LOGIN,
        FORGOT_REQUEST_OTP,
        FORGOT_RESET_PASSWORD
    }
    private ScreenState screenState = ScreenState.LOGIN;
    private String sentOtp;
    private String targetUsername;
    private String targetPhone;

    /**
     * Creates new form LoginFrame
     */
    public LoginFrame() {
        initComponents();
        applyAppearance();
        setupEvents();
    }

    private void applyAppearance() {
        getContentPane().setBackground(new java.awt.Color(244, 246, 248));
        
        setSize(480, 480);
        setLocationRelativeTo(null);
        setTitle("Đăng nhập - Meomeo Store");

        txtUsername.putClientProperty(FlatClientProperties.STYLE, 
                "arc: 12; margin: 4,10,4,10; focusWidth: 2;");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập username của bạn");
        
        txtPassword.putClientProperty(FlatClientProperties.STYLE, 
                "arc: 12; margin: 4,10,4,10; focusWidth: 2;");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu của bạn");
        txtPassword.setText("");

        jButton1.setText("Đăng nhập");
        jButton1.setBackground(new java.awt.Color(227, 138, 69));
        jButton1.setForeground(java.awt.Color.WHITE);
        jButton1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        jButton1.setFocusPainted(false);
        jButton1.setBorderPainted(false);
        jButton1.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 12; borderWidth: 0; focusWidth: 0;");

        jLabel4.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 12));
        jLabel4.setForeground(new java.awt.Color(100, 116, 139));
        jLabel4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel3.setText("MEOMEO STORE");
        jLabel3.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        jLabel3.setForeground(new java.awt.Color(227, 138, 69));
        
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabel2.setForeground(new java.awt.Color(74, 85, 104));
        jLabel2.setText("Tên đăng nhập");
        
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabel1.setForeground(new java.awt.Color(74, 85, 104));
        jLabel1.setText("Mật khẩu");
    }

    private void setupEvents() {
        jButton1.addActionListener(e -> performAction());
        txtPassword.addActionListener(e -> performAction());
        txtUsername.addActionListener(e -> performAction());
        
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (screenState == ScreenState.LOGIN) {
                    switchState(ScreenState.FORGOT_REQUEST_OTP);
                } else if (screenState == ScreenState.FORGOT_REQUEST_OTP) {
                    switchState(ScreenState.LOGIN);
                } else if (screenState == ScreenState.FORGOT_RESET_PASSWORD) {
                    switchState(ScreenState.FORGOT_REQUEST_OTP);
                }
            }
        });
    }

    private void performAction() {
        if (screenState == ScreenState.LOGIN) {
            performLogin();
        } else if (screenState == ScreenState.FORGOT_REQUEST_OTP) {
            performRequestOtp();
        } else if (screenState == ScreenState.FORGOT_RESET_PASSWORD) {
            performResetPassword();
        }
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập Username!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập Password!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        entity.Employee emp = employeeService.login(username, password);
        if (emp != null) {
            util.UserSession.getInstance().setCurrentUser(emp);
            util.UserSession.getInstance().setToken(java.util.UUID.randomUUID().toString());
            
            javax.swing.JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Chào mừng " + emp.getFullName() + ".", "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            util.AppRouter.route(this);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Username hoặc Password không đúng!", "Đăng nhập thất bại", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRequestOtp() {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập Username!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        repository.EmployeeRepository empRepo = new repository.EmployeeRepository();
        entity.Employee foundEmp = empRepo.findByUsername(username);

        if (foundEmp == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản với Username này!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        String phone = foundEmp.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Tài khoản này chưa đăng ký số điện thoại trên hệ thống!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.targetUsername = username;
        this.targetPhone = phone;
        this.sentOtp = generateOtpCode();

        boolean success = util.SmsService.sendOtp(phone, sentOtp);
        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                    "Mã OTP đã được gửi tới số điện thoại: " + maskPhoneNumber(phone) + "\nVui lòng kiểm tra tin nhắn!", 
                    "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            switchState(ScreenState.FORGOT_RESET_PASSWORD);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Gửi mã OTP thất bại! Vui lòng thử lại.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performResetPassword() {
        String inputOtp = txtUsername.getText().trim();
        String newPassword = new String(txtPassword.getPassword());

        if (inputOtp.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập mã OTP!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu mới!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) {
            javax.swing.JOptionPane.showMessageDialog(this, "Mật khẩu phải chứa ít nhất 6 ký tự!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!inputOtp.equals(sentOtp)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Mã OTP không chính xác!", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashedPassword = util.PasswordEncryptionPlugin.hashPassword(newPassword);
        repository.EmployeeRepository empRepo = new repository.EmployeeRepository();
        boolean success = empRepo.updatePassword(targetUsername, hashedPassword);

        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công! Hãy đăng nhập lại bằng mật khẩu mới.", "Thành công", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            switchState(ScreenState.LOGIN);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Cập nhật mật khẩu thất bại! Vui lòng thử lại.", "Lỗi", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchState(ScreenState state) {
        this.screenState = state;
        txtUsername.setText("");
        txtPassword.setText("");
        
        if (state == ScreenState.LOGIN) {
            jLabel3.setText("MEOMEO STORE");
            jLabel2.setText("Tên đăng nhập");
            jLabel1.setText("Mật khẩu");
            jLabel1.setVisible(true);
            txtPassword.setVisible(true);
            jButton1.setText("Đăng nhập");
            jLabel4.setText("Forgot Password ?");
            txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập username của bạn");
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu của bạn");
        } 
        else if (state == ScreenState.FORGOT_REQUEST_OTP) {
            jLabel3.setText("QUÊN MẬT KHẨU");
            jLabel2.setText("Tên đăng nhập");
            jLabel1.setVisible(false);
            txtPassword.setVisible(false);
            jButton1.setText("Gửi mã OTP");
            jLabel4.setText("Quay lại Đăng nhập");
            txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập username để lấy mã OTP");
        } 
        else if (state == ScreenState.FORGOT_RESET_PASSWORD) {
            jLabel3.setText("ĐẶT LẠI MẬT KHẨU");
            jLabel2.setText("Mã xác thực OTP (6 số)");
            jLabel1.setText("Mật khẩu mới");
            jLabel1.setVisible(true);
            txtPassword.setVisible(true);
            jButton1.setText("Xác nhận đổi mật khẩu");
            jLabel4.setText("Gửi lại mã OTP");
            txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mã OTP đã nhận");
            txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập mật khẩu mới");
        }
        
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    private String generateOtpCode() {
        java.util.Random rand = new java.util.Random();
        int num = rand.nextInt(900000) + 100000;
        return String.valueOf(num);
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() <= 4) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtUsername = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Password");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Username");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setText("Login");

        jLabel4.setText("Forgot Password ?");

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setText("Login");

        txtPassword.setText("jPasswordField1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addGap(28, 28, 28)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                    .addComponent(txtPassword)))
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(80, 80, 80))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(166, 166, 166))))
            .addGroup(layout.createSequentialGroup()
                .addGap(172, 172, 172)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(190, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel3)
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(90, 90, 90)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(20, 20, 20)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(84, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}

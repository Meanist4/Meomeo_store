package views;

import com.formdev.flatlaf.FlatClientProperties;
import entity.Employee;
import entity.Role;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JOptionPane;
import service.impl.EmployeeServiceImpl;

public class AddEmployeeFrame extends javax.swing.JFrame {

    private final EmployeeServiceImpl empSrv = new EmployeeServiceImpl();
    private Runnable onClose;
    private Employee editingEmployee;

    public AddEmployeeFrame(Runnable onClose) {
        initFrame(onClose);
    }

    public AddEmployeeFrame(int employeeId, Runnable onClose) {
        initFrame(onClose);
        Employee emp = empSrv.getEmployeeById(employeeId);
        if (emp == null) {
            showError("Không tìm thấy nhân viên.");
            javax.swing.SwingUtilities.invokeLater(this::dispose);
            return;
        }
        editingEmployee = emp;
        configureEditMode();
        fillFormForEdit(emp);
    }

    private void initFrame(Runnable onClose) {
        initComponents();
        this.onClose = onClose;

        setTitle("Thêm nhân viên mới");
        setSize(790, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        applyAppearance();
        loadRoles();
        setupEvents();
    }

    private void configureEditMode() {
        setTitle("Cập nhật nhân viên");
        jLabel1.setText("Edit Employee");
        jLabel3.setText("Update employee information");

        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(240, 240, 240));

        Password.setVisible(false);
        jLabel7.setVisible(false);
        txtPassword.setVisible(false);
        txtConfirmPassword.setVisible(false);
    }

    private void fillFormForEdit(Employee emp) {
        txtFullName.setText(emp.getFullName() != null ? emp.getFullName() : "");
        txtPhone.setText(emp.getPhone() != null ? emp.getPhone() : "");
        txtUsername.setText(emp.getUsername() != null ? emp.getUsername() : "");
        selectRoleById(emp.getRoleId());
    }

    private void selectRoleById(int roleId) {
        for (int i = 0; i < cbRole.getItemCount(); i++) {
            Role role = cbRole.getItemAt(i);
            if (role.getId() == roleId) {
                cbRole.setSelectedIndex(i);
                return;
            }
        }
    }

    private void applyAppearance() {
        getContentPane().setBackground(new Color(244, 246, 248));
        jPanel1.setBackground(Color.WHITE);
        jPanel1.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        btnBackToDashBoard.setText("Back");
        btnBackToDashBoard.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");

        btnReset.setText("Reset");
        btnReset.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");

        btnSaveEmployee.setText("Save employee");
        btnSaveEmployee.setBackground(new Color(227, 138, 69));
        btnSaveEmployee.setForeground(Color.WHITE);
        btnSaveEmployee.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSaveEmployee.setFocusPainted(false);
        btnSaveEmployee.setBorderPainted(false);
        btnSaveEmployee.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 8; borderWidth: 0; focusWidth: 0;");

        txtPassword.setText("");
        txtConfirmPassword.setText("");
    }

    private void setupEvents() {
        btnBackToDashBoard.addActionListener(e -> dispose());
        btnReset.addActionListener(e -> resetForm());
        btnSaveEmployee.addActionListener(e -> saveEmployee());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
            }
        });
    }

    public void loadRoles() {
        cbRole.removeAllItems();
        for (Role role : empSrv.getAllRole()) {
            cbRole.addItem(role);
        }
    }

    private void resetForm() {
        if (editingEmployee != null) {
            fillFormForEdit(editingEmployee);
            return;
        }
        txtFullName.setText("");
        txtPhone.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
        if (cbRole.getItemCount() > 0) {
            cbRole.setSelectedIndex(0);
        }
    }

    private void saveEmployee() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ tên!");
            return;
        }
        if (phone.isEmpty()) {
            showError("Vui lòng nhập số điện thoại!");
            return;
        }
        if (cbRole.getSelectedItem() == null) {
            showError("Vui lòng chọn vai trò!");
            return;
        }

        Role selectedRole = (Role) cbRole.getSelectedItem();

        if (editingEmployee != null) {
            boolean updated = empSrv.updateEmployeeInfo(
                    editingEmployee.getId(), fullName, selectedRole.getId(), phone);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                dispose();
            } else {
                showError("Không thể cập nhật nhân viên. Vui lòng thử lại.");
            }
            return;
        }

        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (username.isEmpty()) {
            showError("Vui lòng nhập username!");
            return;
        }
        if (password.isEmpty()) {
            showError("Vui lòng nhập mật khẩu!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp!");
            return;
        }

        boolean isAdded = empSrv.addEmployee(fullName, selectedRole.getId(), phone, username, password);
        if (isAdded) {
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
            dispose();
        } else {
            showError("Không thể thêm nhân viên. Vui lòng thử lại.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBackToDashBoard = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        txtUsername = new javax.swing.JTextField();
        phoneLabel = new javax.swing.JLabel();
        cbRole = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        Password = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        txtPassword = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        btnReset = new javax.swing.JButton();
        btnSaveEmployee = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(244, 246, 248));

        btnBackToDashBoard.setText("Back");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Add New Employee");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(102, 102, 102));
        jLabel3.setText("Manage your pet shop employee");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtUsername.setPreferredSize(new java.awt.Dimension(64, 31));

        phoneLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        phoneLabel.setText("Phone");

        cbRole.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Full Name");

        txtPhone.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtPhone.setPreferredSize(new java.awt.Dimension(64, 31));

        Password.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Password.setText("Password");

        txtFullName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtFullName.setPreferredSize(new java.awt.Dimension(64, 31));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Username");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Confirm Password");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Role");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap(23, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24,
                                                        Short.MAX_VALUE)
                                                .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 275,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout
                                                .createSequentialGroup()
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(phoneLabel)
                                                        .addComponent(jLabel2))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                275, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(55, 55, 55)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(Password,
                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(jLabel6,
                                                                javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addGap(18, 18, 18))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel7)
                                                        .addGap(10, 10, 10)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 275,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 275,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 275,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(73, 73, 73)));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6)
                                        .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4))
                                .addGap(61, 61, 61)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Password)
                                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(phoneLabel))
                                .addGap(47, 47, 47)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 33,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel7)
                                        .addGroup(jPanel1Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(cbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel2)))
                                .addContainerGap(80, Short.MAX_VALUE)));

        btnReset.setText("Reset");
        btnSaveEmployee.setText("Save employee");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(btnBackToDashBoard)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 81,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(12, 12, 12)
                                                .addComponent(btnSaveEmployee, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGap(34, 34, 34)
                                                        .addGroup(layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(jLabel3)
                                                                .addComponent(jLabel1)))
                                                .addGroup(layout.createSequentialGroup()
                                                        .addGap(24, 24, 24)
                                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap(34, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnSaveEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnBackToDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(34, Short.MAX_VALUE)));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> new AddEmployeeFrame(null).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Password;
    private javax.swing.JButton btnBackToDashBoard;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveEmployee;
    private javax.swing.JComboBox<Role> cbRole;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}

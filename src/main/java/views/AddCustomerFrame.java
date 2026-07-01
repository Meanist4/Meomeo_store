package views;

import entity.Customer;
import javax.swing.JOptionPane;
import service.CustomerService;
import service.impl.CustomerServiceImpl;
import com.formdev.flatlaf.FlatClientProperties;

public class AddCustomerFrame extends javax.swing.JFrame {

    private CustomerServiceImpl customerService = new CustomerServiceImpl();
    private Runnable onClose;
    private Customer editCustomer;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AddCustomerFrame.class.getName());

    public AddCustomerFrame(Runnable onClose) {
        initFrame(onClose);
    }

    public AddCustomerFrame(int customerId, Runnable onClose) {
        initFrame(onClose);
        Customer ctm = customerService.getCustomerById(customerId);
        if (ctm == null) {
            showError("Không tìm thấy khách hàng.");
            javax.swing.SwingUtilities.invokeLater(this::dispose);
            return;
        }
        editCustomer = ctm;
        configureEditMode();
        fillFormForEdit(ctm);
    }

    private void fillFormForEdit(Customer ctm) {
        txtFullname.setText(ctm.getFullName() != null ? ctm.getFullName() : "");
        txtPhone.setText(ctm.getPhone() != null ? ctm.getPhone() : "");
    }

    private void configureEditMode() {
        jLabel4.setText("Edit Customer");
        btnAdd.setText("Update");
    }

    private void saveCustomer() {
        String fullName = txtFullname.getText().trim();
        String phone = txtPhone.getText().trim();

        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ tên!");
            return;
        }
        if (phone.isEmpty()) {
            showError("Vui lòng nhập số điện thoại!");
            return;
        }

        if (editCustomer != null) {
            Customer cus = new Customer(editCustomer.getId(), fullName, phone, false);
            boolean updated = customerService.updateCustomer(cus);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!");
                dispose();
            } else {
                showError("Không thể cập nhật khách hàng. Vui lòng thử lại.");
            }
            return; // Ngăn chặn chạy xuống phần logic tạo mới phía dưới
        }
        Customer cus = new Customer(0, fullName, phone, false);
        boolean ins = customerService.addCustomer(cus);
        if (ins) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            dispose();
        } else {
            showError("Không thể thêm khách hàng. Vui lòng thử lại.");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void setupEvents() {
        btnBack.addActionListener(e -> dispose());
        btnAdd.addActionListener(e -> saveCustomer()); // Kích hoạt hàm xử lý rẽ nhánh

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
            }
        });
    }

    private void initFrame(Runnable onClose) {
        initComponents();
        this.onClose = onClose;
        setTitle("Thêm khách hàng mới");
        setSize(340, 360);
        setLocationRelativeTo(null);
        applyAppearance();
        setupEvents();
    }

    private void applyAppearance() {
        getContentPane().setBackground(new java.awt.Color(244, 246, 248));
        
        // Buttons
        btnBack.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");
        btnAdd.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 8; borderWidth: 0; focusWidth: 0;");
        btnAdd.setBackground(new java.awt.Color(227, 138, 69));
        btnAdd.setForeground(java.awt.Color.WHITE);
        btnAdd.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));

        // Text fields
        txtFullname.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtPhone.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        
        // Font customization
        jLabel4.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        jLabel4.setForeground(new java.awt.Color(30, 41, 59));
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabel1.setForeground(new java.awt.Color(74, 85, 104));
        jLabel2.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        jLabel2.setForeground(new java.awt.Color(74, 85, 104));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnAdd = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        txtFullname = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnAdd.setText("Add");
        btnAdd.addActionListener(this::btnAddActionPerformed);

        btnBack.setText("Back");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Full Name");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel2.setText("Phone Number");

        txtPhone.addActionListener(this::txtPhoneActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel4.setText("Add Customer");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBack)
                .addGap(29, 29, 29)
                .addComponent(btnAdd)
                .addGap(35, 35, 35))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFullname)
                            .addComponent(jLabel2)
                            .addComponent(txtPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(181, 181, 181)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(62, 62, 62)
                        .addComponent(jLabel4)))
                .addContainerGap(70, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addGap(13, 13, 13)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFullname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnBack))
                .addGap(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPhoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPhoneActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AddCustomerFrame(null).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField txtFullname;
    private javax.swing.JTextField txtPhone;
    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (!util.UserSession.getInstance().isLoggedIn()) {
                super.setVisible(false);
                util.AppRouter.showLogin();
                this.dispose();
                return;
            }
        }
        super.setVisible(b);
    }

    // End of variables declaration//GEN-END:variables
}

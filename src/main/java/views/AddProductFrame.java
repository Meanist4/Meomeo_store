package views;

public class AddProductFrame extends javax.swing.JFrame {

    private Runnable onClose;

    public AddProductFrame(Runnable onClose) {
        initComponents();

        this.onClose = onClose;

        setTitle("Thêm sản phẩm mới");
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onClose != null) {
                    onClose.run(); // Gọi hàm load lại bảng bên Dashboard
                }
            }
        });
        btnBackToDashBoard.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackToDashBoardActionPerformed(evt);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBackToDashBoard = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        btnBackToDashBoard.setText("Back");
        btnBackToDashBoard.addActionListener(this::btnBackToDashBoardActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(560, Short.MAX_VALUE)
                .addComponent(btnBackToDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBackToDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(608, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackToDashBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashBoardActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnBackToDashBoardActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new AddProductFrame(() -> {
                System.out.println("Dashboard đã được reload dữ liệu thành công!");
            }).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashBoard;
    // End of variables declaration//GEN-END:variables
}

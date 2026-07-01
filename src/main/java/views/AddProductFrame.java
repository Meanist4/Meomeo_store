package views;

import com.formdev.flatlaf.FlatClientProperties;

import entity.Category;
import entity.Product;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import repository.CategoryRepository;
import repository.ProductRepository;

public class AddProductFrame extends javax.swing.JFrame {

    private String currentImagePath = null;
    private final List<Category> categoryList = new ArrayList<>();
    private boolean isExistingProduct = false;
    private Product editingProduct = null;
    private final ProductRepository productRepository = new ProductRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();

    public AddProductFrame(Runnable onClose) {
        initComponents();
        applyAppearance();

        this.editingProduct = null;
        setSize(790, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        lblImagePlaceHolder.setText("No image uploaded");
        lblImagePlaceHolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblImagePlaceHolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblImagePlaceHolder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        loadCategories();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
            }
        });
    }

    public AddProductFrame(Product product, Runnable onClose) {
        initComponents();
        applyAppearance();

        this.editingProduct = product;

        setTitle("Thêm sản phẩm mới");
        setSize(790, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        lblImagePlaceHolder.setText("No image uploaded");
        lblImagePlaceHolder.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblImagePlaceHolder.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        lblImagePlaceHolder.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        loadCategories();
        fillFormForEdit(product);

        txtBarcode.setEditable(false);
        txtBarcode.setBackground(new java.awt.Color(240, 240, 240));
        
        txtQuantity.setVisible(false);
        lblQuantity.setVisible(false);

        btnScanBarcode.setVisible(false);

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

    private void fillFormForEdit(Product p) {
        txtBarcode.setText(p.getBarcode() != null ? p.getBarcode() : "");
        txtProductName.setText(p.getProductName());
        txtImportPrice.setText(p.getImportPrice() != null
                ? p.getImportPrice().toPlainString() : "");
        txtSellingPrice.setText(p.getPrice().toPlainString());
        txtQuantity.setText(String.valueOf(p.getQuantity()));
        txtUnit.setText(p.getUnit() != null ? p.getUnit() : "");
        txtDescription.setText(p.getDescription() != null ? p.getDescription() : "");

        // Category
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == p.getCategoryId()) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }

        currentImagePath = p.getImagePath();
        if (currentImagePath != null && !currentImagePath.isBlank()) {
            java.net.URL imgUrl = getClass().getResource("/images/" + currentImagePath);
            if (imgUrl != null) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(
                        new javax.swing.ImageIcon(imgUrl).getImage()
                                .getScaledInstance(lblImagePlaceHolder.getWidth(),
                                        lblImagePlaceHolder.getHeight(),
                                        java.awt.Image.SCALE_SMOOTH));
                lblImagePlaceHolder.setIcon(icon);
                lblImagePlaceHolder.setText("");
            }
        }
    }

    private void applyAppearance() {
        getContentPane().setBackground(new java.awt.Color(244, 246, 248));
        jPanel1.setBackground(java.awt.Color.WHITE);
        jPanel1.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");

        // Styling text fields
        txtBarcode.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtProductName.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtImportPrice.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtSellingPrice.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtQuantity.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtUnit.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");
        txtDescription.putClientProperty(FlatClientProperties.STYLE, "arc: 8; margin: 4,6,4,6;");

        // Styling ComboBoxes
        cbCategory.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        
        // Buttons
        btnBackToDashBoard.setText("Back");
        btnBackToDashBoard.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");
        
        btnReset.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");
                
        btnFindImage.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");

        btnScanBarcode.putClientProperty(FlatClientProperties.STYLE,
                "background: #FFFFFF; foreground: #4A5568; arc: 8; borderWidth: 1; focusWidth: 0;");
                
        btnSaveProduct.setBackground(new java.awt.Color(227, 138, 69));
        btnSaveProduct.setForeground(java.awt.Color.WHITE);
        btnSaveProduct.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        btnSaveProduct.setFocusPainted(false);
        btnSaveProduct.setBorderPainted(false);
        btnSaveProduct.putClientProperty(FlatClientProperties.STYLE,
                "background: #E38A45; foreground: #FFFFFF; arc: 8; borderWidth: 0; focusWidth: 0;");
                
        // Label header style
        jLabel1.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        jLabel1.setForeground(new java.awt.Color(30, 41, 59));
        
        // Placeholders
        lblImagePlaceHolder.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 1; borderColor: #CBD5E1;");
    }

    private void loadCategories() {
        cbCategory.removeAllItems();
        try {
            var categories = categoryRepository.getAll(); // trả về List<Category>
            for (var cat : categories) {
                cbCategory.addItem(cat.getCategoryName()); // tuỳ getter của bạn
            }
        } catch (Exception e) {
            cbCategory.addItem("(Lỗi tải danh mục)");
        }
    }

    private int getSelectedCategoryId() {
        String selectedName = (String) cbCategory.getSelectedItem();
        if (selectedName == null) {
            return -1;
        }
        try {
            var categories = categoryRepository.getAll();
            for (var cat : categories) {
                if (cat.getCategoryName().equals(selectedName)) {
                    return cat.getId();
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox<>();
        btnBackToDashBoard = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtProductName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cbCategory = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtImportPrice = new javax.swing.JTextField();
        txtSellingPrice = new javax.swing.JTextField();
        lblQuantity = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        btnFindImage = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        btnScanBarcode = new javax.swing.JButton();
        lblImagePlaceHolder = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();
        txtUnit = new javax.swing.JTextField();
        btnReset = new javax.swing.JButton();
        btnSaveProduct = new javax.swing.JButton();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(244, 246, 248));

        btnBackToDashBoard.setText("Back");
        btnBackToDashBoard.addActionListener(this::btnBackToDashBoardActionPerformed);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Add New Product");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(102, 102, 102));
        jLabel8.setText("Manage your pet shop inventory");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Product Image");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Barcode");

        txtBarcode.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtBarcode.addActionListener(this::txtBarcodeActionPerformed);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Product name");

        txtProductName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtProductName.setPreferredSize(new java.awt.Dimension(64, 31));
        txtProductName.addActionListener(this::txtProductNameActionPerformed);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Category");

        cbCategory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        cbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbCategory.addActionListener(this::cbCategoryActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Unit");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Import price");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Selling price");

        txtImportPrice.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtImportPrice.addActionListener(this::txtImportPriceActionPerformed);

        txtSellingPrice.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtSellingPrice.addActionListener(this::txtSellingPriceActionPerformed);

        lblQuantity.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblQuantity.setText("Quantity");

        txtQuantity.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txtQuantity.addActionListener(this::txtQuantityActionPerformed);

        btnFindImage.setText("Browse Image");
        btnFindImage.addActionListener(this::btnFindImageActionPerformed);

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Description");

        btnScanBarcode.addActionListener(this::btnScanBarcodeActionPerformed);

        txtDescription.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtDescription.addActionListener(this::txtDescriptionActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(btnFindImage, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblImagePlaceHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnScanBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(txtUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(txtProductName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtDescription, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblQuantity, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtQuantity, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(txtImportPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(56, 56, 56)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(txtSellingPrice))))
                        .addGap(43, 43, 43))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(lblImagePlaceHolder, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFindImage, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(156, 156, 156))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnScanBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtBarcode, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
                .addGap(12, 12, 12)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtProductName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUnit)
                    .addComponent(cbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9))
                .addGap(7, 7, 7)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSellingPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtImportPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblQuantity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(107, 107, 107))
        );

        btnReset.setText("Reset");
        btnReset.addActionListener(this::btnResetActionPerformed);

        btnSaveProduct.setText("Save product");
        btnSaveProduct.addActionListener(this::btnSaveProductActionPerformed);

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
                        .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnSaveProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(34, 34, 34)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel8)
                                .addComponent(jLabel1)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBackToDashBoard, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackToDashBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackToDashBoardActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnBackToDashBoardActionPerformed

    private void txtProductNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProductNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtProductNameActionPerformed

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        if (editingProduct != null) {
            fillFormForEdit(editingProduct);
        } else {
            resetForm();
        }
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnSaveProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveProductActionPerformed
        if (editingProduct != null) {
            String productName = txtProductName.getText().trim();
            String importPriceStr = txtImportPrice.getText().trim();
            String sellingPriceStr = txtSellingPrice.getText().trim();
            String quantityStr = txtQuantity.getText().trim();
            String unit = txtUnit.getText().trim();
            String description = txtDescription.getText().trim();
            int categoryId = getSelectedCategoryId();

            if (productName.isEmpty()) {
                showError("Vui lòng nhập tên sản phẩm!");
                return;
            }
            if (sellingPriceStr.isEmpty()) {
                showError("Vui lòng nhập giá bán!");
                return;
            }
            if (quantityStr.isEmpty()) {
                showError("Vui lòng nhập số lượng!");
                return;
            }
            if (categoryId == -1) {
                showError("Vui lòng chọn danh mục!");
                return;
            }

            BigDecimal importPrice, sellingPrice;
            int quantity;

            try {
                importPrice = importPriceStr.isEmpty() ? BigDecimal.ZERO
                        : new BigDecimal(importPriceStr.replace(",", "."));
            } catch (NumberFormatException e) {
                showError("Giá nhập không hợp lệ!");
                return;
            }

            try {
                sellingPrice = new BigDecimal(sellingPriceStr.replace(",", "."));
                if (sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Giá bán phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Giá bán không hợp lệ!");
                return;
            }

            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    showError("Số lượng không được âm!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Số lượng không hợp lệ!");
                return;
            }

            editingProduct.setCategoryId(categoryId);
            editingProduct.setProductName(productName);
            editingProduct.setDescription(description.isEmpty() ? null : description);
            editingProduct.setUnit(unit.isEmpty() ? null : unit);
            editingProduct.setImportPrice(importPrice);
            editingProduct.setPrice(sellingPrice);
            editingProduct.setQuantity(quantity);
            editingProduct.setImagePath(currentImagePath);

            boolean success = productRepository.update(editingProduct);
            if (success) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Cập nhật sản phẩm thành công!", "Thành công",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                showError("Cập nhật thất bại! Vui lòng thử lại.");
            }
            return; // không chạy xuống luồng Add
        }
        if (isExistingProduct) {
            String quantityStr = txtQuantity.getText().trim();

            if (quantityStr.isEmpty()) {
                showError("Vui lòng nhập số lượng cần thêm!");
                txtQuantity.requestFocus();
                return;
            }

            int addQty;
            try {
                addQty = Integer.parseInt(quantityStr);
                if (addQty <= 0) {
                    showError("Số lượng phải lớn hơn 0!");
                    txtQuantity.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Số lượng không hợp lệ! Vui lòng nhập số nguyên.");
                txtQuantity.requestFocus();
                return;
            }

            String barcode = txtBarcode.getText().trim();
            boolean updated = productRepository.addQuantityByBarcode(barcode, addQty);

            if (updated) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Đã cộng thêm " + addQty + " vào tồn kho thành công!",
                        "Thành công",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                isExistingProduct = false;
                unlockAllFields();
                resetForm();
            } else {
                showError("Cập nhật số lượng thất bại! Vui lòng thử lại.");
            }
            return;
        }

        String productName = txtProductName.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String importPriceStr = txtImportPrice.getText().trim();
        String sellingPriceStr = txtSellingPrice.getText().trim();
        String quantityStr = txtQuantity.getText().trim();
        String description = txtDescription.getText().trim();
        String unit = txtUnit.getText().trim();
        int categoryId = getSelectedCategoryId();

        if (productName.isEmpty()) {
            showError("Vui lòng nhập tên sản phẩm!");
            txtProductName.requestFocus();
            return;
        }
        if (sellingPriceStr.isEmpty()) {
            showError("Vui lòng nhập giá bán!");
            txtSellingPrice.requestFocus();
            return;
        }
        if (quantityStr.isEmpty()) {
            showError("Vui lòng nhập số lượng!");
            txtQuantity.requestFocus();
            return;
        }
        if (categoryId == -1) {
            showError("Vui lòng chọn danh mục sản phẩm!");
            return;
        }

        BigDecimal importPrice;
        BigDecimal sellingPrice;
        int quantity;

        try {
            importPrice = importPriceStr.isEmpty()
                    ? BigDecimal.ZERO
                    : new BigDecimal(importPriceStr.replace(",", "."));
            if (importPrice.compareTo(BigDecimal.ZERO) < 0) {
                showError("Giá nhập không được âm!");
                txtImportPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Giá nhập không hợp lệ!");
            txtImportPrice.requestFocus();
            return;
        }

        try {
            sellingPrice = new BigDecimal(sellingPriceStr.replace(",", "."));
            if (sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Giá bán phải lớn hơn 0!");
                txtSellingPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Giá bán không hợp lệ!");
            txtSellingPrice.requestFocus();
            return;
        }

        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                showError("Số lượng không được âm!");
                txtQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Số lượng không hợp lệ!");
            txtQuantity.requestFocus();
            return;
        }

        if (!barcode.isEmpty() && productRepository.isBarcodeExists(barcode)) {
            showError("Barcode \"" + barcode + "\" đã tồn tại trong hệ thống!");
            txtBarcode.requestFocus();
            return;
        }

        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setProductName(productName);
        product.setDescription(description.isEmpty() ? null : description);
        product.setUnit(unit.isEmpty() ? null : unit);
        product.setImportPrice(importPrice);
        product.setPrice(sellingPrice);
        product.setBarcode(barcode.isEmpty() ? null : barcode);
        product.setQuantity(quantity);
        product.setStatus(1);
        product.setIsDeleted(0);
        product.setImagePath(currentImagePath);

        boolean success = productRepository.add(product);
        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Thêm sản phẩm \"" + productName + "\" thành công!",
                    "Thành công",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } else {
            showError("Lưu sản phẩm thất bại! Vui lòng thử lại.");
        }
    }//GEN-LAST:event_btnSaveProductActionPerformed

    private void resetForm() {
        txtProductName.setText("");
        txtBarcode.setText("");
        txtImportPrice.setText("");
        txtSellingPrice.setText("");
        txtQuantity.setText("");
        txtDescription.setText("");
        cbCategory.setSelectedIndex(0);
        txtUnit.setText("");

        // Reset ảnh
        currentImagePath = null;
        lblImagePlaceHolder.setIcon(null);
        lblImagePlaceHolder.setText("No image uploaded");
    }

    private void btnFindImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindImageActionPerformed
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("Chọn hình ảnh từ máy tính");
        javax.swing.filechooser.FileNameExtensionFilter filter
                = new javax.swing.filechooser.FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();

            try {
                java.io.File destDir = new java.io.File("src/main/resources/images");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String uniqueName = "product_" + System.currentTimeMillis() + ext;
                java.io.File destFile = new java.io.File(destDir, uniqueName);

                java.nio.file.Files.copy(
                        selectedFile.toPath(),
                        destFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                currentImagePath = uniqueName;
                javax.swing.ImageIcon rawIcon = new javax.swing.ImageIcon(destFile.getAbsolutePath());
                java.awt.Image scaledImage = rawIcon.getImage().getScaledInstance(
                        lblImagePlaceHolder.getWidth(),
                        lblImagePlaceHolder.getHeight(),
                        java.awt.Image.SCALE_SMOOTH
                );
                lblImagePlaceHolder.setIcon(new javax.swing.ImageIcon(scaledImage));
                lblImagePlaceHolder.setText("");

            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Không thể tải hình ảnh: " + ex.getMessage(), "Lỗi",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                currentImagePath = null;
            }
        }
    }//GEN-LAST:event_btnFindImageActionPerformed

    private void txtBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBarcodeActionPerformed
        String input = txtBarcode.getText().trim();
        if (input.isEmpty()) {
            return;
        }
        String proCode = util.BarcodeHashUtil.isProCode(input)
                ? input
                : util.BarcodeHashUtil.toProCode(input);

        txtBarcode.setText(proCode);

        Product existing = productRepository.findByBarcode(proCode);
        if (existing != null) {
            fillFormFromExisting(existing);
            isExistingProduct = true;
            lockFieldsForExisting();
            JOptionPane.showMessageDialog(this,
                    "Sản phẩm đã tồn tại! Chỉ có thể cập nhật Số lượng.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            isExistingProduct = false;
            unlockAllFields();
        }
    }//GEN-LAST:event_txtBarcodeActionPerformed

    private void btnScanBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScanBarcodeActionPerformed
        util.BarcodeScannerUtil.startScan(this, rawBarcode -> {
            String proCode = util.BarcodeHashUtil.toProCode(rawBarcode);
            txtBarcode.setText(proCode);

            // 2. Tìm trong DB
            Product existing = productRepository.findByBarcode(proCode);

            if (existing != null) {
                // Sản phẩm đã tồn tại → điền form + khóa field
                fillFormFromExisting(existing);
                isExistingProduct = true;
                lockFieldsForExisting();

                JOptionPane.showMessageDialog(this,
                        "Sản phẩm đã tồn tại trong hệ thống!\n"
                        + "Chỉ có thể cập nhật Số lượng.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Barcode mới → mở khóa toàn bộ form
                isExistingProduct = false;
                unlockAllFields();
            }
        });
    }//GEN-LAST:event_btnScanBarcodeActionPerformed

    private void cbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCategoryActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbCategoryActionPerformed

    private void txtImportPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImportPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtImportPriceActionPerformed

    private void txtSellingPriceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSellingPriceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSellingPriceActionPerformed

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void txtDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescriptionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescriptionActionPerformed

    private void lockFieldsForExisting() {
        txtProductName.setEditable(false);
        txtImportPrice.setEditable(false);
        txtSellingPrice.setEditable(false);
        txtDescription.setEditable(false);
        cbCategory.setEnabled(false);
        txtUnit.setEnabled(false);
        btnFindImage.setEnabled(false);

        // Chỉ cho sửa 2 trường này
        txtQuantity.setEditable(true);
        txtBarcode.setEditable(true);

        // Đổi màu để người dùng biết đang bị khóa
        Color locked = new Color(240, 240, 240);
        txtProductName.setBackground(locked);
        txtImportPrice.setBackground(locked);
        txtSellingPrice.setBackground(locked);
        txtDescription.setBackground(locked);
    }

    private void unlockAllFields() {
        txtProductName.setEditable(true);
        txtImportPrice.setEditable(true);
        txtSellingPrice.setEditable(true);
        txtDescription.setEditable(true);
        cbCategory.setEnabled(true);
        txtUnit.setEnabled(true);
        btnFindImage.setEnabled(true);
        txtQuantity.setEditable(true);
        txtBarcode.setEditable(true);

        Color normal = Color.WHITE;
        txtProductName.setBackground(normal);
        txtImportPrice.setBackground(normal);
        txtSellingPrice.setBackground(normal);
        txtDescription.setBackground(normal);
    }

    private void fillFormFromExisting(Product p) {
        txtProductName.setText(p.getProductName());
        txtImportPrice.setText(p.getImportPrice() != null ? p.getImportPrice().toPlainString() : "");
        txtSellingPrice.setText(p.getPrice().toPlainString());
        txtQuantity.setText(String.valueOf(p.getQuantity()));
        txtDescription.setText(p.getDescription() != null ? p.getDescription() : "");
        txtUnit.setText(p.getUnit() != null ? p.getUnit() : "");

        try {
            var categories = categoryRepository.getAll();
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == p.getCategoryId()) {
                    cbCategory.setSelectedIndex(i);
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        if (p.getImagePath() != null && !p.getImagePath().isBlank()) {
            currentImagePath = p.getImagePath();
            java.net.URL imgUrl = getClass().getResource("/images/" + p.getImagePath());
            if (imgUrl != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgUrl)
                        .getImage()
                        .getScaledInstance(lblImagePlaceHolder.getWidth(),
                                lblImagePlaceHolder.getHeight(),
                                java.awt.Image.SCALE_SMOOTH));
                lblImagePlaceHolder.setIcon(icon);
                lblImagePlaceHolder.setText("");
            }
        }
    }

    private void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(this, message, "Lỗi nhập liệu",
                javax.swing.JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String args[]) {
        com.formdev.flatlaf.FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            new AddProductFrame(() -> {
                System.out.println("Dashboard đã được reload dữ liệu thành công!");
            }).setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToDashBoard;
    private javax.swing.JButton btnFindImage;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSaveProduct;
    private javax.swing.JButton btnScanBarcode;
    private javax.swing.JComboBox<String> cbCategory;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblImagePlaceHolder;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtDescription;
    private javax.swing.JTextField txtImportPrice;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtSellingPrice;
    private javax.swing.JTextField txtUnit;
    @Override
    public void setVisible(boolean b) {
        if (b) {
            if (!util.UserSession.getInstance().isLoggedIn()) {
                super.setVisible(false);
                util.AppRouter.showLogin();
                this.dispose();
                return;
            }
            entity.Employee user = util.UserSession.getInstance().getCurrentUser();
            if (user == null || user.getRoleId() != 1) { // Not manager
                super.setVisible(false);
                JOptionPane.showMessageDialog(this, "Chỉ Manager mới được phép thực hiện chức năng này!", "Từ chối truy cập", JOptionPane.ERROR_MESSAGE);
                this.dispose();
                return;
            }
        }
        super.setVisible(b);
    }

    // End of variables declaration//GEN-END:variables
}

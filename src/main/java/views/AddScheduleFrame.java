package views;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import repository.EmployeeRepository;
import repository.ScheduleRepository;

public class AddScheduleFrame extends javax.swing.JFrame {

    private final Runnable onScheduleSaved;
    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private final ScheduleRepository scheduleRepository = new ScheduleRepository();
    private final List<EmployeeRepository.EmployeeRow> selectedEmployees = new ArrayList<>();
    private java.awt.event.AWTEventListener globalClickListener;
    private javax.swing.JPanel pillContainer;

    public AddScheduleFrame(Runnable onScheduleSaved) {
        initComponents();
        this.onScheduleSaved = onScheduleSaved;
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        initSelectedEmployeesScrollArea();

        dateStart.setDateFormatString("HH:mm");
        dateEnd.setDateFormatString("HH:mm");
        initEmployeeSearch();
        initButtonEvents();
        initOutsideClickToCloseDropdown();
        panelSelectedArea.setVisible(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                hideEmployeeDropdown();
                if (globalClickListener != null) {
                    java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(globalClickListener);
                }
            }
        });

    }

    private void initSelectedEmployeesScrollArea() {
        java.awt.Dimension fixedSize = panelSelectedArea.getSize();
        if (fixedSize.height < 10) {
            fixedSize = new java.awt.Dimension(panelSelectedArea.getWidth() > 0 ? panelSelectedArea.getWidth() : 380, 70);
        }

        pillContainer = new javax.swing.JPanel(new ui.WrapLayout(java.awt.FlowLayout.LEFT, 8, 8));
        pillContainer.setBackground(java.awt.Color.WHITE);

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(pillContainer);
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(fixedSize);
        scrollPane.setMinimumSize(fixedSize);
        scrollPane.setMaximumSize(fixedSize);

        panelSelectedArea.setLayout(new java.awt.BorderLayout());
        panelSelectedArea.removeAll();
        panelSelectedArea.add(scrollPane, java.awt.BorderLayout.CENTER);

        panelSelectedArea.setPreferredSize(fixedSize);
        panelSelectedArea.setMinimumSize(fixedSize);
        panelSelectedArea.setMaximumSize(fixedSize);
    }

    private void initOutsideClickToCloseDropdown() {
        globalClickListener = event -> {
            if (!(event instanceof java.awt.event.MouseEvent me)) {
                return;
            }
            if (me.getID() != java.awt.event.MouseEvent.MOUSE_PRESSED) {
                return;
            }
            if (employeeDropdownWindow == null || !employeeDropdownWindow.isVisible()) {
                return;
            }
            java.awt.Component clicked = me.getComponent();
            if (clicked == null) {
                return;
            }
            boolean clickedInsideDropdown = javax.swing.SwingUtilities.isDescendingFrom(clicked, employeeDropdownWindow.getContentPane());
            boolean clickedOnSearchField = (clicked == txtSearchEmployee);
            if (!clickedInsideDropdown && !clickedOnSearchField) {
                hideEmployeeDropdown();
            }
        };
        java.awt.Toolkit.getDefaultToolkit()
                .addAWTEventListener(globalClickListener, java.awt.AWTEvent.MOUSE_EVENT_MASK);
    }

    private void initEmployeeSearch() {
        txtSearchEmployee.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search();
            }
        });

        txtSearchEmployee.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                search();
            }
        });
    }

    private void search() {
        String keyword = txtSearchEmployee.getText().trim();
        new javax.swing.SwingWorker<List<EmployeeRepository.EmployeeRow>, Void>() {
            @Override
            protected List<EmployeeRepository.EmployeeRow> doInBackground() {
                return employeeRepository.findEmployees(null, keyword);
            }

            @Override
            protected void done() {
                try {
                    List<EmployeeRepository.EmployeeRow> result = get().stream()
                            .filter(row -> row.status == 1)
                            .filter(row -> selectedEmployees.stream().noneMatch(s -> s.id == row.id))
                            .toList();
                    showEmployeeDropdown(result);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void addSelectedEmployee(EmployeeRepository.EmployeeRow row) {
        if (selectedEmployees.stream().anyMatch(s -> s.id == row.id)) {
            return;
        }
        selectedEmployees.add(row);
        refreshSelectedPanel();
    }

    private void removeSelectedEmployee(int employeeId) {
        selectedEmployees.removeIf(s -> s.id == employeeId);
        refreshSelectedPanel();
    }

    private void refreshSelectedPanel() {
        pillContainer.removeAll();
        for (EmployeeRepository.EmployeeRow row : selectedEmployees) {
            javax.swing.JPanel pill = buildPillComponent(row);
            pillContainer.add(pill);
        }
        panelSelectedArea.setVisible(!selectedEmployees.isEmpty());
        pillContainer.revalidate();
        pillContainer.repaint();
    }

    private javax.swing.JPanel buildPillComponent(EmployeeRepository.EmployeeRow row) {
        javax.swing.JPanel pill = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 4));
        pill.setBackground(new java.awt.Color(244, 246, 248));
        pill.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(225, 229, 234), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        javax.swing.JLabel nameLabel = new javax.swing.JLabel(row.fullName);
        nameLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        java.awt.Color roleColor = parseColorOrDefault(row.roleColorHex);

        javax.swing.JLabel roleLabel = new javax.swing.JLabel(row.roleName);
        roleLabel.setOpaque(true);
        roleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        roleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 8, 2, 8));
        roleLabel.setForeground(roleColor);
        roleLabel.setBackground(lighten(roleColor, 0.85f));

        javax.swing.JButton removeBtn = new javax.swing.JButton("x");
        removeBtn.setMargin(new java.awt.Insets(0, 4, 0, 4));
        removeBtn.setBorderPainted(false);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setFocusPainted(false);
        removeBtn.addActionListener(e -> removeSelectedEmployee(row.id));

        pill.add(nameLabel);
        pill.add(roleLabel);
        pill.add(removeBtn);
        return pill;
    }

    private java.awt.Color parseColorOrDefault(String hex) {
        try {
            return java.awt.Color.decode(hex);
        } catch (Exception e) {
            return new java.awt.Color(100, 116, 139); // slate-500 fallback nếu hex null/sai định dạng
        }
    }

    private java.awt.Color lighten(java.awt.Color color, float amount) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * amount);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * amount);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * amount);
        return new java.awt.Color(r, g, b);
    }

    private javax.swing.JWindow employeeDropdownWindow;

    private void showEmployeeDropdown(List<EmployeeRepository.EmployeeRow> results) {
        hideEmployeeDropdown();
        if (results.isEmpty()) {
            return;
        }

        javax.swing.JPanel listPanel = new javax.swing.JPanel();
        listPanel.setLayout(new javax.swing.BoxLayout(listPanel, javax.swing.BoxLayout.Y_AXIS));
        listPanel.setBackground(java.awt.Color.WHITE);
        listPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(225, 229, 234)));

        for (EmployeeRepository.EmployeeRow row : results) {
            javax.swing.JLabel item = new javax.swing.JLabel("  " + row.fullName + "   [" + row.roleName + "]");
            item.setOpaque(true);
            item.setBackground(java.awt.Color.WHITE);
            item.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            item.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
            item.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            item.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 30));
            item.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    item.setBackground(new java.awt.Color(244, 246, 248));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    item.setBackground(java.awt.Color.WHITE);
                }

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    addSelectedEmployee(row);
                    search();
                }
            });
            listPanel.add(item);
        }

        employeeDropdownWindow = new javax.swing.JWindow(this);
        employeeDropdownWindow.setContentPane(listPanel);
        employeeDropdownWindow.pack();

        java.awt.Point loc = txtSearchEmployee.getLocationOnScreen();
        employeeDropdownWindow.setLocation(loc.x, loc.y + txtSearchEmployee.getHeight());
        employeeDropdownWindow.setSize(txtSearchEmployee.getWidth(), employeeDropdownWindow.getPreferredSize().height);
        employeeDropdownWindow.setVisible(true);
    }

    private void hideEmployeeDropdown() {
        if (employeeDropdownWindow != null) {
            employeeDropdownWindow.setVisible(false);
            employeeDropdownWindow.dispose();
            employeeDropdownWindow = null;
        }
    }

    private void initButtonEvents() {
        btnCancel.addActionListener(e -> dispose());
        btnBack.addActionListener(e -> dispose());
        btnSaveSchedule.addActionListener(e -> handleSave());
    }

    private void handleSave() {
        java.util.Date workDateRaw = dateDay.getDate();
        java.util.Date startRaw = dateStart.getDate();
        java.util.Date endRaw = dateEnd.getDate();

        if (workDateRaw == null || startRaw == null || endRaw == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ ngày làm và giờ ca.");
            return;
        }

        LocalDate workDate = workDateRaw.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime startTime = startRaw.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        LocalTime endTime = endRaw.toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

        if (!startTime.isBefore(endTime)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Giờ bắt đầu phải trước giờ kết thúc.");
            return;
        }
        if (selectedEmployees.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 nhân viên.");
            return;
        }

        btnSaveSchedule.setEnabled(false);
        new javax.swing.SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                List<String> conflicts = new ArrayList<>();
                for (EmployeeRepository.EmployeeRow row : selectedEmployees) {
                    if (scheduleRepository.existsByEmployeeAndDate(row.id, workDate)) {
                        conflicts.add(row.fullName);
                    } else {
                        scheduleRepository.addSchedule(row.id, workDate, startTime, endTime);
                    }
                }
                return conflicts;
            }

            @Override
            protected void done() {
                btnSaveSchedule.setEnabled(true);
                try {
                    List<String> conflicts = get();
                    if (!conflicts.isEmpty()) {
                        javax.swing.JOptionPane.showMessageDialog(AddScheduleFrame.this,
                                "Các nhân viên sau đã có ca trong ngày này nên bị bỏ qua:\n"
                                + String.join(", ", conflicts));
                    }
                    if (conflicts.size() < selectedEmployees.size()) {
                        if (onScheduleSaved != null) {
                            onScheduleSaved.run();
                        }
                        dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        dateDay = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        dateStart = new com.toedter.calendar.JDateChooser();
        dateEnd = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        txtSearchEmployee = new javax.swing.JTextField();
        lblSelectedEmployees = new javax.swing.JLabel();
        panelSelectedArea = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        btnSaveSchedule = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Add Work Schedule");

        btnBack.setText("X");
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(this::btnBackActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel2.setText("Work Date");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel3.setText("Start Time");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel4.setText("End Time");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        jLabel5.setText("Assign Employees");

        txtSearchEmployee.addActionListener(this::txtSearchEmployeeActionPerformed);

        lblSelectedEmployees.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSelectedEmployees.setText("Selected Employees");

        panelSelectedArea.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelSelectedAreaLayout = new javax.swing.GroupLayout(panelSelectedArea);
        panelSelectedArea.setLayout(panelSelectedAreaLayout);
        panelSelectedAreaLayout.setHorizontalGroup(
            panelSelectedAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelSelectedAreaLayout.setVerticalGroup(
            panelSelectedAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 43, Short.MAX_VALUE)
        );

        btnSaveSchedule.setText("Save Schedule");

        btnCancel.setText("Cancel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(lblSelectedEmployees))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnCancel)
                                .addGap(18, 18, 18)
                                .addComponent(btnSaveSchedule))
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelSelectedArea, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtSearchEmployee, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateDay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(14, 14, 14))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dateDay, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dateStart, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtSearchEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblSelectedEmployees)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSelectedArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnBackActionPerformed

    private void txtSearchEmployeeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchEmployeeActionPerformed

    }//GEN-LAST:event_txtSearchEmployeeActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new AddScheduleFrame(() -> {
        }).setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnSaveSchedule;
    private com.toedter.calendar.JDateChooser dateDay;
    private com.toedter.calendar.JDateChooser dateEnd;
    private com.toedter.calendar.JDateChooser dateStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblSelectedEmployees;
    private javax.swing.JPanel panelSelectedArea;
    private javax.swing.JTextField txtSearchEmployee;
    // End of variables declaration//GEN-END:variables
}

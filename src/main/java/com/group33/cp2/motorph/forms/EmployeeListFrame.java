package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Displays a list of all employees; supports view, add, update, delete actions.
public class EmployeeListFrame extends javax.swing.JFrame {

    private final EmployeeService employeeService = new EmployeeService();
    private final boolean canEditCompensation;
    private List<Employee> employeeList;
    private String selectedEmployeeID;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> employeeSorter;

    public EmployeeListFrame(boolean canEditCompensation) {
        this.canEditCompensation = canEditCompensation;
        initComponents();
        configureFrame();
        configureEmployeeTable();
        rebuildLayout();
        loadEmployeeTable();
    }

    private void configureFrame() {
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(EmployeeListFrame.this)) {
                    dispose();
                }
            }
        });
    }

    private void configureEmployeeTable() {
        employeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeesTable.setRowHeight(38);
        employeesTable.setFillsViewportHeight(true);
        employeesTable.setShowVerticalLines(false);
        employeesTable.setShowHorizontalLines(true);
        employeesTable.setGridColor(new Color(232, 226, 241));
        employeesTable.setIntercellSpacing(new Dimension(0, 1));
        employeesTable.setBackground(Color.WHITE);
        employeesTable.setForeground(new Color(34, 41, 52));
        employeesTable.setSelectionBackground(new Color(223, 208, 255));
        employeesTable.setSelectionForeground(new Color(37, 21, 72));
        employeesTable.setFont(new Font("Noto Sans", Font.PLAIN, 14));
        employeesTable.getTableHeader().setReorderingAllowed(false);
        employeesTable.getTableHeader().setBackground(new Color(239, 233, 250));
        employeesTable.getTableHeader().setForeground(Color.BLACK);
        employeesTable.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 14));
        employeesTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        employeesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    component.setBackground(new Color(223, 208, 255));
                    component.setForeground(new Color(37, 21, 72));
                } else {
                    component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 248, 253));
                    component.setForeground(new Color(34, 41, 52));
                }
                if (component instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    label.setHorizontalAlignment(JLabel.LEFT);
                }
                return component;
            }
        });

        employeesTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int selectedRow = employeesTable.getSelectedRow();
            if (selectedRow < 0) {
                selectedEmployeeID = null;
                btnView.setEnabled(false);
                btnUpdate.setEnabled(false);
                btnDelete.setEnabled(false);
                return;
            }
            int modelRow = employeesTable.convertRowIndexToModel(selectedRow);
            selectedEmployeeID = employeesTable.getModel().getValueAt(modelRow, 0).toString();
            btnView.setEnabled(true);
            btnUpdate.setEnabled(true);
            btnDelete.setEnabled(true);
        });

        btnView.setEnabled(false);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    private void rebuildLayout() {
        getContentPane().removeAll();
        getContentPane().setBackground(new Color(241, 236, 247));
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel shell = new JPanel(new BorderLayout(0, 18));
        shell.setBackground(new Color(241, 236, 247));
        shell.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(103, 58, 183));
        header.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLabel = new JLabel("Employee List");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Noto Sans", Font.BOLD, 30));

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        styleActionButton(btnBack, false);
        styleActionButton(btnLogout, true);
        btnBack.setText("Back");
        headerActions.add(btnBack);
        headerActions.add(btnLogout);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(headerActions, BorderLayout.EAST);

        JPanel contentCard = new JPanel(new BorderLayout(0, 16));
        contentCard.setBackground(new Color(249, 247, 252));
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 202, 229), 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(false);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        styleActionButton(btnView, false);
        styleActionButton(btnAdd, false);
        styleActionButton(btnUpdate, false);
        styleActionButton(btnDelete, false);
        buttonBar.add(btnView);
        buttonBar.add(btnAdd);
        buttonBar.add(btnUpdate);
        buttonBar.add(btnDelete);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Noto Sans", Font.BOLD, 14));
        searchLabel.setForeground(new Color(67, 49, 106));
        searchField = new JTextField(20);
        UITheme.styleTextField(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        });
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        toolbar.add(buttonBar, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        int availableWidth = Math.max(Constants.FRAME_WIDTH - 560, 720);
        buttonBar.setPreferredSize(new Dimension(availableWidth, 52));

        JScrollPane employeeScrollPane = new JScrollPane(employeesTable);
        employeeScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 194, 222), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        employeeScrollPane.getViewport().setBackground(Color.WHITE);

        contentCard.add(toolbar, BorderLayout.NORTH);
        contentCard.add(employeeScrollPane, BorderLayout.CENTER);

        shell.add(header, BorderLayout.NORTH);
        shell.add(contentCard, BorderLayout.CENTER);
        getContentPane().add(shell, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void styleActionButton(javax.swing.JButton button, boolean primary) {
        button.setFocusPainted(false);
        button.setFont(new Font("Noto Sans", Font.BOLD, 14));
        if ("Back".equals(button.getText()) || "Logout".equals(button.getText())) {
            button.setPreferredSize(new Dimension(190, 52));
        } else {
            button.setPreferredSize(new Dimension(170, 52));
        }
        if (primary) {
            if ("Logout".equals(button.getText())) {
                button.setBackground(new Color(186, 67, 67));
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(145, 42, 42), 1),
                        BorderFactory.createEmptyBorder(10, 18, 10, 18)
                ));
            } else {
                button.setBackground(new Color(139, 92, 246));
                button.setForeground(Color.WHITE);
            }
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(74, 55, 112));
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 182, 221), 1),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
            ));
        }
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    private void applySearchFilter() {
        if (employeeSorter == null || searchField == null) {
            return;
        }
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            employeeSorter.setRowFilter(null);
            return;
        }
        employeeSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    // Reloads and displays employee data in a non-editable table.
    private void loadEmployeeTable() {
        employeeService.reloadEmployees();
        employeeList = employeeService.getAllEmployees();

        String[] columnNames = {
            "Employee No.", "Last Name", "First Name",
            "SSS No.", "PhilHealth No.", "TIN", "Pag-IBIG No."
        };

        Object[][] dataTable = new Object[employeeList.size()][columnNames.length];
        for (int i = 0; i < employeeList.size(); i++) {
            Employee emp = employeeList.get(i);
            dataTable[i][0] = emp.getEmployeeID();
            dataTable[i][1] = emp.getLastName();
            dataTable[i][2] = emp.getFirstName();
            dataTable[i][3] = emp.getGovernmentDetails().getSssNumber();
            dataTable[i][4] = emp.getGovernmentDetails().getPhilHealthNumber();
            dataTable[i][5] = emp.getGovernmentDetails().getTinNumber();
            dataTable[i][6] = emp.getGovernmentDetails().getPagibigNumber();
        }

        DefaultTableModel model = new DefaultTableModel(dataTable, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeesTable.setModel(model);
        employeeSorter = new TableRowSorter<>(model);
        employeesTable.setRowSorter(employeeSorter);
        applySearchFilter();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        employeesTable = new javax.swing.JTable();
        btnView = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        txtEmpID = new javax.swing.JTextField();
        txtLastName = new javax.swing.JTextField();
        txtFirstName = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtSSSNumber = new javax.swing.JTextField();
        txtPhilHealthNumber = new javax.swing.JTextField();
        txtTIN = new javax.swing.JTextField();
        txtPagIBIGNumber = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        employeesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {},
                {}
            },
            new String [] {
            }
        ));
        employeesTable.setName("tblEmployeeRecords"); // NOI18N
        employeesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(employeesTable);

        btnView.setText("View Employee");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        btnAdd.setText("New Employee");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update Employee");
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete Employee");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        txtEmpID.setEnabled(false);
        txtEmpID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEmpIDActionPerformed(evt);
            }
        });

        txtLastName.setEnabled(false);
        txtLastName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLastNameActionPerformed(evt);
            }
        });

        txtFirstName.setEnabled(false);

        btnSave.setText("Save");
        btnSave.setEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel1.setText("Employee Number");
        jLabel2.setText("PhilHealth Number");

        txtSSSNumber.setEnabled(false);
        txtPhilHealthNumber.setEnabled(false);
        txtTIN.setEnabled(false);
        txtPagIBIGNumber.setEnabled(false);

        jLabel3.setText("Last Name");
        jLabel4.setText("First Name");
        jLabel5.setText("SSS Number");
        jLabel6.setText("TIN");
        jLabel7.setText("Pag - IBIG Number");

        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnBack.setText("<");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setText("Employee List");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLogout)
                .addGap(22, 22, 22))
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnView)
                        .addGap(18, 18, 18)
                        .addComponent(btnAdd)
                        .addGap(18, 18, 18)
                        .addComponent(btnUpdate)
                        .addGap(18, 18, 18)
                        .addComponent(btnDelete))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 747, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtEmpID, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtLastName, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtFirstName, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtSSSNumber, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtPhilHealthNumber)
                        .addComponent(txtTIN, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtPagIBIGNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnSave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBack)
                    .addComponent(jLabel8)
                    .addComponent(btnLogout))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnView)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEmpID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addGap(13, 13, 13)
                        .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addGap(8, 8, 8)
                        .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSSSNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPhilHealthNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTIN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPagIBIGNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(btnSave))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (selectedEmployeeID == null || selectedEmployeeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        UpdateEmployeeFrame updateFrame = new UpdateEmployeeFrame(selectedEmployeeID, canEditCompensation);
        // Reload the employee table after the update frame is closed (cancel or save).
        // windowClosed fires after dispose(), so the CSV has already been written.
        updateFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadEmployeeTable();
            }
        });
        updateFrame.setVisible(true);
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (selectedEmployeeID == null || selectedEmployeeID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Employee employeeToDelete = employeeService.getEmployeeById(selectedEmployeeID);
        if (employeeToDelete == null) {
            JOptionPane.showMessageDialog(this, "Employee not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String employeeName = employeeToDelete.getFirstName() + " " + employeeToDelete.getLastName();
        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete employee:\n"
                + "ID: " + selectedEmployeeID + "\n"
                + "Name: " + employeeName + "\n\n"
                + "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean deleted = employeeService.deleteEmployee(selectedEmployeeID);

            if (deleted) {
                JOptionPane.showMessageDialog(this,
                        "Employee " + employeeName + " has been successfully deleted.",
                        "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
                selectedEmployeeID = null;
                employeesTable.clearSelection();
                loadEmployeeTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete employee. Please try again.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        if (selectedEmployeeID == null) {
            JOptionPane.showMessageDialog(null, "Employee not found.");
            return;
        }
        NavigationManager.openViewEmployeeFrame(this, selectedEmployeeID, canEditCompensation);
    }//GEN-LAST:event_btnViewActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // NavigationManager.openNewEmployeeFrame already disposes this frame internally.
        // Do NOT call dispose() here — double-disposing fires a second windowClosed event
        // and leaves a stale JFrame instance in Swing's window registry.
        NavigationManager.openNewEmployeeFrame(this, canEditCompensation);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // Inline update form removed — editing is handled by UpdateEmployeeFrame
    }//GEN-LAST:event_btnSaveActionPerformed

    private void txtEmpIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmpIDActionPerformed
    }//GEN-LAST:event_txtEmpIDActionPerformed

    private void txtLastNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLastNameActionPerformed
    }//GEN-LAST:event_txtLastNameActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        if (DialogUtil.confirmLogout(this)) {
            NavigationManager.openLoginFrame(this);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        NavigationManager.openMenuFrame(this);
    }//GEN-LAST:event_btnBackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnView;
    private javax.swing.JTable employeesTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txtEmpID;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtPagIBIGNumber;
    private javax.swing.JTextField txtPhilHealthNumber;
    private javax.swing.JTextField txtSSSNumber;
    private javax.swing.JTextField txtTIN;
    // End of variables declaration//GEN-END:variables
}

package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.HR;
import com.group33.cp2.motorph.forms.EmployeeDashboard;
import com.group33.cp2.motorph.model.LeaveRequest;
import com.group33.cp2.motorph.report.ReportException;
import com.group33.cp2.motorph.report.ReportService;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.service.LeaveService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

// HR Dashboard — employee management and leave approval/rejection for HR roles.
public class HRDashboard extends JFrame {

    private static final Color HR_ORANGE = new Color(230, 136, 37);
    private static final Color HR_ORANGE_DARK = new Color(184, 101, 20);
    private static final Color HR_GREEN = new Color(54, 146, 88);
    private static final Color HR_GREEN_DARK = new Color(34, 112, 63);

    // Domain objects
    private final HR              hrUser;
    private final EmployeeService employeeService;
    private final LeaveService    leaveService;

    // Tab 1 — Employee Management
    private JTable employeeTable;
    private DefaultTableModel employeeTableModel;

    // Tab 2 — Leave Management
    private JTable leaveTable;
    private DefaultTableModel leaveTableModel;
    private JTextField txtRemark;

    // Tab 3 — Payroll Reports
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbPeriod;
    private final ReportService reportService = new ReportService();

    // Column indices for leave table
    private static final int LEAVE_COL_ID       = 0;
    private static final int LEAVE_COL_EMP_NUM  = 1;
    private static final int LEAVE_COL_TYPE     = 2;
    private static final int LEAVE_COL_START    = 3;
    private static final int LEAVE_COL_END      = 4;
    private static final int LEAVE_COL_STATUS   = 5;

    // Column indices for employee table
    private static final int EMP_COL_ID         = 0;
    private static final int EMP_COL_NAME       = 1;
    private static final int EMP_COL_POSITION   = 2;
    private static final int EMP_COL_STATUS     = 3;

    public HRDashboard(HR hrUser, EmployeeService employeeService) {
        this.hrUser          = hrUser;
        this.employeeService = employeeService;
        this.leaveService    = new LeaveService();

        setTitle("HR Dashboard — " + hrUser.getFullName());
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(HRDashboard.this)) {
                    dispose();
                }
            }
        });

        buildUI();
        loadEmployeeTable();
        loadLeaveTable();
    }

   
    //  UI construction
       private void buildUI() {
        JPanel headerPanel = buildHeaderPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Employee Management", buildEmployeeTab());
        tabs.addTab("Leave Management", buildLeaveTab());
        tabs.addTab("Payroll Reports", buildPayrollReportsTab());
        tabs.setBackground(new Color(247, 243, 238));
        tabs.setForeground(new Color(67, 50, 24));
        tabs.setFont(new Font("Noto Sans", Font.BOLD, 13));
        UITheme.styleTabs(tabs);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(247, 243, 238));
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HR_ORANGE_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));

        JLabel title = new JLabel("HR Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Noto Sans", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel welcome = new JLabel("Welcome, " + hrUser.getFullName(), SwingConstants.RIGHT);
        welcome.setFont(new Font("Noto Sans", Font.BOLD, 14));
        welcome.setForeground(Color.WHITE);

        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(247, 243, 238));

        JButton btnMyDashboard = new JButton("My Employee Dashboard");
        UITheme.styleSecondaryButton(btnMyDashboard);
        btnMyDashboard.addActionListener(e -> {
            dispose();
            EmployeeDashboard empDash = new EmployeeDashboard(hrUser, () -> {
                HRDashboard back = new HRDashboard(hrUser, employeeService);
                back.setVisible(true);
            });
            empDash.setVisible(true);
        });
        panel.add(btnMyDashboard);

        JButton btnLogout = new JButton("Logout");
        UITheme.styleDangerButton(btnLogout);
        btnLogout.addActionListener(e -> {
            if (DialogUtil.confirmLogout(this)) {
                NavigationManager.openLoginFrame(this);
            }
        });
        panel.add(btnLogout);
        return panel;
    }

   
    //  Tab 1: Employee Management
       private JPanel buildEmployeeTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(247, 243, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Table
        employeeTableModel = new DefaultTableModel(
                new String[]{"Employee ID", "Full Name", "Position", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        employeeTable = new JTable(employeeTableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        employeeTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        styleTable(employeeTable, scrollPane);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // Button bar
        JButton btnAdd    = new JButton("Add Employee");
        JButton btnEdit   = new JButton("Edit Employee");
        JButton btnDelete = new JButton("Delete Employee");
        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnAdd, false);
        styleButton(btnEdit, false);
        styleButton(btnDelete, false);
        styleRefreshButton(btnRefresh);
        btnAdd.setPreferredSize(new Dimension(170, 48));
        btnEdit.setPreferredSize(new Dimension(170, 48));
        btnDelete.setPreferredSize(new Dimension(195, 48));
        btnRefresh.setPreferredSize(new Dimension(120, 48));

        btnAdd.addActionListener(e -> {
            NavigationManager.openNewEmployeeFrame(this, false);
        });

        btnEdit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an employee to edit.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = employeeTableModel.getValueAt(row, EMP_COL_ID).toString();
            // HR users may not edit compensation data — pass false for canEditCompensation.
            UpdateEmployeeFrame updateFrame = new UpdateEmployeeFrame(empId, false);
            updateFrame.setLocationRelativeTo(this);
            // Reload the employee table after the update frame closes (cancel or save).
            updateFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadEmployeeTable();
                }
            });
            updateFrame.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an employee to delete.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = employeeTableModel.getValueAt(row, EMP_COL_ID).toString();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete employee " + empId + "? This cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                hrUser.deleteEmployee(empId);
                loadEmployeeTable();
            }
        });

        btnRefresh.addActionListener(e -> loadEmployeeTable());

        JLabel sectionTitle = new JLabel("Employee Directory");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(66, 47, 24));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(btnAdd);
        buttonBar.add(btnEdit);
        buttonBar.add(btnDelete);
        buttonBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout(0, 10));
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.NORTH);
        topBar.add(buttonBar, BorderLayout.SOUTH);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel helperText = new JLabel("Review employee records, then select a row to edit or remove.");
        helperText.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helperText.setForeground(new Color(111, 98, 79));
        tableCard.add(helperText, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    
    //  Tab 2: Leave Management
        private JPanel buildLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(247, 243, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Table
        leaveTableModel = new DefaultTableModel(
                new String[]{"Leave ID", "Employee #", "Type", "Start Date", "End Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        leaveTable = new JTable(leaveTableModel);
        leaveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leaveTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(leaveTable);
        styleTable(leaveTable, scrollPane);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // Remark field
        JLabel sectionTitle = new JLabel("Leave Requests");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(66, 47, 24));

        JPanel remarkPanel = new JPanel(new BorderLayout(10, 0));
        remarkPanel.setOpaque(false);
        JLabel remarkLabel = new JLabel("Remark:");
        remarkLabel.setFont(new Font("Noto Sans", Font.BOLD, 13));
        remarkLabel.setForeground(new Color(88, 58, 21));
        txtRemark = new JTextField(30);
        UITheme.styleTextField(txtRemark);
        JPanel remarkFieldRow = new JPanel(new BorderLayout(10, 0));
        remarkFieldRow.setOpaque(false);
        remarkFieldRow.add(remarkLabel, BorderLayout.WEST);
        remarkFieldRow.add(txtRemark, BorderLayout.CENTER);
        remarkPanel.add(remarkFieldRow, BorderLayout.CENTER);

        // Action buttons
        JButton btnApprove = new JButton("Approve");
        JButton btnReject  = new JButton("Reject");
        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnApprove, false);
        styleButton(btnReject, false);
        styleRefreshButton(btnRefresh);
        btnApprove.setPreferredSize(new Dimension(128, 48));
        btnReject.setPreferredSize(new Dimension(128, 48));
        btnRefresh.setPreferredSize(new Dimension(120, 48));

        btnApprove.addActionListener(e -> handleLeaveAction(true));
        btnReject.addActionListener(e  -> handleLeaveAction(false));
        btnRefresh.addActionListener(e -> loadLeaveTable());

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBar.setOpaque(false);
        actionBar.add(btnApprove);
        actionBar.add(btnReject);
        actionBar.add(btnRefresh);
        remarkPanel.add(actionBar, BorderLayout.EAST);

        JPanel topBar = new JPanel(new BorderLayout(0, 10));
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.NORTH);
        topBar.add(remarkPanel, BorderLayout.SOUTH);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        JLabel helperText = new JLabel("Select a pending request, add an optional remark, then approve or reject.");
        helperText.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helperText.setForeground(new Color(111, 98, 79));
        tableCard.add(helperText, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    //  Tab 3: Payroll Reports
    private JPanel buildPayrollReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(247, 243, 238));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── Section title ──
        JLabel sectionTitle = new JLabel("Payroll Report Generator");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(66, 47, 24));

        // ── Filter card ──
        JPanel filterCard = new JPanel(new BorderLayout(0, 12));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel filterTitle = new JLabel("Report Filters");
        filterTitle.setFont(new Font("Noto Sans", Font.BOLD, 14));
        filterTitle.setForeground(new Color(88, 58, 21));

        // Department combo
        cmbDepartment = new JComboBox<>(new String[]{
            "All Departments", "Executive", "Finance & Accounting",
            "Human Resources", "IT", "Sales & Marketing"
        });
        UITheme.styleComboBox(cmbDepartment);
        cmbDepartment.setPreferredSize(new Dimension(220, 36));

        // Period combo — common payroll periods
        cmbPeriod = new JComboBox<>(new String[]{
            "All Periods",
            "2026-01-01 to 2026-01-15", "2026-01-16 to 2026-01-31",
            "2026-02-01 to 2026-02-15", "2026-02-16 to 2026-02-28",
            "2026-03-01 to 2026-03-15", "2026-03-16 to 2026-03-31",
            "2026-04-01 to 2026-04-15", "2026-04-16 to 2026-04-30",
            "2026-05-01 to 2026-05-15", "2026-05-16 to 2026-05-31",
            "2026-06-01 to 2026-06-15", "2026-06-16 to 2026-06-30"
        });
        UITheme.styleComboBox(cmbPeriod);
        cmbPeriod.setPreferredSize(new Dimension(280, 36));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Department:"));
        filterRow.add(cmbDepartment);
        filterRow.add(new JLabel("Period:"));
        filterRow.add(cmbPeriod);

        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(filterRow, BorderLayout.CENTER);

        // ── Info card ──
        JPanel infoCard = new JPanel(new BorderLayout(0, 8));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 201, 193), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel infoTitle = new JLabel("Available Reports");
        infoTitle.setFont(new Font("Noto Sans", Font.BOLD, 14));
        infoTitle.setForeground(new Color(88, 58, 21));

        JLabel infoText = new JLabel(
            "<html>" +
            "<b>Monthly Payroll Summary</b> — All employees with gross income, deductions (SSS, PhilHealth, " +
            "Pag-IBIG, BIR), and net pay. Can be filtered by department.<br><br>" +
            "Select your filters above, then click <b>Generate PDF</b> to export the report." +
            "</html>"
        );
        infoText.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        infoText.setForeground(new Color(80, 68, 50));

        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(infoText, BorderLayout.CENTER);

        // ── Generate button ──
        JButton btnGenerate = new JButton("Generate PDF");
        btnGenerate.setBackground(HR_ORANGE);
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Noto Sans", Font.BOLD, 14));
        btnGenerate.setFocusPainted(false);
        btnGenerate.setOpaque(true);
        btnGenerate.setPreferredSize(new Dimension(180, 48));
        btnGenerate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HR_ORANGE_DARK, 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btnGenerate.addActionListener(e -> handleGeneratePayrollReport());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(btnGenerate);

        // ── Assemble ──
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new javax.swing.BoxLayout(centerStack, javax.swing.BoxLayout.Y_AXIS));
        centerStack.add(filterCard);
        centerStack.add(javax.swing.Box.createVerticalStrut(12));
        centerStack.add(infoCard);
        centerStack.add(javax.swing.Box.createVerticalStrut(16));
        centerStack.add(buttonRow);

        JPanel topBar = new JPanel(new BorderLayout(0, 10));
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.NORTH);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(centerStack, BorderLayout.CENTER);
        return panel;
    }

    // Handles the Generate PDF button — asks user where to save, then calls ReportService.
    private void handleGeneratePayrollReport() {
        String dept   = (String) cmbDepartment.getSelectedItem();
        String period = (String) cmbPeriod.getSelectedItem();

        String deptFilter   = "All Departments".equals(dept)   ? null : dept;
        String periodFilter = "All Periods".equals(period)     ? null : period;

        // Ask user where to save the PDF
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Payroll Report PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        String suggestedName = "PayrollReport_" +
                (deptFilter != null ? deptFilter.replace(" ", "_") : "All") + ".pdf";
        chooser.setSelectedFile(new File(suggestedName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File outputFile = chooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
        }

        // Run in background to avoid freezing the UI
        final File finalOutput = outputFile;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                reportService.generatePayrollReport(
                        hrUser.getEmployeeID(), deptFilter, periodFilter, finalOutput);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(HRDashboard.this,
                            "Report saved to:\n" + finalOutput.getAbsolutePath(),
                            "Report Generated", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(HRDashboard.this,
                            "Failed to generate report:\n" + msg,
                            "Report Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 174, 163), 1, true),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        if (primary) {
            button.setBackground(HR_ORANGE);
            button.setForeground(Color.WHITE);
        } else {
            UITheme.styleNeutralButton(button, new Color(88, 58, 21));
            return;
        }
    }

    private void styleRefreshButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(HR_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HR_GREEN_DARK, 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
    }

    private void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(34, 43, 56));
        table.setSelectionBackground(new Color(248, 224, 190));
        table.setSelectionForeground(new Color(90, 57, 17));
        table.setGridColor(new Color(227, 223, 218));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(new Color(242, 238, 232));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
    }

    
    //  Data loaders
    
    // Reloads the employee table from EmployeeService. Always re-reads CSVs first.
    // Loads employees off the EDT via SwingWorker; updates the table model in done().
    private void loadEmployeeTable() {
        new SwingWorker<List<Employee>, Void>() {
            @Override
            protected List<Employee> doInBackground() throws Exception {
                employeeService.reloadEmployees();
                return employeeService.getAllEmployees();
            }

            @Override
            protected void done() {
                try {
                    List<Employee> employees = get();
                    employeeTableModel.setRowCount(0);
                    for (Employee emp : employees) {
                        employeeTableModel.addRow(new Object[]{
                            emp.getEmployeeID(),
                            emp.getFullName(),
                            emp.getPosition(),
                            emp.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HRDashboard.this,
                            "Failed to load employees: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Loads leave requests off the EDT via SwingWorker; updates the table model in done().
    private void loadLeaveTable() {
        new SwingWorker<List<LeaveRequest>, Void>() {
            @Override
            protected List<LeaveRequest> doInBackground() throws Exception {
                return leaveService.getAllLeaveRequests();
            }

            @Override
            protected void done() {
                try {
                    List<LeaveRequest> requests = get();
                    leaveTableModel.setRowCount(0);
                    for (LeaveRequest req : requests) {
                        leaveTableModel.addRow(new Object[]{
                            req.getLeaveID(),
                            req.getEmployeeID(),
                            req.getLeaveType(),
                            req.getStartDate() != null ? req.getStartDate().toString() : "",
                            req.getEndDate()   != null ? req.getEndDate().toString()   : "",
                            req.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HRDashboard.this,
                            "Failed to load leave requests: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

   
    //  Action helpers
   
    // Approves (true) or rejects (false) the selected leave request.
    private void handleLeaveAction(boolean approve) {
        int row = leaveTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leaveId = leaveTableModel.getValueAt(row, LEAVE_COL_ID).toString();
        String remark  = txtRemark.getText().trim();

        if (!approve && remark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a remark before rejecting.",
                    "Remark Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success;
        if (approve) {
            success = hrUser.approveLeave(leaveId, remark);
        } else {
            success = hrUser.rejectLeave(leaveId, remark);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Leave request " + leaveId + " has been " + (approve ? "approved." : "rejected."),
                    approve ? "Approved" : "Rejected",
                    JOptionPane.INFORMATION_MESSAGE);
            txtRemark.setText("");
            loadLeaveTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Action failed. The request may not be in Pending status or was not found.",
                    "Action Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

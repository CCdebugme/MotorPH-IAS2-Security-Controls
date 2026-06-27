package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.forms.EmployeeDashboard;
import com.group33.cp2.motorph.model.Admin;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.Report;
import com.group33.cp2.motorph.model.UserManagementCallback;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JScrollBar;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

// Admin Dashboard — employee management and system reports for Admin roles.
public class AdminDashboard extends JFrame {

    private static final Color ADMIN_YELLOW = new Color(214, 172, 34);
    private static final Color ADMIN_YELLOW_DARK = new Color(168, 132, 18);
    private static final Color ADMIN_TEXT_DARK = new Color(72, 56, 12);

    private final Admin           adminUser;
    private final EmployeeService employeeService;

    // Tab 1 — Employee Management
    private JTable          employeeTable;
    private DefaultTableModel employeeModel;

    // Tab 2 — System Reports
    private JComboBox<String> cmbReportType;
    private JTextArea         reportArea;
    private JButton           btnLogout;
    private CardLayout        contentCards;
    private JPanel            contentPanel;
    private JButton           btnNavEmployees;
    private JButton           btnNavReports;
    private DateDropdownPanel dateStart;
    private DateDropdownPanel dateEnd;

    // Column index constants for the employee table
    private static final int COL_EMP_ID   = 0;
    private static final int COL_NAME     = 1;
    private static final int COL_POSITION = 2;
    private static final int COL_STATUS   = 3;

    private static final String[] REPORT_TYPES = {"payroll", "attendance"};

    public AdminDashboard(Admin adminUser, EmployeeService employeeService) {
        this.adminUser       = adminUser;
        this.employeeService = employeeService;

        setTitle("Admin Dashboard — " + adminUser.getFullName());
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(AdminDashboard.this)) {
                    dispose();
                }
            }
        });

        buildUI();
        loadEmployeeTable();
    }

    // =========================================================================
    //  UI construction
    // =========================================================================

    private void buildUI() {
        JPanel header = buildHeaderPanel();
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(new Color(230, 233, 238));
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        contentCards = new CardLayout();
        contentPanel = new JPanel(contentCards);
        contentPanel.setOpaque(false);
        contentPanel.add(buildEmployeeTab(), "employees");
        contentPanel.add(buildReportsTab(), "reports");

        body.add(buildSidebarPanel(), BorderLayout.WEST);
        body.add(contentPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(230, 233, 238));
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(body, BorderLayout.CENTER);

        showSection("employees");
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ADMIN_YELLOW_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Noto Sans", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " + adminUser.getFullName(), SwingConstants.RIGHT);
        welcome.setFont(new Font("Noto Sans", Font.BOLD, 16));
        welcome.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSidebarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(217, 222, 228));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 198, 207), 1),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)
        ));
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel sectionLabel = new JLabel("Administrator");
        sectionLabel.setFont(new Font("Noto Sans", Font.BOLD, 16));
        sectionLabel.setForeground(new Color(39, 51, 64));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnNavEmployees = new JButton("Employee Management");
        btnNavReports = new JButton("System Reports");
        btnLogout = new JButton("Logout");

        styleSidebarButton(btnNavEmployees, true);
        styleSidebarButton(btnNavReports, false);
        styleLogoutSidebarButton(btnLogout);

        btnNavEmployees.addActionListener(e -> showSection("employees"));
        btnNavReports.addActionListener(e -> showSection("reports"));
        btnLogout.addActionListener(e -> {
            if (DialogUtil.confirmLogout(this)) {
                NavigationManager.openLoginFrame(this);
            }
        });

        JButton btnMyDashboard = new JButton("My Employee Dashboard");
        styleSidebarButton(btnMyDashboard, false);
        btnMyDashboard.addActionListener(e -> {
            dispose();
            EmployeeDashboard empDash = new EmployeeDashboard(adminUser, () -> {
                AdminDashboard back = new AdminDashboard(adminUser, employeeService);
                back.setVisible(true);
            });
            empDash.setVisible(true);
        });

        panel.add(sectionLabel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnNavEmployees);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnNavReports);
        panel.add(Box.createVerticalGlue());
        panel.add(btnMyDashboard);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnLogout);
        return panel;
    }

    // =========================================================================
    //  Tab 1: Employee Management
    // =========================================================================

    private JPanel buildEmployeeTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(244, 246, 248));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 203, 210), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        employeeModel = new DefaultTableModel(
                new String[]{"Employee ID", "Full Name", "Position", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        employeeTable = new JTable(employeeModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setFillsViewportHeight(true);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JButton btnAdd        = new JButton("Add Employee");
        JButton btnEdit       = new JButton("Edit Employee");
        JButton btnDeactivate = new JButton("Deactivate Employee");
        JButton btnRefresh    = new JButton("Refresh");
        styleActionButton(btnAdd, false);
        styleActionButton(btnEdit, false);
        styleActionButton(btnDeactivate, false);
        styleActionButton(btnRefresh, false);

        btnAdd.addActionListener(e -> {
            adminUser.manageUsers(0, "create", new UserManagementCallback() {
                @Override
                public void onCreateUser() {
                    NewEmployeeFrame frame = new NewEmployeeFrame(true);
                    frame.setLocationRelativeTo(AdminDashboard.this);
                    frame.setVisible(true);
                }
                @Override
                public void onUpdateUser(String employeeId) { /* not used for create */ }
            });
        });

        btnEdit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an employee to edit.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = employeeModel.getValueAt(row, COL_EMP_ID).toString();
            int userId;
            try {
                userId = Integer.parseInt(empId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid employee ID: " + empId,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            adminUser.manageUsers(userId, "update", new UserManagementCallback() {
                @Override
                public void onCreateUser() { /* not used for update */ }
                @Override
                public void onUpdateUser(String employeeId) {
                    // Admin has full access — pass true for canEditCompensation.
                    UpdateEmployeeFrame frame = new UpdateEmployeeFrame(employeeId, true);
                    frame.setLocationRelativeTo(AdminDashboard.this);
                    // Reload the employee table after the update frame closes (cancel or save).
                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            loadEmployeeTable();
                        }
                    });
                    frame.setVisible(true);
                }
            });
        });

        btnDeactivate.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an employee to deactivate.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId  = employeeModel.getValueAt(row, COL_EMP_ID).toString();
            String status = employeeModel.getValueAt(row, COL_STATUS).toString();
            if ("Inactive".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Employee is already inactive.",
                        "No Change", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Deactivate employee " + empId + "? This sets their status to Inactive.",
                    "Confirm Deactivation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            int userId;
            try {
                userId = Integer.parseInt(empId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid employee ID: " + empId,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Delegates to Admin.manageUsers("deactivate")
            boolean ok = adminUser.manageUsers(userId, "deactivate");
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Employee " + empId + " has been set to Inactive.",
                        "Deactivated", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not deactivate employee " + empId + ".",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> loadEmployeeTable());

        // Change A — Delete Employee button declaration
        JButton btnDelete = new JButton("Delete Employee");
        styleActionButton(btnDelete, false);

        // Change B — Delete Employee action listener
        btnDelete.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an employee to delete.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String empId = employeeModel.getValueAt(row, COL_EMP_ID).toString();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Permanently delete employee " + empId + "? This cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            boolean ok = adminUser.deleteEmployee(empId);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Employee " + empId + " has been permanently deleted.",
                        "Deleted", JOptionPane.INFORMATION_MESSAGE);
                loadEmployeeTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete employee " + empId + ".",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel sectionTitle = new JLabel("Employee Directory");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(42, 54, 70));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.WEST);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(btnAdd);
        buttonBar.add(btnEdit);
        buttonBar.add(btnDeactivate);
        buttonBar.add(btnRefresh);
        buttonBar.add(btnDelete);
        topBar.add(buttonBar, BorderLayout.SOUTH);

        JScrollPane employeeScrollPane = new JScrollPane(employeeTable);
        styleDashboardTable(employeeTable, employeeScrollPane);
        employeeScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 209, 217), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        employeeScrollPane.getViewport().setBackground(Color.WHITE);
        employeeTable.setBackground(Color.WHITE);
        employeeTable.setForeground(new Color(36, 43, 53));
        employeeTable.setSelectionBackground(new Color(222, 229, 237));
        employeeTable.setSelectionForeground(new Color(35, 47, 62));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(employeeScrollPane, BorderLayout.CENTER);
        return panel;
    }

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
                    employeeModel.setRowCount(0);
                    for (Employee emp : employees) {
                        employeeModel.addRow(new Object[]{
                            emp.getEmployeeID(),
                            emp.getFullName(),
                            emp.getPosition(),
                            emp.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Failed to load employees: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    //  Tab 2: System Reports
    // =========================================================================

    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(244, 246, 248));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(197, 203, 210), 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Controls bar: report type selector + Generate button
        JLabel sectionTitle = new JLabel("System Reports");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(42, 54, 70));

        JLabel lblType = new JLabel("Report Type:");
        lblType.setFont(new Font("Noto Sans", Font.BOLD, 14));
        lblType.setForeground(new Color(52, 62, 77));
        cmbReportType = new JComboBox<>(new String[]{"Payroll Report", "Attendance Report"});
        UITheme.styleComboBox(cmbReportType);
        JButton btnGenerate = new JButton("Generate Report");
        styleActionButton(btnGenerate, false);
        btnGenerate.addActionListener(e -> handleGenerateReport());

        JLabel lblStart = new JLabel("From:");
        lblStart.setFont(new Font("Noto Sans", Font.BOLD, 14));
        lblStart.setForeground(new Color(52, 62, 77));
        dateStart = new DateDropdownPanel();
        dateStart.setFieldWidths(100, 80, 90);

        JLabel lblEnd = new JLabel("To:");
        lblEnd.setFont(new Font("Noto Sans", Font.BOLD, 14));
        lblEnd.setForeground(new Color(52, 62, 77));
        dateEnd = new DateDropdownPanel();
        dateEnd.setFieldWidths(100, 80, 90);

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlBar.setOpaque(false);
        controlBar.add(lblType);
        controlBar.add(cmbReportType);
        controlBar.add(lblStart);
        controlBar.add(dateStart);
        controlBar.add(lblEnd);
        controlBar.add(dateEnd);
        controlBar.add(btnGenerate);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.NORTH);
        topBar.add(controlBar, BorderLayout.SOUTH);

        // Output area
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        reportArea.setText("Select a report type and click Generate Report.");
        reportArea.setBackground(Color.WHITE);
        reportArea.setForeground(new Color(36, 43, 53));
        reportArea.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        reportArea.setCaretPosition(0);
        reportArea.setLineWrap(false);
        reportArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 209, 217), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        styleReportScrollBar(scrollPane.getVerticalScrollBar());
        styleReportScrollBar(scrollPane.getHorizontalScrollBar());

        JLabel outputTitle = new JLabel("Report Output");
        outputTitle.setFont(new Font("Noto Sans", Font.BOLD, 16));
        outputTitle.setForeground(new Color(42, 54, 70));

        JLabel outputHint = new JLabel("Generated reports appear here in a cleaner printable layout.");
        outputHint.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        outputHint.setForeground(new Color(102, 111, 121));

        JPanel outputHeader = new JPanel();
        outputHeader.setLayout(new BoxLayout(outputHeader, BoxLayout.Y_AXIS));
        outputHeader.setOpaque(false);
        outputHeader.add(outputTitle);
        outputHeader.add(Box.createVerticalStrut(4));
        outputHeader.add(outputHint);

        JPanel reportCard = new JPanel(new BorderLayout(0, 10));
        reportCard.setBackground(Color.WHITE);
        reportCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 209, 217), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        reportCard.add(outputHeader, BorderLayout.NORTH);
        reportCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(reportCard, BorderLayout.CENTER);
        return panel;
    }

    // Generates the selected report via Admin.generateSystemReport() in a SwingWorker.
    // Validates date range before dispatching — Attendance.csv is 15 k+ rows.
    private void handleGenerateReport() {
        int selectedIndex = cmbReportType.getSelectedIndex();
        String reportType = REPORT_TYPES[selectedIndex]; // "payroll" or "attendance"

        LocalDate start = dateStart.getSelectedDate();
        LocalDate end   = dateEnd.getSelectedDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a start date and an end date.",
                    "Date Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this,
                    "End date must not be before start date.",
                    "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        reportArea.setText("Generating report, please wait...");

        final LocalDate finalStart = start;
        final LocalDate finalEnd   = end;
        new SwingWorker<Report, Void>() {
            @Override
            protected Report doInBackground() {
                return adminUser.generateSystemReport(reportType, finalStart, finalEnd);
            }

            @Override
            protected void done() {
                try {
                    Report report = get();
                    reportArea.setText(report.getContent());
                    reportArea.setCaretPosition(0);
                } catch (Exception ex) {
                    reportArea.setText("Error generating report: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void showSection(String section) {
        contentCards.show(contentPanel, section);
        styleSidebarButton(btnNavEmployees, "employees".equals(section));
        styleSidebarButton(btnNavReports, "reports".equals(section));
        styleLogoutSidebarButton(btnLogout);
    }

    private void styleSidebarButton(JButton button, boolean active) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        if (active) {
            button.setBackground(new Color(218, 226, 235));
            button.setForeground(new Color(36, 48, 62));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(144, 159, 176), 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
        } else {
            button.setBackground(new Color(232, 236, 241));
            button.setForeground(new Color(45, 56, 69));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(173, 182, 191), 1, true),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
        }
    }

    private void styleActionButton(JButton button, boolean primary) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(173, 182, 191), 1, true),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        if (primary) {
            button.setBackground(ADMIN_YELLOW);
            button.setForeground(ADMIN_TEXT_DARK);
        } else {
            button.setBackground(new Color(245, 247, 249));
            button.setForeground(new Color(43, 54, 68));
        }
    }

    private void styleLogoutSidebarButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(new Color(186, 67, 67));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 42, 42), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
    }

    private void styleDashboardTable(JTable table, JScrollPane scrollPane) {
        table.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(234, 238, 242));
        table.getTableHeader().setBackground(new Color(231, 235, 240));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    component.setBackground(new Color(222, 229, 237));
                    component.setForeground(new Color(35, 47, 62));
                } else {
                    component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    component.setForeground(new Color(36, 43, 53));
                }
                if (component instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return component;
            }
        });
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    private void styleReportScrollBar(JScrollBar scrollBar) {
        if (scrollBar == null) {
            return;
        }
        scrollBar.setUnitIncrement(18);
        scrollBar.setBackground(new Color(242, 245, 248));
    }
}

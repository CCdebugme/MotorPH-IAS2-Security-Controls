package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.forms.EmployeeDashboard;
import com.group33.cp2.motorph.model.Allowance;
import com.group33.cp2.motorph.model.CompensationDetails;
import com.group33.cp2.motorph.model.Deductions;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.Finance;
import com.group33.cp2.motorph.model.GovernmentDetails;
import com.group33.cp2.motorph.model.Payroll;
import com.group33.cp2.motorph.model.PayrollLog;
import com.group33.cp2.motorph.model.PeriodType;
import com.group33.cp2.motorph.model.SalaryDetails;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.model.Payslip;
import com.group33.cp2.motorph.service.PayrollCalculator;
import com.group33.cp2.motorph.service.PayrollCalculatorService;
import com.group33.cp2.motorph.service.PayrollLogService;
import com.group33.cp2.motorph.service.PayslipService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;

import java.time.YearMonth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// Finance Dashboard — payroll overview, detailed report, and payroll-by-period tabs.
public class FinanceDashboard extends JFrame {

    private static final Color FINANCE_RED = new Color(188, 50, 61);
    private static final Color FINANCE_RED_DARK = new Color(140, 31, 40);
    private static final Color FINANCE_GREEN = new Color(54, 146, 88);
    private static final Color FINANCE_GREEN_DARK = new Color(34, 112, 63);

    private final Finance              financeUser;
    private final EmployeeService      employeeService;
    private final PayrollLogService    payrollLogService;

    // Tab 1 — Payroll Overview
    private JTable            payrollTable;
    private DefaultTableModel payrollModel;

    // Tab 2 — Detailed Report
    private JTable            detailTable;
    private DefaultTableModel detailModel;
    private JTextArea         reportTextArea;

    // Tab 2 — per-employee payslip detail labels
    private JLabel lblDetailEmpName;
    private JLabel lblDetailBasic;
    private JLabel lblDetailGross;
    private JLabel lblDetailSSS;
    private JLabel lblDetailPhilHealth;
    private JLabel lblDetailPagibig;
    private JLabel lblDetailTax;
    private JLabel lblDetailTotalDed;
    private JLabel lblDetailNet;

    // Tab 3 — Payroll by Period controls
    private DateDropdownPanel   coverageStartChooser;
    private DateDropdownPanel   coverageEndChooser;
    private JComboBox<String>   cutoffPeriodCombo;
    private JTable              periodEmpTable;
    private DefaultTableModel   periodEmpModel;
    private TableRowSorter<DefaultTableModel> periodSorter;
    private JTextField          txtPeriodSearch;

    // Tab 3 — right-panel display labels
    private JLabel lblPeriodEmpNum;
    private JLabel lblPeriodName;
    private JLabel lblPeriodSssNum;
    private JLabel lblPeriodPhilHealthNum;
    private JLabel lblPeriodTin;
    private JLabel lblPeriodPagibigNum;
    private JLabel lblPeriodRice;
    private JLabel lblPeriodPhone;
    private JLabel lblPeriodClothing;
    private JLabel lblPeriodTotalAllow;
    private JLabel lblPeriodSss;
    private JLabel lblPeriodPhilHealth;
    private JLabel lblPeriodPagibig;
    private JLabel lblPeriodTax;
    private JLabel lblPeriodTotalDed;
    private JLabel lblPeriodGross;
    private JLabel lblPeriodNet;
    private JLabel lblPeriodStatus;

    // Tab 3 — payroll log table (bottom panel)
    private DefaultTableModel periodLogModel;
    private JTable            periodLogTable;
    private JPanel            periodLogPanel;

    // Tab 3 — active SwingWorker; cancelled before a new one starts to prevent race conditions
    private SwingWorker<?, ?> currentPeriodWorker;
    private boolean updatingCutoffSelection;

    // Tab 3 — month/year picker for "Run All Employees" batch button
    private JComboBox<String>  monthCombo;
    private JComboBox<Integer> yearCombo;

    public FinanceDashboard(Finance financeUser, EmployeeService employeeService) {
        this.financeUser      = financeUser;
        this.employeeService  = employeeService;
        this.payrollLogService = new PayrollLogService();

        setTitle("Finance Dashboard — " + financeUser.getFullName());
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(FinanceDashboard.this)) {
                    dispose();
                }
            }
        });

        buildUI();
        loadPayrollTable();
        loadDetailTable();
        loadPeriodEmployeeTable();
    }

    // =========================================================================
    //  UI construction
    // =========================================================================

    private void buildUI() {
        JPanel header = buildHeaderPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Payroll Overview",    buildOverviewTab());
        tabs.addTab("Detailed Report",     buildDetailedReportTab());
        tabs.addTab("Payroll by Period",   buildPayrollByPeriodTab());
        tabs.setBackground(new Color(248, 242, 243));
        tabs.setForeground(new Color(86, 33, 40));
        tabs.setFont(new Font("Noto Sans", Font.BOLD, 13));
        UITheme.styleTabs(tabs);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 242, 243));
        getContentPane().add(header,              BorderLayout.NORTH);
        getContentPane().add(tabs,                BorderLayout.CENTER);
        getContentPane().add(buildFooterPanel(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(FINANCE_RED_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel title = new JLabel("Finance Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Noto Sans", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " + financeUser.getFullName(), SwingConstants.RIGHT);
        welcome.setFont(new Font("Noto Sans", Font.BOLD, 14));
        welcome.setForeground(Color.WHITE);
        panel.add(title,   BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(248, 242, 243));

        JButton btnMyDashboard = new JButton("My Employee Dashboard");
        UITheme.styleSecondaryButton(btnMyDashboard);
        btnMyDashboard.addActionListener(e -> {
            dispose();
            EmployeeDashboard empDash = new EmployeeDashboard(financeUser, () -> {
                FinanceDashboard back = new FinanceDashboard(financeUser, employeeService);
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

    // =========================================================================
    //  Tab 1: Payroll Overview
    // =========================================================================

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 242, 243));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        payrollModel = new DefaultTableModel(
                new String[]{
                    "Employee ID", "Full Name", "Position",
                    "Basic Salary", "Gross (Monthly Est.)", "Total Deductions", "Net (Monthly Est.)"
                }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        payrollTable = new JTable(payrollModel);
        payrollTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        payrollTable.setFillsViewportHeight(true);
        payrollTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane payrollScroll = new JScrollPane(payrollTable);
        styleTable(payrollTable, payrollScroll);

        JButton btnRefresh = new JButton("Refresh");
        styleRefreshButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadPayrollTable());

        JLabel sectionTitle = new JLabel("Payroll Overview");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(86, 33, 40));

        JLabel subtitle = new JLabel("Monthly Salary Estimate ");
        subtitle.setFont(new Font("Noto Sans", Font.PLAIN, 12));
        subtitle.setForeground(new Color(130, 70, 80));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new javax.swing.BoxLayout(titleStack, javax.swing.BoxLayout.Y_AXIS));
        titleStack.add(sectionTitle);
        titleStack.add(subtitle);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(titleStack, BorderLayout.WEST);
        topBar.add(buttonBar, BorderLayout.EAST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tableCard.add(payrollScroll, BorderLayout.CENTER);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    private void loadPayrollTable() {
        payrollModel.setRowCount(0);
        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee emp : employees) {
            payrollModel.addRow(new Object[]{
                emp.getEmployeeID(),
                emp.getFullName(),
                emp.getPosition(),
                String.format("%.2f", emp.getBasicSalary()),
                String.format("%.2f", emp.calculateGrossSalary()),
                String.format("%.2f", emp.calculateDeductions()),
                String.format("%.2f", emp.calculateNetSalary())
            });
        }
    }

    // =========================================================================
    //  Tab 2: Detailed Report
    // =========================================================================

    private JPanel buildDetailedReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 242, 243));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        detailModel = new DefaultTableModel(
                new String[]{
                    "Emp ID", "Full Name",
                    "Basic Salary", "Allowances",
                    "SSS", "PhilHealth", "Pag-IBIG", "Withholding Tax",
                    "Total Deductions", "Net Salary"
                }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        detailTable = new JTable(detailModel);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setFillsViewportHeight(true);
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane tableScroll = new JScrollPane(detailTable);
        tableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        styleTable(detailTable, tableScroll);
        configureDetailedReportColumns();

        reportTextArea = new JTextArea(8, 80);
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportTextArea.setText("Figures are monthly estimates (basic salary basis). Use 'Payroll by Period' for attendance-based results.");
        reportTextArea.setBackground(Color.WHITE);
        reportTextArea.setForeground(new Color(48, 36, 38));
        reportTextArea.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JScrollPane textScroll = new JScrollPane(reportTextArea);
        textScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        textScroll.getViewport().setBackground(Color.WHITE);

        JButton btnRefresh  = new JButton("Refresh Table");
        JButton btnGenerate = new JButton("Generate Report");
        styleRefreshButton(btnRefresh);
        styleButton(btnGenerate, true);
        btnRefresh.addActionListener(e  -> loadDetailTable());
        btnGenerate.addActionListener(e -> generateDetailedReport());

        JLabel sectionTitle = new JLabel("Detailed Payroll Report");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(86, 33, 40));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(btnRefresh);
        buttonBar.add(btnGenerate);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        tableHeader.add(sectionTitle, BorderLayout.WEST);
        tableHeader.add(buttonBar, BorderLayout.EAST);

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        lblDetailEmpName  = new JLabel("—", SwingConstants.CENTER);
        lblDetailEmpName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailBasic      = new JLabel("—");
        lblDetailGross      = new JLabel("—");
        lblDetailSSS        = new JLabel("—");
        lblDetailPhilHealth = new JLabel("—");
        lblDetailPagibig    = new JLabel("—");
        lblDetailTax        = new JLabel("—");
        lblDetailTotalDed   = new JLabel("—");
        lblDetailNet        = new JLabel("—");

        JPanel detailGrid = new JPanel(new GridLayout(0, 4, 15, 6));
        addLabelValuePair(detailGrid, "Basic Salary:",    lblDetailBasic,    "Gross Salary:",    lblDetailGross);
        addLabelValuePair(detailGrid, "SSS:",             lblDetailSSS,      "PhilHealth:",      lblDetailPhilHealth);
        addLabelValuePair(detailGrid, "Pag-IBIG:",        lblDetailPagibig,  "Withholding Tax:", lblDetailTax);
        addLabelValuePair(detailGrid, "Total Deductions:", lblDetailTotalDed, "Net Salary:",     lblDetailNet);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 5));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                        "Selected Employee Payslip"
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        detailPanel.add(lblDetailEmpName, BorderLayout.NORTH);
        detailPanel.add(detailGrid,       BorderLayout.CENTER);

        // Wire row-selection — cancels any in-flight worker before starting a new one
        detailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = detailTable.getSelectedRow();
                if (row >= 0) {
                    String empId = (String) detailModel.getValueAt(row, 0);
                    loadEmployeePayslipDetail(empId);
                }
            }
        });

        JPanel reportCard = new JPanel(new BorderLayout(0, 8));
        reportCard.setBackground(Color.WHITE);
        reportCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        JLabel reportTitle = new JLabel("Payroll Summary Report");
        reportTitle.setFont(new Font("Noto Sans", Font.BOLD, 16));
        reportTitle.setForeground(new Color(86, 33, 40));
        reportCard.add(reportTitle, BorderLayout.NORTH);
        reportCard.add(textScroll, BorderLayout.CENTER);

        JSplitPane southSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reportCard, detailPanel);
        southSplit.setDividerLocation(210);
        southSplit.setResizeWeight(0.5);
        southSplit.setBorder(BorderFactory.createEmptyBorder());

        panel.add(tableCard, BorderLayout.CENTER);
        panel.add(southSplit, BorderLayout.SOUTH);
        return panel;
    }

    private void configureDetailedReportColumns() {
        int[] widths = {90, 220, 120, 120, 95, 120, 105, 135, 140, 120};
        for (int i = 0; i < widths.length && i < detailTable.getColumnModel().getColumnCount(); i++) {
            detailTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            detailTable.getColumnModel().getColumn(i).setMinWidth(widths[i]);
        }
    }

    // Adds a bold-name / value pair (×2) to a 4-column grid panel.
    private void addLabelValuePair(JPanel grid,
                                   String leftName,  JLabel leftValue,
                                   String rightName, JLabel rightValue) {
        JLabel lName = new JLabel(leftName);
        lName.setFont(lName.getFont().deriveFont(Font.BOLD));
        JLabel rName = new JLabel(rightName);
        rName.setFont(rName.getFont().deriveFont(Font.BOLD));
        grid.add(lName);
        grid.add(leftValue);
        grid.add(rName);
        grid.add(rightValue);
    }

    // Loads payslip detail for one employee in background; updates Tab 2 labels in done().
    private void loadEmployeePayslipDetail(String empId) {
        new SwingWorker<SalaryDetails, Void>() {
            @Override
            protected SalaryDetails doInBackground() throws Exception {
                return new PayrollCalculatorService().getSalaryDetails(empId);
            }

            @Override
            protected void done() {
                try {
                    SalaryDetails d = get();
                    if (d == null) {
                        clearPayslipDetail();
                        return;
                    }
                    double basicSalary = d.grossSalary() - d.totalAllowances();
                    lblDetailBasic.setText(String.format("%.2f", basicSalary));
                    lblDetailGross.setText(String.format("%.2f", d.grossSalary()));
                    lblDetailSSS.setText(String.format("%.2f", d.sssDeduction()));
                    lblDetailPhilHealth.setText(String.format("%.2f", d.philHealthDeduction()));
                    lblDetailPagibig.setText(String.format("%.2f", d.pagibigDeduction()));
                    lblDetailTax.setText(String.format("%.2f", d.withholdingTax()));
                    lblDetailTotalDed.setText(String.format("%.2f", d.totalDeductions()));
                    lblDetailNet.setText(String.format("%.2f", d.netSalary()));
                    lblDetailEmpName.setText("Employee #" + empId);
                } catch (InterruptedException | ExecutionException ex) {
                    clearPayslipDetail();
                }
            }
        }.execute();
    }

    private void clearPayslipDetail() {
        lblDetailEmpName.setText("—");
        lblDetailBasic.setText("—");
        lblDetailGross.setText("—");
        lblDetailSSS.setText("—");
        lblDetailPhilHealth.setText("—");
        lblDetailPagibig.setText("—");
        lblDetailTax.setText("—");
        lblDetailTotalDed.setText("—");
        lblDetailNet.setText("—");
    }

    private void loadDetailTable() {
        detailModel.setRowCount(0);
        List<Employee> employees = employeeService.getAllEmployees();
        for (Employee emp : employees) {
            double basic      = emp.getBasicSalary();
            double allowances = emp.getAllowance();
            double sss        = PayrollCalculator.computeSSSDeduction(basic);
            double philHealth = PayrollCalculator.computePhilhealthDeduction(basic);
            double pagIbig    = PayrollCalculator.computePagibigDeduction(basic);
            double tax        = PayrollCalculator.computeWithholdingTax(basic);
            double totalDeduct = emp.calculateDeductions();
            double net        = emp.calculateNetSalary();

            detailModel.addRow(new Object[]{
                emp.getEmployeeID(),
                emp.getFullName(),
                String.format("%.2f", basic),
                String.format("%.2f", allowances),
                String.format("%.2f", sss),
                String.format("%.2f", philHealth),
                String.format("%.2f", pagIbig),
                String.format("%.2f", tax),
                String.format("%.2f", totalDeduct),
                String.format("%.2f", net)
            });
        }
    }

    // Column indices for detailModel — must match the schema defined in buildDetailedReportTab().
    private static final int DETAIL_COL_EMP_ID      = 0;
    private static final int DETAIL_COL_FULL_NAME   = 1;
    private static final int DETAIL_COL_BASIC       = 2;
    private static final int DETAIL_COL_ALLOWANCES  = 3;
    private static final int DETAIL_COL_SSS         = 4;
    private static final int DETAIL_COL_PHILHEALTH  = 5;
    private static final int DETAIL_COL_PAGIBIG     = 6;
    private static final int DETAIL_COL_TAX         = 7;
    private static final int DETAIL_COL_TOTAL_DED   = 8;
    private static final int DETAIL_COL_NET         = 9;

    // Expected column count for detailModel — used as a structural guard.
    private static final int DETAIL_EXPECTED_COLUMNS = 10;

    // Generates a formatted payroll summary from the already-populated detailModel.
    private void generateDetailedReport() {
        if (detailModel.getRowCount() == 0) {
            reportTextArea.setText("No payroll data to report. Please refresh the table first.");
            return;
        }

        if (detailModel.getColumnCount() != DETAIL_EXPECTED_COLUMNS) {
            throw new IllegalStateException(
                    "detailModel schema mismatch: expected " + DETAIL_EXPECTED_COLUMNS
                    + " columns but found " + detailModel.getColumnCount()
                    + ". Update DETAIL_COL_* constants to match the new schema.");
        }

        double totalBasic  = 0, totalAllow  = 0, totalSss  = 0;
        double totalPh     = 0, totalPib    = 0, totalTax  = 0;
        double totalDeduct = 0, totalNet    = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("MotorPH Payroll Detailed Report\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-10s %-22s %10s %10s %8s %10s %9s %12s %12s %12s%n",
                "Emp ID", "Full Name", "Basic", "Allowance",
                "SSS", "PhilHealth", "PagIbig", "Tax", "Deductions", "Net Pay"));
        sb.append("-".repeat(120)).append("\n");

        int rowCount = detailModel.getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String empId  = (String) detailModel.getValueAt(row, DETAIL_COL_EMP_ID);
            String name   = (String) detailModel.getValueAt(row, DETAIL_COL_FULL_NAME);
            double basic  = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_BASIC));
            double allow  = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_ALLOWANCES));
            double sss    = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_SSS));
            double ph     = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_PHILHEALTH));
            double pib    = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_PAGIBIG));
            double tax    = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_TAX));
            double deduct = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_TOTAL_DED));
            double net    = Double.parseDouble((String) detailModel.getValueAt(row, DETAIL_COL_NET));

            totalBasic  += basic;  totalAllow  += allow;
            totalSss    += sss;    totalPh     += ph;
            totalPib    += pib;    totalTax    += tax;
            totalDeduct += deduct; totalNet    += net;

            String displayName = name.length() > 22 ? name.substring(0, 19) + "..." : name;
            sb.append(String.format("%-10s %-22s %10.2f %10.2f %8.2f %10.2f %9.2f %12.2f %12.2f %12.2f%n",
                    empId, displayName, basic, allow, sss, ph, pib, tax, deduct, net));
        }

        sb.append("=".repeat(120)).append("\n");
        sb.append(String.format("%-10s %-22s %10.2f %10.2f %8.2f %10.2f %9.2f %12.2f %12.2f %12.2f%n",
                "", "TOTALS",
                totalBasic, totalAllow, totalSss, totalPh, totalPib, totalTax, totalDeduct, totalNet));
        sb.append(String.format("%nTotal Employees: %d%n", rowCount));

        reportTextArea.setText(sb.toString());
        reportTextArea.setCaretPosition(0);
    }

    // =========================================================================
    //  Tab 3: Payroll by Period
    // =========================================================================

    private JPanel buildPayrollByPeriodTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(248, 242, 243));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- Top control bar: coverage selectors + Compute + Refresh Log buttons ---
        coverageStartChooser = new DateDropdownPanel();
        coverageEndChooser = new DateDropdownPanel();
        cutoffPeriodCombo = new JComboBox<>(new String[]{
            "Custom Range",
            "First Cutoff (1-15)",
            "Second Cutoff (16-End)"
        });
        cutoffPeriodCombo.addActionListener(e -> applyCutoffSelection());
        UITheme.styleComboBox(cutoffPeriodCombo);
        cutoffPeriodCombo.setPreferredSize(new java.awt.Dimension(190, 38));
        coverageStartChooser.setFieldWidths(82, 78, 96);
        coverageEndChooser.setFieldWidths(82, 78, 96);

        JButton btnComputePeriod = new JButton("Compute Period");
        styleButton(btnComputePeriod, true);
        btnComputePeriod.addActionListener(e -> runBatchPayrollForPeriod());

        JButton btnRefreshLog = new JButton("Refresh Log");
        styleRefreshButton(btnRefreshLog);
        btnRefreshLog.addActionListener(e -> {
            // Validate preconditions and give specific feedback before attempting the load.
            LocalDate rlStart = coverageStartChooser.getSelectedDate();
            LocalDate rlEnd   = coverageEndChooser.getSelectedDate();
            if (rlStart == null || rlEnd == null) {
                JOptionPane.showMessageDialog(FinanceDashboard.this,
                        "Please select both a coverage start date and end date before refreshing the log.",
                        "Coverage Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (rlEnd.isBefore(rlStart)) {
                JOptionPane.showMessageDialog(FinanceDashboard.this,
                        "Coverage end date must be on or after the start date.",
                        "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (rlStart.getMonthValue() != rlEnd.getMonthValue() || rlStart.getYear() != rlEnd.getYear()) {
                JOptionPane.showMessageDialog(FinanceDashboard.this,
                        "Coverage start and end dates must be within the same calendar month.",
                        "Cross-Month Range", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (getSelectedPeriodEmployeeId() == null) {
                JOptionPane.showMessageDialog(FinanceDashboard.this,
                        "Please select an employee from the list before refreshing the log.",
                        "Employee Required", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            loadPeriodLogTable();
        });

        JLabel sectionTitle = new JLabel("Payroll by Period");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(86, 33, 40));

        JPanel controlGrid = new JPanel(new GridBagLayout());
        controlGrid.setOpaque(false);
        GridBagConstraints controlGbc = new GridBagConstraints();
        controlGbc.insets = new Insets(6, 0, 6, 12);
        controlGbc.anchor = GridBagConstraints.WEST;
        controlGbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel cutoffLabel = new JLabel("Cutoff Period:");
        JLabel startLabel = new JLabel("Coverage Start:");
        JLabel endLabel = new JLabel("Coverage End:");
        cutoffLabel.setFont(new Font("Noto Sans", Font.BOLD, 14));
        startLabel.setFont(new Font("Noto Sans", Font.BOLD, 14));
        endLabel.setFont(new Font("Noto Sans", Font.BOLD, 14));
        cutoffLabel.setForeground(new Color(62, 35, 39));
        startLabel.setForeground(new Color(62, 35, 39));
        endLabel.setForeground(new Color(62, 35, 39));

        controlGbc.gridx = 0;
        controlGbc.gridy = 0;
        controlGrid.add(cutoffLabel, controlGbc);

        controlGbc.gridx = 1;
        controlGbc.weightx = 0.32;
        controlGrid.add(cutoffPeriodCombo, controlGbc);

        controlGbc.gridx = 2;
        controlGbc.weightx = 0;
        controlGrid.add(startLabel, controlGbc);

        controlGbc.gridx = 3;
        controlGbc.weightx = 0.34;
        controlGrid.add(coverageStartChooser, controlGbc);

        controlGbc.gridx = 4;
        controlGbc.weightx = 0;
        controlGrid.add(endLabel, controlGbc);

        controlGbc.gridx = 5;
        controlGbc.weightx = 0.34;
        controlGbc.insets = new Insets(6, 0, 6, 0);
        controlGrid.add(coverageEndChooser, controlGbc);

        // Month/year picker — auto-fills coverage dates and enables "Run All Employees"
        String[] monthNames = {
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"
        };
        monthCombo = new JComboBox<>(monthNames);
        UITheme.styleComboBox(monthCombo);
        monthCombo.setPreferredSize(new java.awt.Dimension(130, 38));

        int latestYear = LocalDate.now().getYear();
        Integer[] years = new Integer[latestYear - 2024 + 1];
        for (int i = 0; i < years.length; i++) years[i] = 2024 + i;
        yearCombo = new JComboBox<>(years);
        UITheme.styleComboBox(yearCombo);
        yearCombo.setPreferredSize(new java.awt.Dimension(90, 38));

        // Default to previous complete month so Finance can run payroll with one click
        YearMonth prevMonth = YearMonth.now().minusMonths(1);
        monthCombo.setSelectedIndex(prevMonth.getMonthValue() - 1);
        yearCombo.setSelectedItem(prevMonth.getYear());

        java.awt.event.ActionListener monthYearListener = e -> applyMonthPickerSelection();
        monthCombo.addActionListener(monthYearListener);
        yearCombo.addActionListener(monthYearListener);

        JButton btnRunAllEmployees = new JButton("Run All Employees");
        styleButton(btnRunAllEmployees, false);
        btnRunAllEmployees.addActionListener(e -> runAllEmployeesForPeriod());

        JLabel pickerSeparator = new JLabel(" │ ");
        pickerSeparator.setForeground(new Color(180, 160, 165));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(monthCombo);
        buttonBar.add(yearCombo);
        buttonBar.add(btnRunAllEmployees);
        buttonBar.add(pickerSeparator);
        buttonBar.add(btnRefreshLog);
        buttonBar.add(btnComputePeriod);

        JPanel controlCard = new JPanel(new BorderLayout(0, 10));
        controlCard.setBackground(Color.WHITE);
        controlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        controlCard.add(sectionTitle, BorderLayout.NORTH);
        controlCard.add(controlGrid, BorderLayout.CENTER);
        controlCard.add(buttonBar, BorderLayout.SOUTH);

        // --- Left panel: searchable employee list ---
        periodEmpModel = new DefaultTableModel(new String[]{"Emp #", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        periodEmpTable = new JTable(periodEmpModel);
        periodEmpTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        periodEmpTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane periodEmpScroll = new JScrollPane(periodEmpTable);
        styleTable(periodEmpTable, periodEmpScroll);

        periodSorter = new TableRowSorter<>(periodEmpModel);
        periodEmpTable.setRowSorter(periodSorter);

        txtPeriodSearch = new JTextField(15);
        UITheme.styleTextField(txtPeriodSearch);
        txtPeriodSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterPeriodTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterPeriodTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterPeriodTable(); }
        });

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Noto Sans", Font.BOLD, 13));
        searchLabel.setForeground(new Color(62, 35, 39));
        searchBar.add(searchLabel, BorderLayout.WEST);
        searchBar.add(txtPeriodSearch, BorderLayout.CENTER);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 4));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                        "Employees"
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        leftPanel.add(searchBar,                        BorderLayout.NORTH);
        leftPanel.add(periodEmpScroll,  BorderLayout.CENTER);

        // Wire row-selection — cancels prior worker before starting a new one
        periodEmpTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = periodEmpTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = periodEmpTable.convertRowIndexToModel(viewRow);
                    String empId = (String) periodEmpModel.getValueAt(modelRow, 0);
                    showPeriodPayslip(empId);
                    loadPeriodLogTable();
                }
            }
        });

        // --- Right panel: payslip detail labels ---
        JPanel rightPanel = buildPeriodDetailPanel();

        // --- Split: left list | right detail ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(360);
        splitPane.setResizeWeight(0.32);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // --- Bottom panel: payroll log table ---
        periodLogModel = new DefaultTableModel(
                new String[]{"Emp #", "Employee Name", "Period Start", "Period End", "Gross Salary", "Total Allowance", "Total Deductions", "Net Salary"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        periodLogTable = new JTable(periodLogModel);
        periodLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        periodLogTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane logScroll = new JScrollPane(periodLogTable);
        styleTable(periodLogTable, logScroll);
        logScroll.setPreferredSize(new java.awt.Dimension(0, 150));

        periodLogPanel = new JPanel(new BorderLayout());
        periodLogPanel.setBackground(Color.WHITE);
        periodLogPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                        "Payroll Log — Select coverage and employee above"
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        periodLogPanel.add(logScroll, BorderLayout.CENTER);

        // Wire coverage updates only after all dependent components are ready.
        wireCoverageChooser(coverageStartChooser);
        wireCoverageChooser(coverageEndChooser);
        applyDefaultCutoff();

        panel.add(controlCard,   BorderLayout.NORTH);
        panel.add(splitPane,     BorderLayout.CENTER);
        panel.add(periodLogPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        if (primary) {
            button.setBackground(FINANCE_RED);
            button.setForeground(Color.WHITE);
        } else {
            UITheme.styleNeutralButton(button, new Color(92, 31, 38));
            return;
        }
    }

    private void styleRefreshButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(FINANCE_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FINANCE_GREEN_DARK, 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
    }

    private void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(41, 34, 36));
        table.setSelectionBackground(new Color(245, 216, 220));
        table.setSelectionForeground(new Color(92, 31, 38));
        table.setGridColor(new Color(236, 230, 231));
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(new Color(239, 236, 237));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
    }

    // Builds the right-panel label grid for the "Payroll by Period" tab.
    private JPanel buildPeriodDetailPanel() {
        // Initialise all display labels
        lblPeriodStatus       = new JLabel("Select a coverage and employee to view payslip");
        lblPeriodEmpNum       = new JLabel("—");
        lblPeriodName         = new JLabel("—");
        lblPeriodSssNum       = new JLabel("—");
        lblPeriodPhilHealthNum = new JLabel("—");
        lblPeriodTin          = new JLabel("—");
        lblPeriodPagibigNum   = new JLabel("—");
        lblPeriodRice         = new JLabel("—");
        lblPeriodPhone        = new JLabel("—");
        lblPeriodClothing     = new JLabel("—");
        lblPeriodTotalAllow   = new JLabel("—");
        lblPeriodSss          = new JLabel("—");
        lblPeriodPhilHealth   = new JLabel("—");
        lblPeriodPagibig      = new JLabel("—");
        lblPeriodTax          = new JLabel("—");
        lblPeriodTotalDed     = new JLabel("—");
        lblPeriodGross        = new JLabel("—");
        lblPeriodNet          = new JLabel("—");
        stylePeriodValueLabel(lblPeriodStatus);
        stylePeriodValueLabel(lblPeriodEmpNum);
        stylePeriodValueLabel(lblPeriodName);
        stylePeriodValueLabel(lblPeriodSssNum);
        stylePeriodValueLabel(lblPeriodPhilHealthNum);
        stylePeriodValueLabel(lblPeriodTin);
        stylePeriodValueLabel(lblPeriodPagibigNum);
        stylePeriodValueLabel(lblPeriodRice);
        stylePeriodValueLabel(lblPeriodPhone);
        stylePeriodValueLabel(lblPeriodClothing);
        stylePeriodValueLabel(lblPeriodTotalAllow);
        stylePeriodValueLabel(lblPeriodSss);
        stylePeriodValueLabel(lblPeriodPhilHealth);
        stylePeriodValueLabel(lblPeriodPagibig);
        stylePeriodValueLabel(lblPeriodTax);
        stylePeriodValueLabel(lblPeriodTotalDed);
        stylePeriodValueLabel(lblPeriodGross);
        stylePeriodValueLabel(lblPeriodNet);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Status hint row — spans all 4 columns
        gc.gridx = 0; gc.gridy = row++; gc.gridwidth = 4;
        grid.add(lblPeriodStatus, gc);
        gc.gridwidth = 1;

        // Identity section
        addPeriodRow(grid, gc, row++, "Employee #:",    lblPeriodEmpNum,       "Full Name:",         lblPeriodName);
        addPeriodRow(grid, gc, row++, "SSS #:",         lblPeriodSssNum,       "PhilHealth #:",      lblPeriodPhilHealthNum);
        addPeriodRow(grid, gc, row++, "TIN:",           lblPeriodTin,          "Pag-IBIG #:",        lblPeriodPagibigNum);

        // Separator label
        gc.gridx = 0; gc.gridy = row++; gc.gridwidth = 4;
        JLabel sepEarnings = new JLabel("— Earnings —");
        sepEarnings.setFont(sepEarnings.getFont().deriveFont(Font.BOLD));
        sepEarnings.setForeground(new Color(86, 33, 40));
        grid.add(sepEarnings, gc);
        gc.gridwidth = 1;

        addPeriodRow(grid, gc, row++, "Rice Allowance:",    lblPeriodRice,       "Phone Allowance:",   lblPeriodPhone);
        addPeriodRow(grid, gc, row++, "Clothing Allowance:", lblPeriodClothing,  "Total Allowances:",  lblPeriodTotalAllow);

        // Separator label
        gc.gridx = 0; gc.gridy = row++; gc.gridwidth = 4;
        JLabel sepDeductions = new JLabel("— Deductions —");
        sepDeductions.setFont(sepDeductions.getFont().deriveFont(Font.BOLD));
        sepDeductions.setForeground(new Color(86, 33, 40));
        grid.add(sepDeductions, gc);
        gc.gridwidth = 1;

        addPeriodRow(grid, gc, row++, "SSS:",           lblPeriodSss,          "PhilHealth:",        lblPeriodPhilHealth);
        addPeriodRow(grid, gc, row++, "Pag-IBIG:",      lblPeriodPagibig,      "Withholding Tax:",   lblPeriodTax);
        addPeriodRow(grid, gc, row++, "Total Deductions:", lblPeriodTotalDed,   "",                   new JLabel(""));

        // Separator label
        gc.gridx = 0; gc.gridy = row++; gc.gridwidth = 4;
        JLabel sepSummary = new JLabel("— Summary —");
        sepSummary.setFont(sepSummary.getFont().deriveFont(Font.BOLD));
        sepSummary.setForeground(new Color(86, 33, 40));
        grid.add(sepSummary, gc);
        gc.gridwidth = 1;

        addPeriodRow(grid, gc, row++, "Gross Salary:",  lblPeriodGross,        "Net Salary:",        lblPeriodNet);

        // Push everything to the top
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        grid.add(new JPanel(), gc);

        JScrollPane detailScrollPane = new JScrollPane(grid);
        detailScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailScrollPane.getViewport().setBackground(Color.WHITE);
        detailScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        detailScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        detailScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(214, 206, 208), 1, true),
                        "Payslip Detail"
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        wrapper.add(detailScrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    // Adds one row (2 label+value pairs) to a GridBagLayout panel.
    private void addPeriodRow(JPanel panel, GridBagConstraints gc, int row,
                               String leftLabel, JLabel leftValue,
                               String rightLabel, JLabel rightValue) {
        gc.weightx = 0;
        gc.gridx = 0; gc.gridy = row;
        JLabel lbl1 = new JLabel(leftLabel);
        lbl1.setFont(lbl1.getFont().deriveFont(Font.BOLD));
        lbl1.setForeground(new Color(62, 35, 39));
        panel.add(lbl1, gc);

        gc.weightx = 0.4;
        gc.gridx = 1;
        panel.add(leftValue, gc);

        gc.weightx = 0;
        gc.gridx = 2;
        JLabel lbl2 = new JLabel(rightLabel);
        lbl2.setFont(lbl2.getFont().deriveFont(Font.BOLD));
        lbl2.setForeground(new Color(62, 35, 39));
        panel.add(lbl2, gc);

        gc.weightx = 0.4;
        gc.gridx = 3;
        panel.add(rightValue, gc);
    }

    private void stylePeriodValueLabel(JLabel label) {
        label.setFont(new Font("Noto Sans", Font.PLAIN, 14));
        label.setForeground(new Color(48, 36, 38));
    }

    // Populates the left-panel employee table with all employees from EmployeeService.
    private void loadPeriodEmployeeTable() {
        periodEmpModel.setRowCount(0);
        for (Employee emp : employeeService.getAllEmployees()) {
            periodEmpModel.addRow(new Object[]{emp.getEmployeeID(), emp.getFullName()});
        }
    }

    // Applies a case-insensitive filter to both columns of the period employee table.
    private void filterPeriodTable() {
        String text = txtPeriodSearch.getText().trim();
        if (text.isEmpty()) {
            periodSorter.setRowFilter(null);
        } else {
            periodSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    // Populates the bottom log table with stored payroll runs for the selected period.
    // Shows a placeholder row when no period is selected or no logs exist for the period.
    private void loadPeriodLogTable() {
        periodLogModel.setRowCount(0);

        LocalDate[] coverage = getSelectedCoverage();
        String selectedEmpId = getSelectedPeriodEmployeeId();
        if (coverage == null) {
            periodLogPanel.setBorder(BorderFactory.createTitledBorder("Payroll Log — Select coverage and employee above"));
            periodLogModel.addRow(new Object[]{"—", "Select a start date and end date above", "", "", "", "", "", ""});
            return;
        }

        if (selectedEmpId == null) {
            periodLogPanel.setBorder(BorderFactory.createTitledBorder("Payroll Log — Select an employee"));
            periodLogModel.addRow(new Object[]{"—", "Select an employee from the list", "", "", "", "", "", ""});
            return;
        }

        int month = coverage[0].getMonthValue();
        int year  = coverage[0].getYear();
        String periodLabel = coverage[0] + " to " + coverage[1];
        periodLogPanel.setBorder(BorderFactory.createTitledBorder(
                "Payroll Log — " + selectedEmpId + " | " + periodLabel));

        List<PayrollLog> logs = payrollLogService.getLogsByPeriod(month, year);
        logs.removeIf(log -> !selectedEmpId.equals(log.empNum()));

        if (logs.isEmpty()) {
            periodLogModel.addRow(new Object[]{"—", "No payroll run for the selected employee/coverage", "", "", "", "", "", ""});
            return;
        }

        double totalGross = 0, totalAllow = 0, totalDed = 0, totalNet = 0;

        for (PayrollLog log : logs) {
            Employee emp = employeeService.getEmployeeById(log.empNum());
            String name  = emp != null ? emp.getFullName() : log.empNum();
            periodLogModel.addRow(new Object[]{
                log.empNum(),
                name,
                log.periodStart() != null ? log.periodStart().toString() : "",
                log.periodEnd()   != null ? log.periodEnd().toString()   : "",
                String.format("%.2f", log.grossSalary()),
                String.format("%.2f", log.totalAllowance()),
                String.format("%.2f", log.totalDeductions()),
                String.format("%.2f", log.netSalary())
            });
            totalGross += log.grossSalary();
            totalAllow += log.totalAllowance();
            totalDed   += log.totalDeductions();
            totalNet   += log.netSalary();
        }

        // Totals row
        periodLogModel.addRow(new Object[]{
            "TOTAL",
            "",
            "",
            "",
            String.format("%.2f", totalGross),
            String.format("%.2f", totalAllow),
            String.format("%.2f", totalDed),
            String.format("%.2f", totalNet)
        });
    }

    // Loads payslip detail for the selected employee and period into the right panel.
    // If no period is selected, clears the panel and shows a hint message.
    // Cancels any in-flight worker before starting a new one.
    private void showPeriodPayslip(String empId) {
        if (currentPeriodWorker != null && !currentPeriodWorker.isDone()) {
            currentPeriodWorker.cancel(true);
        }

        LocalDate[] coverage = getSelectedCoverage();
        if (coverage == null) {
            clearPeriodDetail();
            lblPeriodStatus.setText("Select a coverage start and end date above");
            return;
        }

        SwingWorker<Payroll, Void> worker = new SwingWorker<>() {
            // Set in doInBackground(); read in done(). Non-null means a saved log entry was found
            // for the selected employee + period and its totals should be shown instead of the
            // live-computed values — fixing QA-4 (detail panel diverged from the logged value).
            private PayrollLog logEntry = null;

            @Override
            protected Payroll doInBackground() {
                Employee emp = employeeService.getEmployeeById(empId);
                if (emp == null) return null;
                // PH semi-monthly payroll: 1st–15th = 15 days apart (between = 14), 16th–last day = 15 days apart (between = 15).
                // Any range ≤ 16 days is treated as BIWEEKLY; longer ranges are MONTHLY.
                long days = ChronoUnit.DAYS.between(coverage[0], coverage[1]);
                PeriodType periodType = (days <= 16) ? PeriodType.BIWEEKLY : PeriodType.MONTHLY;
                Payroll payroll = new Payroll(empId, emp, coverage[0], coverage[1]);
                payroll.calculateNetSalary(periodType);
                // Look up any saved log entry for this exact period — used in done() to
                // override aggregate totals so the detail panel matches the Payroll Log.
                logEntry = payrollLogService.getSingleLogByPeriod(empId, coverage[0], coverage[1]);
                return payroll;
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    Payroll payroll = get();
                    Employee emp = employeeService.getEmployeeById(empId);
                    GovernmentDetails gov = emp != null ? emp.getGovernmentDetails() : null;
                    if (payroll == null) {
                        clearPeriodDetail();
                        lblPeriodStatus.setText("Employee data not found");
                        return;
                    }
                    if (logEntry != null) {
                        populatePeriodDetailFromPayrollWithLog(empId, payroll, gov, logEntry);
                    } else {
                        populatePeriodDetailFromPayroll(empId, payroll, gov);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    clearPeriodDetail();
                    lblPeriodStatus.setText("Error loading payslip: " + ex.getMessage());
                }
            }
        };

        currentPeriodWorker = worker;
        worker.execute();
    }

    // Populates all right-panel labels with the given salary details and government IDs.
    private void populatePeriodDetail(String empId, SalaryDetails d, GovernmentDetails gov) {
        if (d == null) {
            clearPeriodDetail();
            return;
        }
        Employee emp = employeeService.getEmployeeById(empId);
        lblPeriodEmpNum.setText(empId);
        lblPeriodName.setText(emp != null ? emp.getFullName() : "—");

        if (gov != null) {
            lblPeriodSssNum.setText(gov.getSssNumber());
            lblPeriodPhilHealthNum.setText(gov.getPhilHealthNumber());
            lblPeriodTin.setText(gov.getTinNumber());
            lblPeriodPagibigNum.setText(gov.getPagibigNumber());
        } else {
            lblPeriodSssNum.setText("—");
            lblPeriodPhilHealthNum.setText("—");
            lblPeriodTin.setText("—");
            lblPeriodPagibigNum.setText("—");
        }

        lblPeriodRice.setText(String.format("%.2f", d.riceSubsidy()));
        lblPeriodPhone.setText(String.format("%.2f", d.phoneAllowance()));
        lblPeriodClothing.setText(String.format("%.2f", d.clothingAllowance()));
        lblPeriodTotalAllow.setText(String.format("%.2f", d.totalAllowances()));
        lblPeriodSss.setText(String.format("%.2f", d.sssDeduction()));
        lblPeriodPhilHealth.setText(String.format("%.2f", d.philHealthDeduction()));
        lblPeriodPagibig.setText(String.format("%.2f", d.pagibigDeduction()));
        lblPeriodTax.setText(String.format("%.2f", d.withholdingTax()));
        lblPeriodTotalDed.setText(String.format("%.2f", d.totalDeductions()));
        lblPeriodGross.setText(String.format("%.2f", d.grossSalary()));
        lblPeriodNet.setText(String.format("%.2f", d.netSalary()));
    }

    // Populates right-panel labels from an attendance-based Payroll object.
    private void populatePeriodDetailFromPayroll(String empId, Payroll payroll, GovernmentDetails gov) {
        Employee emp = employeeService.getEmployeeById(empId);
        CompensationDetails cd  = payroll.getCompensationDetails();
        Deductions ded          = cd.getDeductions();
        Allowance allow         = cd.getAllowance();

        lblPeriodEmpNum.setText(empId);
        lblPeriodName.setText(emp != null ? emp.getFullName() : "—");

        if (gov != null) {
            lblPeriodSssNum.setText(gov.getSssNumber());
            lblPeriodPhilHealthNum.setText(gov.getPhilHealthNumber());
            lblPeriodTin.setText(gov.getTinNumber());
            lblPeriodPagibigNum.setText(gov.getPagibigNumber());
        } else {
            lblPeriodSssNum.setText("—");
            lblPeriodPhilHealthNum.setText("—");
            lblPeriodTin.setText("—");
            lblPeriodPagibigNum.setText("—");
        }

        lblPeriodRice.setText(String.format("%.2f", allow.getRiceAllowance()));
        lblPeriodPhone.setText(String.format("%.2f", allow.getPhoneAllowance()));
        lblPeriodClothing.setText(String.format("%.2f", allow.getClothingAllowance()));
        lblPeriodTotalAllow.setText(String.format("%.2f", allow.getTotal()));
        lblPeriodSss.setText(String.format("%.2f", ded.getSss()));
        lblPeriodPhilHealth.setText(String.format("%.2f", ded.getPhilHealth()));
        lblPeriodPagibig.setText(String.format("%.2f", ded.getPagIbig()));
        lblPeriodTax.setText(String.format("%.2f", ded.getTax()));
        lblPeriodTotalDed.setText(String.format("%.2f", ded.getTotal()));
        lblPeriodGross.setText(String.format("%.2f", cd.getGrossSalary()));
        lblPeriodNet.setText(String.format("%.2f", cd.getNetSalary()));

        // Update status line with attendance hours summary
        lblPeriodStatus.setText(String.format("Live estimate — %.2f regular hrs, %.2f OT hrs",
                payroll.getTotalRegularHours(), payroll.getTotalOvertimeHours()));
    }

    // Populates right-panel labels using live-computed breakdown fields but logged aggregate totals.
    // The breakdown (individual SSS/PhilHealth/PagIBIG/tax and allowance components) comes from
    // the live Payroll object because PayrollLog only stores aggregate totals.  The four aggregate
    // labels (totalAllowances, totalDeductions, grossSalary, netSalary) are overridden with the
    // values that were actually saved to the Payroll Log — fixing QA-4.
    private void populatePeriodDetailFromPayrollWithLog(String empId, Payroll payroll,
                                                        GovernmentDetails gov, PayrollLog log) {
        Employee emp = employeeService.getEmployeeById(empId);
        CompensationDetails cd  = payroll.getCompensationDetails();
        Deductions ded          = cd.getDeductions();
        Allowance allow         = cd.getAllowance();

        lblPeriodEmpNum.setText(empId);
        lblPeriodName.setText(emp != null ? emp.getFullName() : "—");

        if (gov != null) {
            lblPeriodSssNum.setText(gov.getSssNumber());
            lblPeriodPhilHealthNum.setText(gov.getPhilHealthNumber());
            lblPeriodTin.setText(gov.getTinNumber());
            lblPeriodPagibigNum.setText(gov.getPagibigNumber());
        } else {
            lblPeriodSssNum.setText("—");
            lblPeriodPhilHealthNum.setText("—");
            lblPeriodTin.setText("—");
            lblPeriodPagibigNum.setText("—");
        }

        // Breakdown — live computation (PayrollLog does not store individual components)
        lblPeriodRice.setText(String.format("%.2f", allow.getRiceAllowance()));
        lblPeriodPhone.setText(String.format("%.2f", allow.getPhoneAllowance()));
        lblPeriodClothing.setText(String.format("%.2f", allow.getClothingAllowance()));
        lblPeriodSss.setText(String.format("%.2f", ded.getSss()));
        lblPeriodPhilHealth.setText(String.format("%.2f", ded.getPhilHealth()));
        lblPeriodPagibig.setText(String.format("%.2f", ded.getPagIbig()));
        lblPeriodTax.setText(String.format("%.2f", ded.getTax()));

        // Aggregate totals — from the saved log entry so they match the Payroll Log table
        lblPeriodTotalAllow.setText(String.format("%.2f", log.totalAllowance()));
        lblPeriodTotalDed.setText(String.format("%.2f", log.totalDeductions()));
        lblPeriodGross.setText(String.format("%.2f", log.grossSalary()));
        lblPeriodNet.setText(String.format("%.2f", log.netSalary()));

        lblPeriodStatus.setText(String.format("Logged value — %s to %s",
                log.periodStart(), log.periodEnd()));
    }

    // Resets all right-panel labels to the placeholder dash.
    private void clearPeriodDetail() {
        lblPeriodStatus.setText("Select a coverage and employee to view payslip");
        lblPeriodEmpNum.setText("—");
        lblPeriodName.setText("—");
        lblPeriodSssNum.setText("—");
        lblPeriodPhilHealthNum.setText("—");
        lblPeriodTin.setText("—");
        lblPeriodPagibigNum.setText("—");
        lblPeriodRice.setText("—");
        lblPeriodPhone.setText("—");
        lblPeriodClothing.setText("—");
        lblPeriodTotalAllow.setText("—");
        lblPeriodSss.setText("—");
        lblPeriodPhilHealth.setText("—");
        lblPeriodPagibig.setText("—");
        lblPeriodTax.setText("—");
        lblPeriodTotalDed.setText("—");
        lblPeriodGross.setText("—");
        lblPeriodNet.setText("—");
    }

    // Validates the coverage + selected employee, computes one payroll run, and logs it.
    private void runBatchPayrollForPeriod() {
        LocalDate[] coverage = getSelectedCoverage();
        if (coverage == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid coverage start and end date.",
                    "Invalid Coverage", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedEmpId = getSelectedPeriodEmployeeId();
        if (selectedEmpId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an employee before computing payroll.",
                    "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int month = coverage[0].getMonthValue();
        int year  = coverage[0].getYear();

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Compute payroll for employee %s from %s to %s?",
                        selectedEmpId, coverage[0], coverage[1]),
                "Confirm Payroll Computation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                Employee emp = employeeService.getEmployeeById(selectedEmpId);
                if (emp == null) {
                    return false;
                }
                try {
                    // Period-specific duplicate check: returns false (not null) to signal "already done"
                    if (payrollLogService.isPeriodAlreadyLogged(selectedEmpId, coverage[0], coverage[1])) {
                        return null; // null signals duplicate period — done() shows a specific message
                    }

                    // PH semi-monthly payroll: 1st–15th = 15 days apart (between = 14), 16th–last day = 15 days apart (between = 15).
                    // Any range ≤ 16 days is treated as BIWEEKLY; longer ranges are MONTHLY.
                    long days = ChronoUnit.DAYS.between(coverage[0], coverage[1]);
                    PeriodType periodType = (days <= 16) ? PeriodType.BIWEEKLY : PeriodType.MONTHLY;

                    Payroll payroll = new Payroll(selectedEmpId, emp, coverage[0], coverage[1]);
                    payroll.calculateNetSalary(periodType);
                    CompensationDetails cd = payroll.getCompensationDetails();
                    payrollLogService.savePayrollRun(selectedEmpId, month, year,
                            coverage[0], coverage[1], cd);
                    Payslip payslip = payroll.generatePayslip();
                    new PayslipService().savePayslip(payslip);
                    return true;
                } catch (Exception ex) {
                    System.err.println("Payroll computation error for " + selectedEmpId + ": " + ex.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    Boolean result = get();
                    if (result == null) {
                        // Duplicate period — inform the user without treating it as an error
                        JOptionPane.showMessageDialog(FinanceDashboard.this,
                                String.format("Payroll for this period has already been computed.%nCoverage: %s to %s",
                                        coverage[0], coverage[1]),
                                "Already Computed",
                                JOptionPane.INFORMATION_MESSAGE);
                        showPeriodPayslip(selectedEmpId);
                        loadPeriodLogTable();
                        return;
                    }
                    if (!result) {
                        JOptionPane.showMessageDialog(FinanceDashboard.this,
                                "Payroll computation failed for the selected employee.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    JOptionPane.showMessageDialog(FinanceDashboard.this,
                            String.format("Payroll computed for employee %s.%nCoverage: %s to %s",
                                    selectedEmpId, coverage[0], coverage[1]),
                            "Payroll Computed",
                            JOptionPane.INFORMATION_MESSAGE);
                    showPeriodPayslip(selectedEmpId);
                    loadPeriodLogTable();
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(FinanceDashboard.this,
                            "Payroll computation failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Sets coverage pickers to the full calendar month selected in the month/year combo boxes.
    // Resets cutoffPeriodCombo to "Custom Range" so it does not override the full-month dates.
    private void applyMonthPickerSelection() {
        int month = monthCombo.getSelectedIndex() + 1;
        int year  = (Integer) yearCombo.getSelectedItem();
        YearMonth ym = YearMonth.of(year, month);
        updatingCutoffSelection = true;
        cutoffPeriodCombo.setSelectedIndex(0); // "Custom Range"
        coverageStartChooser.setDate(ym.atDay(1));
        coverageEndChooser.setDate(ym.atEndOfMonth());
        updatingCutoffSelection = false;
        refreshPeriodSelectionState();
    }

    // Runs payroll for all 34 employees for the currently selected coverage period.
    // Skips employees already logged for the exact period. Runs on a background SwingWorker
    // with a non-dismissable modal progress dialog. Shows a summary on completion.
    private void runAllEmployeesForPeriod() {
        LocalDate[] coverage = getSelectedCoverage();
        if (coverage == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid coverage start and end date.",
                    "Invalid Coverage", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate start = coverage[0];
        LocalDate end   = coverage[1];
        long days = ChronoUnit.DAYS.between(start, end);
        PeriodType periodType = (days <= 16) ? PeriodType.BIWEEKLY : PeriodType.MONTHLY;

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Run payroll for ALL employees?%nPeriod: %s to %s", start, end),
                "Confirm Batch Payroll",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        List<Employee> employees = employeeService.getAllEmployees();

        JDialog progressDialog = new JDialog(this, "Running Payroll\u2026", true);
        JProgressBar progressBar = new JProgressBar(0, employees.size());
        progressBar.setStringPainted(true);
        JLabel statusLabel = new JLabel("Initialising\u2026");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        progressPanel.add(progressBar);
        progressPanel.add(statusLabel);
        progressDialog.setContentPane(progressPanel);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(this);

        SwingWorker<int[], Integer> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int written = 0, skipped = 0, failed = 0;
                for (int i = 0; i < employees.size(); i++) {
                    publish(i);
                    Employee emp = employees.get(i);
                    String empId = emp.getEmployeeID();
                    try {
                        if (payrollLogService.isPeriodAlreadyLogged(empId, start, end)) {
                            skipped++;
                            continue;
                        }
                        Payroll payroll = new Payroll(empId, emp, start, end);
                        payroll.calculateNetSalary(periodType);
                        CompensationDetails cd = payroll.getCompensationDetails();
                        payrollLogService.savePayrollRun(empId, start.getMonthValue(),
                                start.getYear(), start, end, cd);
                        new PayslipService().savePayslip(payroll.generatePayslip());
                        written++;
                    } catch (Exception ex) {
                        System.err.println("Batch payroll error for " + empId + ": " + ex.getMessage());
                        failed++;
                    }
                }
                return new int[]{written, skipped, failed};
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int i = chunks.get(chunks.size() - 1);
                progressBar.setValue(i + 1);
                statusLabel.setText("Processing employee " + (i + 1) + " / " + employees.size());
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                try {
                    int[] r = get();
                    String msg = String.format(
                            "Completed: %d  |  Skipped (already logged): %d", r[0], r[1]);
                    int msgType = JOptionPane.INFORMATION_MESSAGE;
                    String title = "Payroll Run Complete";
                    if (r[2] > 0) {
                        msg += String.format("%nFailed: %d — check console for details.", r[2]);
                        msgType = JOptionPane.WARNING_MESSAGE;
                    }
                    JOptionPane.showMessageDialog(FinanceDashboard.this, msg, title, msgType);
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(FinanceDashboard.this,
                            "Batch payroll failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                loadPeriodLogTable();
            }
        };

        worker.execute();
        progressDialog.setVisible(true); // blocks EDT until dispose() in done()
    }

    private void wireCoverageChooser(DateDropdownPanel chooser) {
        for (java.awt.Component component : chooser.getComponents()) {
            if (component instanceof JComboBox<?> comboBox) {
                comboBox.addActionListener(e -> {
                    syncCutoffSelectionWithCoverage();
                    refreshPeriodSelectionState();
                });
            }
        }
    }

    private void applyDefaultCutoff() {
        updatingCutoffSelection = true;
        cutoffPeriodCombo.setSelectedIndex(1);
        updatingCutoffSelection = false;
        LocalDate now = LocalDate.now();
        applyCutoffRange(now.getYear(), now.getMonthValue(), true);
    }

    private void applyCutoffSelection() {
        if (updatingCutoffSelection) {
            return;
        }
        if (cutoffPeriodCombo.getSelectedIndex() == 0) {
            refreshPeriodSelectionState();
            return;
        }

        LocalDate baseDate = coverageStartChooser.getSelectedDate();
        if (baseDate == null) {
            baseDate = coverageEndChooser.getSelectedDate();
        }
        if (baseDate == null) {
            baseDate = LocalDate.now();
        }
        applyCutoffRange(baseDate.getYear(), baseDate.getMonthValue(), false);
    }

    private void applyCutoffRange(int year, int month, boolean suppressRefresh) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start;
        LocalDate end;
        if (cutoffPeriodCombo.getSelectedIndex() == 2) {
            start = yearMonth.atDay(16);
            end = yearMonth.atEndOfMonth();
        } else {
            start = yearMonth.atDay(1);
            end = yearMonth.atDay(15);
        }

        updatingCutoffSelection = true;
        coverageStartChooser.setDate(start);
        coverageEndChooser.setDate(end);
        updatingCutoffSelection = false;
        if (!suppressRefresh) {
            refreshPeriodSelectionState();
        }
    }

    private void syncCutoffSelectionWithCoverage() {
        if (updatingCutoffSelection) {
            return;
        }
        LocalDate start = coverageStartChooser.getSelectedDate();
        LocalDate end = coverageEndChooser.getSelectedDate();
        if (start == null || end == null) {
            return;
        }

        String target = "Custom Range";
        if (start.getYear() == end.getYear() && start.getMonthValue() == end.getMonthValue()) {
            int endOfMonth = start.lengthOfMonth();
            if (start.getDayOfMonth() == 1 && end.getDayOfMonth() == 15) {
                target = "First Cutoff (1-15)";
            } else if (start.getDayOfMonth() == 16 && end.getDayOfMonth() == endOfMonth) {
                target = "Second Cutoff (16-End)";
            }
        }

        updatingCutoffSelection = true;
        cutoffPeriodCombo.setSelectedItem(target);
        updatingCutoffSelection = false;
    }

    private void refreshPeriodSelectionState() {
        if (periodEmpTable == null || periodEmpModel == null || periodLogModel == null || periodLogPanel == null) {
            return;
        }
        loadPeriodLogTable();
        String selectedEmpId = getSelectedPeriodEmployeeId();
        if (selectedEmpId != null) {
            showPeriodPayslip(selectedEmpId);
        } else {
            clearPeriodDetail();
        }
    }

    private String getSelectedPeriodEmployeeId() {
        if (periodEmpTable == null || periodEmpModel == null) {
            return null;
        }
        int viewRow = periodEmpTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = periodEmpTable.convertRowIndexToModel(viewRow);
        return (String) periodEmpModel.getValueAt(modelRow, 0);
    }

    private LocalDate[] getSelectedCoverage() {
        LocalDate start = coverageStartChooser.getSelectedDate();
        LocalDate end = coverageEndChooser.getSelectedDate();
        if (start == null || end == null) {
            return null;
        }
        if (end.isBefore(start)) {
            return null;
        }
        // Current payroll log storage is month/year-based, so keep one-month coverage.
        if (start.getMonthValue() != end.getMonthValue() || start.getYear() != end.getYear()) {
            return null;
        }
        return new LocalDate[]{start, end};
    }
}

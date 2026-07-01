package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Allowance;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.GovernmentDetails;
import com.group33.cp2.motorph.model.LeaveRequest;
import com.group33.cp2.motorph.model.Payroll;
import com.group33.cp2.motorph.model.Payslip;
import com.group33.cp2.motorph.report.ReportException;
import com.group33.cp2.motorph.report.ReportService;
import com.group33.cp2.motorph.service.LeaveService;
import com.group33.cp2.motorph.service.TimeTrackingService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;
import com.group33.cp2.motorph.util.MaxLengthNoNewlineFilter;
import java.sql.SQLException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.text.AbstractDocument;

// Employee self-service dashboard  info, payslip, time tracking, and leave tabs.
public class EmployeeDashboard extends JFrame {

    private static final Color EMPLOYEE_VIOLET = new Color(128, 83, 214);
    private static final Color EMPLOYEE_VIOLET_DARK = new Color(92, 55, 166);
    private static final Color EMPLOYEE_GREEN = new Color(54, 146, 88);
    private static final Color EMPLOYEE_GREEN_DARK = new Color(34, 112, 63);
    private static final Color EMPLOYEE_BG = new Color(244, 240, 250);
    private static final Color EMPLOYEE_CARD = new Color(250, 248, 253);
    private static final Color EMPLOYEE_BORDER = new Color(208, 199, 225);

    private final Employee            employee;
    private final LeaveService        leaveService;
    private final TimeTrackingService timeTrackingService;
    private final ReportService       reportService = new ReportService();
    /** Non-null when opened from a role dashboard; null for regular employee logins. */
    private final Runnable            onBack;

    // Tab 3 — Time tracking state
    private JTable timeLogTable;
    private DefaultTableModel timeLogModel;

    // Tab 4 — Leave state
    private JLabel lblSickBalance;
    private JLabel lblVacationBalance;

    // Tab 2 — Payslip labels (populated by SwingWorker)
    private JLabel lblPayBasic;
    private JLabel lblPayAllowances;
    private JLabel lblPayGross;
    private JLabel lblPaySSS;
    private JLabel lblPayPhilHealth;
    private JLabel lblPayPagibig;
    private JLabel lblPayTax;
    private JLabel lblPayTotalDed;
    private JLabel lblPayNet;
    private JLabel lblPayCoverage;
    private JLabel lblPayRegularHours;
    private JLabel lblPayOvertimeHours;
    private DateDropdownPanel payrollStartChooser;
    private DateDropdownPanel payrollEndChooser;

    private JComboBox<String> cmbLeaveType;
    private DateDropdownPanel leaveStartChooser;
    private DateDropdownPanel leaveEndChooser;
    private JTextField txtLeaveReason;
    private JTable leaveHistoryTable;
    private DefaultTableModel leaveHistoryModel;

    private static final String[] LEAVE_TYPES = {"Sick Leave", "Vacation Leave"};

    // Bundles leave-balance integers + request list so a single SwingWorker call returns both.
    private record LeaveData(int sick, int vacation, List<LeaveRequest> requests) {}

    /**
     * Creates a dashboard for a regular employee login. No back-navigation is available;
     * the footer shows a Logout button.
     */
    public EmployeeDashboard(Employee employee) {
        this(employee, null);
    }

    /**
     * Creates a dashboard for a role employee (HR, IT, Finance, Admin) who navigated here
     * from their role dashboard. When {@code onBack} is non-null the footer shows a
     * "Back to Role Dashboard" button instead of Logout; invoking it disposes this frame
     * and runs the lambda to rebuild and show the caller's dashboard.
     *
     * @param employee the logged-in employee (may be any Employee subtype)
     * @param onBack   lambda that reopens the caller's role dashboard, or null for regular logins
     */
    public EmployeeDashboard(Employee employee, Runnable onBack) {
        this.employee            = employee;
        this.onBack              = onBack;
        this.leaveService        = new LeaveService();
        this.timeTrackingService = new TimeTrackingService();

        setTitle("Employee Dashboard — " + employee.getFullName());
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (DialogUtil.confirmExit(EmployeeDashboard.this)) {
                    dispose();
                }
            }
        });

        buildUI();
    }

    // =========================================================================
    //  UI construction
    // =========================================================================

    private void buildUI() {
        JPanel header = buildHeaderPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Info",       buildInfoTab());
        tabs.addTab("My Payslip",    buildPayslipTab());
        tabs.addTab("Time Tracking", buildTimeTrackingTab());
        tabs.addTab("Leave",         buildLeaveTab());
        tabs.setBackground(new Color(244, 240, 250));
        tabs.setForeground(new Color(74, 52, 118));
        tabs.setFont(new Font("Noto Sans", Font.BOLD, 13));
        UITheme.styleTabs(tabs);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(244, 240, 250));
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(tabs,   BorderLayout.CENTER);
        getContentPane().add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EMPLOYEE_VIOLET_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel title = new JLabel("Employee Dashboard", SwingConstants.LEFT);
        title.setFont(new Font("Noto Sans", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " + employee.getFullName(), SwingConstants.RIGHT);
        welcome.setFont(new Font("Noto Sans", Font.BOLD, 14));
        welcome.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(244, 240, 250));

        if (onBack != null) {
            // Opened from a role dashboard — show a Back button rather than Logout.
            JButton btnBack = new JButton("Back to Role Dashboard");
            UITheme.styleSecondaryButton(btnBack);
            btnBack.addActionListener(e -> {
                dispose();
                onBack.run();
            });
            panel.add(btnBack);
        } else {
            // Regular employee login — show the Logout button as before.
            JButton btnLogout = new JButton("Logout");
            UITheme.styleDangerButton(btnLogout);
            btnLogout.addActionListener(e -> {
                if (DialogUtil.confirmLogout(this)) {
                    NavigationManager.openLoginFrame(this);
                }
            });
            panel.add(btnLogout);
        }

        return panel;
    }

    // =========================================================================
    //  Tab 1: My Info
    // =========================================================================

    private JScrollPane buildInfoTab() {
        JPanel panel = new JPanel();
        panel.setBackground(EMPLOYEE_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        GovernmentDetails gov = employee.getGovernmentDetails();
        Allowance allowance   = employee.getAllowanceDetails();

        JLabel pageTitle = new JLabel("My Profile");
        pageTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        pageTitle.setForeground(new Color(69, 50, 110));
        JLabel helper = new JLabel("Review your personal, employment, government, and allowance information.");
        helper.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helper.setForeground(new Color(110, 102, 126));
        panel.add(pageTitle);
        panel.add(helper);
        panel.add(Box.createVerticalStrut(12));

        panel.add(infoSection("Personal Information", new String[][]{
            {"Employee ID",  employee.getEmployeeID()},
            {"Full Name",    employee.getFullName()},
            {"Birthday",     employee.getBirthday()},
            {"Address",      employee.getAddress()},
            {"Phone",        employee.getPhoneNumber()}
        }));
        panel.add(Box.createVerticalStrut(12));
        panel.add(infoSection("Employment Details", new String[][]{
            {"Position",    employee.getPosition()},
            {"Status",      employee.getStatus()},
            {"Supervisor",  employee.getImmediateSupervisor()}
        }));
        panel.add(Box.createVerticalStrut(12));
        panel.add(infoSection("Government IDs", new String[][]{
            {"SSS Number",         gov != null ? gov.getSssNumber()        : "N/A"},
            {"PhilHealth Number",  gov != null ? gov.getPhilHealthNumber() : "N/A"},
            {"TIN",                gov != null ? gov.getTinNumber()        : "N/A"},
            {"Pag-IBIG Number",    gov != null ? gov.getPagibigNumber()    : "N/A"}
        }));
        panel.add(Box.createVerticalStrut(12));
        panel.add(infoSection("Allowances", new String[][]{
            {"Rice Allowance",     allowance != null ? String.format("%.2f", allowance.getRiceAllowance())     : "0.00"},
            {"Phone Allowance",    allowance != null ? String.format("%.2f", allowance.getPhoneAllowance())    : "0.00"},
            {"Clothing Allowance", allowance != null ? String.format("%.2f", allowance.getClothingAllowance()) : "0.00"}
        }));
        panel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(EMPLOYEE_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    // Builds a titled section of label-value pairs.
    private JPanel infoSection(String title, String[][] fields) {
        JPanel section = new JPanel(new GridBagLayout());
        styleSectionPanel(section, title);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (String[] field : fields) {
            JLabel lbl = createPayslipFieldLabel(field[0] + ":");
            JLabel val = new JLabel(field[1] == null || field[1].isBlank() ? "N/A" : field[1]);
            val.setForeground(new Color(47, 42, 58));
            val.setFont(new Font("Noto Sans", Font.PLAIN, 12));

            gbc.gridx = 0;
            gbc.weightx = 0.28;
            section.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.72;
            section.add(val, gbc);

            gbc.gridy++;
        }

        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, section.getPreferredSize().height));
        return section;
    }

    // =========================================================================
    //  Tab 2: My Payslip
    // =========================================================================

    private JScrollPane buildPayslipTab() {
        JPanel panel = new JPanel();
        panel.setBackground(EMPLOYEE_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel pageTitle = new JLabel("My Payslip");
        pageTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        pageTitle.setForeground(new Color(69, 50, 110));
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel helper = new JLabel("Select a payroll coverage period and review the computed salary breakdown.");
        helper.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helper.setForeground(new Color(110, 102, 126));
        helper.setAlignmentX(Component.LEFT_ALIGNMENT);

        payrollStartChooser = new DateDropdownPanel();
        payrollEndChooser = new DateDropdownPanel();
        payrollStartChooser.setFieldWidths(90, 80, 100);
        payrollEndChooser.setFieldWidths(90, 80, 100);
        LocalDate now = LocalDate.now();
        payrollStartChooser.setDate(now.withDayOfMonth(1));
        payrollEndChooser.setDate(now.withDayOfMonth(now.lengthOfMonth()));

        JPanel coveragePanel = new JPanel(new GridBagLayout());
        styleSectionPanel(coveragePanel, "Payroll Coverage");
        coveragePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        coveragePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));

        GridBagConstraints coverageGbc = new GridBagConstraints();
        coverageGbc.insets = new Insets(6, 6, 6, 6);
        coverageGbc.anchor = GridBagConstraints.WEST;
        coverageGbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel startLabel = createPayslipFieldLabel("Start Date:");
        JLabel endLabel = createPayslipFieldLabel("End Date:");

        coverageGbc.gridx = 0;
        coverageGbc.gridy = 0;
        coverageGbc.weightx = 0;
        coveragePanel.add(startLabel, coverageGbc);

        coverageGbc.gridx = 1;
        coverageGbc.weightx = 1;
        coveragePanel.add(payrollStartChooser, coverageGbc);

        coverageGbc.gridx = 0;
        coverageGbc.gridy = 1;
        coverageGbc.weightx = 0;
        coveragePanel.add(endLabel, coverageGbc);

        coverageGbc.gridx = 1;
        coverageGbc.weightx = 1;
        coveragePanel.add(payrollEndChooser, coverageGbc);

        coverageGbc.gridx = 2;
        coverageGbc.gridy = 0;
        coverageGbc.weightx = 1;
        coverageGbc.weighty = 1;
        coverageGbc.fill = GridBagConstraints.BOTH;
        coveragePanel.add(new JPanel(), coverageGbc);

        JPanel summaryPanel = new JPanel(new GridBagLayout());
        styleSectionPanel(summaryPanel, "Payslip Summary");
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 430));

        GridBagConstraints summaryGbc = new GridBagConstraints();
        summaryGbc.insets = new Insets(6, 6, 6, 6);
        summaryGbc.gridy = 0;
        summaryGbc.anchor = GridBagConstraints.NORTHWEST;
        summaryGbc.fill = GridBagConstraints.HORIZONTAL;
        summaryGbc.weighty = 0;

        lblPayCoverage   = addPayslipRow(summaryPanel, summaryGbc, "Coverage");
        lblPayBasic      = addPayslipRow(summaryPanel, summaryGbc, "Basic Salary");
        lblPayAllowances = addPayslipRow(summaryPanel, summaryGbc, "Total Allowances");
        lblPayGross      = addPayslipRow(summaryPanel, summaryGbc, "Gross Salary");
        lblPayRegularHours = addPayslipRow(summaryPanel, summaryGbc, "Regular Hours");
        lblPayOvertimeHours = addPayslipRow(summaryPanel, summaryGbc, "Overtime Hours");
        lblPaySSS        = addPayslipRow(summaryPanel, summaryGbc, "SSS Deduction");
        lblPayPhilHealth = addPayslipRow(summaryPanel, summaryGbc, "PhilHealth Deduction");
        lblPayPagibig    = addPayslipRow(summaryPanel, summaryGbc, "Pag-IBIG Deduction");
        lblPayTax        = addPayslipRow(summaryPanel, summaryGbc, "Withholding Tax");
        lblPayTotalDed   = addPayslipRow(summaryPanel, summaryGbc, "Total Deductions");
        lblPayNet        = addPayslipRow(summaryPanel, summaryGbc, "Net Salary");

        JButton btnCalculate = new JButton("Calculate Payslip");
        styleButton(btnCalculate, true);
        btnCalculate.setPreferredSize(new Dimension(220, 48));
        btnCalculate.addActionListener(e -> calculatePayslip());

        JButton btnDownloadPdf = new JButton("Download Payslip PDF");
        btnDownloadPdf.setBackground(EMPLOYEE_GREEN);
        btnDownloadPdf.setForeground(Color.WHITE);
        btnDownloadPdf.setFont(new Font("Noto Sans", Font.BOLD, 13));
        btnDownloadPdf.setFocusPainted(false);
        btnDownloadPdf.setOpaque(true);
        btnDownloadPdf.setPreferredSize(new Dimension(220, 48));
        btnDownloadPdf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EMPLOYEE_GREEN_DARK, 1, true),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        btnDownloadPdf.addActionListener(e -> downloadPayslipPdf());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(EMPLOYEE_BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.add(btnCalculate);
        btnRow.add(btnDownloadPdf);

        panel.add(pageTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(helper);
        panel.add(Box.createVerticalStrut(16));
        panel.add(coveragePanel);
        panel.add(Box.createVerticalStrut(16));
        panel.add(summaryPanel);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnRow);
        panel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getViewport().setBackground(EMPLOYEE_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JLabel createPayslipFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Noto Sans", Font.BOLD, 12));
        label.setForeground(new Color(73, 54, 112));
        return label;
    }

    // Adds a bold label and a mutable value label to the summary grid; returns the value label.
    private JLabel addPayslipRow(JPanel panel, GridBagConstraints template, String name) {
        JLabel fieldLabel = createPayslipFieldLabel(name + ":");
        JLabel valueLabel = new JLabel("\u2014");
        valueLabel.setForeground(new Color(47, 42, 58));
        valueLabel.setFont(new Font("Noto Sans", Font.PLAIN, 12));

        GridBagConstraints labelGbc = (GridBagConstraints) template.clone();
        labelGbc.gridx = 0;
        labelGbc.weightx = 0;
        panel.add(fieldLabel, labelGbc);

        GridBagConstraints valueGbc = (GridBagConstraints) template.clone();
        valueGbc.gridx = 1;
        valueGbc.weightx = 1;
        valueGbc.insets = new Insets(6, 20, 6, 6);
        panel.add(valueLabel, valueGbc);

        template.gridy++;
        return valueLabel;
    }

    // Loads payroll data off the EDT via SwingWorker, then populates payslip labels in done().
    private void calculatePayslip() {
        LocalDate startDate = payrollStartChooser.getSelectedDate();
        LocalDate endDate = payrollEndChooser.getSelectedDate();
        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this,
                    "Please choose both start and end dates for payroll generation.",
                    "Missing Coverage",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (endDate.isBefore(startDate)) {
            JOptionPane.showMessageDialog(this,
                    "Payroll end date cannot be earlier than the start date.",
                    "Invalid Coverage",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        new javax.swing.SwingWorker<Payroll, Void>() {
            @Override
            protected Payroll doInBackground() {
                Payroll payroll = new Payroll(employee.getEmployeeID(), employee, startDate, endDate);
                payroll.calculateNetSalary();
                return payroll;
            }
            @Override
            protected void done() {
                try {
                    Payroll payroll = get();
                    if (payroll == null) {
                        JOptionPane.showMessageDialog(EmployeeDashboard.this,
                                "Payroll data not found for employee #" + employee.getEmployeeID(),
                                "Not Found", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Payslip payslip = payroll.generatePayslip();
                    var compensation = payslip.getCompensationDetails();
                    var deductions = compensation.getDeductions();
                    var allowances = compensation.getAllowance();

                    lblPayCoverage.setText(startDate + " to " + endDate);
                    lblPayBasic.setText(String.format("%.2f", employee.getBasicSalary()));
                    lblPayAllowances.setText(String.format("%.2f", allowances.getTotal()));
                    lblPayGross.setText(String.format("%.2f", compensation.getGrossSalary()));
                    lblPayRegularHours.setText(String.format("%.2f", payslip.getTotalRegularHours()));
                    lblPayOvertimeHours.setText(String.format("%.2f", payslip.getTotalOvertimeHours()));
                    lblPaySSS.setText(String.format("%.2f", deductions.getSss()));
                    lblPayPhilHealth.setText(String.format("%.2f", deductions.getPhilHealth()));
                    lblPayPagibig.setText(String.format("%.2f", deductions.getPagIbig()));
                    lblPayTax.setText(String.format("%.2f", deductions.getTax()));
                    lblPayTotalDed.setText(String.format("%.2f", deductions.getTotal()));
                    lblPayNet.setText(String.format("%.2f", compensation.getNetSalary()));

                    try {
                        employee.addPayslip(payslip);
                    } catch (NullPointerException npe) {
                        // Allowance data missing for this employee — payslip domain object
                        // not added, but the label display above is unaffected.
                        System.err.println("EmployeeDashboard.calculatePayslip: "
                                + "could not create Payslip domain object — " + npe.getMessage());
                    }
                } catch (InterruptedException | java.util.concurrent.ExecutionException ex) {
                    JOptionPane.showMessageDialog(EmployeeDashboard.this,
                            "Failed to calculate payslip: " + ex.getMessage(),
                            "Payroll Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Generates a payslip PDF for the selected date range and saves it to a user-chosen location.
    private void downloadPayslipPdf() {
        LocalDate startDate = payrollStartChooser.getSelectedDate();
        LocalDate endDate   = payrollEndChooser.getSelectedDate();

        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a payroll coverage period first, then click Download.",
                    "Missing Period", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String period = startDate + " to " + endDate;

        // Ask user where to save
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Payslip PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(
                "Payslip_" + employee.getEmployeeID() + "_" + startDate + ".pdf"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File outputFile = chooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getAbsolutePath() + ".pdf");
        }

        final File finalOutput = outputFile;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                reportService.generatePayslip(
                        employee.getEmployeeID(), period, finalOutput);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(EmployeeDashboard.this,
                            "Payslip saved to:\n" + finalOutput.getAbsolutePath(),
                            "Payslip Downloaded", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(EmployeeDashboard.this,
                            "Failed to generate payslip PDF:\n" + msg,
                            "Report Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    //  Tab 3: Time Tracking
    // =========================================================================

    private JPanel buildTimeTrackingTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(EMPLOYEE_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table
        timeLogModel = new DefaultTableModel(
                new String[]{"Employee #", "Date", "Time In", "Time Out", "Hours Worked"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        timeLogTable = new JTable(timeLogModel);
        timeLogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane timeLogScrollPane = new JScrollPane(timeLogTable);
        styleTable(timeLogTable, timeLogScrollPane);

        // Buttons
        JButton btnClockIn  = new JButton("Clock In");
        JButton btnClockOut = new JButton("Clock Out");
        JButton btnRefresh  = new JButton("Refresh Logs");
        styleButton(btnClockIn, false);
        styleButton(btnClockOut, false);
        styleRefreshButton(btnRefresh);
        btnClockIn.setPreferredSize(new Dimension(120, 48));
        btnClockOut.setPreferredSize(new Dimension(130, 48));
        btnRefresh.setPreferredSize(new Dimension(160, 48));

        btnClockIn.addActionListener(e -> handleClockIn());
        btnClockOut.addActionListener(e -> handleClockOut());
        btnRefresh.addActionListener(e -> loadTimeLogs());

        JLabel sectionTitle = new JLabel("Time Tracking");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(69, 50, 110));
        JLabel helper = new JLabel("Track daily attendance and review your recorded work hours.");
        helper.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helper.setForeground(new Color(110, 102, 126));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setBackground(EMPLOYEE_BG);
        buttonBar.add(btnClockIn);
        buttonBar.add(btnClockOut);
        buttonBar.add(btnRefresh);

        JPanel topBar = new JPanel();
        topBar.setBackground(EMPLOYEE_BG);
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.add(sectionTitle);
        topBar.add(helper);
        topBar.add(javax.swing.Box.createVerticalStrut(10));
        topBar.add(buttonBar);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(timeLogScrollPane, BorderLayout.CENTER);

        loadTimeLogs();
        return panel;
    }

    private void handleClockIn() {
        try {
            timeTrackingService.clockIn(employee.getEmployeeID());
            JOptionPane.showMessageDialog(this, "Clocked in successfully.", "Clock In", JOptionPane.INFORMATION_MESSAGE);
            loadTimeLogs();
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Clock in failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // SYS-1: must return here so loadTimeLogs() does not run after a failed clock-in
            return;
        }
    }

    private void handleClockOut() {
        try {
            timeTrackingService.clockOut(employee.getEmployeeID());
            JOptionPane.showMessageDialog(this, "Clocked out successfully.", "Clock Out", JOptionPane.INFORMATION_MESSAGE);
            loadTimeLogs();
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Clock out failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            // SYS-1: must return here so loadTimeLogs() does not run after a failed clock-out
            return;
        }
    }

    // Loads time-log records off the EDT via SwingWorker; populates the table in done().
    private void loadTimeLogs() {
        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                return timeTrackingService.getTimeLogs(employee.getEmployeeID());
            }

            @Override
            protected void done() {
                try {
                    List<String[]> logs = get();
                    timeLogModel.setRowCount(0);
                    for (String[] row : logs) {
                        timeLogModel.addRow(row);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeDashboard.this,
                            "Failed to load time logs: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    //  Tab 4: Leave
    // =========================================================================

    private JPanel buildLeaveTab() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(EMPLOYEE_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Balance summary panel
        JPanel balancePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        styleSectionPanel(balancePanel, "Leave Balances");
        lblSickBalance     = new JLabel("Sick Leave: --", SwingConstants.CENTER);
        lblVacationBalance = new JLabel("Vacation Leave: --", SwingConstants.CENTER);
        styleSummaryLabel(lblSickBalance);
        styleSummaryLabel(lblVacationBalance);
        balancePanel.add(lblSickBalance);
        balancePanel.add(lblVacationBalance);

        // Request form
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 4));
        styleSectionPanel(formPanel, "Submit Leave Request");

        cmbLeaveType     = new JComboBox<>(LEAVE_TYPES);
        leaveStartChooser = new DateDropdownPanel();
        leaveEndChooser   = new DateDropdownPanel();
        leaveStartChooser.setFieldWidths(110, 110, 110);
        leaveEndChooser.setFieldWidths(110, 110, 110);
        txtLeaveReason    = new JTextField(30);
        // VAL-6: cap leave reason at 500 chars and strip newlines to prevent CSV row corruption
        ((AbstractDocument) txtLeaveReason.getDocument())
            .setDocumentFilter(new MaxLengthNoNewlineFilter(500));
        UITheme.styleComboBox(cmbLeaveType);
        UITheme.styleComponentTree(leaveStartChooser);
        UITheme.styleComponentTree(leaveEndChooser);
        UITheme.styleTextField(txtLeaveReason);

        formPanel.add(new JLabel("Leave Type:"));
        formPanel.add(cmbLeaveType);
        formPanel.add(new JLabel("Start Date:"));
        formPanel.add(leaveStartChooser);
        formPanel.add(new JLabel("End Date:"));
        formPanel.add(leaveEndChooser);
        formPanel.add(new JLabel("Reason:"));
        formPanel.add(txtLeaveReason);

        JButton btnSubmit = new JButton("Submit Request");
        styleButton(btnSubmit, true);
        btnSubmit.setPreferredSize(new Dimension(170, 48));
        btnSubmit.addActionListener(e -> handleLeaveSubmit());

        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitRow.setBackground(EMPLOYEE_BG);
        submitRow.add(btnSubmit);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(EMPLOYEE_BG);
        topPanel.add(balancePanel);
        topPanel.add(formPanel);
        topPanel.add(submitRow);

        // History table
        leaveHistoryModel = new DefaultTableModel(
                new String[]{"Leave ID", "Type", "Start", "End", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        leaveHistoryTable = new JTable(leaveHistoryModel);
        JScrollPane leaveHistoryScrollPane = new JScrollPane(leaveHistoryTable);
        styleTable(leaveHistoryTable, leaveHistoryScrollPane);

        JButton btnRefresh = new JButton("Refresh");
        styleRefreshButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(120, 48));
        btnRefresh.addActionListener(e -> loadLeaveData());

        JPanel histHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        histHeader.setBackground(EMPLOYEE_BG);
        histHeader.add(new JLabel("Leave History"));
        histHeader.add(btnRefresh);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(EMPLOYEE_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.add(histHeader, BorderLayout.NORTH);
        bottomPanel.add(leaveHistoryScrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        loadLeaveData();
        return panel;
    }

    // Loads leave balances and history off the EDT via SwingWorker; updates labels and table in done().
    private void loadLeaveData() {
        new SwingWorker<LeaveData, Void>() {
            @Override
            protected LeaveData doInBackground() throws Exception {
                String empId = employee.getEmployeeID();
                int sick     = leaveService.getLeaveBalance(empId, "Sick Leave");
                int vacation = leaveService.getLeaveBalance(empId, "Vacation Leave");
                List<LeaveRequest> requests = leaveService.getLeaveRequestsByEmployee(empId);
                return new LeaveData(sick, vacation, requests);
            }

            @Override
            protected void done() {
                try {
                    LeaveData data = get();
                    // Update balance labels on the EDT
                    lblSickBalance.setText("Sick Leave: " + data.sick() + " days");
                    lblVacationBalance.setText("Vacation Leave: " + data.vacation() + " days");
                    // Reload history table on the EDT
                    leaveHistoryModel.setRowCount(0);
                    for (LeaveRequest req : data.requests()) {
                        leaveHistoryModel.addRow(new Object[]{
                            req.getLeaveID(),
                            req.getLeaveType(),
                            req.getStartDate() != null ? req.getStartDate().toString() : "",
                            req.getEndDate()   != null ? req.getEndDate().toString()   : "",
                            req.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(EmployeeDashboard.this,
                            "Failed to load leave data: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleLeaveSubmit() {
        String leaveType = (String) cmbLeaveType.getSelectedItem();
        String reason    = txtLeaveReason.getText().trim();

        if (!leaveStartChooser.isSelectionComplete()
                || !leaveEndChooser.isSelectionComplete()
                || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.",
                    "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate startDate = leaveStartChooser.getSelectedDate();
        LocalDate endDate = leaveEndChooser.getSelectedDate();
        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Please choose valid start and end dates.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            leaveService.submitLeaveRequest(
                    employee.getEmployeeID(), leaveType, startDate, endDate, reason);
            JOptionPane.showMessageDialog(this,
                    "Leave request submitted successfully.",
                    "Request Submitted",
                    JOptionPane.INFORMATION_MESSAGE);
            leaveStartChooser.clearSelection();
            leaveEndChooser.clearSelection();
            txtLeaveReason.setText("");
            loadLeaveData();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit request: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        if (primary) {
            button.setBackground(EMPLOYEE_VIOLET);
            button.setForeground(Color.WHITE);
        } else {
            UITheme.styleNeutralButton(button, new Color(69, 50, 110));
            return;
        }
    }

    private void styleRefreshButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(EMPLOYEE_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EMPLOYEE_GREEN_DARK, 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
    }

    private void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(37, 38, 51));
        table.setSelectionBackground(new Color(229, 219, 248));
        table.setSelectionForeground(new Color(67, 46, 108));
        table.setGridColor(new Color(224, 220, 232));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(new Color(242, 239, 247));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 201, 217), 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
    }

    private void styleSectionPanel(JPanel panel, String title) {
        panel.setBackground(EMPLOYEE_CARD);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(EMPLOYEE_BORDER, 1, true),
                title
        ));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    private void styleSummaryLabel(JLabel label) {
        label.setFont(new Font("Noto Sans", Font.BOLD, 13));
        label.setForeground(new Color(69, 50, 110));
    }
}

package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.CompensationDetails;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.Payroll;
import com.group33.cp2.motorph.model.PayrollStatus;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.service.PayrollService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;
import com.group33.cp2.motorph.util.Utility;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

// ViewSalaryFrame — salary breakdown for a selected payroll period.
public class ViewSalaryFrame extends javax.swing.JFrame {

    private final EmployeeService employeeService = new EmployeeService();
    private final PayrollService payrollDAO = new PayrollService();
    private final boolean canEditCompensation;
    private Employee selectedEmployee;

    // GUI fields for employee info (left panel)
    private JTextField txtEmployeeNumber;
    private JTextField txtLastName;
    private JTextField txtFirstName;
    private JTextField txtBirthday;
    private JTextField txtAddress;
    private JTextField txtPhoneNumber;
    private JTextField txtSSSNumber;
    private JTextField txtPhilHealthNumber;
    private JTextField txtTIN;
    private JTextField txtPagIbigNumber;
    private JTextField txtStatus;
    private JTextField txtPosition;
    private JTextField txtImmediateSupervisor;
    private JTextField txtBasicSalary;
    private JTextField txtRiceAllowance;
    private JTextField txtPhoneAllowance;
    private JTextField txtClothingAllowance;
    private JTextField txtGrossSemiMonthly;
    private JTextField txtHourlyRate;

    // Computed result labels (right panel)
    private JLabel lblRegularHours;
    private JLabel lblOvertimeHours;
    private JLabel lblRegularPay;
    private JLabel lblNetSalary;
    private JLabel lblOvertimePay;
    private JLabel lblGrossSalary;
    private JLabel lblSSSDeductions;
    private JLabel lblPhilHealthDeductions;
    private JLabel lblPagIBIGDeductions;
    private JLabel lblWithholdingTax;
    private JLabel lblRiceAllowance2;
    private JLabel lblClothingAllowance2;
    private JLabel lblPayrollStatus;
    private JLabel lblPhoneAllowance2;
    private JLabel dateFrom;

    public ViewSalaryFrame(String employeeId, LocalDate selectedStartDate, LocalDate selectEndDate,
                           boolean canEditCompensation) {
        this.canEditCompensation = canEditCompensation;
        setTitle("MotorPH Employee Payroll System");
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(ViewSalaryFrame.this)) {
                    dispose();
                }
            }
        });

        selectedEmployee = employeeService.getEmployeeById(employeeId);

        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID " + employeeId + " was not found.",
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            NavigationManager.openEmployeeListFrame(this, canEditCompensation);
            return;
        }

        JPanel contentPanel = new JPanel(new BorderLayout(0, 18));
        UITheme.styleSurfacePanel(contentPanel);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = UITheme.createPageHeader(
                "PAYROLL REVIEW",
                "Computed Salary Summary",
                "Review payroll totals, deductions, and allowances for the selected coverage."
        );
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.gridy = 0;
        rootGbc.fill = GridBagConstraints.BOTH;
        rootGbc.weighty = 1.0;
        rootGbc.insets = new Insets(0, 0, 0, 18);

        JPanel leftPanel = createEmployeeDetailsPanel();

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(createSummaryHeaderPanel());
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createMetricCard("Hours", new String[][]{
            {"Regular Hours:", ""},
            {"Overtime Hours:", ""}
        }));
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createMetricCard("Pay Breakdown", new String[][]{
            {"Regular Pay:", ""},
            {"Overtime Pay:", ""},
            {"Gross Salary:", ""}
        }));
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createMetricCard("Deductions", new String[][]{
            {"SSS:", ""},
            {"PhilHealth:", ""},
            {"Pag-IBIG:", ""},
            {"Withholding Tax:", ""}
        }));
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createMetricCard("Allowances", new String[][]{
            {"Rice:", ""},
            {"Phone:", ""},
            {"Clothing:", ""}
        }));
        rightPanel.add(Box.createVerticalStrut(14));
        rightPanel.add(createStatusPanel());

        rootGbc.gridx = 0;
        rootGbc.weightx = 1.12;
        mainPanel.add(leftPanel, rootGbc);

        rootGbc.gridx = 1;
        rootGbc.insets = new Insets(0, 0, 0, 0);
        rootGbc.weightx = 0.88;
        mainPanel.add(rightPanel, rootGbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.APP_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(contentPanel);

        disableFields();
        txtEmployeeNumber.setText(selectedEmployee.getEmployeeID());
        setEmployeeDetailsTextFields();

        if (selectedStartDate != null) {
            Date date = Date.from(selectedStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy");
            String startLabel = formatter.format(date);
            String coverageLabel = startLabel;
            if (selectEndDate != null) {
                Date endDate = Date.from(selectEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                coverageLabel = startLabel + " to " + formatter.format(endDate);
            }
            dateFrom.setText(coverageLabel);
            compute(selectedStartDate, selectEndDate);
        }
    }

    private void disableFields() {
        txtEmployeeNumber.setEditable(false);
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtBirthday.setEditable(false);
        txtAddress.setEditable(false);
        txtPhoneNumber.setEditable(false);
        txtStatus.setEditable(false);
        txtPosition.setEditable(false);
        txtImmediateSupervisor.setEditable(false);
        txtSSSNumber.setEditable(false);
        txtPhilHealthNumber.setEditable(false);
        txtTIN.setEditable(false);
        txtPagIbigNumber.setEditable(false);
        txtBasicSalary.setEditable(false);
        txtRiceAllowance.setEditable(false);
        txtPhoneAllowance.setEditable(false);
        txtClothingAllowance.setEditable(false);
        txtGrossSemiMonthly.setEditable(false);
        txtHourlyRate.setEditable(false);
    }

    private void setEmployeeDetailsTextFields() {
        txtLastName.setText(selectedEmployee.getLastName());
        txtFirstName.setText(selectedEmployee.getFirstName());
        txtBirthday.setText(selectedEmployee.getBirthday());
        txtAddress.setText(selectedEmployee.getAddress());
        txtPhoneNumber.setText(selectedEmployee.getPhoneNumber());
        txtSSSNumber.setText(selectedEmployee.getGovernmentDetails().getSssNumber());
        txtPhilHealthNumber.setText(selectedEmployee.getGovernmentDetails().getPhilHealthNumber());
        txtTIN.setText(selectedEmployee.getGovernmentDetails().getTinNumber());
        txtPagIbigNumber.setText(selectedEmployee.getGovernmentDetails().getPagibigNumber());
        txtStatus.setText(selectedEmployee.getStatus());
        txtPosition.setText(selectedEmployee.getPosition());
        txtImmediateSupervisor.setText(selectedEmployee.getImmediateSupervisor());
        txtBasicSalary.setText(Utility.formatTwoDecimal(selectedEmployee.getBasicSalary()));
        txtRiceAllowance.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getRiceAllowance()));
        txtPhoneAllowance.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getPhoneAllowance()));
        txtClothingAllowance.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getClothingAllowance()));
        txtGrossSemiMonthly.setText(Utility.formatTwoDecimal(selectedEmployee.getGrossSemiMonthlyRate()));
        txtHourlyRate.setText(Utility.formatTwoDecimal(selectedEmployee.getHourlyRate()));
    }

    private JPanel createEmployeeDetailsPanel() {
        JPanel panel = createFormCard("Employee Details");

        txtEmployeeNumber = createReadOnlyField();
        txtLastName = createReadOnlyField();
        txtFirstName = createReadOnlyField();
        txtBirthday = createReadOnlyField();
        txtAddress = createReadOnlyField();
        txtPhoneNumber = createReadOnlyField();
        txtSSSNumber = createReadOnlyField();
        txtPhilHealthNumber = createReadOnlyField();
        txtTIN = createReadOnlyField();
        txtPagIbigNumber = createReadOnlyField();
        txtStatus = createReadOnlyField();
        txtPosition = createReadOnlyField();
        txtImmediateSupervisor = createReadOnlyField();
        txtBasicSalary = createReadOnlyField();
        txtRiceAllowance = createReadOnlyField();
        txtPhoneAllowance = createReadOnlyField();
        txtClothingAllowance = createReadOnlyField();
        txtGrossSemiMonthly = createReadOnlyField();
        txtHourlyRate = createReadOnlyField();

        addFormRow(panel, 0, "Employee Number:", txtEmployeeNumber);
        addFormRow(panel, 1, "Last Name:", txtLastName);
        addFormRow(panel, 2, "First Name:", txtFirstName);
        addFormRow(panel, 3, "Birthday:", txtBirthday);
        addFormRow(panel, 4, "Address:", txtAddress);
        addFormRow(panel, 5, "Phone Number:", txtPhoneNumber);
        addFormRow(panel, 6, "SSS Number:", txtSSSNumber);
        addFormRow(panel, 7, "PhilHealth Number:", txtPhilHealthNumber);
        addFormRow(panel, 8, "TIN Number:", txtTIN);
        addFormRow(panel, 9, "Pag-IBIG Number:", txtPagIbigNumber);
        addFormRow(panel, 10, "Status:", txtStatus);
        addFormRow(panel, 11, "Position:", txtPosition);
        addFormRow(panel, 12, "Immediate Supervisor:", txtImmediateSupervisor);
        addFormRow(panel, 13, "Basic Salary:", txtBasicSalary);
        addFormRow(panel, 14, "Rice Subsidy:", txtRiceAllowance);
        addFormRow(panel, 15, "Phone Allowance:", txtPhoneAllowance);
        addFormRow(panel, 16, "Clothing Allowance:", txtClothingAllowance);
        addFormRow(panel, 17, "Gross Semi-Monthly:", txtGrossSemiMonthly);
        addFormRow(panel, 18, "Hourly Rate:", txtHourlyRate);

        return panel;
    }

    private JPanel createSummaryHeaderPanel() {
        JPanel panel = createFormCard("Computed Salary Summary");
        dateFrom = new JLabel("—");
        styleValueLabel(dateFrom, false);
        addLabelRow(panel, 0, "Pay Coverage:", dateFrom, false);
        return panel;
    }

    private JPanel createMetricCard(String title, String[][] rows) {
        JPanel panel = createFormCard(title);
        for (int i = 0; i < rows.length; i++) {
            JLabel valueLabel = new JLabel("—");
            boolean emphasize = "Gross Salary:".equals(rows[i][0]);
            styleValueLabel(valueLabel, emphasize);
            addLabelRow(panel, i, rows[i][0], valueLabel, emphasize);

            switch (rows[i][0]) {
                case "Regular Hours:" -> lblRegularHours = valueLabel;
                case "Overtime Hours:" -> lblOvertimeHours = valueLabel;
                case "Regular Pay:" -> lblRegularPay = valueLabel;
                case "Overtime Pay:" -> lblOvertimePay = valueLabel;
                case "Gross Salary:" -> lblGrossSalary = valueLabel;
                case "SSS:" -> lblSSSDeductions = valueLabel;
                case "PhilHealth:" -> lblPhilHealthDeductions = valueLabel;
                case "Pag-IBIG:" -> lblPagIBIGDeductions = valueLabel;
                case "Withholding Tax:" -> lblWithholdingTax = valueLabel;
                case "Rice:" -> lblRiceAllowance2 = valueLabel;
                case "Phone:" -> lblPhoneAllowance2 = valueLabel;
                case "Clothing:" -> lblClothingAllowance2 = valueLabel;
                default -> {
                }
            }
        }
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = createFormCard("Payroll Status");

        lblNetSalary = new JLabel("—");
        styleValueLabel(lblNetSalary, true);
        lblNetSalary.setFont(lblNetSalary.getFont().deriveFont(Font.BOLD, 22f));
        addLabelRow(panel, 0, "Net Salary:", lblNetSalary, true);

        lblPayrollStatus = new JLabel(PayrollStatus.PENDING.toString());
        styleValueLabel(lblPayrollStatus, false);
        addLabelRow(panel, 1, "Status:", lblPayrollStatus, false);

        JButton btnBack = new JButton("Back");
        UITheme.styleSecondaryButton(btnBack);
        btnBack.addActionListener(e -> NavigationManager.openEmployeeListFrame(this, canEditCompensation));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(btnBack);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 8, 6, 8);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonRow, gbc);

        return panel;
    }

    private void compute(LocalDate selectedStartDate, LocalDate selectedEndDate) {
        Payroll payroll = new Payroll(selectedEmployee.getEmployeeID(), selectedEmployee, selectedStartDate, selectedEndDate);

        payroll.calculateNetSalary();

        if (payroll.getTotalRegularHours() <= 0) {
            NavigationManager.openEmployeeListFrame(this, canEditCompensation);
            JOptionPane.showMessageDialog(
                this,
                "No salary record found for the selected month.",
                "Notice",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        payrollDAO.create(payroll);

        String regularHours = Utility.formatTwoDecimal(payroll.getTotalRegularHours());
        String overtimeHours = Utility.formatTwoDecimal(payroll.getTotalOvertimeHours());

        CompensationDetails compensationDetails = payroll.getCompensationDetails();

        String regularPay = Utility.formatTwoDecimal(compensationDetails.getRegularPay());
        String overtimePay = Utility.formatTwoDecimal(compensationDetails.getOvertimePay());
        String grossSalary = Utility.formatTwoDecimal(compensationDetails.getGrossSalary());

        String sss = Utility.formatTwoDecimal(compensationDetails.getDeductions().getSss());
        String pagibig = Utility.formatTwoDecimal(compensationDetails.getDeductions().getPagIbig());
        String philhealth = Utility.formatTwoDecimal(compensationDetails.getDeductions().getPhilHealth());
        String tax = Utility.formatTwoDecimal(compensationDetails.getDeductions().getTax());

        String rice = Utility.formatTwoDecimal(compensationDetails.getAllowance().getRiceAllowance());
        String phone = Utility.formatTwoDecimal(compensationDetails.getAllowance().getPhoneAllowance());
        String clothing = Utility.formatTwoDecimal(compensationDetails.getAllowance().getClothingAllowance());

        String netPay = Utility.formatTwoDecimal(compensationDetails.getNetSalary());

        lblRegularHours.setText(regularHours);
        lblOvertimeHours.setText(overtimeHours);
        lblRegularPay.setText(regularPay);
        lblOvertimePay.setText(overtimePay);
        lblGrossSalary.setText(grossSalary);

        lblSSSDeductions.setText(sss);
        lblPagIBIGDeductions.setText(pagibig);
        lblPhilHealthDeductions.setText(philhealth);
        lblWithholdingTax.setText(tax);

        lblRiceAllowance2.setText(rice);
        lblClothingAllowance2.setText(clothing);
        lblPhoneAllowance2.setText(phone);

        lblNetSalary.setText(netPay);
        lblPayrollStatus.setText(payroll.getStatus().toString());
    }

    private JPanel createFormCard(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.styleCardPanel(panel, title);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private JTextField createReadOnlyField() {
        JTextField field = new JTextField();
        UITheme.styleReadOnlyField(field);
        return field;
    }

    private void addFormRow(JPanel panel, int row, String labelText, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(6, 8, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        UITheme.styleLabel(label);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 8);
        panel.add(field, gbc);
    }

    private void addLabelRow(JPanel panel, int row, String labelText, JLabel valueLabel, boolean emphasize) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(6, 8, 6, 12);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        UITheme.styleLabel(label);
        label.setFont(label.getFont().deriveFont(emphasize ? Font.BOLD : Font.PLAIN));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 8);
        panel.add(valueLabel, gbc);
    }

    private void styleValueLabel(JLabel label, boolean emphasize) {
        label.setForeground(UITheme.INK);
        label.setFont(emphasize ? UITheme.TITLE_FONT.deriveFont(18f) : UITheme.FIELD_FONT);
    }
}

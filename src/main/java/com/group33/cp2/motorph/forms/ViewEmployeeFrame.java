package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.Payroll;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;
import com.group33.cp2.motorph.util.Utility;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.Year;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

// View Employee — read-only employee detail with payroll period selection.
// JDateChooser fields removed; clearAllFields() corrected to only reference declared fields.
public class ViewEmployeeFrame extends javax.swing.JFrame {

    private EmployeeService employeeService = new EmployeeService();
    private final boolean canEditCompensation;
    private Employee selectedEmployee;

    // GUI fields for employee and payroll info
    private JTextField txtEmployeeNumber;
    private JTextField txtLastName;
    private JTextField txtFirstName;
    private JTextField txtBirthday;
    private JTextField txtAddress;
    private JTextField txtPosition;
    private JTextField txtHourlyRate;
    private JTextField txtSSSNumber;
    private JTextField txtPhilHealthNumber;
    private JTextField txtPagibigNumber;
    private JTextField txtTINNumber;
    private JTextField txtRiceSubsidy;
    private JTextField txtClothingAllowance;
    private JTextField txtPhoneAllowance;
    private JTextField txtPhoneNumber;
    private JTextField txtStatus;
    private JTextField txtImmediateSupervisor;
    private JTextField txtGrossSemiMonthly;
    private JTextField txtBasicSalary;
    private DateDropdownPanel coverageStartChooser;
    private DateDropdownPanel coverageEndChooser;

    public ViewEmployeeFrame(String employeeId, boolean canEditCompensation) {
        this.canEditCompensation = canEditCompensation;
        setTitle("MotorPH Employee Payroll System");
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(ViewEmployeeFrame.this)) {
                    dispose();
                }
            }
        });

        // Fetch employee once
        selectedEmployee = employeeService.getEmployeeById(employeeId);

        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID " + employeeId + " was not found.",
                    "Load Error", JOptionPane.ERROR_MESSAGE);
            NavigationManager.openEmployeeListFrame(this, canEditCompensation); // Go back if employee not found
            return;
        }

        JPanel contentPanel = new JPanel(new BorderLayout(0, 18));
        UITheme.styleSurfacePanel(contentPanel);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = UITheme.createPageHeader(
                "EMPLOYEE RECORDS",
                "Employee Payroll Profile",
                "Review employee, government, and compensation details before running payroll."
        );
        contentPanel.add(header, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.insets = new Insets(0, 0, 0, 18);
        rootGbc.fill = GridBagConstraints.BOTH;
        rootGbc.weighty = 1.0;

        // Left panel: Employee details
        JPanel leftPanel = createEmployeeDetailsPanel();

        // Right panel: Government details, compensation, and payroll period selection
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(createGovernmentDetailsPanel());
        rightPanel.add(javax.swing.Box.createVerticalStrut(14));
        rightPanel.add(createCompensationDetailsPanel());
        rightPanel.add(javax.swing.Box.createVerticalStrut(14));
        rightPanel.add(createPayrollPanel(employeeId)); // employeeId still needed for payroll panel's compute action

        // Populate fields using the fetched selectedEmployee
        setEmployeeDetails();
        setGovernmentDetails();
        setCompensationDetails();

        rootGbc.gridx = 0;
        rootGbc.gridy = 0;
        rootGbc.weightx = 1.15;
        mainPanel.add(leftPanel, rootGbc);

        rootGbc.gridx = 1;
        rootGbc.insets = new Insets(0, 0, 0, 0);
        rootGbc.weightx = 0.95;
        mainPanel.add(rightPanel, rootGbc);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.APP_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(contentPanel);

        disableFields();
    }

    private void setEmployeeDetails() {
        txtEmployeeNumber.setText(selectedEmployee.getEmployeeID());
        txtLastName.setText(selectedEmployee.getLastName());
        txtFirstName.setText(selectedEmployee.getFirstName());
        txtBirthday.setText(selectedEmployee.getBirthday());
        txtAddress.setText(selectedEmployee.getAddress());
        txtPhoneNumber.setText(selectedEmployee.getPhoneNumber());
        txtStatus.setText(selectedEmployee.getStatus());
        txtPosition.setText(selectedEmployee.getPosition());
        txtImmediateSupervisor.setText(selectedEmployee.getImmediateSupervisor());
    }

    private void setGovernmentDetails() {
        txtSSSNumber.setText(selectedEmployee.getGovernmentDetails().getSssNumber());
        txtPhilHealthNumber.setText(selectedEmployee.getGovernmentDetails().getPhilHealthNumber());
        txtTINNumber.setText(selectedEmployee.getGovernmentDetails().getTinNumber());
        txtPagibigNumber.setText(selectedEmployee.getGovernmentDetails().getPagibigNumber());
    }

    private void setCompensationDetails() {
        txtRiceSubsidy.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getRiceAllowance()));
        txtPhoneAllowance.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getPhoneAllowance()));
        txtClothingAllowance.setText(Utility.formatTwoDecimal(selectedEmployee.getAllowanceDetails().getClothingAllowance()));
        txtGrossSemiMonthly.setText(Utility.formatTwoDecimal(selectedEmployee.getGrossSemiMonthlyRate()));
        txtHourlyRate.setText(Utility.formatTwoDecimal(selectedEmployee.getHourlyRate()));
        txtBasicSalary.setText(Utility.formatTwoDecimal(selectedEmployee.getBasicSalary()));
    }
    // Disables all text fields to prevent editing (read-only view).
    private void disableFields() {
        txtEmployeeNumber.setEditable(false);
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtBirthday.setEditable(false);
        txtAddress.setEditable(false);
        txtPosition.setEditable(false);
        txtPhoneNumber.setEditable(false);
        txtStatus.setEditable(false);
        txtImmediateSupervisor.setEditable(false);
        txtSSSNumber.setEditable(false);
        txtPhilHealthNumber.setEditable(false);
        txtTINNumber.setEditable(false);
        txtPagibigNumber.setEditable(false);
        txtPhoneAllowance.setEditable(false);
        txtClothingAllowance.setEditable(false);
        txtGrossSemiMonthly.setEditable(false);
        txtHourlyRate.setEditable(false);
        txtBasicSalary.setEditable(false);
        txtRiceSubsidy.setEditable(false);
    }

    // Creates the panel displaying employee personal information.
    private JPanel createEmployeeDetailsPanel() {
        JPanel panel = createFormCard("Employee Details");
        txtEmployeeNumber = createReadOnlyField();
        txtLastName = createReadOnlyField();
        txtFirstName = createReadOnlyField();
        txtBirthday = createReadOnlyField();
        txtAddress = createReadOnlyField();
        txtPhoneNumber = createReadOnlyField();
        txtStatus = createReadOnlyField();
        txtPosition = createReadOnlyField();
        txtImmediateSupervisor = createReadOnlyField();

        addFormRow(panel, 0, "Employee Number:", txtEmployeeNumber);
        addFormRow(panel, 1, "Last Name:", txtLastName);
        addFormRow(panel, 2, "First Name:", txtFirstName);
        addFormRow(panel, 3, "Birthday:", txtBirthday);
        addFormRow(panel, 4, "Address:", txtAddress);
        addFormRow(panel, 5, "Phone Number:", txtPhoneNumber);
        addFormRow(panel, 6, "Status:", txtStatus);
        addFormRow(panel, 7, "Position:", txtPosition);
        addFormRow(panel, 8, "Immediate Supervisor:", txtImmediateSupervisor);
        return panel;
    }

    // Creates the panel displaying government ID details.
    private JPanel createGovernmentDetailsPanel() {
        JPanel panel = createFormCard("Government Details");
        txtSSSNumber = createReadOnlyField();
        txtPhilHealthNumber = createReadOnlyField();
        txtTINNumber = createReadOnlyField();
        txtPagibigNumber = createReadOnlyField();

        addFormRow(panel, 0, "SSS Number:", txtSSSNumber);
        addFormRow(panel, 1, "PhilHealth Number:", txtPhilHealthNumber);
        addFormRow(panel, 2, "TIN Number:", txtTINNumber);
        addFormRow(panel, 3, "Pag-IBIG Number:", txtPagibigNumber);
        return panel;
    }

    // Creates the panel displaying compensation and allowance details.
    private JPanel createCompensationDetailsPanel() {
        JPanel panel = createFormCard("Compensation Details");
        txtRiceSubsidy = createReadOnlyField();
        txtPhoneAllowance = createReadOnlyField();
        txtClothingAllowance = createReadOnlyField();
        txtGrossSemiMonthly = createReadOnlyField();
        txtHourlyRate = createReadOnlyField();
        txtBasicSalary = createReadOnlyField();

        addFormRow(panel, 0, "Rice Subsidy:", txtRiceSubsidy);
        addFormRow(panel, 1, "Phone Allowance:", txtPhoneAllowance);
        addFormRow(panel, 2, "Clothing Allowance:", txtClothingAllowance);
        addFormRow(panel, 3, "Gross Semi Monthly Rate:", txtGrossSemiMonthly);
        addFormRow(panel, 4, "Hourly Rate:", txtHourlyRate);
        addFormRow(panel, 5, "Basic Salary:", txtBasicSalary);
        return panel;
    }

    // Creates the payroll period selection panel with month/year combo boxes and Compute/Cancel buttons.
    private JPanel createPayrollPanel(String employeeId) {
        JPanel panel = createFormCard("Payroll");

        coverageStartChooser = new DateDropdownPanel();
        coverageEndChooser = new DateDropdownPanel();
        LocalDate now = LocalDate.now();
        coverageStartChooser.setDate(now.withDayOfMonth(1));
        coverageEndChooser.setDate(now.withDayOfMonth(now.lengthOfMonth()));

        JButton btnCancel = new JButton("Cancel");
        JButton btnCompute = new JButton("Compute");

        UITheme.styleDangerButton(btnCancel);
        UITheme.stylePrimaryButton(btnCompute);
        btnCancel.setPreferredSize(new Dimension(170, 42));
        btnCompute.setPreferredSize(new Dimension(170, 42));

        btnCancel.addActionListener(e -> NavigationManager.openEmployeeListFrame(this, canEditCompensation));

        // Capture as effectively-final references for use inside the SwingWorker anonymous class.
        final JButton computeBtn = btnCompute;

        btnCompute.addActionListener(e -> {
            LocalDate selectedStartDate = coverageStartChooser.getSelectedDate();
            LocalDate selectedEndDate = coverageEndChooser.getSelectedDate();

            if (selectedStartDate == null || selectedEndDate == null) {
                JOptionPane.showMessageDialog(
                        ViewEmployeeFrame.this,
                        "Please select both a start and end date for the pay coverage.",
                        "Coverage Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selectedEndDate.isBefore(selectedStartDate)) {
                JOptionPane.showMessageDialog(
                        ViewEmployeeFrame.this,
                        "Pay coverage end date cannot be earlier than the start date.",
                        "Invalid Coverage",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Disable the button immediately to prevent double-clicks while I/O runs.
            computeBtn.setEnabled(false);

            new SwingWorker<Double, Void>() {
                @Override
                protected Double doInBackground() throws Exception {
                    // Payroll construction reads attendance CSV — runs off the EDT.
                    Payroll payroll = new Payroll(
                            selectedEmployee.getEmployeeID(),
                            selectedEmployee,
                            selectedStartDate,
                            selectedEndDate);
                    return payroll.getTotalRegularHours();
                }

                @Override
                protected void done() {
                    // Re-enable the button regardless of outcome.
                    computeBtn.setEnabled(true);
                    try {
                        double totalRegularHours = get();
                        if (totalRegularHours <= 0) {
                            JOptionPane.showMessageDialog(
                                    ViewEmployeeFrame.this,
                                    "No salary record found for the selected month.",
                                    "Notice",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            NavigationManager.openViewSalaryFrame(
                                    ViewEmployeeFrame.this, employeeId,
                                    selectedStartDate, selectedEndDate, canEditCompensation);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                ViewEmployeeFrame.this,
                                "Failed to compute payroll: " + ex.getMessage(),
                                "Compute Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCoverage = new JLabel("Pay Coverage:");
        UITheme.styleLabel(lblCoverage);
        lblCoverage.setFont(lblCoverage.getFont().deriveFont(Font.BOLD));

        JLabel lblCoverageStart = new JLabel("Start Date:");
        UITheme.styleLabel(lblCoverageStart);
        lblCoverageStart.setFont(lblCoverageStart.getFont().deriveFont(Font.BOLD));

        JLabel lblCoverageEnd = new JLabel("End Date:");
        UITheme.styleLabel(lblCoverageEnd);
        lblCoverageEnd.setFont(lblCoverageEnd.getFont().deriveFont(Font.BOLD));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(lblCoverage, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(lblCoverageStart, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        panel.add(coverageStartChooser, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(lblCoverageEnd, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1;
        panel.add(coverageEndChooser, gbc);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(btnCancel);
        buttonRow.add(btnCompute);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        panel.add(buttonRow, gbc);

        return panel;
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
        gbc.fill = GridBagConstraints.NONE;

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

    // Clears only employee detail fields.
    private void clearEmployeeFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtBirthday.setText("");
        txtAddress.setText("");
        txtPosition.setText("");
        txtHourlyRate.setText("");
    }

    // Clears all declared fields. Original had JDateChooser/PayrollFormMs1 refs that were removed.
    private void clearAllFields() {
        txtEmployeeNumber.setText("");
        clearEmployeeFields();
        txtSSSNumber.setText("");
        txtPhilHealthNumber.setText("");
        txtPagibigNumber.setText("");
        txtTINNumber.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");
        txtGrossSemiMonthly.setText("");
        txtBasicSalary.setText("");
        txtPhoneNumber.setText("");
        txtStatus.setText("");
        txtImmediateSupervisor.setText("");
    }

}

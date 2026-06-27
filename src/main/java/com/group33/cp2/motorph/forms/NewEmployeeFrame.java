package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Allowance;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.GovernmentDetails;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DecimalOnlyFilter;
import com.group33.cp2.motorph.util.DialogUtil;
import com.group33.cp2.motorph.util.DigitLengthFilter;
import com.group33.cp2.motorph.util.FormattedDigitsFilter;
import com.group33.cp2.motorph.util.LettersOnlyFilter;
import com.group33.cp2.motorph.util.MaxLengthNoNewlineFilter;
import com.group33.cp2.motorph.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;

// New Employee form — collects and validates personal/government/compensation details,
// then saves via EmployeeService. Birthday uses month/day/year combo boxes.
public class NewEmployeeFrame extends javax.swing.JFrame {

    private EmployeeService employeeService;
    private final boolean canEditCompensation;

    // Personal Information Fields (Left Column)
    private JTextField txtEmployeeNumber;
    private JTextField txtLastName;
    private JTextField txtFirstName;
    private DateDropdownPanel birthdayChooser;
    private JTextField txtAddress;
    private JTextField txtPhoneNumber;
    private JComboBox<String> cmbStatus;
    private JTextField txtPosition;
    private JTextField txtImmediateSupervisor;

    // Government Details Fields (Right Column, Top)
    private JTextField txtSSSNumber;
    private JTextField txtPhilHealthNumber;
    private JTextField txtTINNumber;
    private JTextField txtPagIBIGNumber;

    // Compensation Details Fields (Right Column, Bottom)
    private JTextField txtRiceSubsidy;
    private JTextField txtPhoneAllowance;
    private JTextField txtClothingAllowance;
    private JTextField txtGrossSemiMonthly;
    private JTextField txtHourlyRate;
    private JTextField txtBasicSalary;

    // Action Buttons
    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnClear;

    public NewEmployeeFrame(boolean canEditCompensation) {
        this.canEditCompensation = canEditCompensation;
        employeeService = new EmployeeService();
        initializeComponents();
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setupLayout();
        setupEventHandlers();
        setDefaultValues();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(NewEmployeeFrame.this)) {
                    dispose();
                }
            }
        });
    }

    // Initialises all GUI components.
    private void initializeComponents() {
        setTitle("Add Employee");
        getContentPane().setBackground(UITheme.APP_BACKGROUND);

        txtEmployeeNumber = new JTextField(15);
        txtEmployeeNumber.setEditable(false);

        txtLastName = new JTextField(20);
        txtFirstName = new JTextField(20);
        birthdayChooser = new DateDropdownPanel();

        txtAddress = new JTextField(25);
        txtPhoneNumber = new JTextField(15);

        // Status options must match Employee.VALID_STATUSES whitelist
        String[] statusOptions = {"Regular", "Probationary", "Active", "Inactive", "On Leave", "Terminated"};
        cmbStatus = new JComboBox<>(statusOptions);
        cmbStatus.setSelectedIndex(0);

        txtPosition = new JTextField(20);
        txtImmediateSupervisor = new JTextField(20);

        txtSSSNumber = new JTextField(15);
        txtPhilHealthNumber = new JTextField(15);
        txtTINNumber = new JTextField(15);
        txtPagIBIGNumber = new JTextField(15);

        txtRiceSubsidy = new JTextField(15);
        txtPhoneAllowance = new JTextField(15);
        txtClothingAllowance = new JTextField(15);
        txtGrossSemiMonthly = new JTextField(15);
        txtHourlyRate = new JTextField(15);
        txtBasicSalary = new JTextField(15);

        btnSave = new JButton("Save Employee");
        btnCancel = new JButton("Cancel");
        btnClear = new JButton("Clear All Fields");

        Dimension buttonSize = new Dimension(170, 38);
        btnSave.setPreferredSize(buttonSize);
        btnCancel.setPreferredSize(buttonSize);
        btnClear.setPreferredSize(buttonSize);

        applyTheme();
        configureInputFormatting();
    }

    // Builds the two-column layout and adds it to the frame.
    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        UITheme.styleSurfacePanel(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JPanel contentStack = new JPanel();
        contentStack.setLayout(new BoxLayout(contentStack, BoxLayout.Y_AXIS));
        UITheme.styleSurfacePanel(contentStack);

        JPanel headerPanel = UITheme.createPageHeader(
                "EMPLOYEE RECORDS",
                "Add a New Team Member",
                "Capture personal, government, and compensation details."
        );

        JPanel formPanel = new JPanel(new GridBagLayout());
        UITheme.styleSurfacePanel(formPanel);

        JPanel leftColumn = createPersonalInformationPanel();
        JPanel rightTopColumn = createGovernmentDetailsPanel();
        JPanel rightBottomColumn = createCompensationDetailsPanel();
        JPanel buttonPanel = createButtonPanel();

        JPanel rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(0, 0, 10, 0);
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;
        rightGbc.gridx = 0;
        rightGbc.gridy = 0;
        rightGbc.weighty = 0.5; // Distribute vertical space
        rightColumn.add(rightTopColumn, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 1;
        rightGbc.weighty = 0.5; // Distribute vertical space
        rightColumn.add(rightBottomColumn, rightGbc);

        rightGbc.gridx = 0;
        rightGbc.gridy = 2;
        rightGbc.weighty = 0.0;
        rightGbc.insets = new Insets(14, 0, 0, 0);
        rightColumn.add(buttonPanel, rightGbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 15);
        formPanel.add(leftColumn, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 15, 0, 0);
        formPanel.add(rightColumn, gbc);

        contentStack.add(headerPanel);
        contentStack.add(formPanel);

        mainPanel.add(contentStack, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.APP_BACKGROUND);

        add(scrollPane, BorderLayout.CENTER);
        pack();
        setSize(Math.max(getWidth(), Constants.FRAME_WIDTH), Math.max(getHeight(), Constants.FRAME_HEIGHT));
        setLocationRelativeTo(getParent());
    }

    private JPanel createPersonalInformationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.styleCardPanel(panel, "Personal Information");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Employee Number:"), gbc);
        gbc.gridx = 1;
        panel.add(txtEmployeeNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Last Name: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtLastName, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("First Name: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtFirstName, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Birthday:"), gbc);
        gbc.gridx = 1;
        panel.add(birthdayChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        panel.add(txtAddress, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhoneNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPosition, gbc);

        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(new JLabel("Immediate Supervisor:"), gbc);
        gbc.gridx = 1;
        panel.add(txtImmediateSupervisor, gbc);

        return panel;
    }

    private JPanel createGovernmentDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.styleCardPanel(panel, "Government Details");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("SSS Number: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtSSSNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("PhilHealth Number: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhilHealthNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("TIN Number: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtTINNumber, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Pag-IBIG Number: *"), gbc);
        gbc.gridx = 1;
        panel.add(txtPagIBIGNumber, gbc);

        return panel;
    }

    private JPanel createCompensationDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UITheme.styleCardPanel(panel, "Compensation Details");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Basic Salary:"), gbc);
        gbc.gridx = 1;
        panel.add(txtBasicSalary, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Gross Semi-Monthly:"), gbc);
        gbc.gridx = 1;
        panel.add(txtGrossSemiMonthly, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Hourly Rate:"), gbc);
        gbc.gridx = 1;
        panel.add(txtHourlyRate, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Rice Subsidy:"), gbc);
        gbc.gridx = 1;
        panel.add(txtRiceSubsidy, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Phone Allowance:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPhoneAllowance, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Clothing Allowance:"), gbc);
        gbc.gridx = 1;
        panel.add(txtClothingAllowance, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panel.setOpaque(false);
        panel.add(btnSave);
        panel.add(btnClear);
        panel.add(btnCancel);
        return panel;
    }

    private void applyTheme() {
        UITheme.styleTextField(txtEmployeeNumber);
        UITheme.styleReadOnlyField(txtEmployeeNumber);

        JTextField[] textFields = {
            txtLastName, txtFirstName, txtAddress, txtPhoneNumber, txtPosition,
            txtImmediateSupervisor, txtSSSNumber, txtPhilHealthNumber,
            txtTINNumber, txtPagIBIGNumber, txtRiceSubsidy, txtPhoneAllowance,
            txtClothingAllowance, txtGrossSemiMonthly, txtHourlyRate, txtBasicSalary
        };
        for (JTextField field : textFields) {
            UITheme.styleTextField(field);
        }

        UITheme.styleComboBox(cmbStatus);
        UITheme.styleComponentTree(birthdayChooser);

        UITheme.stylePrimaryButton(btnSave);
        UITheme.styleSecondaryButton(btnClear);
        UITheme.styleDangerButton(btnCancel);
    }

    private void configureInputFormatting() {
        // VAL-4: cap address at 200 chars and strip newlines to prevent CSV corruption
        ((AbstractDocument) txtAddress.getDocument())
            .setDocumentFilter(new MaxLengthNoNewlineFilter(200));
        applyFormattedDigitsFilter(txtPhoneNumber, 10, 3, 3, 4);
        applyFormattedDigitsFilter(txtSSSNumber, 10, 2, 7, 1);
        applyDigitLengthFilter(txtPagIBIGNumber, 12);
        applyDigitLengthFilter(txtPhilHealthNumber, 12);
        applyFormattedDigitsFilter(txtTINNumber, 12, 3, 3, 3, 3);
        applyDecimalFilter(txtBasicSalary);
        applyDecimalFilter(txtGrossSemiMonthly);
        applyDecimalFilter(txtHourlyRate);
        applyDecimalFilter(txtRiceSubsidy);
        applyDecimalFilter(txtPhoneAllowance);
        applyDecimalFilter(txtClothingAllowance);
        applyLettersOnlyFilter(txtLastName);
        applyLettersOnlyFilter(txtFirstName);
        applyLettersOnlyFilter(txtPosition);
        applyLettersOnlyFilter(txtImmediateSupervisor);
    }

    private void applyDigitLengthFilter(JTextField field, int maxDigits) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DigitLengthFilter(maxDigits));
    }

    private void applyFormattedDigitsFilter(JTextField field, int maxDigits, int... groupSizes) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new FormattedDigitsFilter(maxDigits, groupSizes));
    }

    private void applyDecimalFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DecimalOnlyFilter());
    }

    private void applyLettersOnlyFilter(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new LettersOnlyFilter());
    }

    private void setupEventHandlers() {
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveEmployee();
            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFields();
            }
        });

        JFrame currentFrame = this;
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        NewEmployeeFrame.this,
                        "Are you sure you want to cancel? All unsaved data will be lost.",
                        "Confirm Cancel",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (option == JOptionPane.YES_OPTION) {
                    NavigationManager.openEmployeeListFrame(currentFrame, canEditCompensation);
                }
            }
        });
    }

    private void setDefaultValues() {
        String newEmployeeID = employeeService.generateEmployeeID();
        txtEmployeeNumber.setText(newEmployeeID);

        txtRiceSubsidy.setText("1500.00");
        txtPhoneAllowance.setText("1000.00");
        txtClothingAllowance.setText("1000.00");
        txtBasicSalary.setText("");
        txtHourlyRate.setText("");
        txtGrossSemiMonthly.setText("");

        txtLastName.setText("");
        txtFirstName.setText("");
        birthdayChooser.clearSelection();
        txtAddress.setText("");
        txtPhoneNumber.setText("");
        txtPosition.setText("");
        txtImmediateSupervisor.setText("");
        cmbStatus.setSelectedIndex(0);

        txtSSSNumber.setText("");
        txtPhilHealthNumber.setText("");
        txtTINNumber.setText("");
        txtPagIBIGNumber.setText("");

        txtLastName.requestFocus();
    }

    private void saveEmployee() {
        try {
            if (!validateRequiredFields()) {
                return;
            }
            if (!validateFormats()) {
                return;
            }
            if (!validateNumericFields()) {
                return;
            }

            // Duplicate-ID guard: reject if the auto-generated ID already exists
            // (can happen after an out-of-order delete-then-add cycle).
            String generatedId = txtEmployeeNumber.getText().trim();
            if (employeeService.getEmployeeById(generatedId) != null) {
                JOptionPane.showMessageDialog(this,
                        "Employee ID " + generatedId + " already exists.\n"
                        + "Please close this form and try again to get a new unique ID.",
                        "Duplicate Employee ID",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Employee newEmployee = createEmployeeFromForm();
            employeeService.addEmployee(newEmployee);

            JOptionPane.showMessageDialog(
                    this,
                    "Employee " + newEmployee.getFirstName() + " has been successfully added!\n"
                    + "Employee ID: " + newEmployee.getEmployeeID() + "\n"
                    + "The employee data has been saved to the CSV file.",
                    "Employee Added Successfully",
                    JOptionPane.INFORMATION_MESSAGE
            );

            NavigationManager.openEmployeeListFrame(this, canEditCompensation);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please check that all numeric fields contain valid numbers.\n"
                    + "Error details: " + e.getMessage(),
                    "Invalid Number Format", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "An error occurred while saving the employee:\n" + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean validateRequiredFields() {
        if (txtLastName.getText().trim().isEmpty()) {
            showValidationError("Last Name is required.", txtLastName);
            return false;
        }
        if (txtFirstName.getText().trim().isEmpty()) {
            showValidationError("First Name is required.", txtFirstName);
            return false;
        }
        if (!birthdayChooser.isSelectionComplete()) {
            showValidationError("Birthday is required.", null);
            birthdayChooser.focusFirstField();
            return false;
        }
        if (txtSSSNumber.getText().trim().isEmpty()) {
            showValidationError("SSS Number is required.", txtSSSNumber);
            return false;
        }
        if (txtPhilHealthNumber.getText().trim().isEmpty()) {
            showValidationError("PhilHealth Number is required.", txtPhilHealthNumber);
            return false;
        }
        if (txtTINNumber.getText().trim().isEmpty()) {
            showValidationError("TIN Number is required.", txtTINNumber);
            return false;
        }
        if (txtPagIBIGNumber.getText().trim().isEmpty()) {
            showValidationError("Pag-IBIG Number is required.", txtPagIBIGNumber);
            return false;
        }
        if (txtPhoneNumber.getText().trim().isEmpty()) {
            showValidationError("Phone Number is required.", txtPhoneNumber);
            return false;
        }
        return true;
    }

    private boolean validateNumericFields() {
        double basicSalary, hourlyRate, grossSemi;
        try {
            basicSalary = Double.parseDouble(txtBasicSalary.getText().trim().replace(",", ""));
            hourlyRate  = Double.parseDouble(txtHourlyRate.getText().trim().replace(",", ""));
            grossSemi   = Double.parseDouble(txtGrossSemiMonthly.getText().trim().replace(",", ""));
            Double.parseDouble(txtRiceSubsidy.getText().trim().replace(",", ""));
            Double.parseDouble(txtPhoneAllowance.getText().trim().replace(",", ""));
            Double.parseDouble(txtClothingAllowance.getText().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values for all salary and allowance fields. Do not include currency symbols.",
                    "Invalid Numeric Input", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (basicSalary <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Basic Salary must be greater than zero.",
                    "Invalid Salary", JOptionPane.WARNING_MESSAGE);
            txtBasicSalary.requestFocus();
            return false;
        }
        // VAL-5: grossSemiMonthly was parsed but never range-validated — zero/negative silently written to CSV
        if (grossSemi <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Gross Semi-Monthly must be greater than zero.",
                    "Invalid Value", JOptionPane.WARNING_MESSAGE);
            txtGrossSemiMonthly.requestFocus();
            return false;
        }
        if (hourlyRate <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Hourly Rate must be greater than zero.",
                    "Invalid Rate", JOptionPane.WARNING_MESSAGE);
            txtHourlyRate.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateFormats() {
        LocalDate birthday = birthdayChooser.getSelectedDate();
        if (birthday == null) {
            showValidationError("Birthday must be a valid date.", null);
            birthdayChooser.focusFirstField();
            return false;
        }
        if (birthday.isAfter(LocalDate.now())) {
            showValidationError("Birthday cannot be in the future.", null);
            birthdayChooser.focusFirstField();
            return false;
        }

        String sss = txtSSSNumber.getText().trim();
        if (!ValidationUtil.isValidSSS(sss)) {
            showValidationError("SSS Number should follow format: XX-XXXXXXX-X (e.g., 44-4506057-3)", txtSSSNumber);
            return false;
        }

        String tin = txtTINNumber.getText().trim();
        if (!ValidationUtil.isValidTIN(tin)) {
            showValidationError("TIN Number should follow format: XXX-XXX-XXX-XXX (e.g., 442-605-657-000)", txtTINNumber);
            return false;
        }

        String philhealth = txtPhilHealthNumber.getText().trim();
        if (!ValidationUtil.isValidPhilHealth(philhealth)) {
            showValidationError("PhilHealth Number should be 12 digits (e.g., 820126853951)", txtPhilHealthNumber);
            return false;
        }

        String pagibig = txtPagIBIGNumber.getText().trim();
        if (!ValidationUtil.isValidPagIBIG(pagibig)) {
            showValidationError("Pag-IBIG Number should be 12 digits (e.g., 691295330870)", txtPagIBIGNumber);
            return false;
        }

        return true;
    }

    private Employee createEmployeeFromForm() {
        String employeeID = txtEmployeeNumber.getText().trim();
        String lastName = txtLastName.getText().trim();
        String firstName = txtFirstName.getText().trim();
        String birthday = birthdayChooser.getFormattedDate();
        String address = txtAddress.getText().trim();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String position = txtPosition.getText().trim();
        String immediateSupervisor = txtImmediateSupervisor.getText().trim();

        double basicSalary = Double.parseDouble(txtBasicSalary.getText().trim().replace(",", ""));
        double hourlyRate = Double.parseDouble(txtHourlyRate.getText().trim().replace(",", ""));
        double grossSemiMonthly = Double.parseDouble(txtGrossSemiMonthly.getText().trim().replace(",", ""));
        double riceSubsidy = Double.parseDouble(txtRiceSubsidy.getText().trim().replace(",", ""));
        double phoneAllowance = Double.parseDouble(txtPhoneAllowance.getText().trim().replace(",", ""));
        double clothingAllowance = Double.parseDouble(txtClothingAllowance.getText().trim().replace(",", ""));

        GovernmentDetails governmentDetails = new GovernmentDetails(
                employeeID,
                txtSSSNumber.getText().trim(),
                txtPhilHealthNumber.getText().trim(),
                txtTINNumber.getText().trim(),
                txtPagIBIGNumber.getText().trim()
        );

        Allowance allowance = new Allowance(employeeID, riceSubsidy, phoneAllowance, clothingAllowance);

        // Delegate subtype selection to EmployeeService.createEmployee():
        // the form no longer decides between RegularEmployee and ProbationaryEmployee.
        return employeeService.createEmployee(
                employeeID, lastName, firstName, birthday, address, phoneNumber,
                basicSalary, hourlyRate, grossSemiMonthly, status, position,
                immediateSupervisor, allowance, governmentDetails
        );
    }
    private void clearAllFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        birthdayChooser.clearSelection();
        txtAddress.setText("");
        txtPhoneNumber.setText("");
        cmbStatus.setSelectedIndex(0);
        txtPosition.setText("");
        txtImmediateSupervisor.setText("");

        txtSSSNumber.setText("");
        txtPhilHealthNumber.setText("");
        txtTINNumber.setText("");
        txtPagIBIGNumber.setText("");

        txtBasicSalary.setText("");
        txtGrossSemiMonthly.setText("");
        txtHourlyRate.setText("");
        txtRiceSubsidy.setText("");
        txtPhoneAllowance.setText("");
        txtClothingAllowance.setText("");

        setDefaultValues();
    }

    private void showValidationError(String message, java.awt.Component component) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
        if (component != null) {
            component.requestFocus();
        }
    }

}

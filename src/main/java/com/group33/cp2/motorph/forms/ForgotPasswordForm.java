package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.service.PasswordResetException;
import com.group33.cp2.motorph.service.PasswordResetService;
import com.group33.cp2.motorph.util.DialogUtil;
import java.sql.SQLException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

// Forgot Password form — submits a PasswordResetRequest; employee provides 5-digit ID and full name.
// All I/O delegates to PasswordResetService; no direct dao/ imports.
public class ForgotPasswordForm extends JFrame {

 
    //  Constants
     private static final int    FORM_WIDTH        = 420;
    private static final int    FORM_HEIGHT       = 310;
    private static final String EMP_NUM_PATTERN   = "\\d{5}";
    private static final String EMP_NAME_PATTERN  = "[a-zA-Z ]+";
    private static final Color  BUTTON_COLOR      = new Color(0, 70, 153);
    private static final Color  BUTTON_TEXT_COLOR = Color.WHITE;

 
    //  Service dependency
     private final PasswordResetService resetService;

 
    //  UI components
     private JTextField txtEmpNum;
    private JTextField txtEmpName;
    private JButton    btnSubmit;
    private JLabel     lblBack;

  
    //  Constructor
      public ForgotPasswordForm() {
        this.resetService = new PasswordResetService();
        initComponents();
        setResizable(true);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(ForgotPasswordForm.this)) {
                    dispose();
                }
            }
        });
    }



    // Builds the GridBagLayout form. No NetBeans generator — safe to hand-edit.
    private void initComponents() {
        setTitle("Forgot Password");
        setPreferredSize(new Dimension(FORM_WIDTH, FORM_HEIGHT));
        setSize(FORM_WIDTH, FORM_HEIGHT);

        // ---- Title panel ----
        JLabel lblTitle = new JLabel("Forgot Password?");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblInstruction = new JLabel(
                "<html><center>Enter your employee number and full name<br>"
                + "to submit a password reset request.</center></html>");
        lblInstruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInstruction.setHorizontalAlignment(SwingConstants.CENTER);

        // ---- Form fields ----
        JLabel lblEmpNum  = new JLabel("Employee Number:");
        JLabel lblEmpName = new JLabel("Full Name (First Last):");

        txtEmpNum  = new JTextField();
        txtEmpName = new JTextField();
        txtEmpNum.setPreferredSize(new Dimension(220, 28));
        txtEmpName.setPreferredSize(new Dimension(220, 28));

        // ---- Submit button ----
        btnSubmit = new JButton("Submit Request");
        btnSubmit.setBackground(BUTTON_COLOR);
        btnSubmit.setForeground(BUTTON_TEXT_COLOR);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSubmit.addActionListener(e -> handleSubmit());

        // Allow Enter key in either text field to trigger submit
        txtEmpNum.addActionListener(e  -> handleSubmit());
        txtEmpName.addActionListener(e -> handleSubmit());

        // ---- "Back to Login" hyperlink label ----
        lblBack = new JLabel("<html><u>Back to Login</u></html>");
        lblBack.setForeground(new Color(0, 0, 200));
        lblBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblBack.setHorizontalAlignment(SwingConstants.CENTER);
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                goBackToLogin();
            }
        });

        // ---- Layout ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Row 0 — title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblTitle, gbc);

        // Row 1 — instruction
        gbc.gridy = 1;
        formPanel.add(lblInstruction, gbc);

        // Row 2 — emp number label + field
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.3;
        gbc.gridx = 0;
        formPanel.add(lblEmpNum, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtEmpNum, gbc);

        // Row 3 — name label + field
        gbc.gridy = 3; gbc.weightx = 0.3;
        gbc.gridx = 0;
        formPanel.add(lblEmpName, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        formPanel.add(txtEmpName, gbc);

        // Row 4 — submit button (full width)
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(14, 6, 4, 6);
        formPanel.add(btnSubmit, gbc);

        // Row 5 — back-to-login link (full width)
        gbc.gridy = 5;
        gbc.insets = new Insets(2, 6, 6, 6);
        formPanel.add(lblBack, gbc);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(formPanel, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    //  Event handlers
    // -------------------------------------------------------------------------

    // Validates inputs and calls PasswordResetService.submitResetRequest(); shows result dialogs.
    private void handleSubmit() {
        String empNum  = txtEmpNum.getText().trim();
        String empName = txtEmpName.getText().trim();

        // --- Input presence check ---
        if (empNum.isEmpty() || empName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in both fields.",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- Format: empNum must be exactly 5 digits ---
        if (!empNum.matches(EMP_NUM_PATTERN)) {
            JOptionPane.showMessageDialog(this,
                    "Employee number must be exactly 5 digits (e.g., 10001).",
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
            txtEmpNum.requestFocus();
            return;
        }

        // --- Format: name must contain only letters and spaces ---
        if (!empName.matches(EMP_NAME_PATTERN)) {
            JOptionPane.showMessageDialog(this,
                    "Full name must contain only letters and spaces.",
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
            txtEmpName.requestFocus();
            return;
        }

        // --- Delegate to service ---
        try {
            resetService.submitResetRequest(empNum, empName);
            JOptionPane.showMessageDialog(this,
                    "Password reset request submitted successfully.\n"
                    + "Please contact your IT administrator.",
                    "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            goBackToLogin();

        } catch (PasswordResetException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid employee details. Please check your employee number and full name.",
                    "Validation Failed", JOptionPane.ERROR_MESSAGE);

        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "An error occurred while submitting the request. Please try again.",
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Disposes this form and opens the login screen.
    private void goBackToLogin() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Clears both input fields.
    private void clearFields() {
        txtEmpNum.setText("");
        txtEmpName.setText("");
    }
}

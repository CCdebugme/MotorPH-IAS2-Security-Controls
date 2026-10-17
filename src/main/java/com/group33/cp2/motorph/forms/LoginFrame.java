package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.Admin;
import com.group33.cp2.motorph.model.Employee;
import com.group33.cp2.motorph.model.Finance;
import com.group33.cp2.motorph.model.HR;
import com.group33.cp2.motorph.model.IT;
import com.group33.cp2.motorph.service.AuthService;
import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.service.LoginAttemptService;
import com.group33.cp2.motorph.util.Constants;
import com.group33.cp2.motorph.util.DialogUtil;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;

// Login screen. BCrypt auth against Login.csv; dispatches to role dashboard on success.
// If changePassword=YES, user must set a new password before proceeding.
public class LoginFrame extends javax.swing.JFrame {

    private static final Color LOGIN_BG = new Color(8, 28, 58);
    private static final Color LOGIN_BUTTON = new Color(59, 113, 202);
    private static final Color TITLE_COLOR = Color.WHITE;
    private static final Font LABEL_FONT = new Font("Noto Sans", Font.BOLD, 18);
    private static final Font TITLE_FONT = new Font("Noto Sans", Font.BOLD, 48);
    private static final Font BUTTON_FONT = new Font("Noto Sans", Font.BOLD, 18);

    private final AuthService     authService     = new AuthService();
    private final EmployeeService employeeService = new EmployeeService();
    private final LoginAttemptService loginAttemptService = new LoginAttemptService();
    private char passwordEchoChar;

    public LoginFrame() {
        initComponents();
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(LoginFrame.this)) {
                    dispose();
                }
            }
        });

        // Allow pressing Enter in either field to trigger login
        txtPassword.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "login");
        txtPassword.getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLogin.doClick();
            }
        });

        txtUsername.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "login");
        txtUsername.getActionMap().put("login", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLogin.doClick();
            }
        });

        // Wire the "Forgot Password?" hyperlink mouse listener.
        lblForgotPassword.setForeground(LOGIN_BUTTON);
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                openForgotPassword();
            }
        });

        applyCustomStyling();
        passwordEchoChar = txtPassword.getEchoChar();

        setLocationRelativeTo(null);
    }

    // Initializes form components. Maven-only project; hand edits here are safe.
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtUsername = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        lblForgotPassword = new javax.swing.JLabel();
        chkShowPassword = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        getContentPane().removeAll();
        getContentPane().setBackground(LOGIN_BG);

        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });
        txtUsername.setBackground(Color.WHITE);
        txtUsername.setForeground(new Color(20, 20, 20));
        txtUsername.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtUsername.setMaximumSize(new Dimension(280, 42));

        jLabel1.setFont(LABEL_FONT);
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setText("Username");

        jLabel2.setFont(LABEL_FONT);
        jLabel2.setForeground(Color.WHITE);
        jLabel2.setText("Password");

        btnLogin.setBackground(LOGIN_BUTTON);
        btnLogin.setFont(BUTTON_FONT);
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btnLogin.setText("LOGIN");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        jLabel3.setFont(TITLE_FONT);
        jLabel3.setForeground(TITLE_COLOR);
        jLabel3.setText("MotorPH");

        jLabel4.setFont(TITLE_FONT);
        jLabel4.setForeground(TITLE_COLOR);
        jLabel4.setText("<html>Payroll<br>System</html>");

        txtPassword.setBackground(Color.WHITE);
        txtPassword.setForeground(new Color(20, 20, 20));
        txtPassword.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        txtPassword.setMaximumSize(new Dimension(280, 42));

        chkShowPassword.setText("Show Password");
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(Color.WHITE);
        chkShowPassword.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        chkShowPassword.setFocusPainted(false);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowPasswordActionPerformed(evt);
            }
        });

        // "Forgot Password?" hyperlink — styled and wired in constructor after initComponents()
        lblForgotPassword.setText("<html><u>Forgot Password?</u></html>");
        lblForgotPassword.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblForgotPassword.setForeground(LOGIN_BUTTON);
        lblForgotPassword.setFont(LABEL_FONT);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(LOGIN_BG);

        JPanel shellPanel = new JPanel();
        shellPanel.setLayout(new BoxLayout(shellPanel, BoxLayout.X_AXIS));
        shellPanel.setBackground(LOGIN_BG);
        shellPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(42, 72, 122), 1),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));
        shellPanel.setPreferredSize(new Dimension(760, 420));

        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(LOGIN_BG);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 36));
        brandPanel.setPreferredSize(new Dimension(330, 360));

        JLabel brandSubtitle = new JLabel("Secure access portal");
        brandSubtitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        brandSubtitle.setForeground(new Color(210, 223, 248));
        brandSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(Box.createVerticalGlue());
        jLabel3.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel4.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandPanel.add(jLabel3);
        brandPanel.add(Box.createVerticalStrut(10));
        brandPanel.add(jLabel4);
        brandPanel.add(Box.createVerticalStrut(18));
        brandPanel.add(brandSubtitle);
        brandPanel.add(Box.createVerticalGlue());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(LOGIN_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 12));

        jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblForgotPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(Box.createVerticalGlue());
        formPanel.add(jLabel1);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(jLabel2);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(txtPassword);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(chkShowPassword);
        formPanel.add(Box.createVerticalStrut(24));
        formPanel.add(btnLogin);
        formPanel.add(Box.createVerticalStrut(16));
        formPanel.add(lblForgotPassword);
        formPanel.add(Box.createVerticalGlue());

        shellPanel.add(brandPanel);
        shellPanel.add(Box.createHorizontalStrut(24));
        shellPanel.add(formPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(32, 32, 32, 32);
        gbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(shellPanel, gbc);

        setContentPane(outerPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void applyCustomStyling() {
        jLabel1.setFont(LABEL_FONT);
        jLabel2.setFont(LABEL_FONT);

        jLabel3.setFont(TITLE_FONT);
        jLabel3.setForeground(TITLE_COLOR);
        jLabel4.setFont(TITLE_FONT);
        jLabel4.setForeground(TITLE_COLOR);

        btnLogin.setFont(BUTTON_FONT);
        getContentPane().setBackground(LOGIN_BG);
        btnLogin.setBackground(LOGIN_BUTTON);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(true);

        lblForgotPassword.setFont(LABEL_FONT);
        lblForgotPassword.setForeground(LOGIN_BUTTON);
    }

    // Reads inputs, authenticates via BCrypt against Login.csv, dispatches to role dashboard.
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username should not be empty",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password should not be empty",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        // Look up Login.csv row: [0]=empNum, [1]=username, [2]=roleName, [3]=password, [4]=changePassword
        String[] loginRow = authService.getLoginDataByUsername(username);

        if (loginRow == null) {
            loginAttemptService.recordUnknownUser(username);
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Lockout check: block the attempt before the password is even checked.
        // Fails closed: if the lock status can't be read, login is refused.
        try {
            long lockedSeconds = loginAttemptService.getRemainingLockSeconds(username);
            if (lockedSeconds > 0) {
                long minutes = (lockedSeconds + 59) / 60;
                txtPassword.setText("");
                JOptionPane.showMessageDialog(this,
                        "Too many failed attempts. Try again in " + minutes + " minute(s).",
                        "Account Locked", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Login is temporarily unavailable. Please try again.",
                    "Login Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String storedHash = loginRow.length > 3 ? loginRow[3].trim() : "";
        if (!BCrypt.checkpw(password, storedHash)) {
            loginAttemptService.recordFailure(username);
            txtPassword.setText("");
            txtPassword.requestFocusInWindow();
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loginAttemptService.recordSuccess(username);

        // Check if password change is required
        String changePw = loginRow.length > 4 ? loginRow[4].trim() : "NO";
        if ("YES".equalsIgnoreCase(changePw)) {
            handleForcePasswordChange(loginRow[0].trim());
            return;
        }

        // Successful login — dispatch by role
        String empNum = loginRow[0].trim();
        String role   = loginRow.length > 2 ? loginRow[2].trim().toUpperCase() : "EMPLOYEE";

        JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + username + ".");
        openDashboardForRole(role, empNum);
    }

    // Prompts the user to set a new password and writes the BCrypt hash to Login.csv.
    // Requires a successful change before proceeding to the dashboard.
    private void handleForcePasswordChange(String empNum) {
        JPasswordField newPwField     = new JPasswordField();
        JPasswordField confirmPwField = new JPasswordField();
        Object[] message = {
            "Your password must be changed before you can continue.",
            "New Password:", newPwField,
            "Confirm Password:", confirmPwField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Change Password Required", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) {
            return; // User cancelled — stay on login screen
        }

        String newPw      = new String(newPwField.getPassword()).trim();
        String confirmPw  = new String(confirmPwField.getPassword()).trim();

        if (newPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPw.equals(confirmPw)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String hashed = BCrypt.hashpw(newPw, BCrypt.gensalt());
        try {
            boolean updated = authService.changeUserPassword(empNum, hashed);
            if (!updated) {
                JOptionPane.showMessageDialog(this, "Password change failed: employee not found.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (IOException | SQLException e) {
            JOptionPane.showMessageDialog(this, "Password change failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Password changed successfully. Please log in again.",
                "Password Changed", JOptionPane.INFORMATION_MESSAGE);
        // Don't dispatch yet — require the user to log in again with the new password
    }

    // Opens the appropriate dashboard for the given role and employee number.
    private void openDashboardForRole(String role, String empNum) {
        Employee emp = employeeService.getEmployeeById(empNum);

        switch (role) {
            case "HR" -> {
                if (emp instanceof HR hrEmp) {
                    dispose();
                    HRDashboard dash = new HRDashboard(hrEmp, employeeService);
                    dash.setVisible(true);
                } else {
                    // Employee record exists but type is not HR — open fallback
                    NavigationManager.openMenuFrame(this);
                }
            }
            case "IT" -> {
                if (emp instanceof IT itEmp) {
                    dispose();
                    ITDashboard dash = new ITDashboard(itEmp);
                    dash.setVisible(true);
                } else {
                    NavigationManager.openMenuFrame(this);
                }
            }
            case "FINANCE" -> {
                if (emp instanceof Finance finEmp) {
                    dispose();
                    FinanceDashboard dash = new FinanceDashboard(finEmp, employeeService);
                    dash.setVisible(true);
                } else {
                    NavigationManager.openMenuFrame(this);
                }
            }
            case "ADMIN" -> {
                if (emp instanceof Admin adminEmp) {
                    dispose();
                    AdminDashboard dash = new AdminDashboard(adminEmp, employeeService);
                    dash.setVisible(true);
                } else {
                    NavigationManager.openMenuFrame(this);
                }
            }
            default -> {
                // EMPLOYEE and any unrecognised roles
                if (emp != null) {
                    dispose();
                    EmployeeDashboard dash = new EmployeeDashboard(emp);
                    dash.setVisible(true);
                } else {
                    // emp not found in Employee.csv — fall back to legacy menu
                    NavigationManager.openMenuFrame(this);
                }
            }
        }
    }

    // Disposes the login frame and opens the ForgotPasswordForm.
    private void openForgotPassword() {
        dispose();
        new ForgotPasswordForm().setVisible(true);
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        login();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        // handled via key binding
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void chkShowPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowPasswordActionPerformed
        txtPassword.setEchoChar(chkShowPassword.isSelected() ? (char) 0 : passwordEchoChar);
    }//GEN-LAST:event_chkShowPasswordActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JCheckBox chkShowPassword;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblForgotPassword;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
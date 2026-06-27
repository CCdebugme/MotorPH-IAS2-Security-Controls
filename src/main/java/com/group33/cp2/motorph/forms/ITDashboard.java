package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.model.IT;
import com.group33.cp2.motorph.model.PasswordResetRequest;
import com.group33.cp2.motorph.forms.EmployeeDashboard;
import com.group33.cp2.motorph.service.PasswordResetService;
import com.group33.cp2.motorph.service.ResetPasswordProcessor;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

// IT Dashboard — password reset request table; approval generates a temp password and updates Login.csv.
public class ITDashboard extends JFrame {

    private static final Color IT_BLUE = new Color(28, 92, 184);
    private static final Color IT_BLUE_DARK = new Color(18, 68, 142);
    private static final Color IT_GREEN = new Color(54, 146, 88);
    private static final Color IT_GREEN_DARK = new Color(34, 112, 63);

    private final IT itUser;
    private final PasswordResetService   passwordResetService;
    private final ResetPasswordProcessor processor;

    private JTable requestTable;
    private DefaultTableModel requestModel;

    private static final int COL_EMP_NUM  = 0;
    private static final int COL_EMP_NAME = 1;
    private static final int COL_DATE_REQ = 2;
    private static final int COL_STATUS   = 3;

    public ITDashboard(IT itUser) {
        this.itUser               = itUser;
        this.passwordResetService = new PasswordResetService();
        this.processor            = new ResetPasswordProcessor(passwordResetService);

        setTitle("IT Dashboard — " + itUser.getFullName());
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (DialogUtil.confirmExit(ITDashboard.this)) {
                    dispose();
                }
            }
        });

        buildUI();
        loadPasswordResetRequests();
    }

    
    //  UI construction
    
    private void buildUI() {
        JPanel header = buildHeaderPanel();

        // Table
        requestModel = new DefaultTableModel(
                new String[]{"Employee #", "Employee Name", "Date Requested", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        requestTable = new JTable(requestModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.setAutoCreateRowSorter(true);
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        styleTable(requestTable, tableScrollPane);

        // Buttons
        JButton btnReset   = new JButton("Reset Password");
        JButton btnRefresh = new JButton("Refresh");
        styleButton(btnReset, false);
        styleRefreshButton(btnRefresh);
        btnReset.setPreferredSize(new Dimension(170, 48));
        btnRefresh.setPreferredSize(new Dimension(120, 48));

        btnReset.addActionListener(e -> handleResetPassword());
        btnRefresh.addActionListener(e -> loadPasswordResetRequests());

        JLabel sectionTitle = new JLabel("Password Reset Requests");
        sectionTitle.setFont(new Font("Noto Sans", Font.BOLD, 18));
        sectionTitle.setForeground(new Color(28, 47, 79));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(btnReset);
        buttonBar.add(btnRefresh);

        JPanel topBar = new JPanel(new BorderLayout(0, 10));
        topBar.setOpaque(false);
        topBar.add(sectionTitle, BorderLayout.NORTH);
        topBar.add(buttonBar, BorderLayout.SOUTH);

        JPanel helperRow = new JPanel(new BorderLayout());
        helperRow.setOpaque(false);
        JLabel helperText = new JLabel("Review pending password resets and process requests from the selected row.");
        helperText.setFont(new Font("Noto Sans", Font.PLAIN, 13));
        helperText.setForeground(new Color(90, 102, 118));
        helperRow.add(helperText, BorderLayout.WEST);

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(202, 210, 219), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        tableCard.add(helperRow, BorderLayout.NORTH);
        tableCard.add(tableScrollPane, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(new Color(240, 244, 249));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(topBar, BorderLayout.NORTH);
        content.add(tableCard, BorderLayout.CENTER);

        JPanel footer = buildFooterPanel();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(IT_BLUE_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel title = new JLabel("IT Dashboard — Password Reset Requests", SwingConstants.LEFT);
        title.setFont(new Font("Noto Sans", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " + itUser.getFullName(), SwingConstants.RIGHT);
        welcome.setFont(new Font("Noto Sans", Font.BOLD, 14));
        welcome.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.WEST);
        panel.add(welcome, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        panel.setBackground(new Color(240, 244, 249));

        JButton btnMyDashboard = new JButton("My Employee Dashboard");
        UITheme.styleSecondaryButton(btnMyDashboard);
        btnMyDashboard.addActionListener(e -> {
            dispose();
            EmployeeDashboard empDash = new EmployeeDashboard(itUser, () -> {
                ITDashboard back = new ITDashboard(itUser);
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
    //  Data and actions
    // =========================================================================

    // Loads password reset requests off the EDT via SwingWorker; updates the table in done().
    // Passed as this::loadPasswordResetRequests to ResetPasswordProcessor as a PasswordResetCallback lambda.
    public void loadPasswordResetRequests() {
        new SwingWorker<List<PasswordResetRequest>, Void>() {
            @Override
            protected List<PasswordResetRequest> doInBackground() throws Exception {
                return passwordResetService.getAllRequests();
            }

            @Override
            protected void done() {
                try {
                    List<PasswordResetRequest> requests = get();
                    requestModel.setRowCount(0);
                    for (PasswordResetRequest req : requests) {
                        requestModel.addRow(new Object[]{
                            req.getEmployeeNumber(),
                            req.getEmployeeName(),
                            req.getDateOfRequest(),
                            req.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ITDashboard.this,
                            "Failed to load password reset requests: " + ex.getMessage(),
                            "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Handles the Reset Password button: validates selection and calls processor.
    private void handleResetPassword() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a request.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = requestModel.getValueAt(row, COL_STATUS).toString();
        if (!"Pending".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This request has already been processed.",
                    "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String empNum = requestModel.getValueAt(row, COL_EMP_NUM).toString();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Reset password for employee " + empNum + "?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // ResetPasswordProcessor no longer shows dialogs — it stores the result message.
            // This form is responsible for displaying the outcome dialog.
            boolean success = processor.resetPassword(empNum, itUser.getFullName(),
                    itUser.getEmployeeID(), this::loadPasswordResetRequests);

            String msg = processor.getLastResultMessage();
            if (success) {
                JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 194, 208), 1, true),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        if (primary) {
            button.setBackground(IT_BLUE);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(248, 250, 252));
            button.setForeground(new Color(35, 51, 74));
        }
    }

    private void styleRefreshButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(IT_GREEN);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(IT_GREEN_DARK, 1, true),
                BorderFactory.createEmptyBorder(9, 18, 9, 18)
        ));
    }

    private void styleTable(JTable table, JScrollPane scrollPane) {
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(34, 43, 56));
        table.setSelectionBackground(new Color(214, 228, 246));
        table.setSelectionForeground(new Color(22, 43, 74));
        table.setGridColor(new Color(220, 226, 234));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(new Color(238, 242, 247));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Noto Sans", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 228), 1, true),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
    }
}

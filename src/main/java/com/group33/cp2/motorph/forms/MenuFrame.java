package com.group33.cp2.motorph.forms;

import com.group33.cp2.motorph.util.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// Main menu frame: provides navigation to employee management.
public class MenuFrame extends JFrame {

    private JButton btnManageEmployees;
    private JButton btnAddEmployee;
    private JButton btnLogout;

    public MenuFrame() {
        buildUI();
        setTitle("MotorPH Home");
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(
                        MenuFrame.this,
                        "Are you sure you want to exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                );
                if (response == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
    }

    private void buildUI() {
        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(239, 234, 227));

        JPanel shell = new JPanel(new BorderLayout(0, 24));
        shell.setBackground(new Color(239, 234, 227));
        shell.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 56, 86));
        header.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel title = new JLabel("MotorPH Control Center");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Noto Sans", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Choose a workspace to continue.", SwingConstants.LEFT);
        subtitle.setForeground(new Color(216, 225, 235));
        subtitle.setFont(new Font("Noto Sans", Font.PLAIN, 15));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(6));
        titleStack.add(subtitle);
        header.add(titleStack, BorderLayout.WEST);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);

        JPanel heroCard = new JPanel(new BorderLayout());
        heroCard.setBackground(new Color(251, 248, 243));
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 205, 191), 1),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        btnManageEmployees = new JButton("Manage Employees");
        stylePrimaryButton(btnManageEmployees);
        btnManageEmployees.addActionListener(evt -> NavigationManager.openEmployeeListFrame(this, true));

        btnAddEmployee = new JButton("Add Employee");
        styleSecondaryButton(btnAddEmployee);
        btnAddEmployee.addActionListener(evt -> NavigationManager.openNewEmployeeFrame(this, true));

        btnLogout = new JButton("Logout");
        styleDangerButton(btnLogout);
        btnLogout.addActionListener(evt -> NavigationManager.openLoginFrame(this));

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.add(Box.createVerticalGlue());

        JPanel actionGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actionGrid.setOpaque(false);
        actionGrid.add(btnManageEmployees);
        actionGrid.add(btnAddEmployee);
        actionGrid.add(btnLogout);
        heroContent.add(actionGrid);
        heroContent.add(Box.createVerticalStrut(18));

        JLabel helperText = new JLabel("Choose an action to continue.", SwingConstants.CENTER);
        helperText.setFont(new Font("Noto Sans", Font.PLAIN, 14));
        helperText.setForeground(new Color(95, 103, 114));
        helperText.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroContent.add(helperText);
        heroContent.add(Box.createVerticalGlue());

        heroCard.add(heroContent, BorderLayout.CENTER);

        body.add(heroCard, BorderLayout.CENTER);

        shell.add(header, BorderLayout.NORTH);
        shell.add(body, BorderLayout.CENTER);
        getContentPane().add(shell, BorderLayout.CENTER);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(24, 60, 88));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(39, 89, 126), 1, true),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));
        button.setPreferredSize(new Dimension(260, 52));
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(250, 248, 244));
        button.setForeground(new Color(43, 54, 68));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(184, 174, 163), 1, true),
                new javax.swing.border.EmptyBorder(14, 24, 14, 24)
        ));
        button.setPreferredSize(new Dimension(220, 52));
    }

    private void styleDangerButton(JButton button) {
        button.setFont(new Font("Noto Sans", Font.BOLD, 18));
        button.setFocusPainted(false);
        button.setBackground(new Color(186, 67, 67));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(145, 42, 42), 1, true),
                new javax.swing.border.EmptyBorder(14, 24, 14, 24)
        ));
        button.setPreferredSize(new Dimension(180, 52));
    }
}

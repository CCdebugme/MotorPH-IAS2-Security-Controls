package com.group33.cp2.motorph.forms;

import java.awt.Window;
import java.time.LocalDate;
import javax.swing.JFrame;

// Static helper for switching between screens. Each method closes the current frame and opens the next.
public class NavigationManager {

    public static void openMenuFrame(JFrame currentFrame) {
        currentFrame.dispose();
        MenuFrame home = new MenuFrame();
        home.setLocationRelativeTo(null);
        home.setVisible(true);
    }

    public static void openLoginFrame(JFrame currentFrame) {
        // Create LoginFrame before the sweep so we can exclude it by identity
        LoginFrame login = new LoginFrame();
        login.setLocationRelativeTo(null);

        // Dispose every open window except the new LoginFrame
        // Window.getWindows() returns all Window instances allocated by this JVM
        for (Window w : Window.getWindows()) {
            if (w != login) {
                w.dispose();
            }
        }

        login.setVisible(true);
    }

    public static void openEmployeeListFrame(JFrame currentFrame, boolean canEditCompensation) {
        currentFrame.dispose();
        EmployeeListFrame list = new EmployeeListFrame(canEditCompensation);
        list.setLocationRelativeTo(null);
        list.setVisible(true);
    }

    public static void openViewEmployeeFrame(JFrame currentFrame, String selectedEmployeeID,
                                             boolean canEditCompensation) {
        currentFrame.dispose();
        ViewEmployeeFrame view = new ViewEmployeeFrame(selectedEmployeeID, canEditCompensation);
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }

    public static void openViewSalaryFrame(JFrame currentFrame, String employeeId,
                                           LocalDate selectedStartDate, LocalDate selectedEndDate,
                                           boolean canEditCompensation) {
        currentFrame.dispose();
        ViewSalaryFrame salary = new ViewSalaryFrame(employeeId, selectedStartDate, selectedEndDate,
                canEditCompensation);
        salary.setLocationRelativeTo(null);
        salary.setVisible(true);
    }

    public static void openNewEmployeeFrame(JFrame currentFrame, boolean canEditCompensation) {
        currentFrame.dispose();
        NewEmployeeFrame frame = new NewEmployeeFrame(canEditCompensation);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

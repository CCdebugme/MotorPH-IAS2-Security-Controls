package com.group33.cp2.motorph.util;

import java.awt.Component;
import javax.swing.JOptionPane;

// Centralized utility for common confirmation dialogs (exit, logout) shared across all frames.
// Callers receive a simple boolean result; the dialog text and options are encapsulated here.
public class DialogUtil {

    private DialogUtil() {
        // Utility class — no instances
    }

    // Shows a "Confirm Exit" dialog; returns true if the user clicked Yes.
    public static boolean confirmExit(Component parent) {
        return JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to exit?", "Confirm Exit",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    // Shows a "Confirm Logout" dialog; returns true if the user clicked Yes.
    public static boolean confirmLogout(Component parent) {
        return JOptionPane.showConfirmDialog(parent,
            "Are you sure you want to log out?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}

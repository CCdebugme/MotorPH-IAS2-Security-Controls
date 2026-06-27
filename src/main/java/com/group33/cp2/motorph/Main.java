package com.group33.cp2.motorph;

import com.group33.cp2.motorph.forms.LoginFrame;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

// App entry point. Sets up the Nimbus theme and opens the login screen.
public class Main extends javax.swing.JFrame {

    public static void main(String[] args) {
        // Must run before any UI class loads — LoginFrame.pack() needs font metrics.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        registerBundledFonts();
        setLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            LoginFrame form = new LoginFrame();
            form.setVisible(true);
        });
    }

    // Registers the bundled Noto Sans fonts from the JAR so they are available
    // on all platforms regardless of what fonts are installed on the host OS.
    static void registerBundledFonts() {
        String[] resources = {"/fonts/NotoSans-Regular.ttf", "/fonts/NotoSans-Bold.ttf"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String path : resources) {
            try (InputStream is = Main.class.getResourceAsStream(path)) {
                if (is != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                }
            } catch (Exception e) {
                System.err.println("Font registration failed for " + path + ": " + e.getMessage());
            }
        }
    }

    // falls back to the platform default if Nimbus isn't available
    public static void setLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InstantiationException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
}

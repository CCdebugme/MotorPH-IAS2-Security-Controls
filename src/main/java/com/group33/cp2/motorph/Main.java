package com.group33.cp2.motorph;

import com.group33.cp2.motorph.forms.LoginFrame;
import com.group33.cp2.motorph.util.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Application entry point for the MotorPH Payroll System.
 *
 * <p>Startup sequence:</p>
 * <ol>
 *   <li>Configure system font rendering properties</li>
 *   <li>Register bundled Noto Sans fonts</li>
 *   <li>Apply Nimbus look-and-feel</li>
 *   <li>Validate database schema — exits with a clear message if tables are missing</li>
 *   <li>Open the login screen</li>
 * </ol>
 */
public class Main extends javax.swing.JFrame {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Must run before any UI class loads — LoginFrame.pack() needs font metrics.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        registerBundledFonts();
        setLookAndFeel();

        // Validate that all required database tables exist before opening the UI.
        // Fails fast with a clear error message if the database is not configured correctly.
        try {
            SchemaValidator.validate();
        } catch (IllegalStateException e) {
            log.error("Startup aborted — database schema validation failed: {}", e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Database setup is incomplete.\n\n" + e.getMessage()
                + "\n\nPlease check db.properties and run the SQL setup script.",
                "Startup Error — Missing Tables",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) {
            log.error("Startup aborted — could not connect to database: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(null,
                "Could not connect to the database.\n\n" + e.getMessage()
                + "\n\nPlease check that MySQL is running and db.properties is correct.",
                "Startup Error — Database Connection",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame form = new LoginFrame();
            form.setVisible(true);
        });
    }

    /**
     * Registers the bundled Noto Sans fonts from the JAR so they are available
     * on all platforms regardless of what fonts are installed on the host OS.
     */
    static void registerBundledFonts() {
        String[] resources = {"/fonts/NotoSans-Regular.ttf", "/fonts/NotoSans-Bold.ttf"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String path : resources) {
            try (InputStream is = Main.class.getResourceAsStream(path)) {
                if (is != null) {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
                }
            } catch (Exception e) {
                log.warn("Font registration failed for {}: {}", path, e.getMessage());
            }
        }
    }

    /**
     * Applies the Nimbus look-and-feel.
     * Falls back to the platform default if Nimbus is not available.
     */
    public static void setLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InstantiationException
                | UnsupportedLookAndFeelException e) {
            log.warn("Could not apply Nimbus look-and-feel, using platform default: {}",
                    e.getMessage());
        }
    }
}

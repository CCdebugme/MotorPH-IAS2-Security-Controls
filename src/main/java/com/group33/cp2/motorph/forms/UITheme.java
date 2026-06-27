package com.group33.cp2.motorph.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.JTableHeader;

// Shared visual system for the premium Swing refresh.
public final class UITheme {

    // Noto Sans is bundled in the JAR and registered by Main.registerBundledFonts()
    // before any UI class loads, so this name is always resolvable on all platforms.
    private static final String FONT_FAMILY = "Noto Sans";

    public static final Color APP_BACKGROUND = new Color(243, 239, 232);
    public static final Color CARD_BACKGROUND = new Color(252, 250, 246);
    public static final Color CARD_BORDER = new Color(215, 207, 194);
    public static final Color INK = new Color(33, 41, 52);
    public static final Color MUTED_TEXT = new Color(103, 112, 122);
    public static final Color INPUT_BACKGROUND = new Color(255, 253, 249);
    public static final Color INPUT_BORDER = new Color(197, 188, 176);
    public static final Color READ_ONLY_BACKGROUND = new Color(231, 227, 219);
    public static final Color PRIMARY = new Color(24, 60, 88);
    public static final Color PRIMARY_HIGHLIGHT = new Color(34, 88, 125);
    public static final Color ACCENT = new Color(181, 140, 76);
    public static final Color DARK_NAVY = new Color(8, 28, 58);
    public static final Color SURFACE_DARK = new Color(13, 37, 74);
    public static final Color SURFACE_DARKER = new Color(10, 31, 63);
    public static final Color TABLE_GRID = new Color(54, 82, 125);
    public static final Color TABLE_HEADER = new Color(34, 67, 117);
    public static final Color TABLE_ROW = new Color(20, 45, 86);
    public static final Color TABLE_ALT_ROW = new Color(24, 53, 99);

    public static final Font TITLE_FONT = new Font(FONT_FAMILY, Font.BOLD, 28);
    public static final Font SUBTITLE_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font SECTION_FONT = new Font(FONT_FAMILY, Font.BOLD, 16);
    public static final Font LABEL_FONT = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FIELD_FONT = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font(FONT_FAMILY, Font.BOLD, 13);

    private static final Border FIELD_BORDER = new CompoundBorder(
            new LineBorder(INPUT_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)
    );

    private UITheme() {
    }

    public static JPanel createPageHeader(String eyebrow, String title, String subtitle) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(true);
        panel.setBackground(APP_BACKGROUND);
        panel.setBorder(new EmptyBorder(6, 0, 22, 0));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new javax.swing.BoxLayout(textStack, javax.swing.BoxLayout.Y_AXIS));

        JLabel eyebrowLabel = new JLabel(eyebrow);
        eyebrowLabel.setForeground(ACCENT);
        eyebrowLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        eyebrowLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(INK);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(MUTED_TEXT);
        subtitleLabel.setFont(SUBTITLE_FONT);

        textStack.add(eyebrowLabel);
        textStack.add(titleLabel);
        textStack.add(subtitleLabel);
        panel.add(textStack);
        return panel;
    }

    public static void styleCardPanel(JPanel panel, String title) {
        panel.setOpaque(true);
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        if (title != null && !title.isEmpty()) {
            TitledBorder titledBorder = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                    title
            );
            titledBorder.setTitleFont(SECTION_FONT);
            titledBorder.setTitleColor(INK);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    titledBorder,
                    new EmptyBorder(12, 12, 12, 12)
            ));
        }
    }

    public static void styleLabel(JLabel label) {
        label.setForeground(INK);
        label.setFont(LABEL_FONT);
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FIELD_FONT);
        field.setForeground(INK);
        field.setBackground(INPUT_BACKGROUND);
        field.setCaretColor(PRIMARY);
        field.setBorder(FIELD_BORDER);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(FIELD_FONT);
        comboBox.setForeground(INK);
        comboBox.setBackground(INPUT_BACKGROUND);
        comboBox.setBorder(FIELD_BORDER);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 38));
    }

    public static void styleReadOnlyField(JTextField field) {
        styleTextField(field);
        field.setBackground(READ_ONLY_BACKGROUND);
        field.setForeground(MUTED_TEXT);
    }

    public static void stylePrimaryButton(JButton button) {
        styleButtonBase(button);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_HIGHLIGHT, 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleSecondaryButton(JButton button) {
        styleButtonBase(button);
        button.setBackground(CARD_BACKGROUND);
        button.setForeground(INK);
        button.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleGhostButton(JButton button) {
        styleButtonBase(button);
        button.setBackground(APP_BACKGROUND);
        button.setForeground(MUTED_TEXT);
        button.setBorder(new CompoundBorder(
                new LineBorder(APP_BACKGROUND, 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleDangerButton(JButton button) {
        styleButtonBase(button);
        button.setBackground(new Color(186, 67, 67));
        button.setForeground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(145, 42, 42), 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleNeutralButton(JButton button, Color textColor) {
        styleButtonBase(button);
        button.setBackground(new Color(250, 248, 244));
        button.setForeground(textColor);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(184, 174, 163), 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleDarkOutlineButton(JButton button) {
        styleButtonBase(button);
        button.setBackground(SURFACE_DARK);
        button.setForeground(Color.WHITE);
        button.setBorder(new CompoundBorder(
                new LineBorder(TABLE_GRID, 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
    }

    public static void styleSurfacePanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(APP_BACKGROUND);
    }

    public static void styleDarkSurfacePanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(DARK_NAVY);
    }

    public static void styleDarkCardPanel(JPanel panel, String title) {
        panel.setOpaque(true);
        panel.setBackground(SURFACE_DARK);

        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TABLE_GRID, 1, true),
                title
        );
        titledBorder.setTitleFont(SECTION_FONT);
        titledBorder.setTitleColor(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                titledBorder,
                new EmptyBorder(12, 12, 12, 12)
        ));
    }

    public static void styleDarkLabel(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(LABEL_FONT);
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(FIELD_FONT);
        area.setForeground(Color.WHITE);
        area.setBackground(TABLE_ROW);
        area.setCaretColor(Color.WHITE);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setFont(FIELD_FONT);
        table.setForeground(Color.WHITE);
        table.setBackground(TABLE_ROW);
        table.setSelectionBackground(PRIMARY_HIGHLIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(TABLE_GRID);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.BLACK);
        header.setFont(BUTTON_FONT);
        header.setBorder(new LineBorder(TABLE_GRID, 1, true));

        scrollPane.getViewport().setBackground(TABLE_ROW);
        scrollPane.setBorder(new LineBorder(TABLE_GRID, 1, true));
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(BUTTON_FONT);
        tabs.setBackground(APP_BACKGROUND);
        tabs.setForeground(INK);
        tabs.setOpaque(false);
        tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(196, 186, 176)));
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                tabInsets = new Insets(8, 16, 8, 16);
                selectedTabPadInsets = new Insets(2, 2, 2, 2);
                tabAreaInsets = new Insets(10, 12, 2, 12);
                contentBorderInsets = new Insets(12, 0, 0, 0);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                              int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(isSelected ? new Color(255, 251, 244) : new Color(228, 219, 208));
                g2.fillRoundRect(x, y + 2, w - 1, h - 3, 14, 14);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                          int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(isSelected ? new Color(161, 128, 77) : new Color(170, 159, 147));
                g2.drawRoundRect(x, y + 2, w - 1, h - 3, 14, 14);
                g2.dispose();
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, java.awt.FontMetrics metrics,
                                     int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font.deriveFont(isSelected ? Font.BOLD : Font.PLAIN));
                g.setColor(isSelected ? PRIMARY : INK);
                super.paintText(g, tabPlacement, g.getFont(), metrics, tabIndex, title, textRect, isSelected);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                                               Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // No focus ring for cleaner dashboard tabs.
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Container panels provide their own border styling.
            }
        });
    }

    public static void styleComponentTree(Component component) {
        if (component instanceof JLabel label) {
            styleLabel(label);
        } else if (component instanceof JTextField textField) {
            styleTextField(textField);
        } else if (component instanceof JComboBox<?> comboBox) {
            styleComboBox(comboBox);
        } else if (component instanceof JPanel panel) {
            panel.setOpaque(false);
        }

        if (component instanceof JComponent jComponent) {
            for (Component child : jComponent.getComponents()) {
                styleComponentTree(child);
            }
        }
    }

    private static void styleButtonBase(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }
}

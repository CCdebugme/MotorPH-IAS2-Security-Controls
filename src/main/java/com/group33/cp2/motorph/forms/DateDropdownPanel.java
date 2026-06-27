package com.group33.cp2.motorph.forms;

import java.awt.FlowLayout;
import java.awt.Dimension;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;
import javax.swing.JComboBox;
import javax.swing.JPanel;

// Reusable month/day/year chooser backed by JComboBox controls.
public class DateDropdownPanel extends JPanel {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final JComboBox<String> monthComboBox;
    private final JComboBox<String> dayComboBox;
    private final JComboBox<String> yearComboBox;

    public DateDropdownPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setOpaque(false);

        monthComboBox = new JComboBox<>(buildMonthItems());
        dayComboBox = new JComboBox<>(buildDayItems(31));
        yearComboBox = new JComboBox<>(buildYearItems());

        monthComboBox.setPreferredSize(new Dimension(120, 38));
        dayComboBox.setPreferredSize(new Dimension(100, 38));
        yearComboBox.setPreferredSize(new Dimension(110, 38));

        monthComboBox.addActionListener(e -> refreshDays());
        yearComboBox.addActionListener(e -> refreshDays());

        add(monthComboBox);
        add(dayComboBox);
        add(yearComboBox);
    }

    public void clearSelection() {
        monthComboBox.setSelectedIndex(0);
        yearComboBox.setSelectedIndex(0);
        refreshDays();
        dayComboBox.setSelectedIndex(0);
    }

    public void setDate(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            clearSelection();
            return;
        }

        LocalDate date = LocalDate.parse(dateText.trim(), DISPLAY_FORMAT);
        monthComboBox.setSelectedIndex(date.getMonthValue());
        yearComboBox.setSelectedItem(String.valueOf(date.getYear()));
        refreshDays();
        dayComboBox.setSelectedItem(String.format("%02d", date.getDayOfMonth()));
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            clearSelection();
            return;
        }

        monthComboBox.setSelectedIndex(date.getMonthValue());
        yearComboBox.setSelectedItem(String.valueOf(date.getYear()));
        refreshDays();
        dayComboBox.setSelectedItem(String.format("%02d", date.getDayOfMonth()));
    }

    public String getFormattedDate() {
        LocalDate date = getSelectedDate();
        return date == null ? "" : date.format(DISPLAY_FORMAT);
    }

    public LocalDate getSelectedDate() {
        if (!isSelectionComplete()) {
            return null;
        }

        int month = monthComboBox.getSelectedIndex();
        int day = Integer.parseInt((String) dayComboBox.getSelectedItem());
        int year = Integer.parseInt((String) yearComboBox.getSelectedItem());

        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException ex) {
            return null;
        }
    }

    public boolean isSelectionComplete() {
        return monthComboBox.getSelectedIndex() > 0
                && dayComboBox.getSelectedIndex() > 0
                && yearComboBox.getSelectedIndex() > 0;
    }

    public void focusFirstField() {
        monthComboBox.requestFocusInWindow();
    }

    public void setFieldWidths(int monthWidth, int dayWidth, int yearWidth) {
        monthComboBox.setPreferredSize(new Dimension(monthWidth, 38));
        dayComboBox.setPreferredSize(new Dimension(dayWidth, 38));
        yearComboBox.setPreferredSize(new Dimension(yearWidth, 38));
        revalidate();
        repaint();
    }

    private void refreshDays() {
        String selectedDay = (String) dayComboBox.getSelectedItem();
        int dayCount = getAvailableDayCount();

        dayComboBox.removeAllItems();
        dayComboBox.addItem("Day");
        for (int day = 1; day <= dayCount; day++) {
            dayComboBox.addItem(String.format("%02d", day));
        }

        if (selectedDay != null) {
            dayComboBox.setSelectedItem(selectedDay);
        } else {
            dayComboBox.setSelectedIndex(0);
        }
    }

    private int getAvailableDayCount() {
        int month = monthComboBox.getSelectedIndex();
        String yearValue = (String) yearComboBox.getSelectedItem();

        if (month == 0 || yearValue == null || "Year".equals(yearValue)) {
            return 31;
        }

        int year = Integer.parseInt(yearValue);
        return YearMonthLookup.lengthOfMonth(year, month);
    }

    private static String[] buildMonthItems() {
        return new String[]{
            "Month", "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12"
        };
    }

    private static String[] buildDayItems(int count) {
        String[] items = new String[count + 1];
        items[0] = "Day";
        for (int day = 1; day <= count; day++) {
            items[day] = String.format("%02d", day);
        }
        return items;
    }

    private static String[] buildYearItems() {
        int currentYear = Year.now().getValue();
        String[] years = new String[101];
        years[0] = "Year";
        IntStream.rangeClosed(0, 99)
                .forEach(index -> years[index + 1] = String.valueOf(currentYear - index));
        return years;
    }

    // Small helper to keep month-length logic local to the chooser.
    private static final class YearMonthLookup {
        private static int lengthOfMonth(int year, int month) {
            return LocalDate.of(year, month, 1).lengthOfMonth();
        }
    }
}

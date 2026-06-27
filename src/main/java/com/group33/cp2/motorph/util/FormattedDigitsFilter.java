package com.group33.cp2.motorph.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Strips non-digit characters, enforces a maximum digit count, and auto-inserts dashes
// between digit groups. Group sizes are specified at construction time.
// Example: maxDigits=10, groups={3,3,4} → "9668602709" → "966-860-2709"
// Used for phone numbers, SSS, TIN, and other formatted ID fields.
public class FormattedDigitsFilter extends DocumentFilter {

    private final int maxDigits;
    private final int[] groupSizes;

    public FormattedDigitsFilter(int maxDigits, int... groupSizes) {
        this.maxDigits = maxDigits;
        this.groupSizes = groupSizes;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String updated = current.substring(0, offset) + (text == null ? "" : text) + current.substring(offset + length);
        String digitsOnly = updated.replaceAll("\\D", "");
        if (digitsOnly.length() > maxDigits) {
            digitsOnly = digitsOnly.substring(0, maxDigits);
        }
        String formatted = formatDigits(digitsOnly);
        fb.replace(0, fb.getDocument().getLength(), formatted, attrs);
    }

    private String formatDigits(String digitsOnly) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (int groupSize : groupSizes) {
            if (index >= digitsOnly.length()) {
                break;
            }
            int groupEnd = Math.min(index + groupSize, digitsOnly.length());
            if (builder.length() > 0) {
                builder.append('-');
            }
            builder.append(digitsOnly, index, groupEnd);
            index = groupEnd;
        }
        return builder.toString();
    }
}

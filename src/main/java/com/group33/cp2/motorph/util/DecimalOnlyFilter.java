package com.group33.cp2.motorph.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Allows only non-negative decimal values with up to two decimal places.
// Accepts: digits, a single decimal point, and up to two digits after the point.
// Used for salary and allowance fields.
public class DecimalOnlyFilter extends DocumentFilter {

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
        if (updated.isEmpty() || updated.matches("\\d*(\\.\\d{0,2})?")) {
            fb.replace(offset, length, text, attrs);
        }
    }
}

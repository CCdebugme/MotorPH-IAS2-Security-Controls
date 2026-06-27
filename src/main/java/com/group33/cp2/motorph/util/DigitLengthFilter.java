package com.group33.cp2.motorph.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Strips non-digit characters and enforces a maximum digit count.
// Used for government ID fields that accept digits only (e.g., PhilHealth, Pag-IBIG).
public class DigitLengthFilter extends DocumentFilter {

    private final int maxDigits;

    public DigitLengthFilter(int maxDigits) {
        this.maxDigits = maxDigits;
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
        if (digitsOnly.length() <= maxDigits) {
            fb.replace(0, fb.getDocument().getLength(), digitsOnly, attrs);
        }
    }
}

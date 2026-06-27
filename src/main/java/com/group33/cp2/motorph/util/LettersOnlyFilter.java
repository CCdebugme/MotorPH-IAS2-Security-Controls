package com.group33.cp2.motorph.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

// Allows only letters, spaces, and common name punctuation (period, comma, apostrophe, hyphen).
// Used for name and position fields to prevent numeric or symbol input.
public class LettersOnlyFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String replacement = text == null ? "" : text;
        String updated = current.substring(0, offset) + replacement + current.substring(offset + length);
        if (updated.matches("[A-Za-z .,'-]*")) {
            fb.replace(offset, length, replacement, attrs);
        }
    }
}

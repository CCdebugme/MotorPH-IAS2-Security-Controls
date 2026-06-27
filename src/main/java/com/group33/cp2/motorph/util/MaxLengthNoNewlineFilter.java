package com.group33.cp2.motorph.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * DocumentFilter that enforces a maximum character length and strips newline/
 * carriage-return characters from any inserted or replaced text.
 *
 * Used on address and free-text fields (JTextField and JTextArea) to prevent:
 *   - CSV row corruption caused by embedded newlines (VAL-3, VAL-4, VAL-6)
 *   - Model-layer IllegalArgumentException from Employee.setAddress() 200-char limit
 *
 * Silently truncates pasted content that exceeds the limit; does not show a dialog.
 */
public class MaxLengthNoNewlineFilter extends DocumentFilter {

    private final int maxLength;

    /**
     * @param maxLength maximum number of characters allowed in the document
     */
    public MaxLengthNoNewlineFilter(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be > 0. Received: " + maxLength);
        }
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (string == null) {
            return;
        }
        String cleaned = stripNewlines(string);
        int currentLength = fb.getDocument().getLength();
        int available = maxLength - currentLength;
        if (available <= 0) {
            return;
        }
        if (cleaned.length() > available) {
            cleaned = cleaned.substring(0, available);
        }
        super.insertString(fb, offset, cleaned, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) {
            text = "";
        }
        String cleaned = stripNewlines(text);
        int currentLength = fb.getDocument().getLength();
        int afterDelete = currentLength - length;
        int available = maxLength - afterDelete;
        if (available <= 0) {
            // Only allow the deletion part — no new text
            super.replace(fb, offset, length, "", attrs);
            return;
        }
        if (cleaned.length() > available) {
            cleaned = cleaned.substring(0, available);
        }
        super.replace(fb, offset, length, cleaned, attrs);
    }

    /** Removes all newline and carriage-return characters from the input. */
    private String stripNewlines(String text) {
        return text.replace("\n", "").replace("\r", "");
    }
}

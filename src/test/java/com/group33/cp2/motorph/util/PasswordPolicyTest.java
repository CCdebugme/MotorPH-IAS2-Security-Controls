package com.group33.cp2.motorph.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

    @Test void strongPasswordAccepted() {
        assertTrue(PasswordPolicy.isValid("Mot0rPH#2026x", "jcruz"));
    }
    @Test void tooShortRejected() {
        assertFalse(PasswordPolicy.isValid("Ab1!xyz", "jcruz"));
    }
    @Test void missingUppercaseRejected() {
        assertFalse(PasswordPolicy.isValid("motorph#2026x", "jcruz"));
    }
    @Test void missingLowercaseRejected() {
        assertFalse(PasswordPolicy.isValid("MOTORPH#2026X", "jcruz"));
    }
    @Test void missingDigitRejected() {
        assertFalse(PasswordPolicy.isValid("MotorPH#Secure", "jcruz"));
    }
    @Test void missingSpecialRejected() {
        assertFalse(PasswordPolicy.isValid("MotorPH2026Secure", "jcruz"));
    }
    @Test void whitespaceRejected() {
        assertFalse(PasswordPolicy.isValid("Motor PH#2026x", "jcruz"));
    }
    @Test void containingUsernameRejected() {
        assertFalse(PasswordPolicy.isValid("Jcruz#2026abc", "jcruz"));
    }
    @Test void emptyAndNullRejected() {
        assertFalse(PasswordPolicy.isValid("", "jcruz"));
        assertFalse(PasswordPolicy.isValid(null, "jcruz"));
    }
    @Test void tooLongRejected() {
        assertFalse(PasswordPolicy.isValid("Aa1!" + "x".repeat(70), "jcruz"));
    }
}

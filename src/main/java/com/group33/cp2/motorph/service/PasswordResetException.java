package com.group33.cp2.motorph.service;

// Thrown when a password reset fails due to a business rule violation
// (e.g., employee not found, request already processed).
public class PasswordResetException extends Exception {

    public PasswordResetException(String message) {
        super(message);
    }
}

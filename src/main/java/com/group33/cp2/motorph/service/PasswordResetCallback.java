package com.group33.cp2.motorph.service;

// Functional interface invoked by ResetPasswordProcessor after a successful reset.
// The GUI passes a lambda (e.g., this::loadPasswordResetRequests) — the processor
// calls back without importing any GUI types.
@FunctionalInterface
public interface PasswordResetCallback {

    void onPasswordResetComplete();
}

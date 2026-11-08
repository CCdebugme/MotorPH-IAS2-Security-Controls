package com.group33.cp2.motorph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Password complexity rules enforced before a new password is hashed and stored.
// Hashing itself is BCrypt (jBCrypt) with the work factor below; the plain-text
// password is never stored or logged.
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 10;
    public static final int MAX_LENGTH = 64;   // BCrypt only uses the first 72 bytes
    public static final int BCRYPT_COST = 12;  // 2^12 rounds; default (10) is weaker

    private static final Set<String> COMMON = Set.of(
            "password", "password1", "password123", "passw0rd", "12345678", "123456789",
            "qwerty123", "letmein123", "welcome123", "admin123", "motorph123", "iloveyou1");

    private PasswordPolicy() { }

    // Returns the list of rules the password breaks. Empty list = acceptable.
    public static List<String> validate(String password, String username) {
        List<String> problems = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            problems.add("Password must not be empty.");
            return problems;
        }
        if (password.length() < MIN_LENGTH) {
            problems.add("At least " + MIN_LENGTH + " characters.");
        }
        if (password.length() > MAX_LENGTH) {
            problems.add("At most " + MAX_LENGTH + " characters.");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            problems.add("At least one uppercase letter.");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            problems.add("At least one lowercase letter.");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            problems.add("At least one digit.");
        }
        if (password.chars().allMatch(c -> Character.isLetterOrDigit(c))) {
            problems.add("At least one special character (e.g. ! @ # $ %).");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            problems.add("No spaces.");
        }
        String lower = password.toLowerCase(Locale.ROOT);
        if (username != null && username.length() >= 3
                && lower.contains(username.toLowerCase(Locale.ROOT))) {
            problems.add("Must not contain your username.");
        }
        if (COMMON.contains(lower)) {
            problems.add("Too common; choose something less guessable.");
        }
        return problems;
    }

    public static boolean isValid(String password, String username) {
        return validate(password, username).isEmpty();
    }

    // Human-readable message for a dialog.
    public static String describe(List<String> problems) {
        StringBuilder sb = new StringBuilder("Password does not meet the policy:\n");
        for (String p : problems) sb.append(" - ").append(p).append('\n');
        return sb.toString();
    }
}

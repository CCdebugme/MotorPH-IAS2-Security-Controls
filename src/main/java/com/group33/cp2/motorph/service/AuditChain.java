package com.group33.cp2.motorph.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// Tamper-evidence primitives for the audit log.
// Every line ends with  prev=<mac of previous line> mac=<HMAC-SHA256 of everything before " mac=">.
// Editing, deleting, inserting or reordering a line breaks the chain, and without the
// secret key an attacker cannot recompute valid MACs.
public final class AuditChain {

    public static final String GENESIS = "0".repeat(64);
    private static final String MAC_TAG = " mac=";

    private AuditChain() { }

    public static String hmacHex(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }

    // Builds the final log line: body + prev + mac.
    public static String seal(byte[] key, String body, String prevMac) {
        String unsigned = body + " prev=" + prevMac;
        return unsigned + MAC_TAG + hmacHex(key, unsigned);
    }

    // Returns the mac stored on a line, or null if the line is malformed.
    public static String macOf(String line) {
        int i = line.lastIndexOf(MAC_TAG);
        return i < 0 ? null : line.substring(i + MAC_TAG.length());
    }

    // Returns the prev= value stored on a line, or null if missing.
    public static String prevOf(String line) {
        int i = line.lastIndexOf(" prev=");
        int j = line.lastIndexOf(MAC_TAG);
        if (i < 0 || j < 0 || j < i) return null;
        return line.substring(i + 6, j);
    }

    // True when the line's own mac matches its content.
    public static boolean macValid(byte[] key, String line) {
        int i = line.lastIndexOf(MAC_TAG);
        if (i < 0) return false;
        String expected = hmacHex(key, line.substring(0, i));
        // constant-time compare
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                line.substring(i + MAC_TAG.length()).getBytes(StandardCharsets.UTF_8));
    }

    // seq=<n> at the start of the line, or -1.
    public static long seqOf(String line) {
        if (!line.startsWith("seq=")) return -1;
        int end = line.indexOf(' ');
        if (end < 0) return -1;
        try {
            return Long.parseLong(line.substring(4, end));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Anchor file content: protects against deleting the END of the log.
    public static String anchorLine(byte[] key, long seq, String lastMac) {
        String body = "anchor seq=" + seq + " last=" + lastMac;
        return body + MAC_TAG + hmacHex(key, body);
    }
}

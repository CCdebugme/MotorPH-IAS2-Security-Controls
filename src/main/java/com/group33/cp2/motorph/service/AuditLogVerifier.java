package com.group33.cp2.motorph.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Re-computes the hash chain of the audit log and reports the first problem found.
// Also usable from the command line:
//   java -cp ... com.group33.cp2.motorph.service.AuditLogVerifier [logFile anchorFile keyFile]
// With no arguments it checks the default locations used by AuditService.
public final class AuditLogVerifier {

    public static final class Result {
        public final boolean ok;
        public final long linesChecked;
        public final String message;
        Result(boolean ok, long linesChecked, String message) {
            this.ok = ok; this.linesChecked = linesChecked; this.message = message;
        }
        @Override public String toString() { return (ok ? "OK: " : "TAMPERED: ") + message; }
    }

    private AuditLogVerifier() { }

    public static Result verify(Path log, Path anchor, byte[] key) {
        List<String> lines;
        try {
            if (!Files.exists(log)) {
                if (anchor != null && Files.exists(anchor)) {
                    return new Result(false, 0, "audit log file is missing but anchor exists (log deleted)");
                }
                return new Result(true, 0, "no audit log yet");
            }
            lines = Files.readAllLines(log, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new Result(false, 0, "cannot read audit log: " + e.getMessage());
        }

        String prev = AuditChain.GENESIS;
        long expectedSeq = 1;
        long count = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNo = i + 1;
            if (line.isEmpty()) {
                return new Result(false, count, "line " + lineNo + ": blank line inserted");
            }
            if (!AuditChain.macValid(key, line)) {
                return new Result(false, count, "line " + lineNo + ": content or mac was modified");
            }
            if (!prev.equals(AuditChain.prevOf(line))) {
                return new Result(false, count, "line " + lineNo + ": chain broken (a line was removed, inserted or reordered)");
            }
            if (AuditChain.seqOf(line) != expectedSeq) {
                return new Result(false, count, "line " + lineNo + ": sequence number " + AuditChain.seqOf(line)
                        + " but expected " + expectedSeq);
            }
            prev = AuditChain.macOf(line);
            expectedSeq++;
            count++;
        }

        // Anchor: detects removal of lines from the end of the log.
        if (anchor != null && Files.exists(anchor)) {
            try {
                String a = Files.readString(anchor, StandardCharsets.UTF_8).trim();
                int tag = a.lastIndexOf(" mac=");
                if (tag < 0 || !a.substring(tag + 5).equals(AuditChain.hmacHex(key, a.substring(0, tag)))) {
                    return new Result(false, count, "anchor file was modified");
                }
                String expected = AuditChain.anchorLine(key, count, prev);
                if (!expected.equals(a)) {
                    return new Result(false, count, "log ends at seq " + count
                            + " but anchor expects a later entry (lines removed from the end)");
                }
            } catch (IOException e) {
                return new Result(false, count, "cannot read anchor: " + e.getMessage());
            }
        } else if (count > 0) {
            return new Result(false, count, "anchor file is missing");
        }
        return new Result(true, count, count + " entries verified, chain intact");
    }

    public static void main(String[] args) throws Exception {
        Path log = args.length > 0 ? Paths.get(args[0]) : AuditService.defaultLogPath();
        Path anchor = args.length > 1 ? Paths.get(args[1]) : AuditService.defaultAnchorPath();
        Path keyFile = args.length > 2 ? Paths.get(args[2]) : AuditService.defaultKeyPath();
        byte[] key = AuditService.loadKey(keyFile, false);
        Result r = verify(log, anchor, key);
        System.out.println(r);
        System.exit(r.ok ? 0 : 1);
    }
}

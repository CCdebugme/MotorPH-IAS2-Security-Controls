package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.util.SessionContext;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

// Single writer for the security audit log.
//  - Each line records WHO (actor, role), WHAT (event, outcome, detail), WHEN (ts)
//    and FROM WHERE (host + local IP of the machine running the app).
//  - Lines are chained with HMAC-SHA256 (see AuditChain) so edits/deletions are detectable.
//  - The log directory and files are restricted to the OS account running the app.
//  - The secret key lives OUTSIDE the project/log folder (~/.motorph/audit.key) or in the
//    MOTORPH_AUDIT_KEY environment variable.
// Logging never throws: a failure is reported on stderr and the app keeps running.
public final class AuditService {

    private static final AuditService INSTANCE = new AuditService();

    public static AuditService getInstance() { return INSTANCE; }

    // ---- default locations -------------------------------------------------
    public static Path defaultLogPath()    { return Paths.get("logs", "audit", "motorph-audit.chain.log"); }
    public static Path defaultAnchorPath() { return homeDir().resolve("audit.anchor"); }
    public static Path defaultKeyPath()    { return homeDir().resolve("audit.key"); }

    private static Path homeDir() {
        return Paths.get(System.getProperty("user.home"), ".motorph");
    }

    // ---- instance state ----------------------------------------------------
    private Path logPath;
    private Path anchorPath;
    private byte[] key;
    private long seq;
    private String lastMac = AuditChain.GENESIS;
    private boolean initialised;
    private String restrictionStatus = "not applied";
    private final String host;
    private final String ip;

    private AuditService() {
        this.host = resolveHost();
        this.ip = resolveIp();
    }

    // Test hook: point the service at other files (used by unit tests).
    public synchronized void configure(Path log, Path anchor, byte[] hmacKey) {
        this.logPath = log;
        this.anchorPath = anchor;
        this.key = hmacKey;
        this.initialised = false;
        this.seq = 0;
        this.lastMac = AuditChain.GENESIS;
    }

    // ---- public logging API -----------------------------------------------

    // Normal case: the actor is the logged-in user from SessionContext.
    public boolean log(String event, String outcome, String detail) {
        return write(SessionContext.actor(), SessionContext.getRole(), event, outcome, detail);
    }

    // For events before a session exists (login attempts) or on behalf of another user.
    public boolean logAs(String actor, String role, String event, String outcome, String detail) {
        return write(actor, role, event, outcome, detail);
    }

    public String restrictionStatus() { return restrictionStatus; }

    public synchronized AuditLogVerifier.Result verify() {
        ensureInit();
        return AuditLogVerifier.verify(logPath, anchorPath, key);
    }

    // ---- internals ---------------------------------------------------------

    private synchronized boolean write(String actor, String role, String event, String outcome, String detail) {
        try {
            ensureInit();
            long next = seq + 1;
            String body = "seq=" + next
                    + " ts=" + OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS)
                    + " event=" + token(event)
                    + " outcome=" + token(outcome)
                    + " actor=" + token(actor)
                    + " role=" + token(role)
                    + " host=" + token(host)
                    + " ip=" + token(ip)
                    + " detail=\"" + clean(detail) + "\"";
            String line = AuditChain.seal(key, body, lastMac);
            Files.writeString(logPath, line + "\n", StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            seq = next;
            lastMac = AuditChain.macOf(line);
            Files.writeString(anchorPath, AuditChain.anchorLine(key, seq, lastMac) + "\n",
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (Exception e) {
            System.err.println("AuditService: could not write audit entry (" + event + "): " + e.getMessage());
            return false;
        }
    }

    // Opens the log, restricts access, loads the key, checks the existing chain.
    private void ensureInit() {
        if (initialised) return;
        try {
            if (logPath == null) logPath = defaultLogPath();
            if (anchorPath == null) anchorPath = defaultAnchorPath();
            if (key == null) key = loadKey(defaultKeyPath(), true);

            Path logDir = logPath.toAbsolutePath().getParent();
            Files.createDirectories(logDir);
            Files.createDirectories(anchorPath.toAbsolutePath().getParent());
            restrictToOwner(logDir, true);
            restrictToOwner(anchorPath.toAbsolutePath().getParent(), true);

            boolean tampered = false;
            String reason = "";
            if (Files.exists(logPath)) {
                AuditLogVerifier.Result r = AuditLogVerifier.verify(logPath, anchorPath, key);
                tampered = !r.ok;
                reason = r.message;
                List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    String last = lines.get(lines.size() - 1);
                    long s = AuditChain.seqOf(last);
                    String m = AuditChain.macOf(last);
                    if (s >= 0 && m != null) { seq = s; lastMac = m; }
                }
            } else {
                Files.createFile(logPath);
            }
            restrictToOwner(logPath, false);
            initialised = true;
            if (tampered) {
                // Keep running, but leave a permanent marker that the check failed at start-up.
                write("SYSTEM", "", "AUDIT_INTEGRITY_FAILED", "ALERT", reason);
            }
        } catch (Exception e) {
            throw new IllegalStateException("audit init failed: " + e.getMessage(), e);
        }
    }

    // Loads the HMAC key. Order: env MOTORPH_AUDIT_KEY, then key file; creates the file if allowed.
    public static byte[] loadKey(Path keyFile, boolean createIfMissing) throws IOException {
        String env = System.getenv("MOTORPH_AUDIT_KEY");
        if (env != null && !env.isBlank()) return sha256(env);
        if (Files.exists(keyFile)) {
            return sha256(Files.readString(keyFile, StandardCharsets.UTF_8).trim());
        }
        if (!createIfMissing) throw new IOException("audit key not found: " + keyFile);
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        StringBuilder hex = new StringBuilder();
        for (byte b : raw) hex.append(String.format("%02x", b));
        Files.createDirectories(keyFile.toAbsolutePath().getParent());
        Files.writeString(keyFile, hex.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        restrictToOwnerStatic(keyFile, false);
        return sha256(hex.toString());
    }

    private static byte[] sha256(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- file permission restriction --------------------------------------

    private void restrictToOwner(Path p, boolean dir) {
        restrictionStatus = restrictToOwnerStatic(p, dir);
    }

    // Owner-only access. POSIX permissions where available (Linux/macOS), otherwise a
    // Windows ACL containing a single ALLOW entry for the current owner (inheritance removed
    // because setAcl replaces the whole list). Best effort: returns what happened.
    static String restrictToOwnerStatic(Path p, boolean dir) {
        try {
            if (Files.getFileStore(p).supportsFileAttributeView("posix")) {
                Files.setPosixFilePermissions(p, PosixFilePermissions.fromString(dir ? "rwx------" : "rw-------"));
                return "posix " + (dir ? "700" : "600") + " on " + p;
            }
            AclFileAttributeView view = Files.getFileAttributeView(p, AclFileAttributeView.class);
            if (view != null) {
                AclEntry owner = AclEntry.newBuilder()
                        .setType(AclEntryType.ALLOW)
                        .setPrincipal(view.getOwner())
                        .setPermissions(java.util.EnumSet.allOf(AclEntryPermission.class))
                        .build();
                view.setAcl(Collections.singletonList(owner));
                return "windows ACL owner-only on " + p;
            }
            return "no supported permission view for " + p;
        } catch (Exception e) {
            return "could not restrict " + p + ": " + e.getMessage();
        }
    }

    // ---- sanitising / host info -------------------------------------------

    // Single-token value: no spaces or control characters.
    private static String token(String s) {
        if (s == null || s.isEmpty()) return "-";
        return s.replaceAll("[\\p{Cntrl}\\s=\"]", "_");
    }

    // Free text inside quotes: control characters removed so one event = one line.
    private static String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\p{Cntrl}]", "_").replace("\"", "'");
    }

    private static String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return System.getenv().getOrDefault("COMPUTERNAME", "unknown-host");
        }
    }

    // First non-loopback IPv4 address of this machine. A desktop app has no remote client IP;
    // this is the workstation address, which is what identifies the source of the action.
    private static String resolveIp() {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics != null && nics.hasMoreElements()) {
                NetworkInterface n = nics.nextElement();
                if (!n.isUp() || n.isLoopback() || n.isVirtual()) continue;
                Enumeration<InetAddress> addrs = n.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof Inet4Address && !a.isLoopbackAddress()) return a.getHostAddress();
                }
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}

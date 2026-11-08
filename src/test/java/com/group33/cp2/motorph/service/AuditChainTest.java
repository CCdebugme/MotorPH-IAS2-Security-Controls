package com.group33.cp2.motorph.service;

import static org.junit.jupiter.api.Assertions.*;

import com.group33.cp2.motorph.util.SessionContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AuditChainTest {

    @TempDir Path dir;
    private Path log;
    private Path anchor;
    private final byte[] key = "unit-test-key".getBytes(StandardCharsets.UTF_8);
    private AuditService audit;

    @BeforeEach
    void setUp() {
        log = dir.resolve("audit").resolve("chain.log");
        anchor = dir.resolve("anchor");
        audit = AuditService.getInstance();
        audit.configure(log, anchor, key);
        SessionContext.start("hr.user", "10002", "HR");
        audit.log("EMPLOYEE_CREATED", "SUCCESS", "id=10050");
        audit.log("EMPLOYEE_UPDATED", "SUCCESS", "id=10050 fields=phone");
        audit.log("EMPLOYEE_DELETED", "SUCCESS", "id=10050");
    }

    @AfterEach
    void tearDown() { SessionContext.end(); }

    private List<String> lines() throws Exception {
        return new ArrayList<>(Files.readAllLines(log, StandardCharsets.UTF_8));
    }

    private void write(List<String> l) throws Exception {
        Files.write(log, l, StandardCharsets.UTF_8);
    }

    @Test
    void untouchedLogVerifies() {
        AuditLogVerifier.Result r = AuditLogVerifier.verify(log, anchor, key);
        assertTrue(r.ok, r.message);
        assertEquals(3, r.linesChecked);
    }

    @Test
    void lineRecordsActorRoleHostAndIp() throws Exception {
        String first = lines().get(0);
        assertTrue(first.contains("actor=hr.user"));
        assertTrue(first.contains("role=HR"));
        assertTrue(first.contains(" host="));
        assertTrue(first.contains(" ip="));
    }

    @Test
    void editedLineIsDetected() throws Exception {
        List<String> l = lines();
        l.set(1, l.get(1).replace("fields=phone", "fields=none"));
        write(l);
        assertFalse(AuditLogVerifier.verify(log, anchor, key).ok);
    }

    @Test
    void deletedMiddleLineIsDetected() throws Exception {
        List<String> l = lines();
        l.remove(1);
        write(l);
        assertFalse(AuditLogVerifier.verify(log, anchor, key).ok);
    }

    @Test
    void deletedLastLineIsDetectedByAnchor() throws Exception {
        List<String> l = lines();
        l.remove(l.size() - 1);
        write(l);
        AuditLogVerifier.Result r = AuditLogVerifier.verify(log, anchor, key);
        assertFalse(r.ok);
        assertTrue(r.message.contains("end"), r.message);
    }

    @Test
    void reorderedLinesAreDetected() throws Exception {
        List<String> l = lines();
        String t = l.get(0); l.set(0, l.get(1)); l.set(1, t);
        write(l);
        assertFalse(AuditLogVerifier.verify(log, anchor, key).ok);
    }

    @Test
    void forgedLineWithoutKeyIsDetected() throws Exception {
        List<String> l = lines();
        byte[] wrong = "attacker-guess".getBytes(StandardCharsets.UTF_8);
        String prev = AuditChain.macOf(l.get(l.size() - 1));
        l.add(AuditChain.seal(wrong, "seq=4 ts=x event=FAKE outcome=SUCCESS actor=x", prev));
        write(l);
        assertFalse(AuditLogVerifier.verify(log, anchor, key).ok);
    }

    @Test
    void deletedLogFileIsDetected() throws Exception {
        Files.delete(log);
        assertFalse(AuditLogVerifier.verify(log, anchor, key).ok);
    }

    @Test
    void newlineInDetailCannotForgeSecondEntry() throws Exception {
        audit.log("LOGIN_FAILED", "FAILURE", "user=bob\nseq=99 event=LOGIN_SUCCESS actor=admin");
        assertEquals(4, lines().size());
        assertTrue(AuditLogVerifier.verify(log, anchor, key).ok);
    }
}

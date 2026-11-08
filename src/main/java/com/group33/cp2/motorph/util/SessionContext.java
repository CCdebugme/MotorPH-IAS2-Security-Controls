package com.group33.cp2.motorph.util;

// Holds "who is logged in" for the running desktop app so every audit event can
// record the identity of the user who performed the action.
// One logged-in user at a time (Swing desktop app), so a static holder is enough.
public final class SessionContext {

    private static volatile String username = "";
    private static volatile String employeeNum = "";
    private static volatile String role = "";

    private SessionContext() { }

    // Called by LoginFrame right after a successful login.
    public static void start(String user, String empNum, String roleName) {
        username = user == null ? "" : user;
        employeeNum = empNum == null ? "" : empNum;
        role = roleName == null ? "" : roleName;
    }

    // Called on logout / window close.
    public static void end() {
        username = "";
        employeeNum = "";
        role = "";
    }

    public static boolean isActive() { return !username.isEmpty(); }
    public static String getUsername() { return username; }
    public static String getEmployeeNum() { return employeeNum; }
    public static String getRole() { return role; }

    // Value written to the audit log as actor=. "SYSTEM" if nobody is logged in.
    public static String actor() {
        return isActive() ? username : "SYSTEM";
    }
}

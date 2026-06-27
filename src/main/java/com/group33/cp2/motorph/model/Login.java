package com.group33.cp2.motorph.model;

// Holds login credentials and role for one user. Password is write-only — no getter provided.
public class Login {

    private String employeeID;
    private String username;
    // password field is write-only; BCrypt authentication is performed in LoginFrame
    private String password;
    private Role role;

    public Login(String employeeID, String username, String password, Role role) {
        this.employeeID = employeeID;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // getPassword() not provided — BCrypt authentication is performed in LoginFrame

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

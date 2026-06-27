package com.group33.cp2.motorph.model;

// Represents a password reset request. employeeNumber and employeeName are
// immutable after construction; status/admin info are mutable via setters.
public class PasswordResetRequest {

    private final String employeeNumber;
    private final String employeeName;
    private final String dateOfRequest;
    private String status;
    private String adminName;
    private String adminEmployeeNumber;
    private String dateOfReset;

    // Full constructor; used when reading existing records from the CSV.
    public PasswordResetRequest(String employeeNumber, String employeeName,
                                String dateOfRequest, String status,
                                String adminName, String adminEmployeeNumber,
                                String dateOfReset) {
        this.employeeNumber       = employeeNumber;
        this.employeeName         = employeeName;
        this.dateOfRequest        = dateOfRequest;
        this.status               = status;
        this.adminName            = adminName;
        this.adminEmployeeNumber  = adminEmployeeNumber;
        this.dateOfReset          = dateOfReset;
    }

    // Constructs a new pending reset request with status "Pending".
    public PasswordResetRequest(String employeeNumber, String employeeName, String dateOfRequest) {
        this.employeeNumber       = employeeNumber;
        this.employeeName         = employeeName;
        this.dateOfRequest        = dateOfRequest;
        this.status               = "Pending";
        this.adminName            = "";
        this.adminEmployeeNumber  = "";
        this.dateOfReset          = "";
    }

    public String getEmployeeNumber() { return employeeNumber; }
    public String getEmployeeName() { return employeeName; }
    public String getDateOfRequest() { return dateOfRequest; }
    public String getStatus() { return status; }
    public String getAdminName() { return adminName; }
    public String getAdminEmployeeNumber() { return adminEmployeeNumber; }
    public String getDateOfReset() { return dateOfReset; }

    public void setStatus(String status) { this.status = status; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setAdminEmployeeNumber(String adminEmployeeNumber) {
        this.adminEmployeeNumber = adminEmployeeNumber;
    }
    public void setDateOfReset(String dateOfReset) { this.dateOfReset = dateOfReset; }

    // Returns this request as a 7-element String array for CSV serialization.
    public String[] toArray() {
        return new String[]{
            employeeNumber,
            employeeName,
            dateOfRequest,
            status,
            adminName,
            adminEmployeeNumber,
            dateOfReset
        };
    }
}

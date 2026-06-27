package com.group33.cp2.motorph.model;

import java.time.LocalDate;
import java.util.List;

// Represents a single leave request. All fields are private; state transitions
// (approve/reject) use dedicated methods to keep the object consistent.
public class LeaveRequest {

    private static final List<String> VALID_STATUSES =
        List.of("Pending", "Approved", "Rejected");

    private String leaveID;
    private String employeeID;
    private String leaveType;
    private LocalDate dateRequest;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String approver;
    private LocalDate dateResponded;
    private String remark;

    // Full constructor used when reading existing records from the CSV.
    public LeaveRequest(String leaveID, String employeeID, String leaveType,
                        LocalDate dateRequest, LocalDate startDate, LocalDate endDate,
                        String reason, String status, String approver,
                        LocalDate dateResponded, String remark) {
        this.leaveID = leaveID;
        this.employeeID = employeeID;
        this.leaveType = leaveType;
        this.dateRequest = dateRequest;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.approver = approver;
        this.dateResponded = dateResponded;
        this.remark = remark;
    }

    // =========================================================================
    //  Business-state transition methods
    // =========================================================================

    // Marks this leave request as Approved; throws IllegalStateException if not currently Pending.
    public void approve(String approverName) {
        if (!"Pending".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException(
                "Cannot approve leave request '" + leaveID + "': current status is '" + status + "'");
        }
        this.status = "Approved";
        this.approver = approverName;
        this.dateResponded = LocalDate.now();
    }

    // Marks this leave request as Rejected; throws IllegalStateException if not currently Pending.
    public void reject(String approverName, String rejectionRemark) {
        if (!"Pending".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException(
                "Cannot reject leave request '" + leaveID + "': current status is '" + status + "'");
        }
        this.status = "Rejected";
        this.approver = approverName;
        this.dateResponded = LocalDate.now();
        this.remark = rejectionRemark;
    }

    // =========================================================================
    //  Getters and setters
    // =========================================================================

    public String getLeaveID() { return leaveID; }
    public void setLeaveID(String leaveID) { this.leaveID = leaveID; }

    public String getEmployeeID() { return employeeID; }
    public void setEmployeeID(String employeeID) { this.employeeID = employeeID; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getDateRequest() { return dateRequest; }
    public void setDateRequest(LocalDate dateRequest) { this.dateRequest = dateRequest; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }

    // Sets the status; must be one of "Pending", "Approved", or "Rejected".
    public void setStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                "Status must be one of " + VALID_STATUSES + ". Received: '" + status + "'");
        }
        this.status = status;
    }

    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }

    public LocalDate getDateResponded() { return dateResponded; }
    public void setDateResponded(LocalDate dateResponded) { this.dateResponded = dateResponded; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "LeaveRequest{leaveID='" + leaveID + "', employeeID='" + employeeID
                + "', type='" + leaveType + "', status='" + status + "'}";
    }
}

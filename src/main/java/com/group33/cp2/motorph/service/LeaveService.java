package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.EmployeeLeaveTracker;
import com.group33.cp2.motorph.dao.LeaveRequestReader;
import com.group33.cp2.motorph.model.LeaveRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Service facade for all leave-related operations.
// Aggregates LeaveRequestReader, EmployeeLeaveTracker, and LeaveProcessor so that
// forms/ classes never need to import from dao/.
// HR model delegates approveLeave/rejectLeave here to keep model/ free of dao/ imports.
public class LeaveService {

    private final LeaveRequestReader leaveRequestReader;
    private final LeaveProcessor     leaveProcessor;
    private final EmployeeLeaveTracker leaveTracker;

    public LeaveService() {
        this.leaveRequestReader = new LeaveRequestReader();
        this.leaveProcessor     = new LeaveProcessor();
        this.leaveTracker = new EmployeeLeaveTracker();
    }

    // Returns the leave balance for the given employee and leave type.
    public int getLeaveBalance(String empId, String leaveType) throws IOException, SQLException {
        return leaveTracker.getLeaveBalance(empId, leaveType);
    }

    // Returns all leave requests for the given employee.
    public List<LeaveRequest> getLeaveRequestsByEmployee(String empId) {
        return leaveRequestReader.getAllLeaveRequests().stream()
                .filter(req -> req.getEmployeeID().equals(empId))
                .collect(Collectors.toList());
    }

    // Returns all leave requests from the CSV (all employees).
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestReader.getAllLeaveRequests();
    }

    // Validates and submits a leave request; delegates all logic to LeaveProcessor.
    public void submitLeaveRequest(String empId, String leaveType,
            LocalDate startDate, LocalDate endDate, String reason) throws IOException, SQLException {
        leaveProcessor.processLeaveRequest(empId, leaveType, startDate, endDate, reason);
    }

    // Approves a pending leave request, persists the updated status, and deducts the
    // corresponding days from the employee's leave balance.
    // Returns true on success; false if the request is not found or is not Pending.
    // Called by HR.approveLeave() so the HR model class has no direct dao/ imports.
    public boolean approveLeave(String leaveId, String hrName, String remark) throws IOException, SQLException {
        LeaveRequest request = leaveRequestReader.getLeaveById(leaveId);
        if (request == null || !"Pending".equalsIgnoreCase(request.getStatus())) {
            return false;
        }
        request.approve(hrName);
        if (remark != null && !remark.isBlank()) {
            request.setRemark(remark);
        }
        leaveRequestReader.updateLeaveRequest(request);

        boolean usesCalendarDays = "Sick Leave".equalsIgnoreCase(request.getLeaveType())
                || "Vacation Leave".equalsIgnoreCase(request.getLeaveType())
                || "Birthday Leave".equalsIgnoreCase(request.getLeaveType());
        int daysRequested = usesCalendarDays
                ? calculateCalendarDays(request.getStartDate(), request.getEndDate())
                : calculateWeekdays(request.getStartDate(), request.getEndDate());
        leaveTracker.updateLeaveBalance(
                request.getEmployeeID(), request.getLeaveType(), daysRequested);
        return true;
    }

    // Rejects a pending leave request and persists the updated status with the supplied remark.
    // Returns true on success; false if the request is not found or is not Pending.
    // Called by HR.rejectLeave() so the HR model class has no direct dao/ imports.
    public boolean rejectLeave(String leaveId, String hrName, String remark) throws IOException, SQLException {
        LeaveRequest request = leaveRequestReader.getLeaveById(leaveId);
        if (request == null || !"Pending".equalsIgnoreCase(request.getStatus())) {
            return false;
        }
        request.reject(hrName, remark);
        leaveRequestReader.updateLeaveRequest(request);
        return true;
    }

    private int calculateWeekdays(LocalDate startDate, LocalDate endDate) {
        int weekdays = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdays++;
            }
        }
        return weekdays;
    }

    private int calculateCalendarDays(LocalDate startDate, LocalDate endDate) {
        int days = 0;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            days++;
        }
        return days;
    }
}

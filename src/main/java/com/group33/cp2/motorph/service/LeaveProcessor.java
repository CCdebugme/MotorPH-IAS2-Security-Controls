package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.EmployeeLeaveTracker;
import com.group33.cp2.motorph.dao.LeaveRequestReader;
import com.group33.cp2.motorph.model.LeaveRequest;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

// Validates and submits leave requests: leave-type date rules, date-order check, weekend check,
// balance verification, ID generation, and CSV write.
// NOTE: Leave balance is NOT deducted on submission — only on HR approval (HR.approveLeave).
public class LeaveProcessor {

    // Validates and submits a new leave request for the given employee.
    // Throws IllegalArgumentException if any rule is violated; IOException on CSV failure.
    public void processLeaveRequest(String employeeId, String leaveType,
            LocalDate startDate, LocalDate endDate, String reason) throws IOException, SQLException {

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (leaveType == null || leaveType.isBlank()) {
            throw new IllegalArgumentException("Leave type is required.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason is required.");
        }

        boolean isSickLeave = "Sick Leave".equalsIgnoreCase(leaveType);
        boolean isVacationLeave = "Vacation Leave".equalsIgnoreCase(leaveType);
        boolean isBirthdayLeave = "Birthday Leave".equalsIgnoreCase(leaveType);
        boolean allowsWeekendDates = isSickLeave || isVacationLeave || isBirthdayLeave;

        if (startDate.isBefore(LocalDate.now()) || endDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Leave cannot be in the past!");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be later than end date!");
        }

        if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || startDate.getDayOfWeek() == DayOfWeek.SUNDAY
                || endDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || endDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Leave cannot start or end on a weekend!");
        }

        int leaveDuration = allowsWeekendDates
                ? calculateCalendarDays(startDate, endDate)
                : calculateWeekdays(startDate, endDate);
        if (leaveDuration <= 0) {
            throw new IllegalArgumentException("Leave request must include at least one valid leave day.");
        }
        int balance = EmployeeLeaveTracker.getLeaveBalance(employeeId, leaveType);
        if (balance < leaveDuration) {
            throw new IllegalArgumentException("Insufficient leave balance.");
        }

        // balance deduction is deferred to HR.approveLeave() to prevent double-deduction
        String leaveId = generateLeaveId();
        LeaveRequest leaveRequest = new LeaveRequest(
                leaveId, employeeId, leaveType, LocalDate.now(),
                startDate, endDate, reason, "Pending", "", null, "");
        LeaveRequestReader.addLeaveRequest(leaveRequest);
    }

    // Counts only Monday–Friday days in the inclusive range [startDate, endDate].
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

    // Returns the next leave ID in L### format (e.g. L003 becomes L004; L001 if none exist).
    private String generateLeaveId() {
        List<LeaveRequest> requests = new LeaveRequestReader().getAllLeaveRequests();
        int maxId = 0;
        for (LeaveRequest request : requests) {
            String idStr = request.getLeaveID().replace("L", "");
            try {
                int idNum = Integer.parseInt(idStr);
                if (idNum > maxId) {
                    maxId = idNum;
                }
            } catch (NumberFormatException ignored) {
                // Non-numeric IDs (e.g. legacy timestamp IDs) are skipped
            }
        }
        return "L" + String.format("%03d", maxId + 1);
    }
}

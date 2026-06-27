package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.AttendanceDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;

// Service facade for time-tracking: wraps AttendanceDAO so forms/ never import from dao/.
// Each row returned by getTimeLogs: [0]=empNum [1]=date [2]=logIn [3]=logOut [4]=hoursWorked
public class TimeTrackingService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // Records a clock-in for the given employee at the current system time.
    public void clockIn(String empId) throws IOException, SQLException {
        attendanceDAO.clockIn(empId);
    }

    // Records a clock-out for the employee's most recent open clock-in.
    // Throws IOException if no active clock-in is found.
    public void clockOut(String empId) throws IOException, SQLException {
        attendanceDAO.clockOut(empId);
    }

    // Returns all time-log rows for the given employee (including open clock-ins).
    // Each row: [0]=empNum [1]=date [2]=logIn [3]=logOut [4]=hoursWorked
    public List<String[]> getTimeLogs(String empId) throws IOException, SQLException {
        return attendanceDAO.getTimeLogs(empId);
    }

    // Returns time-log rows for the given employee filtered to the date range [start, end] (inclusive).
    public List<String[]> getTimeLogs(String empId, LocalDate start, LocalDate end) throws IOException, SQLException {
        return attendanceDAO.getTimeLogs(empId, start, end);
    }
}

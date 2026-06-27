package com.group33.cp2.motorph.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// Stores one day's attendance record: login/logout times and computed hours.
public class Attendance {

    private static final LocalTime SHIFT_START = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_END = LocalTime.of(17, 0);
    private static final int GRACE_PERIOD_MINUTES = 10;

    // auto-generated at construction, no setter
    private final String attendanceID;
    private String employeeID;
    private LocalDate date;
    private LocalTime loginTime;
    private LocalTime logoutTime;

    // Creates an attendance record; unique ID is auto-generated via UUID.
    public Attendance(String employeeID, LocalDate date, LocalTime loginTime, LocalTime logoutTime) {
        this.attendanceID = UUID.randomUUID().toString();
        this.employeeID = employeeID;
        this.date = date;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
    }

    public String getAttendanceID() {
        return attendanceID;
    }

    // setAttendanceID() intentionally not provided

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public LocalDate getDate() {
        return date;
    }

    // OOP-3 / NPE-2: null date would cause NullPointerException in Payroll.isWithinPeriod()
    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Attendance date must not be null.");
        }
        this.date = date;
    }

    public LocalTime getLoginTime() {
        return loginTime;
    }

    // OOP-3 / NPE-2: null loginTime would cause NPE in isLate() and getTotalHours()
    public void setLoginTime(LocalTime loginTime) {
        if (loginTime == null) {
            throw new IllegalArgumentException("Login time must not be null.");
        }
        this.loginTime = loginTime;
    }

    // caps logout at 5 PM if employee was late (no overtime).
    // NPE-2: returns null safely if logoutTime was not yet set (e.g., employee has only clocked in).
    public LocalTime getLogoutTime() {
        if (logoutTime == null) {
            return null;
        }
        if (isLate()) {
            if (logoutTime.isAfter(SHIFT_END)) {
                logoutTime = SHIFT_END;
            }
        }
        return logoutTime;
    }

    // OOP-3 / NPE-2: null logoutTime would cause NPE in getLogoutTime() and getTotalHours()
    public void setLogoutTime(LocalTime logoutTime) {
        if (logoutTime == null) {
            throw new IllegalArgumentException("Logout time must not be null.");
        }
        this.logoutTime = logoutTime;
    }

    // deducts 1 hour for lunch if total worked > 5 hours.
    // NPE-2: returns 0.0 safely if logoutTime is null (employee has not yet clocked out).
    public double getTotalHours() {
        LocalTime effectiveLogout = getLogoutTime();
        if (effectiveLogout == null) {
            return 0.0;
        }
        Duration workedDuration = Duration.between(getLoginTime(), effectiveLogout);
        double totalHours = workedDuration.toMinutes() / 60.0;

        if (totalHours > 5) {
            totalHours -= 1.0;
        }
        return totalHours;
    }

    // regular hours capped at 8 per day
    public double getRegularHours() {
        return Math.min(getTotalHours(), 8.0);
    }

    // overtime only counts if not late; returns 0 otherwise
    public double getOvertimeHours() {
        if (!isLate()) {
            return Math.max(0, getTotalHours() - 8.0);
        }
        return 0.0;
    }

    public double getRoundedRegularHours() {
        double regularHours = Math.min(getTotalHours(), 8.0);
        return Math.round(regularHours * 100.0) / 100.0;
    }

    // Returns overtime hours (beyond 8.0) rounded to 2 decimal places.
    // Late employees are not eligible for overtime — returns 0.0 if late.
    public double getRoundedOvertimeHours() {
        if (isLate()) {
            return 0.0;
        }
        double overtimeHours = Math.max(0, getTotalHours() - 8.0);
        return Math.round(overtimeHours * 100.0) / 100.0;
    }

    // late = logged in after the 10-minute grace period past 8:00 AM
    public boolean isLate() {
        LocalTime graceEnd = SHIFT_START.plusMinutes(GRACE_PERIOD_MINUTES);
        // On time: logged in before or within the grace window (08:00 – 08:10)
        if (!loginTime.isBefore(SHIFT_START) && loginTime.isBefore(graceEnd)) {
            return false;
        }
        // Late: logged in after the grace window ends (after 08:10)
        if (loginTime.isAfter(graceEnd)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return " Employee ID: " + employeeID
                + ", Date: " + date
                + ", Login Time: " + loginTime
                + ", Logout Time: " + logoutTime
                + ", Total Hours Worked: " + getTotalHours();
    }
}

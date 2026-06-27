package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.dao.AttendanceDAO;
import com.group33.cp2.motorph.model.Attendance;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// Loads attendance records from Attendance.csv and provides read-only access to them.
// Data is loaded once at construction time (startup snapshot).
// Live clock-ins written during the session are not reflected until app restart.
public class AttendanceService {

    private final List<Attendance> attendanceList;

    public AttendanceService() {
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        List<Attendance> loaded;
        try {
            loaded = attendanceDAO.loadAllAttendance();
        } catch (SQLException e) {
            System.err.println("AttendanceService: failed to load Attendance.csv — " + e.getMessage());
            loaded = Collections.emptyList();
        }
        attendanceList = loaded;
    }

    // read-only; attendance data doesn't change after loading
    public List<Attendance> getAllAttendance() {
        return Collections.unmodifiableList(attendanceList);
    }
}

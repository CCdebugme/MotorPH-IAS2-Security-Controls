package com.group33.cp2.motorph.model;

import com.group33.cp2.motorph.service.EmployeeService;
import com.group33.cp2.motorph.service.PayrollLogService;
import com.group33.cp2.motorph.service.TimeTrackingService;
import java.sql.SQLException;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Admin department employee: full payroll treatment plus user management and system reports.
// Overtime at 1.25x; all four deductions apply.
public class Admin extends Employee implements PayrollCalculable, AdminOperations {

    // EmployeeService.reloadEmployees() creates Admin, so constructing EmployeeService
    // inside the Admin constructor would recurse infinitely. Always access via getEmployeeService().
    private EmployeeService employeeService;

    private EmployeeService getEmployeeService() {
        if (employeeService == null) {
            employeeService = new EmployeeService();
        }
        return employeeService;
    }

    private TimeTrackingService timeTrackingService;

    private TimeTrackingService getTimeTrackingService() {
        if (timeTrackingService == null) {
            timeTrackingService = new TimeTrackingService();
        }
        return timeTrackingService;
    }

    private PayrollLogService payrollLogService;

    private PayrollLogService getPayrollLogService() {
        if (payrollLogService == null) {
            payrollLogService = new PayrollLogService();
        }
        return payrollLogService;
    }

    public Admin(String employeeID, String lastName, String firstName, String birthday,
                 String address, String phoneNumber, double basicSalary, double hourlyRate,
                 double grossSemiMonthlyRate, String status, String position,
                 String immediateSupervisor, Allowance allowance,
                 GovernmentDetails governmentDetails) {
        super(employeeID, lastName, firstName, birthday, address, phoneNumber,
              basicSalary, hourlyRate, grossSemiMonthlyRate, status, position,
              immediateSupervisor, allowance, governmentDetails);
    }

    public String getDepartment() { return "Administration"; }

    @Override
    public double calculateGrossSalary() {
        return getBasicSalary() + getAllowance();
    }

    @Override
    public double calculateGrossSalary(double salary) {
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be > 0. Received: " + salary);
        }
        return salary;
    }

    @Override
    public double calculateGrossSalary(double salary, double overtimeHours) {
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be > 0. Received: " + salary);
        }
        if (overtimeHours < 0) {
            throw new IllegalArgumentException("Overtime hours must be >= 0. Received: " + overtimeHours);
        }
        return salary + (overtimeHours * getHourlyRate() * 1.25);
    }

    @Override
    public double calculateOvertimePay(double overtimeHours) {
        return overtimeHours * getHourlyRate() * 1.25;
    }

    @Override
    public double calculateDeductions() {
        return computeFullDeductions();
    }

    @Override
    public double calculateNetSalary() {
        return calculateGrossSalary() - calculateDeductions();
    }

    @Override
    public boolean manageUsers(int userId, String action) {
        return manageUsers(userId, action, null);
    }

    // Callback overload: action intent is signalled via UserManagementCallback so this
    // model class has no javax.swing imports. AdminDashboard supplies the callback lambda.
    public boolean manageUsers(int userId, String action, UserManagementCallback callback) {
        if (action == null) return false;
        String empId = String.valueOf(userId);

        return switch (action.toLowerCase()) {
            case "create" -> {
                if (callback != null) callback.onCreateUser();
                yield true;
            }
            case "update" -> {
                Employee target = getEmployeeService().getEmployeeById(empId);
                if (target == null) yield false;
                if (callback != null) callback.onUpdateUser(empId);
                yield true;
            }
            case "deactivate" -> {
                Employee target = getEmployeeService().getEmployeeById(empId);
                if (target == null) yield false;
                target.setStatus("Inactive");
                getEmployeeService().updateEmployee(target);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean deleteEmployee(String empId) {
        return getEmployeeService().deleteEmployee(empId);
    }

    @Override
    public Report generateSystemReport(String reportType) {
        return generateSystemReport(reportType, LocalDate.MIN, LocalDate.MAX);
    }

    @Override
    public Report generateSystemReport(String reportType, LocalDate start, LocalDate end) {
        if (reportType == null) return new Report("");
        return switch (reportType.toLowerCase()) {
            case "payroll"    -> generatePayrollReport(start, end);
            case "attendance" -> generateAttendanceReport(start, end);
            default           -> new Report("Unknown report type: " + reportType);
        };
    }

    private Report generatePayrollReport(LocalDate start, LocalDate end) {
        boolean allTime = start.equals(LocalDate.MIN) && end.equals(LocalDate.MAX);
        List<PayrollLog> logs = getPayrollLogService().getAllLogs();

        // Filter by date range; null-date (old-format) rows only appear in all-time view.
        List<PayrollLog> filtered = new ArrayList<>();
        for (PayrollLog log : logs) {
            if (log.periodStart() == null || log.periodEnd() == null) {
                if (allTime) filtered.add(log);
            } else {
                if (!log.periodStart().isBefore(start) && !log.periodEnd().isAfter(end)) {
                    filtered.add(log);
                }
            }
        }

        // Remove monthly roll-up entries for months that also have bi-weekly entries,
        // preventing double-counting when both exist in the same (empNum, month, year).
        filtered = deduplicatePayrollLogs(filtered);

        if (filtered.isEmpty()) {
            String msg = allTime
                    ? "No payroll records found."
                    : "No records found for " + formatDate(start) + " - " + formatDate(end) + ".";
            return new Report(msg);
        }

        // Sort chronologically, then by employee ID within each period.
        filtered.sort(Comparator
                .comparing((PayrollLog l) -> l.periodStart() != null
                        ? l.periodStart() : LocalDate.of(l.year(), l.month(), 1))
                .thenComparing(PayrollLog::empNum));

        // Bank-statement layout: one month block per calendar month.
        final int LINE = 97;
        final String THIN  = "-".repeat(LINE);
        final String THICK = "=".repeat(LINE);
        final String COL_HDR = String.format("  %-10s %-28s %-18s %14s %14s %14s%n",
                "Emp ID", "Full Name", "Period", "Gross", "Deductions", "Net Pay");

        StringBuilder sb = new StringBuilder();
        sb.append("=== MotorPH Payroll Report ===\n");
        if (!allTime) {
            sb.append(String.format("Period  : %s - %s%n", formatDate(start), formatDate(end)));
        }
        sb.append(String.format("Generated: %s%n", formatDate(LocalDate.now())));

        double grandGross = 0, grandDeduct = 0, grandNet = 0;
        Set<String> allEmpIds = new LinkedHashSet<>();
        int totalPeriods = 0;

        String currentGroupKey = null;
        double grpGross = 0, grpDeduct = 0, grpNet = 0;

        for (PayrollLog log : filtered) {
            LocalDate anchor = log.periodStart() != null
                    ? log.periodStart() : LocalDate.of(log.year(), log.month(), 1);
            String groupKey   = anchor.getYear() + "-" + String.format("%02d", anchor.getMonthValue());
            String groupLabel = anchor.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

            if (!groupKey.equals(currentGroupKey)) {
                // Close the previous month block.
                if (currentGroupKey != null) {
                    sb.append("  ").append(THIN).append("\n");
                    sb.append(String.format("  %-10s %-28s %-18s %14.2f %14.2f %14.2f%n",
                            "", "", "Month Total:", grpGross, grpDeduct, grpNet));
                    sb.append("\n");
                }
                // Open a new month block with a labelled header bar.
                String bar = "=== " + groupLabel + " ";
                sb.append("\n").append(bar).append("=".repeat(Math.max(0, LINE - bar.length()))).append("\n");
                sb.append(COL_HDR);
                sb.append("  ").append(THIN).append("\n");
                currentGroupKey = groupKey;
                grpGross = 0; grpDeduct = 0; grpNet = 0;
            }

            Employee emp = getEmployeeService().getEmployeeById(log.empNum());
            String name   = emp != null ? truncate(emp.getFullName(), 28) : log.empNum();
            String period = log.periodStart() != null
                    ? formatShortDate(log.periodStart()) + " - " + formatShortDate(log.periodEnd())
                    : String.format("%02d/%04d", log.month(), log.year());

            sb.append(String.format("  %-10s %-28s %-18s %14.2f %14.2f %14.2f%n",
                    log.empNum(), name, period,
                    log.grossSalary(), log.totalDeductions(), log.netSalary()));

            grpGross    += log.grossSalary();
            grpDeduct   += log.totalDeductions();
            grpNet      += log.netSalary();
            grandGross  += log.grossSalary();
            grandDeduct += log.totalDeductions();
            grandNet    += log.netSalary();
            allEmpIds.add(log.empNum());
            totalPeriods++;
        }

        // Close the last month block.
        if (currentGroupKey != null) {
            sb.append("  ").append(THIN).append("\n");
            sb.append(String.format("  %-10s %-28s %-18s %14.2f %14.2f %14.2f%n",
                    "", "", "Month Total:", grpGross, grpDeduct, grpNet));
        }

        // Grand total footer.
        sb.append("\n").append(THICK).append("\n");
        sb.append(String.format("  %-10s %-28s %-18s %14.2f %14.2f %14.2f%n",
                "", "GRAND TOTAL", "", grandGross, grandDeduct, grandNet));
        sb.append(String.format("%n  Total Employees: %d   |   Total Pay Periods: %d%n",
                allEmpIds.size(), totalPeriods));

        return new Report(sb.toString());
    }

    // For each (empNum, month, year) group: if bi-weekly entries exist (period span <= 16 days),
    // drop the monthly roll-up entry (span > 16 days) to prevent double-counting totals.
    private List<PayrollLog> deduplicatePayrollLogs(List<PayrollLog> logs) {
        Map<String, List<PayrollLog>> grouped = new LinkedHashMap<>();
        for (PayrollLog log : logs) {
            String key = log.empNum() + ":" + log.year() + ":" + log.month();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(log);
        }
        List<PayrollLog> result = new ArrayList<>();
        for (List<PayrollLog> group : grouped.values()) {
            boolean hasBiWeekly = false;
            for (PayrollLog log : group) {
                if (log.periodStart() != null && log.periodEnd() != null) {
                    long span = java.time.temporal.ChronoUnit.DAYS
                            .between(log.periodStart(), log.periodEnd());
                    if (span <= 16) { hasBiWeekly = true; break; }
                }
            }
            for (PayrollLog log : group) {
                if (log.periodStart() == null || log.periodEnd() == null) {
                    if (!hasBiWeekly) result.add(log);
                } else {
                    long span = java.time.temporal.ChronoUnit.DAYS
                            .between(log.periodStart(), log.periodEnd());
                    if (hasBiWeekly && span > 16) continue; // drop the monthly roll-up
                    result.add(log);
                }
            }
        }
        return result;
    }

    private Report generateAttendanceReport(LocalDate start, LocalDate end) {
        boolean allTime = start.equals(LocalDate.MIN) && end.equals(LocalDate.MAX);
        List<Employee> employees = getEmployeeService().getAllEmployees();
        StringBuilder sb = new StringBuilder();
        sb.append("=== MotorPH Attendance Report ===\n");
        if (!allTime) {
            sb.append(String.format("Period  : %s \u2013 %s%n", formatDate(start), formatDate(end)));
        }
        sb.append(String.format("Generated: %s%n%n", formatDate(LocalDate.now())));

        if (allTime) {
            // Aggregate summary — old format preserved for all-time view.
            sb.append(String.format("%-10s %-25s %10s %10s%n",
                    "Emp ID", "Full Name", "Sessions", "Total Hrs"));
            sb.append("-".repeat(60)).append("\n");

            for (Employee emp : employees) {
                try {
                    List<String[]> logRows = getTimeTrackingService().getTimeLogs(emp.getEmployeeID());
                    double totalHours = 0;
                    int sessions = 0;
                    for (String[] row : logRows) {
                        if (row.length > 4 && !row[4].isBlank() && !row[4].equalsIgnoreCase("Error")) {
                            totalHours += applyLunchDeduction(parseHoursWorked(row[4]));
                            sessions++;
                        }
                    }
                    sb.append(String.format("%-10s %-25s %10d %10.2f%n",
                            emp.getEmployeeID(), truncate(emp.getFullName(), 25), sessions, totalHours));
                } catch (IOException | SQLException e) {
                    sb.append(String.format("%-10s %-25s  (error reading logs)%n",
                            emp.getEmployeeID(), truncate(emp.getFullName(), 25)));
                }
            }
        } else {
            // Date-range format: one row per calendar weekday per employee; absent rows synthesized.
            DateTimeFormatter dateFmt  = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter dispFmt  = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            sb.append(String.format("%-10s %-25s %-16s %8s %8s %8s %8s%n",
                    "Emp ID", "Full Name", "Date", "Time In", "Time Out", "Hours", "Status"));
            sb.append("-".repeat(92)).append("\n");

            double grandTotal = 0;
            for (Employee emp : employees) {
                try {
                    List<String[]> logRows = getTimeTrackingService()
                            .getTimeLogs(emp.getEmployeeID(), start, end);

                    Map<LocalDate, String[]> logMap = new LinkedHashMap<>();
                    for (String[] row : logRows) {
                        if (row.length > 1 && !row[1].isBlank()) {
                            try {
                                LocalDate d = LocalDate.parse(row[1].trim(), dateFmt);
                                logMap.put(d, row);
                            } catch (Exception ignored) {}
                        }
                    }

                    double empHours = 0;
                    int empDays = 0;
                    LocalDate cursor = start;
                    while (!cursor.isAfter(end)) {
                        DayOfWeek dow = cursor.getDayOfWeek();
                        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                            empDays++;
                            String[] row = logMap.get(cursor);
                            if (row != null) {
                                String timeIn    = row.length > 2 ? row[2] : "";
                                String timeOut   = row.length > 3 ? row[3] : "";
                                String hoursStr  = row.length > 4 ? row[4] : "";
                                double hours     = applyLunchDeduction(parseHoursWorked(hoursStr));
                                empHours += hours;
                                String status = determineStatus(timeIn);
                                sb.append(String.format("%-10s %-25s %-16s %8s %8s %8s %8s%n",
                                        emp.getEmployeeID(), truncate(emp.getFullName(), 25),
                                        cursor.format(dispFmt),
                                        timeIn.isBlank()   ? "\u2014" : timeIn,
                                        timeOut.isBlank()  ? "\u2014" : timeOut,
                                        hoursStr.isBlank() ? "\u2014" : String.format("%.2f", hours),
                                        status));
                            } else {
                                sb.append(String.format("%-10s %-25s %-16s %8s %8s %8s %8s%n",
                                        emp.getEmployeeID(), truncate(emp.getFullName(), 25),
                                        cursor.format(dispFmt),
                                        "\u2014", "\u2014", "\u2014", "Absent"));
                            }
                        }
                        cursor = cursor.plusDays(1);
                    }
                    grandTotal += empHours;
                    sb.append(String.format("%-10s %-25s %42s%8.2f hrs  (%d days)%n",
                            "", "", "Subtotal: ", empHours, empDays));
                } catch (IOException | SQLException e) {
                    sb.append(String.format("%-10s %-25s  (error reading logs)%n",
                            emp.getEmployeeID(), truncate(emp.getFullName(), 25)));
                }
            }
            sb.append("-".repeat(92)).append("\n");
            sb.append(String.format("Grand Total Hours: %.2f   |   Total Employees: %d%n",
                    grandTotal, employees.size()));
        }

        return new Report(sb.toString());
    }

    // Returns "On Time" if logIn <= 08:10, "Late" otherwise; "—" input yields "On Time".
    private String determineStatus(String timeIn) {
        if (timeIn == null || timeIn.isBlank() || timeIn.equals("\u2014")) return "On Time";
        try {
            String[] parts = timeIn.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return (h > 8 || (h == 8 && m > 10)) ? "Late" : "On Time";
        } catch (Exception e) {
            return "On Time";
        }
    }

    private double parseHoursWorked(String hoursStr) {
        try {
            String[] parts = hoursStr.split(":");
            if (parts.length < 2) return 0.0;
            return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]) / 60.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Mirrors Attendance.getTotalHours(): deducts 1 hour for lunch on sessions longer than 5 hours.
    // Applied when summing hoursWorked from CSV so report totals match payroll totals.
    private double applyLunchDeduction(double rawHours) {
        return rawHours > 5.0 ? rawHours - 1.0 : rawHours;
    }

    private String formatDate(LocalDate date) {
        if (date == null || date.equals(LocalDate.MIN) || date.equals(LocalDate.MAX)) return "";
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private String formatShortDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("MMM dd"));
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    @Override
    public String toString() {
        return "Admin {" + super.toString() + "}";
    }
}

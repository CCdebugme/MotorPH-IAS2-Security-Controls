package com.group33.cp2.motorph.report;

import com.group33.cp2.motorph.dao.AllowanceDetailsReader;
import com.group33.cp2.motorph.dao.AttendanceDAO;
import com.group33.cp2.motorph.dao.EmployeeDetailsReader;
import com.group33.cp2.motorph.dao.PayrollLogReader;
import com.group33.cp2.motorph.dao.SalaryDetailsReader;
import com.group33.cp2.motorph.model.Allowance;
import com.group33.cp2.motorph.model.PayrollLog;
import com.group33.cp2.motorph.model.Salary;
import com.group33.cp2.motorph.service.PayrollCalculator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collection;

/**
 * ReportService — JasperReports PDF generator for the MotorPH Payroll System.
 *
 * Provides three report types:
 *   1. generatePayslip()         — per-employee payslip PDF
 *   2. generatePayrollReport()   — monthly payroll summary (method overloaded x2)
 *   3. generateTimecardReport()  — employee attendance/timecard PDF
 *
 * Method Overloading is demonstrated via the two generatePayrollReport()
 * signatures: one for all departments, one with a department/period filter.
 */
public class ReportService {

    // ── DAOs ──────────────────────────────────────────────────────────
    private final EmployeeDetailsReader  employeeReader  = new EmployeeDetailsReader();
    private final PayrollLogReader       payrollReader   = new PayrollLogReader();
    private final AttendanceDAO          attendanceDAO   = new AttendanceDAO();
    private final SalaryDetailsReader    salaryReader    = new SalaryDetailsReader();
    private final AllowanceDetailsReader allowanceReader = new AllowanceDetailsReader();

    private static final DateTimeFormatter DB_DATE_FMT  =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter OUT_DATE_FMT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // ════════════════════════════════════════════════════════════════
    // 1. PAYSLIP REPORT
    // ════════════════════════════════════════════════════════════════

    /**
     * Generates a single-employee payslip PDF.
     *
     * @param empNum     employee ID (e.g. "10001")
     * @param period     payroll period string (e.g. "2026-01-01 to 2026-01-15")
     * @param outputFile destination PDF file
     */
    public void generatePayslip(String empNum,
                                String period,
                                File   outputFile) throws ReportException {

        // ── Employee row (indexes: 0=empNum 1=lastName 2=firstName ... 6=sss 7=ph 8=tin 9=pagibig 11=position) ──
        String[] empRow = findEmployee(empNum);
        if (empRow == null) throw new ReportException("Employee not found: " + empNum);

        // ── Salary ──
        Salary salary = salaryReader.getSalary(empNum);
        if (salary == null) throw new ReportException("Salary not found for: " + empNum);
        double semiMonthly = salary.getBasicSalary();
        double hourlyRate  = salary.getHourlyRate();
        double dailyRate   = hourlyRate * 8;

        // ── Allowances ──
        Allowance allowance   = allowanceReader.getAllowance(empNum);
        double riceSubsidy    = allowance != null ? allowance.getRiceAllowance()     : 0;
        double phoneAllow     = allowance != null ? allowance.getPhoneAllowance()    : 0;
        double clothingAllow  = allowance != null ? allowance.getClothingAllowance() : 0;
        double totalBenefits  = riceSubsidy + phoneAllow + clothingAllow;

        // ── Payroll log for gross/net ──
        PayrollLog log = findPayrollLog(empNum, period);
        double gross = log != null ? log.grossSalary()    : semiMonthly;
        double net   = log != null ? log.netSalary()      : 0;

        // ── Deductions via PayrollCalculator static methods ──
        double sss        = PayrollCalculator.computeSSSDeduction(semiMonthly);
        double philhealth = PayrollCalculator.computePhilhealthDeduction(semiMonthly);
        double pagibig    = PayrollCalculator.computePagibigDeduction(semiMonthly);
        double tax        = PayrollCalculator.computeWithholdingTax(semiMonthly);
        double totalDed   = sss + philhealth + pagibig + tax;

        // ── Days worked from attendance ──
        int daysWorked = getDaysWorked(empNum, period);

        // ── Overtime ──
        double overtimePay = 0; // extend here if overtime data is available

        // ── Period split ──
        String[] parts      = period.split("\\s+to\\s+");
        String   periodStart = parts.length == 2 ? parts[0].trim() : period;
        String   periodEnd   = parts.length == 2 ? parts[1].trim() : period;

        // ── Build params ──
        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID",   empNum);
        params.put("EMPLOYEE_NAME", safeCol(empRow, 2) + " " + safeCol(empRow, 1));
        params.put("POSITION",      safeCol(empRow, 11));
        params.put("DEPARTMENT",    safeCol(empRow, 11)); // position used as dept label
        params.put("PERIOD",        period);
        params.put("PERIOD_START",  periodStart);
        params.put("PERIOD_END",    periodEnd);
        params.put("PAYSLIP_NO",    "PS-" + empNum + "-" + periodStart);

        params.put("BASIC_SALARY",   semiMonthly);
        params.put("DAILY_RATE",     dailyRate);
        params.put("DAYS_WORKED",    (double) daysWorked);
        params.put("OVERTIME",       overtimePay);
        params.put("GROSS_INCOME",   gross);
        params.put("GROSS_PAY",      gross);

        params.put("RICE_SUBSIDY",   riceSubsidy);
        params.put("PHONE_ALLOW",    phoneAllow);
        params.put("CLOTHING_ALLOW", clothingAllow);
        params.put("TOTAL_BENEFITS", totalBenefits);

        params.put("SSS",            sss);
        params.put("PHILHEALTH",     philhealth);
        params.put("PAGIBIG",        pagibig);
        params.put("TAX",            tax);
        params.put("TOTAL_DEDUCT",   totalDed);
        params.put("NET_PAY",        net > 0 ? net : gross - totalDed);

        // Gov IDs are at indexes 6=sss 7=philhealth 8=tin 9=pagibig in getAllEmployees()
        params.put("SSS_NO",        safeCol(empRow, 6));
        params.put("PHILHEALTH_NO", safeCol(empRow, 7));
        params.put("TIN_NO",        safeCol(empRow, 8));
        params.put("PAGIBIG_NO",    safeCol(empRow, 9));

        fillAndExport("/reports/payslip.jrxml", new JREmptyDataSource(), params, outputFile);
    }

    // ════════════════════════════════════════════════════════════════
    // 2. MONTHLY PAYROLL REPORT  —  METHOD OVERLOADING
    // ════════════════════════════════════════════════════════════════

    /**
     * Overload 1 — All departments, all periods.
     * Demonstrates method overloading (same name, different parameters).
     */
    public void generatePayrollReport(String generatedByEmpNum,
                                      File   outputFile) throws ReportException {
        generatePayrollReport(generatedByEmpNum, null, null, outputFile);
    }

    /**
     * Overload 2 — Filtered by department and/or period.
     * Demonstrates method overloading (same name, different parameters).
     */
    public void generatePayrollReport(String generatedByEmpNum,
                                      String departmentFilter,
                                      String periodFilter,
                                      File   outputFile) throws ReportException {

        // ── Pull all employees and payroll logs ──
        List<String[]>  allEmps = employeeReader.getAllEmployees();
        List<PayrollLog> allLogs;
        try {
            allLogs = payrollReader.getAllLogs();
        } catch (Exception e) {
            throw new ReportException("Could not load payroll logs: " + e.getMessage(), e);
        }

        // ── Build lookup maps keyed by empNum ──
        Map<String, String>   nameMap       = new LinkedHashMap<>();
        Map<String, String>   deptMap       = new LinkedHashMap<>();
        Map<String, String>   positionMap   = new LinkedHashMap<>();
        Map<String, String[]> govIdsMap     = new LinkedHashMap<>();
        Map<String, double[]> deductionsMap = new LinkedHashMap<>();

        for (String[] emp : allEmps) {
            String id = emp[0].trim();
            nameMap.put(id,     safeCol(emp, 2) + " " + safeCol(emp, 1));
            positionMap.put(id, safeCol(emp, 11));
            deptMap.put(id,     safeCol(emp, 11));
            govIdsMap.put(id,   new String[]{
                safeCol(emp, 6), safeCol(emp, 7), safeCol(emp, 8), safeCol(emp, 9)
            });

            Salary sal = salaryReader.getSalary(id);
            double gross = sal != null ? sal.getBasicSalary() : 0;
            double sss        = PayrollCalculator.computeSSSDeduction(gross);
            double philhealth = PayrollCalculator.computePhilhealthDeduction(gross);
            double pagibig    = PayrollCalculator.computePagibigDeduction(gross);
            double tax        = PayrollCalculator.computeWithholdingTax(gross);
            deductionsMap.put(id, new double[]{sss, philhealth, pagibig, tax});
        }

        // ── Labels ──
        String deptLabel   = (departmentFilter == null || departmentFilter.isBlank())
                             ? "All Departments" : departmentFilter;
        String periodLabel = (periodFilter == null || periodFilter.isBlank())
                             ? "All Periods" : periodFilter;

        // ── Convert PayrollLog list to PayrollReportRow objects for ReportDataSource ──
        List<PayrollReportRow> filtered = allLogs.stream()
            .filter(log -> {
                String dept = deptMap.getOrDefault(log.empNum(), "");
                boolean deptOk   = "All Departments".equals(deptLabel) ||
                                   deptLabel.equalsIgnoreCase(dept);
                boolean periodOk = "All Periods".equals(periodLabel) ||
                                   periodLabel.equals(log.periodStart() + " to " + log.periodEnd());
                return deptOk && periodOk;
            })
            .collect(Collectors.toMap(
                PayrollLog::empNum,
                log -> {
                    String   id       = log.empNum();
                    String[] govIds   = govIdsMap.getOrDefault(id, new String[]{"","","",""});
                    double[] ded      = deductionsMap.getOrDefault(id, new double[]{0,0,0,0});
                    return new PayrollReportRow(
                        id,
                        nameMap.getOrDefault(id, ""),
                        positionMap.getOrDefault(id, ""),
                        deptMap.getOrDefault(id, ""),
                        log.grossSalary(),
                        log.netSalary(),
                        govIds.length > 0 ? govIds[0] : "",
                        govIds.length > 1 ? govIds[1] : "",
                        govIds.length > 2 ? govIds[2] : "",
                        govIds.length > 3 ? govIds[3] : "",
                        ded[0], ded[1], ded[2], ded[3]
                    );
                },
                (a, b) -> b,
                LinkedHashMap::new
            ))
            .values().stream()
            .sorted(Comparator.comparing(PayrollReportRow::getEmpNum))
            .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            throw new ReportException(
                "No payroll records found for \"" + deptLabel + "\" / \"" + periodLabel + "\".");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("DEPARTMENT",     deptLabel);
        params.put("PERIOD",         periodLabel);
        params.put("GENERATED_BY",   generatedByEmpNum);
        params.put("GENERATED_DATE", LocalDate.now().format(OUT_DATE_FMT));

        ReportDataSource ds = new ReportDataSource(filtered);

        fillAndExport("/reports/payroll_department.jrxml", ds, params, outputFile);
    }

    // ════════════════════════════════════════════════════════════════
    // 3. TIMECARD REPORT
    // ════════════════════════════════════════════════════════════════

    /**
     * Generates an employee timecard PDF showing daily attendance for a period.
     */
    public void generateTimecardReport(String empNum,
                                       String period,
                                       File   outputFile) throws ReportException {

        String[] empRow = findEmployee(empNum);
        if (empRow == null) throw new ReportException("Employee not found: " + empNum);

        // ── Parse period into LocalDate range ──
        String[] parts = period.split("\\s+to\\s+");
        if (parts.length != 2)
            throw new ReportException("Invalid period format. Expected: yyyy-MM-dd to yyyy-MM-dd");

        LocalDate start, end;
        try {
            start = LocalDate.parse(parts[0].trim());
            end   = LocalDate.parse(parts[1].trim());
        } catch (Exception e) {
            throw new ReportException("Could not parse period dates: " + e.getMessage(), e);
        }

        // ── Get attendance rows ──
        List<String[]> rawLogs;
        try {
            rawLogs = attendanceDAO.getTimeLogs(empNum, start, end);
        } catch (Exception e) {
            throw new ReportException("Could not load attendance: " + e.getMessage(), e);
        }

        if (rawLogs.isEmpty())
            throw new ReportException(
                "No attendance records found for employee " + empNum +
                " in period \"" + period + "\".");

        // ── Convert to List<Map> for JRMapCollectionDataSource ──
        // getTimeLogs row: [0]=empNum [1]=date [2]=logIn [3]=logOut [4]=hoursWorked
        List<Map<String, Object>> rows = new ArrayList<>();
        double totalHours = 0;
        for (String[] row : rawLogs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date",       row[1]);
            map.put("dayOfWeek",  getDayOfWeek(row[1]));
            map.put("timeIn",     row[2]);
            map.put("timeOut",    row[3]);
            map.put("totalHours", row[4]);
            map.put("remarks",    "Regular");
            rows.add(map);
            try { totalHours += Double.parseDouble(row[4]); } catch (Exception ignored) {}
        }

        Map<String, Object> params = new HashMap<>();
        params.put("EMPLOYEE_ID",   empNum);
        params.put("EMPLOYEE_NAME", safeCol(empRow, 2) + " " + safeCol(empRow, 1));
        params.put("POSITION",      safeCol(empRow, 11));
        params.put("DEPARTMENT",    safeCol(empRow, 11));
        params.put("PERIOD_END",    parts[1].trim());
        params.put("TOTAL_HOURS",   totalHours);

        fillAndExport("/reports/timecard.jrxml",
                new JRMapCollectionDataSource((Collection<Map<String, ?>>) (Collection<?>) rows), params, outputFile);
    }

    // ════════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════════

    private void fillAndExport(String resource,
                               JRDataSource ds,
                               Map<String, Object> params,
                               File outputFile) throws ReportException {
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is == null)
                throw new ReportException(
                    "JRXML not found: " + resource +
                    ". Place it in src/main/resources/reports/");

            JasperReport compiled = JasperCompileManager.compileReport(is);
            JasperPrint  print    = JasperFillManager.fillReport(compiled, params, ds);

            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(print));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();

        } catch (ReportException re) {
            throw re;
        } catch (Exception e) {
            throw new ReportException("Report generation failed: " + e.getMessage(), e);
        }
    }

    private String[] findEmployee(String empNum) {
        return employeeReader.getAllEmployees().stream()
                .filter(e -> e[0].trim().equals(empNum.trim()))
                .findFirst().orElse(null);
    }

    private PayrollLog findPayrollLog(String empNum, String period) {
        try {
            return payrollReader.getAllLogs().stream()
                    .filter(l -> l.empNum().equals(empNum))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private int getDaysWorked(String empNum, String period) {
        try {
            String[] parts = period.split("\\s+to\\s+");
            if (parts.length != 2) return 0;
            LocalDate start = LocalDate.parse(parts[0].trim());
            LocalDate end   = LocalDate.parse(parts[1].trim());
            List<String[]> logs = attendanceDAO.getTimeLogs(empNum, start, end);
            return (int) logs.stream()
                    .filter(r -> r[3] != null && !r[3].isBlank())
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getDayOfWeek(String dateStr) {
        try {
            LocalDate d = LocalDate.parse(dateStr.trim(), DB_DATE_FMT);
            return d.getDayOfWeek().toString().substring(0, 3);
        } catch (Exception e) {
            return "";
        }
    }

    private String safeCol(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null)
               ? arr[idx].trim() : "";
    }
}

package com.group33.cp2.motorph.report;

/**
 * Represents a single row in the Monthly Payroll Summary Report.
 *
 * <p>Replaces the raw {@code String[]} array that was previously passed to
 * {@link ReportDataSource}. Using a dedicated model class improves readability,
 * type safety, and maintainability — field access is by name rather than by
 * array index, eliminating silent bugs caused by index misalignment.</p>
 *
 * <p>Instances are created by {@link ReportService} and consumed by
 * {@link ReportDataSource} during JasperReports PDF generation.</p>
 */
public class PayrollReportRow {

    private final String empNum;
    private final String empName;
    private final String position;
    private final String department;
    private final double grossIncome;
    private final double netPay;

    // Government ID numbers
    private final String sssNo;
    private final String philhealthNo;
    private final String tin;
    private final String pagibigNo;

    // Deduction amounts
    private final double sssContribution;
    private final double philhealthContribution;
    private final double pagibigContribution;
    private final double withholdingTax;

    /**
     * Constructs a fully populated payroll report row.
     *
     * @param empNum                 employee number
     * @param empName                full name (First Last)
     * @param position               job position title
     * @param department             department name
     * @param grossIncome            gross salary for the period
     * @param netPay                 take-home pay after all deductions
     * @param sssNo                  SSS ID number
     * @param philhealthNo           PhilHealth ID number
     * @param tin                    BIR TIN number
     * @param pagibigNo              Pag-IBIG ID number
     * @param sssContribution        SSS deduction amount
     * @param philhealthContribution PhilHealth deduction amount
     * @param pagibigContribution    Pag-IBIG deduction amount
     * @param withholdingTax         BIR withholding tax amount
     */
    public PayrollReportRow(String empNum,
                            String empName,
                            String position,
                            String department,
                            double grossIncome,
                            double netPay,
                            String sssNo,
                            String philhealthNo,
                            String tin,
                            String pagibigNo,
                            double sssContribution,
                            double philhealthContribution,
                            double pagibigContribution,
                            double withholdingTax) {
        this.empNum                 = empNum;
        this.empName                = empName;
        this.position               = position;
        this.department             = department;
        this.grossIncome            = grossIncome;
        this.netPay                 = netPay;
        this.sssNo                  = sssNo;
        this.philhealthNo           = philhealthNo;
        this.tin                    = tin;
        this.pagibigNo              = pagibigNo;
        this.sssContribution        = sssContribution;
        this.philhealthContribution = philhealthContribution;
        this.pagibigContribution    = pagibigContribution;
        this.withholdingTax         = withholdingTax;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** Returns the employee number (e.g. "10001"). */
    public String getEmpNum()                 { return empNum; }

    /** Returns the employee's full name in "First Last" format. */
    public String getEmpName()                { return empName; }

    /** Returns the employee's job position title. */
    public String getPosition()               { return position; }

    /** Returns the employee's department name. */
    public String getDepartment()             { return department; }

    /** Returns the gross salary for the pay period. */
    public double getGrossIncome()            { return grossIncome; }

    /** Returns the net take-home pay after all deductions. */
    public double getNetPay()                 { return netPay; }

    /** Returns the employee's SSS ID number. */
    public String getSssNo()                  { return sssNo; }

    /** Returns the employee's PhilHealth ID number. */
    public String getPhilhealthNo()           { return philhealthNo; }

    /** Returns the employee's BIR TIN number. */
    public String getTin()                    { return tin; }

    /** Returns the employee's Pag-IBIG ID number. */
    public String getPagibigNo()              { return pagibigNo; }

    /** Returns the SSS contribution amount deducted for the period. */
    public double getSssContribution()        { return sssContribution; }

    /** Returns the PhilHealth contribution amount deducted for the period. */
    public double getPhilhealthContribution() { return philhealthContribution; }

    /** Returns the Pag-IBIG contribution amount deducted for the period. */
    public double getPagibigContribution()    { return pagibigContribution; }

    /** Returns the BIR withholding tax amount deducted for the period. */
    public double getWithholdingTax()         { return withholdingTax; }
}

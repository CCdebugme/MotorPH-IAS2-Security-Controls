package com.group33.cp2.motorph.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.List;
import java.util.Map;

/**
 * Custom JRDataSource for the Monthly Payroll Summary Report.
 *
 * Each row in {@code data} is a payroll record array from PayrollLogReader,
 * expected column order:
 *   [0] payroll_id  [1] empNum  [2] empName  [3] period
 *   [4] regular_hours  [5] overtime_hours  [6] gross_pay
 *   [7] total_deductions  [8] net_pay
 *
 * Government IDs and individual deduction amounts are looked up
 * via the supplied maps rather than hitting the DB on every field call.
 *
 * Field names must match the &lt;field name="..."&gt; declarations in
 * payroll_department.jrxml exactly.
 */
public class ReportDataSource implements JRDataSource {

    private final List<String[]>        data;
    private final Map<String, String>   deptMap;       // empNum  -> department name
    private final Map<String, String>   nameMap;       // empNum  -> "First Last"
    private final Map<String, String[]> govIdsMap;     // empNum  -> [sssNo, phNo, tin, pagibigNo]
    private final Map<String, double[]> deductionsMap; // empNum  -> [sss, philhealth, pagibig, tax]
    private final Map<String, String>   positionMap;   // empNum  -> position title

    private int index = -1;

    /**
     * @param data          List of payroll row arrays (one per employee)
     * @param deptMap       empNum → department name
     * @param nameMap       empNum → full name
     * @param govIdsMap     empNum → [sssNo, philhealthNo, tin, pagibigNo]
     * @param deductionsMap empNum → [sssAmt, philhealthAmt, pagibigAmt, taxAmt]
     * @param positionMap   empNum → position title
     */
    public ReportDataSource(List<String[]>        data,
                            Map<String, String>   deptMap,
                            Map<String, String>   nameMap,
                            Map<String, String[]> govIdsMap,
                            Map<String, double[]> deductionsMap,
                            Map<String, String>   positionMap) {
        this.data          = data;
        this.deptMap       = deptMap;
        this.nameMap       = nameMap;
        this.govIdsMap     = govIdsMap;
        this.deductionsMap = deductionsMap;
        this.positionMap   = positionMap;
    }

    @Override
    public boolean next() throws JRException {
        index++;
        return index < data.size();
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        String[] row   = data.get(index);
        String   empId = row[1].trim();
        String   name  = field.getName();

        // ── Text fields ──────────────────────────────────────────────
        if ("empNo".equals(name))         return empId;
        if ("empName".equals(name))       return nameMap.getOrDefault(empId, "Unknown");
        if ("department".equals(name))    return deptMap.getOrDefault(empId, "General");
        if ("position".equals(name))      return positionMap.getOrDefault(empId, "Employee");

        // ── Financial totals ─────────────────────────────────────────
        try {
            if ("grossIncome".equals(name)) return parseDouble(row[6]);
            if ("netPay".equals(name))      return parseDouble(row[8]);
        } catch (Exception e) {
            return 0.0;
        }

        // ── Deduction amounts (from pre-built map) ───────────────────
        double[] ded = deductionsMap.getOrDefault(empId, new double[]{0, 0, 0, 0});
        if ("sssContribution".equals(name))        return ded[0];
        if ("philhealthContribution".equals(name)) return ded[1];
        if ("pagibigContribution".equals(name))    return ded[2];
        if ("withholdingTax".equals(name))         return ded[3];

        // ── Government ID numbers (from pre-built map) ───────────────
        String[] govIds = govIdsMap.getOrDefault(empId, new String[]{"", "", "", ""});
        if ("sssNo".equals(name))        return govIds[0];
        if ("philhealthNo".equals(name)) return govIds[1];
        if ("tin".equals(name))          return govIds[2];
        if ("pagibigNo".equals(name))    return govIds[3];

        return null;
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.replace(",", "").replace("\u20b1", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}

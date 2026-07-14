package com.group33.cp2.motorph.report;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Custom {@link JRDataSource} for the Monthly Payroll Summary Report.
 *
 * <p>Feeds a list of {@link PayrollReportRow} objects to the JasperReports
 * engine row by row. Each call to {@link #next()} advances to the next row,
 * and {@link #getFieldValue(JRField)} returns the value for the requested field.</p>
 *
 * <p>Field names are declared as constants (e.g. {@link #FIELD_EMP_NO}) rather
 * than raw string literals to prevent typos and make refactoring safer. Each
 * constant must match the {@code <field name="...">} declaration in
 * {@code payroll_department.jrxml} exactly.</p>
 *
 * <p>Replaces the raw {@code String[]} array approach with a typed
 * {@link PayrollReportRow} model class for improved readability,
 * type safety, and maintainability.</p>
 */
public class ReportDataSource implements JRDataSource {

    private static final Logger log = LoggerFactory.getLogger(ReportDataSource.class);

    // ── JRXML field name constants ────────────────────────────────────────────
    // Must match <field name="..."> declarations in payroll_department.jrxml exactly.

    public static final String FIELD_EMP_NO               = "empNo";
    public static final String FIELD_EMP_NAME             = "empName";
    public static final String FIELD_POSITION             = "position";
    public static final String FIELD_DEPARTMENT           = "department";
    public static final String FIELD_GROSS_INCOME         = "grossIncome";
    public static final String FIELD_NET_PAY              = "netPay";
    public static final String FIELD_SSS_NO               = "sssNo";
    public static final String FIELD_SSS_CONTRIBUTION     = "sssContribution";
    public static final String FIELD_PHILHEALTH_NO        = "philhealthNo";
    public static final String FIELD_PHILHEALTH_CONTRIB   = "philhealthContribution";
    public static final String FIELD_PAGIBIG_NO           = "pagibigNo";
    public static final String FIELD_PAGIBIG_CONTRIB      = "pagibigContribution";
    public static final String FIELD_TIN                  = "tin";
    public static final String FIELD_WITHHOLDING_TAX      = "withholdingTax";

    // ── Data ─────────────────────────────────────────────────────────────────

    private final List<PayrollReportRow> rows;
    private int index = -1;

    /**
     * Constructs a data source from the given list of payroll report rows.
     *
     * @param rows list of {@link PayrollReportRow} objects to feed to JasperReports;
     *             must not be null or empty
     */
    public ReportDataSource(List<PayrollReportRow> rows) {
        this.rows = rows;
    }

    /**
     * Advances the cursor to the next row.
     *
     * @return true if there is another row to process; false when all rows
     *         have been consumed
     * @throws JRException if an error occurs advancing the cursor
     */
    @Override
    public boolean next() throws JRException {
        index++;
        return index < rows.size();
    }

    /**
     * Returns the value for the requested JRXML field from the current row.
     *
     * <p>Field names are matched against the constants defined in this class.
     * If an unrecognised field name is requested, a warning is logged and
     * {@code null} is returned rather than throwing — this prevents the entire
     * report from failing due to a single unknown field.</p>
     *
     * @param field the JasperReports field descriptor whose value is needed
     * @return the field value from the current {@link PayrollReportRow},
     *         or {@code null} if the field name is not recognised
     * @throws JRException if an error occurs retrieving the value
     */
    @Override
    public Object getFieldValue(JRField field) throws JRException {
        PayrollReportRow row  = rows.get(index);
        String           name = field.getName();

        // ── Text fields ───────────────────────────────────────────────────────
        if (FIELD_EMP_NO.equals(name))     return row.getEmpNum();
        if (FIELD_EMP_NAME.equals(name))   return row.getEmpName();
        if (FIELD_POSITION.equals(name))   return row.getPosition();
        if (FIELD_DEPARTMENT.equals(name)) return row.getDepartment();
        if (FIELD_SSS_NO.equals(name))     return row.getSssNo();
        if (FIELD_PHILHEALTH_NO.equals(name)) return row.getPhilhealthNo();
        if (FIELD_TIN.equals(name))        return row.getTin();
        if (FIELD_PAGIBIG_NO.equals(name)) return row.getPagibigNo();

        // ── Numeric fields ────────────────────────────────────────────────────
        if (FIELD_GROSS_INCOME.equals(name))       return row.getGrossIncome();
        if (FIELD_NET_PAY.equals(name))            return row.getNetPay();
        if (FIELD_SSS_CONTRIBUTION.equals(name))   return row.getSssContribution();
        if (FIELD_PHILHEALTH_CONTRIB.equals(name)) return row.getPhilhealthContribution();
        if (FIELD_PAGIBIG_CONTRIB.equals(name))    return row.getPagibigContribution();
        if (FIELD_WITHHOLDING_TAX.equals(name))    return row.getWithholdingTax();

        // ── Unknown field — log warning and return null ───────────────────────
        log.warn("ReportDataSource: unrecognised field name '{}' — returning null. "
                + "Check that JRXML field names match the constants in ReportDataSource.",
                name);
        return null;
    }
}

package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.model.Deductions;
import com.group33.cp2.motorph.model.PeriodType;
import com.group33.cp2.motorph.model.SSSDeductionsBracket;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

// Computes government deductions (SSS, PhilHealth, Pag-IBIG, withholding tax)
// and bi-weekly period logic.
public class PayrollCalculator {

    // ── BIR Withholding Tax — bracket thresholds (semi-monthly taxable income) ──
    private static final double TAX_BRACKET_1  =  20_832.00;  // 0% threshold
    private static final double TAX_BRACKET_2  =  33_333.00;  // 20% bracket starts
    private static final double TAX_BRACKET_3  =  66_667.00;  // 25% bracket starts
    private static final double TAX_BRACKET_4  = 166_667.00;  // 30% bracket starts
    private static final double TAX_BRACKET_5  = 666_667.00;  // 32% bracket starts

    // ── BIR Withholding Tax — base amounts per bracket ──
    private static final double TAX_BASE_2     =      0.00;   // bracket 2 base
    private static final double TAX_BASE_3     =  2_500.00;   // bracket 3 base
    private static final double TAX_BASE_4     = 10_833.00;   // bracket 4 base
    private static final double TAX_BASE_5     = 40_833.33;   // bracket 5 base
    private static final double TAX_BASE_6     = 200_833.33;  // bracket 6 base

    // ── BIR Withholding Tax — excess rates per bracket ──
    private static final double TAX_RATE_2     = 0.20;
    private static final double TAX_RATE_3     = 0.25;
    private static final double TAX_RATE_4     = 0.30;
    private static final double TAX_RATE_5     = 0.32;
    private static final double TAX_RATE_6     = 0.35;

    // ── BIR Withholding Tax — bracket excess-over amounts ──
    private static final double TAX_EXCESS_2   =  20_833.00;
    private static final double TAX_EXCESS_3   =  33_333.00;
    private static final double TAX_EXCESS_4   =  66_667.00;
    private static final double TAX_EXCESS_5   = 166_667.00;
    private static final double TAX_EXCESS_6   = 666_667.00;

    // ── PhilHealth ──
    // Premium rate applied to basic salary; split equally between employee and employer.
    private static final double PHILHEALTH_RATE        = 0.03;
    private static final double PHILHEALTH_MIN_SALARY  = 10_000.00; // no contribution below this

    // ── Pag-IBIG ──
    private static final double PAGIBIG_LOW_RATE       = 0.01;  // 1% for salary <= 1,500
    private static final double PAGIBIG_HIGH_RATE      = 0.02;  // 2% for salary > 1,500
    private static final double PAGIBIG_SALARY_BRACKET = 1_500.00;
    private static final double PAGIBIG_MAX_CONTRIBUTION = 100.00; // capped at PHP 100

    // ── Pay period divisors ──
    private static final int DIVISOR_WEEKLY    = 4;
    private static final int DIVISOR_BIWEEKLY  = 2;
    private static final int DIVISOR_MONTHLY   = 1;
    private static final int MINIMUM_FIVE_WEEKS = 5;

    public static int getWeekOfMonth(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfMonth());
    }

    public static boolean isWeek1OverlappingPreviousMonth(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        int weekNumberOfFirstDay = firstDayOfMonth.get(weekFields.weekOfMonth());
        return weekNumberOfFirstDay != 1;
    }

    public static boolean hasFiveWeeks(LocalDate date) {
        LocalDate firstDay = date.withDayOfMonth(1);
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());
        int firstWeek = getWeekOfMonth(firstDay);
        int lastWeek = getWeekOfMonth(lastDay);
        return lastWeek - firstWeek + 1 >= MINIMUM_FIVE_WEEKS;
    }

    // WEEKLY=4, BIWEEKLY=2, MONTHLY=1
    public static int getDivisor(PeriodType periodType) {
        if (periodType == null) {
            return DIVISOR_MONTHLY;
        }
        return switch (periodType) {
            case WEEKLY   -> DIVISOR_WEEKLY;
            case BIWEEKLY -> DIVISOR_BIWEEKLY;
            default       -> DIVISOR_MONTHLY;
        };
    }

    // Always returns half the monthly statutory deductions (SSS, PhilHealth, PagIBIG,
    // withholding tax). periodStart is kept in the signature to avoid changing call sites
    // but is not used inside the method body.
    public static Deductions getDeductionsBiWeekly(String employeeID, double basicSalary, LocalDate periodStart) {
        int divisor       = getDivisor(PeriodType.BIWEEKLY);          // returns 2
        double sss        = computeSSSDeduction(basicSalary)        / divisor;
        double philhealth = computePhilhealthDeduction(basicSalary) / divisor;
        double pagibig    = computePagibigDeduction(basicSalary)    / divisor;
        double tax        = computeWithholdingTax(basicSalary)      / divisor;
        return new Deductions(employeeID, sss, philhealth, pagibig, tax);
    }

    public static Deductions getDeductionsMonthly(String employeeID, double basicSalary) {
        int divisor = getDivisor(PeriodType.MONTHLY);
        double sss = computeSSSDeduction(basicSalary) / divisor;
        double philhealth = computePhilhealthDeduction(basicSalary) / divisor;
        double pagibig = computePagibigDeduction(basicSalary) / divisor;
        double tax = computeWithholdingTax(basicSalary) / divisor;
        return new Deductions(employeeID, sss, philhealth, pagibig, tax);
    }

    public static double getSSSPhilhealthPagibig(double basicSalary) {
        return computeSSSDeduction(basicSalary)
             + computePhilhealthDeduction(basicSalary)
             + computePagibigDeduction(basicSalary);
    }

    // ── Method Overloading: computeNetPay ─────────────────────────────────────
    // Overload 1 — computes net pay from basic salary alone (deductions auto-computed).
    public static double computeNetPay(double basicSalary) {
        double sss        = computeSSSDeduction(basicSalary);
        double philhealth = computePhilhealthDeduction(basicSalary);
        double pagibig    = computePagibigDeduction(basicSalary);
        double tax        = computeWithholdingTax(basicSalary);
        return basicSalary - sss - philhealth - pagibig - tax;
    }

    // Overload 2 — computes net pay when caller already knows the total deductions.
    public static double computeNetPay(double basicSalary, double totalDeductions) {
        return basicSalary - totalDeductions;
    }

    // Overload 3 — computes net pay including allowances and a known total deductions figure.
    public static double computeNetPay(double basicSalary, double totalAllowances, double totalDeductions) {
        return basicSalary + totalAllowances - totalDeductions;
    }

    // ── Method Overloading: computeWithholdingTax ─────────────────────────────
    // Overload 1 — derives taxable income internally from basicSalary minus statutory deductions.
    // BIR 6-tier progressive tax; taxable income = salary minus SSS/PhilHealth/PagIbig
    public static double computeWithholdingTax(double basicSalary) {
        double deductions    = getSSSPhilhealthPagibig(basicSalary);
        double taxableIncome = basicSalary - deductions;

        if (taxableIncome <= TAX_BRACKET_1) {
            return 0;
        } else if (taxableIncome < TAX_BRACKET_2) {
            return TAX_BASE_2 + (taxableIncome - TAX_EXCESS_2) * TAX_RATE_2;
        } else if (taxableIncome < TAX_BRACKET_3) {
            return TAX_BASE_3 + (taxableIncome - TAX_EXCESS_3) * TAX_RATE_3;
        } else if (taxableIncome < TAX_BRACKET_4) {
            return TAX_BASE_4 + (taxableIncome - TAX_EXCESS_4) * TAX_RATE_4;
        } else if (taxableIncome < TAX_BRACKET_5) {
            return TAX_BASE_5 + (taxableIncome - TAX_EXCESS_5) * TAX_RATE_5;
        } else {
            return TAX_BASE_6 + (taxableIncome - TAX_EXCESS_6) * TAX_RATE_6;
        }
    }

    // Overload 2 — caller supplies the already-computed taxable income directly.
    // Used by ReportService where gross and deductions are already known.
    public static double computeWithholdingTax(double basicSalary, double taxableIncome) {
        if (taxableIncome <= TAX_BRACKET_1) {
            return 0;
        } else if (taxableIncome < TAX_BRACKET_2) {
            return TAX_BASE_2 + (taxableIncome - TAX_EXCESS_2) * TAX_RATE_2;
        } else if (taxableIncome < TAX_BRACKET_3) {
            return TAX_BASE_3 + (taxableIncome - TAX_EXCESS_3) * TAX_RATE_3;
        } else if (taxableIncome < TAX_BRACKET_4) {
            return TAX_BASE_4 + (taxableIncome - TAX_EXCESS_4) * TAX_RATE_4;
        } else if (taxableIncome < TAX_BRACKET_5) {
            return TAX_BASE_5 + (taxableIncome - TAX_EXCESS_5) * TAX_RATE_5;
        } else {
            return TAX_BASE_6 + (taxableIncome - TAX_EXCESS_6) * TAX_RATE_6;
        }
    }

    // 1% if salary <= 1,500; 2% if above; capped at PHP 100 per HDMF rules
    public static double computePagibigDeduction(double basicSalary) {
        double rate = (basicSalary <= PAGIBIG_SALARY_BRACKET)
                      ? PAGIBIG_LOW_RATE : PAGIBIG_HIGH_RATE;
        return Math.min(basicSalary * rate, PAGIBIG_MAX_CONTRIBUTION);
    }

    // 3% premium split equally; no contribution if salary is below PHP 10,000
    public static double computePhilhealthDeduction(double basicSalary) {
        return (basicSalary >= PHILHEALTH_MIN_SALARY)
               ? (basicSalary * PHILHEALTH_RATE) / 2 : 0;
    }

    // looks up the SSS contribution from the bracket table
    public static double computeSSSDeduction(double basicSalary) {
        return SSSDeductionsBracket.getDeductions()
                                   .floorEntry(basicSalary)
                                   .getValue();
    }
}

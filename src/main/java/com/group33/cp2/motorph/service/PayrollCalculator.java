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
        return lastWeek - firstWeek + 1 >= 5;
    }

    // WEEKLY=4, BIWEEKLY=2, MONTHLY=1
    public static int getDivisor(PeriodType periodType) {
        if (periodType == null) {
            return 1;
        }
        return switch (periodType) {
            case WEEKLY -> 4;
            case BIWEEKLY -> 2;
            default -> 1;
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
        double deductions = getSSSPhilhealthPagibig(basicSalary);
        double taxableIncome = basicSalary - deductions;

        if (taxableIncome < 20832) {
            return 0;
        } else if (taxableIncome < 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome < 66667) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome < 166667) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome < 666667) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    // Overload 2 — caller supplies the already-computed taxable income directly.
    // Used by ReportService where gross and deductions are already known.
    public static double computeWithholdingTax(double basicSalary, double taxableIncome) {
        if (taxableIncome < 20832) {
            return 0;
        } else if (taxableIncome < 33333) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome < 66667) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome < 166667) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome < 666667) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    // 1% if salary <= 1500, 2% if above; capped at PHP 100
    public static double computePagibigDeduction(double basicSalary) {
        double rate = (basicSalary <= 1500) ? 0.01 : 0.02;
        return Math.min(basicSalary * rate, 100);
    }

    // 3% premium split in half; no contribution below PHP 10,000
    public static double computePhilhealthDeduction(double basicSalary) {
        return (basicSalary >= 10000) ? (basicSalary * 0.03) / 2 : 0;
    }

    // looks up the SSS contribution from the bracket table
    public static double computeSSSDeduction(double basicSalary) {
        return SSSDeductionsBracket.getDeductions()
                                   .floorEntry(basicSalary)
                                   .getValue();
    }
}

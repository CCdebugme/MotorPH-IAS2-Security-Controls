package com.group33.cp2.motorph.service;

import com.group33.cp2.motorph.model.CompensationDetails;
import com.group33.cp2.motorph.model.Deductions;
import com.group33.cp2.motorph.model.Payslip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// Persists Payslip objects to Payslips.csv, one row per payslip.
// Creates the file with a header on the first write; appends on all subsequent writes.
public class PayslipService {

    static final String CSV_PATH = "src/main/resources/data/Payslips.csv";
    static final String CSV_HEADER =
            "payslipID,employeeID,payrollID,periodStart,periodEnd,issueDate," +
            "regularHours,overtimeHours,regularPay,overtimePay," +
            "grossSalary,allowances,sss,philHealth,pagibig,tax,totalDeductions,netSalary";

    // Appends one row to Payslips.csv. Writes the header first if the file is new or empty.
    public void savePayslip(Payslip p) throws IOException {
        File file = new File(CSV_PATH);
        boolean needsHeader = !file.exists() || file.length() == 0;
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            if (needsHeader) out.println(CSV_HEADER);
            out.println(format(p));
        }
    }

    String format(Payslip p) {
        CompensationDetails cd = p.getCompensationDetails();
        Deductions d = cd.getDeductions();
        return String.join(",",
                p.getPayslipID(),
                p.getEmployeeID(),
                p.getPayrollID(),
                p.getPeriodStartDate().toString(),
                p.getPeriodEndDate().toString(),
                p.getIssueDate().toString(),
                String.format("%.2f", p.getTotalRegularHours()),
                String.format("%.2f", p.getTotalOvertimeHours()),
                String.format("%.2f", cd.getRegularPay()),
                String.format("%.2f", cd.getOvertimePay()),
                String.format("%.2f", cd.getGrossSalary()),
                String.format("%.2f", cd.getAllowance().getTotal()),
                String.format("%.2f", d.getSss()),
                String.format("%.2f", d.getPhilHealth()),
                String.format("%.2f", d.getPagIbig()),
                String.format("%.2f", d.getTax()),
                String.format("%.2f", d.getTotal()),
                String.format("%.2f", cd.getNetSalary())
        );
    }
}

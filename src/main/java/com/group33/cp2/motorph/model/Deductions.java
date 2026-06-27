package com.group33.cp2.motorph.model;

// Holds SSS, PhilHealth, Pag-IBIG, and withholding tax amounts for one payroll period.
public class Deductions {

    private String employeeID;
    private double sss;
    private double philHealth;
    private double pagIbig;
    private double tax;

    public Deductions(String employeeID, double sss, double philHealth, double pagIbig, double tax) {
        this.employeeID = employeeID;
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.tax = tax;
    }

    // zero-value default
    public Deductions() {
        this.sss = 0.0;
        this.philHealth = 0.0;
        this.pagIbig = 0.0;
        this.tax = 0.0;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public double getSss() {
        return sss;
    }

    public void setSssRate(double sss) {
        this.sss = sss;
    }

    public double getPhilHealth() {
        return philHealth;
    }

    public void setPhilHealth(double philHealth) {
        this.philHealth = philHealth;
    }

    public double getPagIbig() {
        return pagIbig;
    }

    public void setPagIbigRate(double pagIbig) {
        this.pagIbig = pagIbig;
    }

    public double getTax() {
        return tax;
    }

    public void setTaxRate(double tax) {
        this.tax = tax;
    }

    public double getTotal() {
        return sss + philHealth + pagIbig + tax;
    }

    @Override
    public String toString() {
        return "\n  SSS : " + sss
                + ",\n  PhilHealth : " + philHealth
                + ",\n  Pag-IBIG : " + pagIbig
                + ",\n  Tax : " + tax
                + "\n";
    }
}

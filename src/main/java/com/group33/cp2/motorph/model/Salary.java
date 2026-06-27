package com.group33.cp2.motorph.model;

// Immutable salary data read from Salary.csv: basicSalary, hourlyRate, grossSMRate.
public class Salary {

    private final double basicSalary;
    private final double hourlyRate;
    private final double grossSMRate;

    public Salary(double basicSalary, double hourlyRate, double grossSMRate) {
        this.basicSalary = basicSalary;
        this.hourlyRate  = hourlyRate;
        this.grossSMRate = grossSMRate;
    }

    public double getBasicSalary() { return basicSalary; }
    public double getHourlyRate()  { return hourlyRate;  }
    public double getGrossSMRate() { return grossSMRate; }
}

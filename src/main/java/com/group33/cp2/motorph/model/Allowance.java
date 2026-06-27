package com.group33.cp2.motorph.model;

// Holds rice, phone, and clothing allowance amounts for an employee.
public class Allowance {

    private String employeeID;
    private double riceAllowance;
    private double phoneAllowance;
    private double clothingAllowance;

    // OOP-2: use setters so negative values are rejected even when passed at construction time
    public Allowance(String employeeID, double riceAllowance, double phoneAllowance, double clothingAllowance) {
        this.employeeID = employeeID;
        setRiceAllowance(riceAllowance);
        setPhoneAllowance(phoneAllowance);
        setClothingAllowance(clothingAllowance);
    }

    // zero-value default
    public Allowance() {
        this.riceAllowance = 0.0;
        this.phoneAllowance = 0.0;
        this.clothingAllowance = 0.0;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public double getRiceAllowance() {
        return riceAllowance;
    }

    public void setRiceAllowance(double riceAllowance) {
        if (riceAllowance < 0) {
            throw new IllegalArgumentException("Rice allowance must be >= 0. Received: " + riceAllowance);
        }
        this.riceAllowance = riceAllowance;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        if (phoneAllowance < 0) {
            throw new IllegalArgumentException("Phone allowance must be >= 0. Received: " + phoneAllowance);
        }
        this.phoneAllowance = phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) {
            throw new IllegalArgumentException("Clothing allowance must be >= 0. Received: " + clothingAllowance);
        }
        this.clothingAllowance = clothingAllowance;
    }

    public double getTotal() {
        return riceAllowance + phoneAllowance + clothingAllowance;
    }

    @Override
    public String toString() {
        return String.format(
                "\n  Rice Allowance: %.2f\n  Phone Allowance: %.2f\n  Clothing Allowance: %.2f\n",
                riceAllowance, phoneAllowance, clothingAllowance
        );
    }
}

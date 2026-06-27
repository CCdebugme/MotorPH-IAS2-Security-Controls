package com.group33.cp2.motorph.model;

// Holds government ID numbers for an employee: SSS, PhilHealth, TIN, Pag-IBIG.
// OOP-1: all setters now enforce null/blank guards (Encapsulation — validate at the boundary).
public class GovernmentDetails {

    private String employeeID;
    private String sssNumber;
    private String philHealthNumber;
    private String tinNumber;
    private String pagibigNumber;

    public GovernmentDetails(String employeeID, String sssNumber, String philHealthNumber,
                             String tinNumber, String pagibigNumber) {
        this.employeeID = employeeID;
        this.sssNumber = sssNumber;
        this.philHealthNumber = philHealthNumber;
        this.tinNumber = tinNumber;
        this.pagibigNumber = pagibigNumber;
    }

    public String getEmployeeID() {
        return this.employeeID;
    }

    public void setEmployeeID(String employeeID) {
        if (employeeID == null || employeeID.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be null or blank.");
        }
        this.employeeID = employeeID;
    }

    public String getSssNumber() {
        return sssNumber;
    }

    // OOP-1: null or blank SSS number would corrupt the government ID record in CSV
    public void setSssNumber(String sssNumber) {
        if (sssNumber == null || sssNumber.isBlank()) {
            throw new IllegalArgumentException("SSS number must not be null or blank.");
        }
        this.sssNumber = sssNumber;
    }

    public String getPhilHealthNumber() {
        return philHealthNumber;
    }

    // OOP-1: null or blank PhilHealth number would corrupt the government ID record in CSV
    public void setPhilHealthNumber(String philHealthNumber) {
        if (philHealthNumber == null || philHealthNumber.isBlank()) {
            throw new IllegalArgumentException("PhilHealth number must not be null or blank.");
        }
        this.philHealthNumber = philHealthNumber;
    }

    public String getTinNumber() {
        return tinNumber;
    }

    // OOP-1: null or blank TIN number would corrupt the government ID record in CSV
    public void setTinNumber(String tinNumber) {
        if (tinNumber == null || tinNumber.isBlank()) {
            throw new IllegalArgumentException("TIN number must not be null or blank.");
        }
        this.tinNumber = tinNumber;
    }

    public String getPagibigNumber() {
        return pagibigNumber;
    }

    // OOP-1: null or blank Pag-IBIG number would corrupt the government ID record in CSV
    public void setPagibigNumber(String pagibigNumber) {
        if (pagibigNumber == null || pagibigNumber.isBlank()) {
            throw new IllegalArgumentException("Pag-IBIG number must not be null or blank.");
        }
        this.pagibigNumber = pagibigNumber;
    }
}

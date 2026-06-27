package com.group33.cp2.motorph.model;

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

// SSS contribution bracket table. Use floorEntry(salary) to look up the contribution amount.
public class SSSDeductionsBracket {

    private static final NavigableMap<Double, Double> sssDeductions = new TreeMap<>();

    static {
        sssDeductions.put(0.0, 135.00);
        sssDeductions.put(3250.0, 157.50);
        sssDeductions.put(3750.0, 180.00);
        sssDeductions.put(4250.0, 202.50);
        sssDeductions.put(4750.0, 222.00);
        sssDeductions.put(5250.0, 247.50);
        sssDeductions.put(5750.0, 270.00);
        sssDeductions.put(6250.0, 292.50);
        sssDeductions.put(6750.0, 315.00);
        sssDeductions.put(7250.0, 337.50);
        sssDeductions.put(7750.0, 360.00);
        sssDeductions.put(8250.0, 382.50);
        sssDeductions.put(8750.0, 405.00);
        sssDeductions.put(9250.0, 427.50);
        sssDeductions.put(9750.0, 450.00);
        sssDeductions.put(10250.0, 472.50);
        sssDeductions.put(10750.0, 495.00);
        sssDeductions.put(11250.0, 517.50);
        sssDeductions.put(11750.0, 540.00);
        sssDeductions.put(12250.0, 562.50);
        sssDeductions.put(12750.0, 585.00);
        sssDeductions.put(13250.0, 607.50);
        sssDeductions.put(13750.0, 630.00);
        sssDeductions.put(14250.0, 652.50);
        sssDeductions.put(14750.0, 675.00);
        sssDeductions.put(15250.0, 697.50);
        sssDeductions.put(15750.0, 720.00);
        sssDeductions.put(16250.0, 742.50);
        sssDeductions.put(16750.0, 765.00);
        sssDeductions.put(17250.0, 787.50);
        sssDeductions.put(17750.0, 810.00);
        sssDeductions.put(18250.0, 832.50);
        sssDeductions.put(18750.0, 855.00);
        sssDeductions.put(19250.0, 877.50);
        sssDeductions.put(19750.0, 900.00);
        sssDeductions.put(20250.0, 922.50);
        sssDeductions.put(20750.0, 945.00);
        sssDeductions.put(21250.0, 967.50);
        sssDeductions.put(21750.0, 990.00);
        sssDeductions.put(22250.0, 1012.50);
        sssDeductions.put(22750.0, 1035.00);
        sssDeductions.put(23250.0, 1057.50);
        sssDeductions.put(23750.0, 1080.00);
        sssDeductions.put(24250.0, 1102.50);
        sssDeductions.put(24750.0, 1125.00);
    }

    // returns a read-only view of the bracket table
    public static NavigableMap<Double, Double> getDeductions() {
        return Collections.unmodifiableNavigableMap(sssDeductions);
    }
}

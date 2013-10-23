package com.ajjpj.asysmon.data;

/**
 * @author arno
 */
public class AGlobalDataPoint {
    private static double[] pow = new double[] {1.0, 10.0, 100.0, 1000.0, 10*1000.0, 100*1000.0, 1000*1000.0, 10*1000*1000.0, 100*1000*1000.0, 1000*1000*1000.0};

    private final String name;
    private final int value;
    private final int numFracDigits;

    public AGlobalDataPoint(String name, int value, int numFracDigits) {
        this.name = name;
        this.value = value;
        this.numFracDigits = numFracDigits;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value / pow[numFracDigits];
    }

    public int getNumFracDigits() {
        return numFracDigits;
    }
}

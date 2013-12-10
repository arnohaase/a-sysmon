package com.ajjpj.asysmon.data;

import com.ajjpj.asysmon.util.AUUID;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author arno
 */
public class AScalarDataPoint {
    private static double[] pow = new double[] {1.0, 10.0, 100.0, 1000.0, 10*1000.0, 100*1000.0, 1000*1000.0, 10*1000*1000.0, 100*1000*1000.0, 1000*1000*1000.0};
    private static String[] patterns = new String[] {"#,##0",
            "#,##0.0",       "#,##0.00",       "#,##0.000",
            "#,##0.0000",    "#,##0.00000",    "#,##0.000000",
            "#,##0.0000000", "#,##0.00000000", "#,##0.000000000",
            "#,##0.0000000000"};

    private final AUUID uuid = AUUID.createRandom();
    private final long timestamp;
    private final String name;
    private final long value;
    private final int numFracDigits;

    public AScalarDataPoint(long timestamp, String name, long value, int numFracDigits) {
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
        this.numFracDigits = numFracDigits;
    }

    public AUUID getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public long getValueRaw() {
        return value;
    }
    public double getValue() {
        return value / pow[numFracDigits];
    }

    public int getNumFracDigits() {
        return numFracDigits;
    }

    public String getFormattedValue() {
        if(numFracDigits == 0) {
            return NumberFormat.getIntegerInstance().format(value);
        }
        else {
            return new DecimalFormat (patterns[numFracDigits]).format(value);
        }
    }

    @Override
    public String toString() {
        return "AScalarDataPoint{" + name + ": " + getValue() + "}";
    }
}

package com.ajjpj.asysmon.measure.scalar;


import com.ajjpj.asysmon.data.AScalarDataPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * @author arno
 */
public class ASystemLoadMeasurer implements AScalarMeasurer {
    public static final String IDENT_LOAD_1_MIN = "load-1-minute";
    public static final String IDENT_LOAD_5_MIN = "load-5-minutes";
    public static final String IDENT_LOAD_15_MIN = "load-15-minutes";

    private final File procFile = new File("/proc/loadavg");

    @Override public void prepareMeasurements(Map<String, Object> mementos) {
    }

    @Override
    public void contributeMeasurements(Map<String, AScalarDataPoint> result, long timestamp, Map<String, Object> mementos) {
        try {
            final BufferedReader in = new BufferedReader(new FileReader(procFile));
            final String[] raw = in.readLine().split(" ");

            final int load1 = (int)(Double.parseDouble(raw[0])*100);
            final int load5 = (int)(Double.parseDouble(raw[1])*100);
            final int load15 = (int)(Double.parseDouble(raw[2])*100);

            result.put(IDENT_LOAD_1_MIN, new AScalarDataPoint(timestamp, IDENT_LOAD_1_MIN, load1, 2));
            result.put(IDENT_LOAD_5_MIN, new AScalarDataPoint(timestamp, IDENT_LOAD_5_MIN, load5, 2));
            result.put(IDENT_LOAD_15_MIN, new AScalarDataPoint(timestamp, IDENT_LOAD_15_MIN, load15, 2));
        }
        catch(Exception exc) { // do nothing //TODO move this to the caller
        }
    }

    @Override public void shutdown() {
    }
}

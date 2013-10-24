package com.ajjpj.asysmon.measure.global;

import com.ajjpj.asysmon.data.AGlobalDataPoint;

import java.util.Map;

/**
 * @author arno
 */
public class AMemoryMeasurer implements AGlobalMeasurer {
    public static final String IDENT_MEM_FREE  = "mem-free";
    public static final String IDENT_MEM_USED = "mem-used";
    public static final String IDENT_MEM_TOTAL = "mem-total";
    public static final String IDENT_MEM_MAX   = "mem-max";

    @Override public void contributeMeasurements(Map<String, AGlobalDataPoint> data) {
        data.put(IDENT_MEM_FREE, new AGlobalDataPoint(IDENT_MEM_FREE, Runtime.getRuntime().freeMemory(), 0));
        data.put(IDENT_MEM_TOTAL, new AGlobalDataPoint(IDENT_MEM_TOTAL, Runtime.getRuntime().totalMemory(), 0));
        data.put(IDENT_MEM_MAX, new AGlobalDataPoint(IDENT_MEM_MAX, Runtime.getRuntime().maxMemory(), 0));
        data.put(IDENT_MEM_USED, new AGlobalDataPoint(IDENT_MEM_USED, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), 0));
    }
}

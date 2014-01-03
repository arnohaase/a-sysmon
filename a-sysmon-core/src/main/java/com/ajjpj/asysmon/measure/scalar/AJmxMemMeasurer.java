package com.ajjpj.asysmon.measure.scalar;


import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;

import javax.management.NotificationEmitter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Map;

/**
 * @author arno
 */
public class AJmxMemMeasurer implements AScalarMeasurer {
    public static final String PREFIX_MEM = "mem:";
    public static final String SUFFIX_INITIAL = ":initial";
    public static final String SUFFIX_COMMITTED = ":committed";
    public static final String SUFFIX_MAX = ":max";
    public static final String SUFFIX_USED = ":used";

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp) {
//        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
//        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

        for (MemoryPoolMXBean mxBean : ManagementFactory.getMemoryPoolMXBeans()) {
            final String prefix = PREFIX_MEM + mxBean.getName();
//            mxBean.getType();
//            mxBean.getCollectionUsage();
//            mxBean.getPeakUsage();

            putValue(data, prefix + SUFFIX_COMMITTED, timestamp, mxBean.getUsage().getCommitted());
            putValue(data, prefix + SUFFIX_USED,      timestamp, mxBean.getUsage().getUsed());
            putValue(data, prefix + SUFFIX_INITIAL,   timestamp, mxBean.getUsage().getInit());
            putValue(data, prefix + SUFFIX_MAX,       timestamp, mxBean.getUsage().getMax());
        }
    }

    private void putValue(Map<String, AScalarDataPoint> data, String name, long timestamp, long value) {
        if(value != -1) {
            data.put(name, new AScalarDataPoint(timestamp, name, value, 0));
        }
    }

    @Override public void shutdown() throws Exception {
    }
}

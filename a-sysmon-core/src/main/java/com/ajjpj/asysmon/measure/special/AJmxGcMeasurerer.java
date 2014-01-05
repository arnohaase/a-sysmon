package com.ajjpj.asysmon.measure.special;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.scalar.AScalarMeasurer;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class AJmxGcMeasurerer implements AScalarMeasurer {
    public static final String IDENT_GC_TRACE_ROOT = "Garbage Collection";

    public static final String KEY_ID = "gc-id";
    public static final String KEY_CAUSE = "cause";
    public static final String KEY_ALGORITHM = "algorithm";

    public static final String KEY_PREFIX_MEM = "memgc:";

    public static final String KEY_SUFFIX_USED = ":used-after";
    public static final String KEY_SUFFIX_USED_DELTA = ":used-delta";
    public static final String KEY_SUFFIX_COMMITTED = ":committed-after";
    public static final String KEY_SUFFIX_COMMITTED_DELTA = ":committed-delta";

    private static final String SCALAR_PREFIX_FRAC_PERCENT = "gc-%:";
    private static final String SCALAR_PREFIX_FREQ_PER_MINUTE = "gc-per-minute:";

    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final int MILLION = 1000*1000;

    private final ASysMon sysMon;

    private final GcNotificationListener listener = new GcNotificationListener();

    private final Map<String, Long> prevGcTimeMillis = new ConcurrentHashMap<String, Long>();
    private final Map<String, Long> timeFracInGcPpm = new ConcurrentHashMap<String, Long>();
    private final Map<String, Long> gcFrequencyPerMinuteTimes100 = new ConcurrentHashMap<String, Long>();

    /**
     * This method encapsulates registration logic for JMX based GC measurements. It also registers callbacks for
     *  orderly deregistration during shutdown.
     */
    public static void init(ASysMon sysMon) {
        if(sysMon.getConfig().isGloballyDisabled()) {
            return;
        }

        final AJmxGcMeasurerer jmxGcMeasurerer = new AJmxGcMeasurerer(sysMon);
        ASysMonConfigurer.addScalarMeasurer(sysMon, jmxGcMeasurerer);
    }


    AJmxGcMeasurerer(ASysMon sysMon) {
        this.sysMon = sysMon;

        for (GarbageCollectorMXBean gcbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            final NotificationEmitter emitter = (NotificationEmitter) gcbean;
            emitter.addNotificationListener(listener, null, null);
        }
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp) {
        for(String key: timeFracInGcPpm.keySet()) {
            data.put(SCALAR_PREFIX_FRAC_PERCENT + key, new AScalarDataPoint(timestamp, SCALAR_PREFIX_FRAC_PERCENT + key, timeFracInGcPpm.get(key), 4));
        }
        for(String key: gcFrequencyPerMinuteTimes100.keySet()) {
            data.put(SCALAR_PREFIX_FREQ_PER_MINUTE + key, new AScalarDataPoint(timestamp, SCALAR_PREFIX_FREQ_PER_MINUTE + key, gcFrequencyPerMinuteTimes100.get(key), 2));
        }
    }

    @Override public void shutdown() throws Exception {
        for (GarbageCollectorMXBean gcbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            final NotificationEmitter emitter = (NotificationEmitter) gcbean;
            try {
                emitter.removeNotificationListener(listener, null, null);
            } catch (ListenerNotFoundException e) { // ignore this to facilitate repeated shutdown
            }
        }
    }

    private void updateScalars(long now, String gcType, long durationNanos) {
        // Callbacks are currently single threaded. Even if they weren't, the chance for a race condition is pretty
        //  slim here, and we decide to live with it. If GC notifications start overtaking each other, we are probably
        //  in deep trouble.

        final Long prevTimeMillis = prevGcTimeMillis.get(gcType);

        if(prevTimeMillis != null) {
            // seeming singularity due to limited measurement accuracy --> we fake-increase the interval
            final long intervalMillis = Math.max(1, now - prevTimeMillis);

            final long perMinute100 = MILLIS_PER_MINUTE * 100 / intervalMillis;
            gcFrequencyPerMinuteTimes100.put(gcType, perMinute100); //TODO flowing average or such?

            final long timeFracPpm = durationNanos / intervalMillis;
            timeFracInGcPpm.put(gcType, timeFracPpm); //TODO flowing average or such?
        }

        prevGcTimeMillis.put(gcType, now);
    }

    public static String getUsedAfterKey(String memKind) {
        return KEY_PREFIX_MEM + memKind + KEY_SUFFIX_USED;
    }
    public static String getCommittedAfterKey(String memKind) {
        return KEY_PREFIX_MEM + memKind + KEY_SUFFIX_COMMITTED;
    }
    public static String getUsedDeltaKey(String memKind) {
        return KEY_PREFIX_MEM + memKind + KEY_SUFFIX_USED_DELTA;
    }
    public static String getCommittedDeltaKey(String memKind) {
        return KEY_PREFIX_MEM + memKind + KEY_SUFFIX_COMMITTED_DELTA;
    }

    // code based on http://www.fasterj.com/articles/gcnotifs.shtml - thanks for the input, it was extremely helpful!!!
    class GcNotificationListener implements NotificationListener {
        private AHierarchicalData toHierarchicalData(GarbageCollectionNotificationInfo info) {
            final long durationNanos = info.getGcInfo().getDuration() * 1000;
            final long startMillis = System.currentTimeMillis() - durationNanos / MILLION;
            final String gcType = info.getGcAction();

            updateScalars(startMillis, gcType, durationNanos);

            final Map<String, String> paramMap = new HashMap<String, String>();

            paramMap.put(KEY_ID, String.valueOf(info.getGcInfo().getId()));
            paramMap.put(KEY_CAUSE, info.getGcCause());
            paramMap.put(KEY_ALGORITHM, info.getGcName());

            for(String memKey: info.getGcInfo().getMemoryUsageAfterGc().keySet()) {
                if(!isGcRelevantMemoryKind(memKey)) {
                    continue;
                }

                final MemoryUsage before = info.getGcInfo().getMemoryUsageBeforeGc().get(memKey);
                final MemoryUsage after  = info.getGcInfo().getMemoryUsageAfterGc().get(memKey);

                paramMap.put(getUsedAfterKey(memKey), String.valueOf(after.getUsed()));
                paramMap.put(getCommittedAfterKey(memKey), String.valueOf(after.getCommitted()));

                paramMap.put(getUsedDeltaKey(memKey), String.valueOf(after.getUsed() - before.getUsed()));
                paramMap.put(getCommittedDeltaKey(memKey), String.valueOf(after.getCommitted() - before.getCommitted()));

                // 'initial' and 'max' are independent of individual garbage collections and are better monitored separately
//                after.getInit();
//                after.getMax();
            }

            final AHierarchicalData byGcTypeNode = new AHierarchicalData(true, startMillis, durationNanos, gcType, Collections.<String,String>emptyMap(), Collections.<AHierarchicalData>emptyList());
            return new AHierarchicalData(true, startMillis, durationNanos, IDENT_GC_TRACE_ROOT, paramMap, Arrays.asList(byGcTypeNode));
        }

        private boolean isGcRelevantMemoryKind(String memKey) {
            if("Code Cache".equals(memKey)) {
                return false;
            }
            return true;
        }

        @Override public void handleNotification(Notification notification, Object handback) {
            //we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
            if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                final GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
                sysMon.injectSyntheticMeasurement(new AHierarchicalDataRoot(toHierarchicalData(info), Collections.<ACorrelationId>emptyList(), Collections.<ACorrelationId>emptyList()));
            }
        }
    }
}

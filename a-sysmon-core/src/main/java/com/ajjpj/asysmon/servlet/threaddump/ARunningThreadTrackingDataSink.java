package com.ajjpj.asysmon.servlet.threaddump;

import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This data sink tracks how long measurements have been running for each thread, providing an approximation for
 *  how long the thread has been 'doing its current work'.<p />
 *
 * Its collaboration with the thread dumper relies on uniqueness of thread names. While not guaranteed, it is usually
 *  a given in practical applications.
 *
 * @author arno
 */
class ARunningThreadTrackingDataSink implements ADataSink {
    private static final ASysMonLogger log = ASysMonLogger.get(ARunningThreadTrackingDataSink.class);

    private static final int MAX_NUM_THREADS = 100*1000;
    private final Map<String, Long> startTimestamps = new ConcurrentHashMap<String, Long>();

    private void ensureAgainstMemoryLeaks() {
        // Using ASysMon without closing measurements could cause a memory leak here. This method acts as a safeguard
        //  against that. Using a WeakHashMap plus synchronization might appear more elegant, would however introduce
        //  a global lock for which application code would have to wait.
        if(startTimestamps.size() > MAX_NUM_THREADS) {
            log.warn("timestamps more than " + MAX_NUM_THREADS + " were cached - assuming resource leak (i.e. measurements that never get closed) and discarding them");
            startTimestamps.clear();
        }
    }

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
        startTimestamps.put(Thread.currentThread().getName(), System.currentTimeMillis());
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        startTimestamps.remove(Thread.currentThread().getName());
    }

    public Map<String, Long> getStartTimestamps() {
        // return a stable snapshot
        return new HashMap<String, Long>(startTimestamps);
    }

    @Override public void shutdown() throws Exception {
    }
}

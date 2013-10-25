package com.ajjpj.asysmon.measure.threadpool;


import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AThreadPoolTrackingDataSink implements ADataSink {
    private final Map<String, AtomicInteger> threadCounts = new ConcurrentHashMap<String, AtomicInteger>();

    private String curThreadPool() {
        final String threadName = Thread.currentThread().getName();
        final int idxCount = threadName.lastIndexOf('-');
        if(idxCount < 0) {
            return "<other>";
        }
        return threadName.substring(0, idxCount);
    }

    @Override public void onStartedHierarchicalMeasurement() {
        final String poolName = curThreadPool();
        if (threadCounts.get(poolName) == null) {
            final AtomicInteger prev = threadCounts.put(poolName, new AtomicInteger(1));
            if(prev != null) {
                // race condition for first write
                threadCounts.get(poolName).addAndGet(prev.get());
            }
        }
        else {
            threadCounts.get(poolName).incrementAndGet();
        }
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data) {
        threadCounts.get(curThreadPool()).decrementAndGet();
    }

    public Map<String, AtomicInteger> getThreadCounts() {
        return threadCounts;
    }
}

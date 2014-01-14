package com.ajjpj.asysmon.measure.threadpool;


import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
class AThreadPoolTrackingDataSink implements ADataSink {
    private final Map<String, AtomicInteger> threadCounts = new ConcurrentHashMap<String, AtomicInteger>();

    public static String threadPoolName(String threadName) {
        final int idxCount = threadName.lastIndexOf('-');
        if(idxCount < 0) {
            return "<other>";
        }
        return threadName.substring(0, idxCount);
    }

    private String curThreadPool() {
        return threadPoolName(Thread.currentThread().getName());
    }

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
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

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        threadCounts.get(curThreadPool()).decrementAndGet();
    }

    @Override public void shutdown() {
    }

    public Map<String, AtomicInteger> getThreadCounts() {
        return threadCounts;
    }
}

package com.ajjpj.asysmon.servlet_.threaddump;

import com.ajjpj.asysmon.measure.threadpool.AThreadPoolTrackingDataSink;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.Collator;
import java.util.*;


/**
 * @author arno
 */
class AThreadDumper {
    static final Comparator<ThreadInfo> threadNameComparator = new Comparator<ThreadInfo>() {
        @Override public int compare(ThreadInfo o1, ThreadInfo o2) {
            return Collator.getInstance().compare(o1.getThreadName(), o2.getThreadName());
        }
    };

    private static final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

    public static Collection<ThreadInfo> getThreadInfo() {
        final Collection<ThreadInfo> result = new TreeSet<ThreadInfo>(threadNameComparator);

        result.addAll(Arrays.asList(mxBean.dumpAllThreads(mxBean.isObjectMonitorUsageSupported(), mxBean.isSynchronizerUsageSupported())));

        return result;
    }

    public static Collection<Long> getDeadlockedThreads() {
        final Collection<Long> result = new HashSet<Long>();

        for(long threadId: mxBean.findDeadlockedThreads()) {
            result.add(threadId);
        }

        for(long threadId: mxBean.findMonitorDeadlockedThreads()) {
            result.add(threadId);
        }

        return result;
    }

    public Map<String, Collection<ThreadInfo>> dumpThreadsByGroup() {
        final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        final Map<String, Collection<ThreadInfo>> result = new TreeMap<String, Collection<ThreadInfo>>();

        for(ThreadInfo ti: mxBean.dumpAllThreads(mxBean.isObjectMonitorUsageSupported(), mxBean.isSynchronizerUsageSupported())) {
            final String threadPoolName = AThreadPoolTrackingDataSink.threadPoolName(ti.getThreadName());

            Collection<ThreadInfo> coll = result.get(threadPoolName);
            if(coll == null) {
                coll = new TreeSet<ThreadInfo>(threadNameComparator);
                result.put(threadPoolName, coll);
            }
            coll.add(ti);
        }
        return result;
    }
}

package com.ajjpj.asysmon.servlet.threaddump;

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

    private static final long[] NO_LONGS = new long[0];
    private static long[] nullToEmpty(long[] longs) {
        return longs != null ? longs : NO_LONGS;
    }

    public static Collection<Long> getDeadlockedThreads() {
        final Collection<Long> result = new HashSet<Long>();

        if(mxBean != null) { // may happen, depending on JVM version
            for(long threadId: nullToEmpty(mxBean.findDeadlockedThreads())) {
                result.add(threadId);
            }

            for(long threadId: nullToEmpty(mxBean.findMonitorDeadlockedThreads())) {
                result.add(threadId);
            }
        }

        return result;
    }
}

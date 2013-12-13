package com.ajjpj.asysmon.servlet.threaddump;

import java.lang.management.*;

/**
 * @author arno
 */
public class AJmxThreadMeasurer {
    public AJmxThreadMeasurer() {
        final ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();

        final long[] monitorDeadlocked = mxBean.findMonitorDeadlockedThreads();
        final long[] deadlocked = mxBean.findDeadlockedThreads();

        final ThreadInfo[] threadInfos = mxBean.dumpAllThreads(mxBean.isObjectMonitorUsageSupported(), mxBean.isSynchronizerUsageSupported());

        final Thread.State state = threadInfos[0].getThreadState();
        final String threadName = threadInfos[0].getThreadName();
        final long threadId = threadInfos[0].getThreadId();

        final boolean isInNative = threadInfos[0].isInNative();
//        final boolean isSuspended = threadInfos[0].isSuspended();

        final LockInfo lockInfo = threadInfos[0].getLockInfo();
        final long lockOwner = threadInfos[0].getLockOwnerId();

        final MonitorInfo[] lockedMonitors = threadInfos[0].getLockedMonitors();
        final LockInfo[] lockedSynchronizers = threadInfos[0].getLockedSynchronizers();


//        threadInfos[0].getBlockedCount();
//        threadInfos[0].getBlockedTime();

//        threadInfos[0].getLockName();
//        threadInfos[0].getLockOwnerName();
//        threadInfos[0].getWaitedCount();
//        threadInfos[0].getWaitedTime();

        final StackTraceElement[] stackTrace = threadInfos[0].getStackTrace();
        stackTrace[0].getClassName();
        stackTrace[0].getMethodName();
        stackTrace[0].getFileName();
        stackTrace[0].getLineNumber();
        stackTrace[0].isNativeMethod();
        stackTrace[0].toString();

    }
}

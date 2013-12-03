package com.ajjpj.asysmon.server.services;

/**
 * @author arno
 */
public class ConfigData {
    // queues that buffer uploaded data before it is stored in the database
    private final int environmentQueueSize;
    private final int scalarQueueSize;
    private final int traceQueueSize;

    // thread pool sizes for pumping data from the queues into the database
    private final int numEnvironmentWorkerThreads;
    private final int numScalarWorkerThreads;
    private final int numTraceWorkerThreads;

    public ConfigData(int environmentQueueSize, int scalarQueueSize, int traceQueueSize, int numEnvironmentWorkerThreads, int numScalarWorkerThreads, int numTraceWorkerThreads) {
        this.environmentQueueSize = environmentQueueSize;
        this.scalarQueueSize = scalarQueueSize;
        this.traceQueueSize = traceQueueSize;
        this.numEnvironmentWorkerThreads = numEnvironmentWorkerThreads;
        this.numScalarWorkerThreads = numScalarWorkerThreads;
        this.numTraceWorkerThreads = numTraceWorkerThreads;
    }

    public int getEnvironmentQueueSize() {
        return environmentQueueSize;
    }

    public int getScalarQueueSize() {
        return scalarQueueSize;
    }

    public int getTraceQueueSize() {
        return traceQueueSize;
    }

    public int getNumEnvironmentWorkerThreads() {
        return numEnvironmentWorkerThreads;
    }

    public int getNumScalarWorkerThreads() {
        return numScalarWorkerThreads;
    }

    public int getNumTraceWorkerThreads() {
        return numTraceWorkerThreads;
    }
}

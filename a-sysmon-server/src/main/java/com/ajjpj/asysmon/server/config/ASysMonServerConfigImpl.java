package com.ajjpj.asysmon.server.config;

/**
 * @author arno
 */
public class ASysMonServerConfigImpl implements ASysMonServerConfig {
    private final int uploadPortNumber;

    private final int environmentQueueSize;
    private final int scalarQueueSize;
    private final int traceQueueSize;

    private final int numEnvironmentWorkerThreads;
    private final int numScalarWorkerThreads;
    private final int numTraceWorkerThreads;

    public ASysMonServerConfigImpl(int uploadPortNumber,
                                   int environmentQueueSize, int scalarQueueSize, int traceQueueSize,
                                   int numEnvironmentWorkerThreads, int numScalarWorkerThreads, int numTraceWorkerThreads) {
        this.uploadPortNumber = uploadPortNumber;
        this.environmentQueueSize = environmentQueueSize;
        this.scalarQueueSize = scalarQueueSize;
        this.traceQueueSize = traceQueueSize;
        this.numEnvironmentWorkerThreads = numEnvironmentWorkerThreads;
        this.numScalarWorkerThreads = numScalarWorkerThreads;
        this.numTraceWorkerThreads = numTraceWorkerThreads;
    }

    @Override public int getUploadPortNumber() {
        return uploadPortNumber;
    }

    @Override public int getEnvironmentQueueSize() {
        return environmentQueueSize;
    }

    @Override public int getScalarQueueSize() {
        return scalarQueueSize;
    }

    @Override public int getTraceQueueSize() {
        return traceQueueSize;
    }

    @Override public int getNumEnvironmentWorkerThreads() {
        return numEnvironmentWorkerThreads;
    }

    @Override public int getNumScalarWorkerThreads() {
        return numScalarWorkerThreads;
    }

    @Override public int getNumTraceWorkerThreads() {
        return numTraceWorkerThreads;
    }
}

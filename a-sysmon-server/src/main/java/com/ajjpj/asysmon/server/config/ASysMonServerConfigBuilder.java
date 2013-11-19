package com.ajjpj.asysmon.server.config;

/**
 * @author arno
 */
public class ASysMonServerConfigBuilder {
    private int uploadPortNumber = 8899;

    private int environmentQueueSize = 1000;
    private int scalarQueueSize = 10_000;
    private int traceQueueSize = 100;

    private int numEnvironmentWorkerThreads = 4;
    private int numScalarWorkerThreads = 10;
    private int numTraceWorkerThreads = 10;


    public ASysMonServerConfigBuilder setUploadPortNumber(int uploadPortNumber) {
        this.uploadPortNumber = uploadPortNumber;
        return this;
    }

    public ASysMonServerConfigBuilder setEnvironmentQueueSize(int environmentQueueSize) {
        this.environmentQueueSize = environmentQueueSize;
        return this;
    }

    public ASysMonServerConfigBuilder setScalarQueueSize(int scalarQueueSize) {
        this.scalarQueueSize = scalarQueueSize;
        return this;
    }

    public ASysMonServerConfigBuilder setTraceQueueSize(int traceQueueSize) {
        this.traceQueueSize = traceQueueSize;
        return this;
    }

    public ASysMonServerConfigBuilder setNumEnvironmentWorkerThreads(int numEnvironmentWorkerThreads) {
        this.numEnvironmentWorkerThreads = numEnvironmentWorkerThreads;
        return this;
    }

    public ASysMonServerConfigBuilder setNumScalarWorkerThreads(int numScalarWorkerThreads) {
        this.numScalarWorkerThreads = numScalarWorkerThreads;
        return this;
    }

    public ASysMonServerConfigBuilder setNumTraceWorkerThreads(int numTraceWorkerThreads) {
        this.numTraceWorkerThreads = numTraceWorkerThreads;
        return this;
    }

    public ASysMonServerConfig build() {
        return new ASysMonServerConfigImpl(uploadPortNumber,
                environmentQueueSize, scalarQueueSize, traceQueueSize,
                numEnvironmentWorkerThreads, numScalarWorkerThreads, numTraceWorkerThreads);
    }
}

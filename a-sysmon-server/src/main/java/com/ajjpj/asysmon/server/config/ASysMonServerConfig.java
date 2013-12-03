package com.ajjpj.asysmon.server.config;

/**
 * @author arno
 */
public interface ASysMonServerConfig {
    int getUploadPortNumber();

    // inputprocessing
    int getEnvironmentQueueSize();
    int getScalarQueueSize();
    int getTraceQueueSize();

    int getNumEnvironmentWorkerThreads();
    int getNumScalarWorkerThreads();
    int getNumTraceWorkerThreads();

}

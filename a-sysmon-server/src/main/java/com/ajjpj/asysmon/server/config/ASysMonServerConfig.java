package com.ajjpj.asysmon.server.config;

/**
 * @author arno
 */
public interface ASysMonServerConfig {
    int getUploadPortNumber();

    // processing
    int getEnvironmentQueueSize();
    int getScalarQueueSize();
    int getTraceQueueSize();

    int getNumEnvironmentWorkerThreads();
    int getNumScalarWorkerThreads();
    int getNumTraceWorkerThreads();

}

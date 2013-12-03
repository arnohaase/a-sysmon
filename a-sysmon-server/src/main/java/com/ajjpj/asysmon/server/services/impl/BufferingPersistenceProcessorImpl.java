package com.ajjpj.asysmon.server.services.impl;

import com.ajjpj.asysmon.server.eventbus.EventBus;
import com.ajjpj.asysmon.server.eventbus.NewDataListener;
import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import com.ajjpj.asysmon.server.services.BufferingPersistenceProcessor;
import com.ajjpj.asysmon.server.storage.ScalarDataDao;
import com.ajjpj.asysmon.util.AShutdownable;
import com.ajjpj.asysmon.util.ASoftlyLimitedBlockingQueue;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class receives all new data from the event eventbus, buffering it in queues and storing it persistently from worker
 *  threads. Its purpose is to decouple event eventbus response time from the potentially slow I/O required for
 *  persistent storage.
 *
 * @author arno
 */
public class BufferingPersistenceProcessorImpl implements BufferingPersistenceProcessor, AShutdownable {
        private static final Logger log = Logger.getLogger(BufferingPersistenceProcessorImpl.class);

    private volatile boolean isShutDown = false;

    private final EventBus eventBus;
    private final ScalarDataDao scalarDataDao;


    private final ASoftlyLimitedBlockingQueue<ScalarNode> scalarQueue;
    private final ASoftlyLimitedBlockingQueue<TraceRootNode> traceQueue;
    private final ASoftlyLimitedBlockingQueue<EnvironmentNode> environmentQueue;

    private final ExecutorService scalarEs;
    private final ExecutorService traceEs;
    private final ExecutorService environmentEs;

    private final NewDataListener listener = new NewDataListener() {
        @Override public void onNewScalarData(ScalarNode scalarData) {
            scalarQueue.add(scalarData);
        }

        @Override public void onNewTrace(TraceRootNode traceData) {
            traceQueue.add(traceData);
        }

        @Override public void onNewEnvironmentData(EnvironmentNode environmentData) {
            environmentQueue.add(environmentData);
        }
    };

    private final Runnable scalarWorker = new Runnable() {
        @Override public void run() {
            while (!isShutDown) {
                try {
                    scalarDataDao.storeScalarData(scalarQueue.take());
                } catch (Exception e) {
                    log.error("exception storing scalar", e);
                }
            }
        }
    };

    private final Runnable traceWorker = new Runnable() {
        @Override public void run() {
//            while (!isShutDown) {
//                try {
//                    persister.storeTraceData(traceQueue.take());
//                } catch (Exception e) {
//                    log.error("exception storing trace", e);
//                }
//            }
        }
    };

    private final Runnable environmentWorker = new Runnable() {
        @Override public void run() {
//            while (!isShutDown) {
//                try {
//                    persister.storeEnvironmentData(environmentQueue.take());
//                } catch (Exception e) {
//                    log.warn("exception storing environment", e);
//                }
//            }
        }
    };


    @Inject
    public BufferingPersistenceProcessorImpl(EventBus eventBus, ScalarDataDao scalarDataDao, ASysMonServerConfig config) {
        this.scalarDataDao = scalarDataDao;
        this.eventBus = eventBus;

        scalarQueue      = new ASoftlyLimitedBlockingQueue<>(config.getScalarQueueSize(),      new Log4JWarnCallback("scalar queue overflow - discarding data"));
        traceQueue       = new ASoftlyLimitedBlockingQueue<>(config.getTraceQueueSize(),       new Log4JWarnCallback("trace queue overflow - discarding data"));
        environmentQueue = new ASoftlyLimitedBlockingQueue<>(config.getEnvironmentQueueSize(), new Log4JWarnCallback("environment queue overflow - discarding data"));

        scalarEs      = Executors.newFixedThreadPool(config.getNumScalarWorkerThreads());
        traceEs       = Executors.newFixedThreadPool(config.getNumTraceWorkerThreads());
        environmentEs = Executors.newFixedThreadPool(config.getNumEnvironmentWorkerThreads());

        for(int i=0; i<config.getNumScalarWorkerThreads(); i++) {
            scalarEs.submit(scalarWorker);
        }

        for(int i=0; i<config.getNumTraceWorkerThreads(); i++) {
            traceEs.submit(traceWorker);
        }

        for(int i=0; i<config.getNumEnvironmentWorkerThreads(); i++) {
            environmentEs.submit(environmentWorker);
        }

        eventBus.addListener(listener);
    }

    @Override public void shutdown() throws Exception {
        isShutDown = true;
        eventBus.removeListener(listener);

        scalarEs.shutdown();
        traceEs.shutdown();
        environmentEs.shutdown();
    }

    private static class Log4JWarnCallback implements Runnable {
        private final String msg;

        private Log4JWarnCallback(String msg) {
            this.msg = msg;
        }

        @Override public void run() {
            log.warn(msg);
        }
    }
}

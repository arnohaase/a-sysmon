package com.ajjpj.asysmon.datasink.offloadhttpjson;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.log.ASysMonLogger;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.util.ASoftlyLimitedQueue;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author arno
 */
public class AHttpJsonOffloadingDataSink implements ADataSink {
    public static final int NO_DATA_SLEEP_MILLIS = 10;

    private static final ASysMonLogger log = ASysMonLogger.get(AHttpJsonOffloadingDataSink.class);

    private final ASysMonConfig config;

    //TODO how many concurrent HTTP connections does this provide? --> configure!
    private final CloseableHttpClient httpClient = HttpClients.createDefault(); //TODO make this configurable
    private final URI uri;

    private final String sender;
    private final String senderInstance;

    private final ASoftlyLimitedQueue<AHierarchicalDataRoot> traceQueue;
    private final ASoftlyLimitedQueue<AScalarDataPoint> scalarQueue;

    private final ExecutorService offloadingThreadPool;
    private final ScheduledExecutorService scalarMeasurementPool;

    private volatile boolean isShutDown = false;

    public AHttpJsonOffloadingDataSink(final ASysMonApi sysMon, String uri, String sender, String senderInstance, int traceQueueSize, int scalarQueueSize, int numOffloadingThreads, int scalarMeasurementFrequencyMillis) {
        this.config = sysMon.getConfig();

        this.uri = URI.create(uri);
        this.sender = sender;
        this.senderInstance = senderInstance;

        this.traceQueue = new ASoftlyLimitedQueue<AHierarchicalDataRoot>(traceQueueSize, new DiscardedLogger("trace queue overflow - discarding oldest trace"));
        this.scalarQueue = new ASoftlyLimitedQueue<AScalarDataPoint>(scalarQueueSize, new DiscardedLogger("environment queue overflow - discarding oldest data"));

        offloadingThreadPool = Executors.newFixedThreadPool(numOffloadingThreads);
        for(int i=0; i<numOffloadingThreads; i++) {
            offloadingThreadPool.submit(new OffloadingRunnable());
        }

        scalarMeasurementPool = Executors.newSingleThreadScheduledExecutor();
        scalarMeasurementPool.scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                //TODO introduce 'AScalarProvider' interface for callbacks like this
                for(AScalarDataPoint scalar: sysMon.getScalarMeasurements().values()) { //TODO ensure that this ASysMon call will never throw exceptions
                    scalarQueue.add(scalar);
                }
            }
        }, 0, scalarMeasurementFrequencyMillis, TimeUnit.MILLISECONDS);
    }


    @Override public void onStartedHierarchicalMeasurement(String identifier) { }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {

        //TODO make resending of arbitrary data idempotent --> send a unique identifier with every *atom* (trace, environment measurement, ...)
        //TODO re-enter the data into the queues when the server returns a non-OK http response code

        traceQueue.add(data);
    }

    private void doOffload() throws Exception {
        final List<AHierarchicalDataRoot> traces = new ArrayList<AHierarchicalDataRoot>();
        AHierarchicalDataRoot candidate;
        while ((candidate = traceQueue.poll()) != null) { //TODO limit number per HTTP request?!
            traces.add(candidate);
        }

        final List<AScalarDataPoint> scalars = new ArrayList<AScalarDataPoint>();
        AScalarDataPoint scalar;
        while ((scalar = scalarQueue.poll()) != null) { //TODO limit number per HTTP request?
            scalars.add(scalar);
        }

        if(traces.isEmpty() && scalars.isEmpty()) {
            Thread.sleep(NO_DATA_SLEEP_MILLIS);
        }
        else {
            try {
            final HttpPost httpPost = new HttpPost(uri);

                final AJsonOffloadingEntity entity = new AJsonOffloadingEntity(traces, scalars, sender, senderInstance);
                httpPost.setEntity(entity);

                final CloseableHttpResponse response = httpClient.execute(httpPost);
                try {
                    //TODO response with commands for monitoring this app?!
                } finally {
                    response.close();
                }
            }
            catch(Exception exc) {
                log.error(exc);

                // add the data to the queue again for later retry
                scalarQueue.addAll(scalars);

                System.out.println(" == " + scalarQueue);

                // wait a grace period for the situation to improve
                Thread.sleep(5000); //TODO make this configurable
            }
        }
    }

    @Override public void shutdown() throws IOException {
        isShutDown = true;
        httpClient.close();
        scalarMeasurementPool.shutdown();
        offloadingThreadPool.shutdown();
    }

    private static class DiscardedLogger implements Runnable {
        private final String msg;

        private DiscardedLogger(String msg) {
            this.msg = msg;
        }

        @Override public void run() {
            log.warn(msg);
        }
    }

    private class OffloadingRunnable implements Runnable {
        @Override public void run() {
            while(! isShutDown) {
                try {
                    doOffload();
                } catch (Exception e) {
                    e.printStackTrace(); //TODO exception handling
                }
            }
        }
    }
}

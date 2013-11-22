package com.ajjpj.asysmon.datasink.offloadhttpjson;

import com.ajjpj.asysmon.config.AGlobalConfig;
import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.util.ASoftlyLimitedQueue;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * @author arno
 */
public class AHttpJsonOffloadingDataSink implements ADataSink { //TODO support substituting HttpClient 4.3 with some other HTTP client functionality
    private final CloseableHttpClient httpClient = HttpClients.createDefault(); //TODO make this configurable
    private final URI uri;

    private final ASoftlyLimitedQueue<AHierarchicalData> traceQueue;
    private final ASoftlyLimitedQueue<AGlobalDataPoint> scalarQueue;

    public AHttpJsonOffloadingDataSink(String uri, int traceQueueSize, int scalarQueueSize) {
        this.uri = URI.create(uri);

        this.traceQueue = new ASoftlyLimitedQueue<AHierarchicalData>(traceQueueSize, new DiscardedLogger("trace queue overflow - discarding oldest trace"));
        this.scalarQueue = new ASoftlyLimitedQueue<AGlobalDataPoint>(scalarQueueSize, new DiscardedLogger("scalar queue overflow - discarding oldest data"));
    }

    @Override public void onStartedHierarchicalMeasurement() { }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        //TODO async handling --> enqueue
        //TODO offload scalars

        //TODO make resending of arbitrary data idempotent --> send a unique identifier with every *atom* (trace, scalar measurement, ...)
        //TODO re-enter the data into the queues when the server returns a non-OK http response code

        try {
            final HttpPost httpPost = new HttpPost(uri);

            final AJsonOffloadingEntity entity = new AJsonOffloadingEntity(Arrays.asList(data), Collections.<AGlobalDataPoint>emptyList());
            httpPost.setEntity(entity);

            final CloseableHttpResponse response = httpClient.execute(httpPost);
            try {

            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace(); //TODO
        }
    }

//    private void doOffload() throws IOException {
//            final HttpPost httpPost = new HttpPost(uri);
//
//            final AJsonOffloadingEntity entity = new AJsonOffloadingEntity(Arrays.asList(data), Arrays.asList(startedFlows), Arrays.asList(joinedFlows), Collections.<AGlobalDataPoint>emptyList());
//            httpPost.setEntity(entity);
//
//            final CloseableHttpResponse response = httpClient.execute(httpPost);
//            try {
//
//            } finally {
//                response.close();
//            }
//    }
//

    @Override public void shutdown() throws IOException {
            httpClient.close();
    }

    private static class DiscardedLogger implements Runnable {
        private final String msg;

        private DiscardedLogger(String msg) {
            this.msg = msg;
        }

        @Override public void run() {
            AGlobalConfig.getLogger().warn(msg);
        }
    }
}

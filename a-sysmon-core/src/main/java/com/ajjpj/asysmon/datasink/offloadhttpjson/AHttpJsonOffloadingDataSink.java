package com.ajjpj.asysmon.datasink.offloadhttpjson;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;
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

    public AHttpJsonOffloadingDataSink(String uri) {
        this.uri = URI.create(uri);
    }

    @Override public void onStartedHierarchicalMeasurement() { }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
        //TODO async handling --> enqueue
        //TODO offload scalars

        try {
            final HttpPost httpPost = new HttpPost(uri);

            final AJsonOffloadingEntity entity = new AJsonOffloadingEntity(Arrays.asList(data), Arrays.asList(startedFlows), Arrays.asList(joinedFlows), Collections.<AGlobalDataPoint>emptyList());
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

    @Override public void shutdown() throws IOException {
            httpClient.close();
    }
}

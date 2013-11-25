package com.ajjpj.asysmon.server.store;


import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author arno
 */
public class DataStoreImpl implements DataProvider, DataPersister {
    private static final Logger log = Logger.getLogger(DataStoreImpl.class);

    final Node node = NodeBuilder.nodeBuilder() //TODO externalize this configuration
//                .local(true)
            .client(false)
                    //TODO configure location of node files in file system
            .node();
    final Client client = node.client();

    @Override public void storeScalarData(ScalarNode scalarData) {
        log.debug("storing " + scalarData);
        client.prepareIndex("scalar", "scalar", scalarData.getUuid()) //TODO one index? one index per type? ...?
                .setSource(scalarToJson(scalarData))
                .execute();
    }

    @Override public void storeTraceData(TraceRootNode traceData) {
        log.debug("storing " + traceData);

        final SearchResponse response = client.prepareSearch("scalar")
                .setSearchType(SearchType.QUERY_AND_FETCH)
                .setQuery(QueryBuilders.fieldQuery("name", "load-1-minute"))
                .execute()
                .actionGet();

        System.out.println(response);
    }

    @Override public void storeEnvironmentData(EnvironmentNode environmentData) {
        log.debug("storing " + environmentData);
    }

    private Map<String, Object> scalarToJson(ScalarNode scalarData) {
        final Map<String, Object> result = new HashMap<>();
        result.put("applicationId", scalarData.getInstanceIdentifier().getApplicationId());
        result.put("instanceId", scalarData.getInstanceIdentifier().getInstanceId());
        result.put("name", scalarData.getName());
        result.put("value", scalarData.getValue());
        result.put("timestamp", scalarData.getAdjustedTimestamp());
        result.put("senderTimestamp", scalarData.getSenderTimestamp());
        return result;
    }
}

package com.ajjpj.asysmon.server.store.backend.mongo;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import com.ajjpj.asysmon.server.store.DataPersister;
import com.ajjpj.asysmon.server.store.DataProvider;
import com.ajjpj.asysmon.server.store.backend.ScalarDataPoint;
import com.mongodb.*;
import org.apache.log4j.Logger;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO remove elastic search dependenccy
/**
 * @author arno
 */
public class DataStoreImpl implements DataProvider, DataPersister {
    public static final String INDEX_NAME_SCALAR_INSTANCE_AND_TIME = "instance-and-time";

    private final MongoClient mongoClient;
    private final DB db;

    public DataStoreImpl() throws UnknownHostException {
        mongoClient = new MongoClient(); //TODO make this configurable
        db = mongoClient.getDB("a-sysmon");
    }

    private static final Logger log = Logger.getLogger(DataStoreImpl.class);

//    final Node node = NodeBuilder.nodeBuilder() //TODO externalize this configuration
////                .local(true)
//            .client(false)
//                    //TODO configure location of node files in file system
//            .node();
//    final Client client = node.client();

    @Override public void storeScalarData(ScalarNode scalarData) {
        new ScalarDataDaoMongoImpl(db).storeScalarData(scalarData);
    }

    @Override
    public List<ScalarDataPoint> findAll(String appId, String scalarName, long fromTimestamp, long toTimestamp, String... instanceIds) {
        return new ScalarDataDaoMongoImpl(db).findAll(appId, scalarName, fromTimestamp, toTimestamp, instanceIds);
    }

    @Override public void storeTraceData(TraceRootNode traceData) {
        log.debug("storing " + traceData);

//        final SearchResponse response = client.prepareSearch("scalar")
//                .setSearchType(SearchType.QUERY_AND_FETCH)
//                .setQuery(QueryBuilders.fieldQuery("name", "load-1-minute"))
//                .execute()
//                .actionGet();
//
//        System.out.println(response);
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

    private String getScalarCollectionName(InstanceIdentifier instanceIdentifier, String scalarName) {
        return instanceIdentifier.getApplicationId() + ":" + scalarName;
    }

    private void initializeScalarCollection(InstanceIdentifier instanceIdentifier, String scalarName) {
        final String collName = getScalarCollectionName(instanceIdentifier, scalarName);
        if(db.getCollectionNames().contains(collName)) {
            //TODO log a warning if one of the expected indexes is missing
            return;
        }

        // If the collection is actually created here, it is safe to create indexes automatically. If there was
        //  a significant amount of data in the collection, that could take significant time and should therefore
        //  be done explicitly in a maintenance window
        //TODO management infrastructure that detects the absence of indexes

        final DBCollection coll = db.getCollection(collName);

        final BasicDBObject idx = new BasicDBObject();
        idx.append("instance", 1);
        idx.append("timestamp", 1);
        coll.ensureIndex(idx, INDEX_NAME_SCALAR_INSTANCE_AND_TIME);
    }
}

package com.ajjpj.asysmon.server.storage.impl;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.storage.MonitoredApplicationDao;
import com.ajjpj.asysmon.server.storage.ScalarDataDao;
import com.ajjpj.asysmon.server.storage.ScalarMetaData;
import com.ajjpj.asysmon.server.storage.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.storage.ScalarDataPoint;
import com.mongodb.*;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author arno
 */
@Singleton
public class ScalarDataDaoImpl implements ScalarDataDao {
    private static final Logger log = Logger.getLogger(ScalarDataDaoImpl.class);

    public static final String FIELD_NAME_INSTANCE = "instance";
    public static final String FIELD_NAME_TIMESTAMP = "timestamp";
    public static final String FIELD_NAME_VALUE = "value";

    public static final String INDEX_NAME_SCALAR_INSTANCE_AND_TIME = "timestamp-and-instance";

    private final DB db;

    private final MonitoredApplicationDao monitoredApplicationDao;
    private final ScalarMetaDataDao scalarMetaDataDao;

    @Inject
    public ScalarDataDaoImpl(DB db, MonitoredApplicationDao monitoredApplicationDao, ScalarMetaDataDao scalarMetaDataDao) {
        this.db = db;
        this.monitoredApplicationDao = monitoredApplicationDao;
        this.scalarMetaDataDao = scalarMetaDataDao;
    }

    private String getScalarCollectionName(String appId, String scalarName) {
        return appId + MongoDbHelper.SEPARATOR + scalarName;
    }

    private DBCollection getScalarCollection(String appId, String scalarName) {
        return db.getCollection(getScalarCollectionName(appId, scalarName));
    }

    public DBCollection getScalarCollection(InstanceIdentifier instanceIdentifier, String scalarName) {
        return getScalarCollection(instanceIdentifier.getApplicationId(), scalarName);
    }


    @Override public void storeScalarData(ScalarNode scalarData) {
        log.debug("storing " + scalarData);

        final BasicDBObject object = new BasicDBObject();
        object.append("_id", scalarData.getUuid());
        object.append(FIELD_NAME_INSTANCE, scalarData.getInstanceIdentifier().getInstanceId());
        object.append(FIELD_NAME_TIMESTAMP, scalarData.getAdjustedTimestamp());
        object.append("sender-timestamp", scalarData.getSenderTimestamp());
        object.append(FIELD_NAME_VALUE, scalarData.getValue());

        try {
            getScalarCollection(scalarData.getInstanceIdentifier(), scalarData.getName()).insert(object);
        } catch (MongoException.DuplicateKey e) {
            log.warn("Attempted to insert environment data " + scalarData + " twice, ignoring. This is probably due to a client sending the same data twice.");
        }
    }

    private ScalarDataPoint fromDbObject(DBObject o) {
        final String instance = (String) o.get(FIELD_NAME_INSTANCE);
        final long timestamp = MongoDbHelper.asLongExpected(o, FIELD_NAME_TIMESTAMP);
        final double value = MongoDbHelper.asDoubleExpected(o, FIELD_NAME_VALUE);

        return new ScalarDataPoint(instance, timestamp, value);
    }

    public List<ScalarDataPoint> findAll(String appId, String scalarName, long fromTimestamp, long toTimestamp, String... instanceIds) {
        final BasicDBObject query = new BasicDBObject();
        query.append(FIELD_NAME_TIMESTAMP, MongoDbHelper.createQueryTimestampRange(fromTimestamp, toTimestamp));
        query.append(FIELD_NAME_INSTANCE, MongoDbHelper.createQueryInClause(instanceIds));

//        System.out.println(getScalarCollection(appId, scalarName).find(query).explain());
//        System.out.println(getScalarCollection(appId, scalarName).find(query).sort(MongoDbHelper.createSortSpec(FIELD_NAME_TIMESTAMP, FIELD_NAME_INSTANCE)).explain());

        //TODO sort
        //TODO verify that the index is actually used

        final List<ScalarDataPoint> result = new ArrayList<>();
        for (DBObject o: getScalarCollection(appId, scalarName).find(query).sort(MongoDbHelper.createSortSpec(FIELD_NAME_TIMESTAMP, FIELD_NAME_INSTANCE))) {
            result.add(fromDbObject(o));
        }
        return result;
    }

    /**
     * Calling this method is not obligatory since MongoDB creates collections when they are first accessed. This method
     *  however creates some useful indices for collections that do not yet exist, and logs warnings about missing
     *  indices and stuff for collections that already exist.<p />
     *
     * This method does *not* itself create indices for pre-existing collections. This is a conscious trade-off because
     *  creating indices can take significant time and, worse, significantly impact read and write performance during
     *  that time.
     */
    public void initSchema() {
        final List<String> monitoredApplications = monitoredApplicationDao.getMonitoredApplicationNames();
        final Collection<ScalarMetaData> scalarMetaData = scalarMetaDataDao.getAll().values();

        for(ScalarMetaData scalar: scalarMetaData) {
            for(String appId: monitoredApplications) {
                final DBCollection coll = getScalarCollection(appId, scalar.name);
                if(coll.count() == 0) {
                    final BasicDBObject idx = new BasicDBObject("timestamp", 1);
                    idx.append("instance", 1);
                    coll.ensureIndex(idx, INDEX_NAME_SCALAR_INSTANCE_AND_TIME);
                    //TODO check if an update is necessary, and if so, log at info level
                }
                else {
                    //TODO coll.getIndexInfo() and warn
                }
            }
        }
    }
}

package com.ajjpj.asysmon.server.storage.impl;

import com.ajjpj.asysmon.server.storage.MonitoredApplicationDao;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the DAO for meta data about monitored applications. The schema is as follows:
 *
 * <ul>
 *     <li>_id: name of the monitored application</li>
 * </ul>
 * @author arno
 */
@Singleton
public class MonitoredApplicationDaoImpl implements MonitoredApplicationDao {
    /**
     * the collection with information about all monitored applications.
     */
    public static final String COLL_APPLICATION_META_DATA = "application-meta-data";

    private final DB db;

    @Inject
    public MonitoredApplicationDaoImpl(DB db) {
        this.db = db;
    }

    public List<String> getMonitoredApplicationNames() {
        final List<String> result = new ArrayList<>();

        final DBCursor monitoredApplications = db.getCollection(COLL_APPLICATION_META_DATA).find();
        while(monitoredApplications.hasNext()) {
            result.add((String) monitoredApplications.next().get("_id"));
        }
        return result;
    }

    public List<DBObject> getMonitoredApplicationsRaw() {
        final List<DBObject> result = new ArrayList<>();

        final DBCursor monitoredApplications = db.getCollection(COLL_APPLICATION_META_DATA).find();
        while(monitoredApplications.hasNext()) {
            result.add(monitoredApplications.next());
        }
        return result;
    }
}

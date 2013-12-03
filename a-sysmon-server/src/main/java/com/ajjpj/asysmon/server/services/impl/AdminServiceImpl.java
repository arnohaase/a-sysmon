package com.ajjpj.asysmon.server.services.impl;

import com.ajjpj.asysmon.server.services.AdminService;
import com.ajjpj.asysmon.server.storage.MonitoredApplicationDao;
import com.ajjpj.asysmon.server.storage.ScalarMetaData;
import com.ajjpj.asysmon.server.storage.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.util.AOption;
import com.ajjpj.asysmon.server.util.json.ListWrapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
@Singleton
@Path("/admin")
public class AdminServiceImpl implements AdminService {
    public static final long SCALAR_META_REFRESH_INTERVAL_MILLIS = 10*1000;

    public static final String INDEX_NAME_SCALAR_INSTANCE_AND_TIME = "instance-and-time";

    private final MonitoredApplicationDao monitoredApplicationDao;
    private final ScalarMetaDataDao scalarMetaDataDao;

    private volatile Map<String, ScalarMetaData> scalarMetaData = new HashMap<>();
    private volatile long scalarMetaDataLastRefreshed = 0;

    @Inject
    public AdminServiceImpl(MonitoredApplicationDao monitoredApplicationDao, ScalarMetaDataDao scalarMetaDataDao) {
        this.monitoredApplicationDao = monitoredApplicationDao;
        this.scalarMetaDataDao = scalarMetaDataDao;
    }

    @GET
    @Path("allApps")
    @Produces(MediaType.APPLICATION_JSON)
    public ListWrapper<String> getMonitoredApplicationNames() {
        return new ListWrapper<String>(monitoredApplicationDao.getMonitoredApplicationNames());
    }

    private void checkRefresh() {
        final long now = System.currentTimeMillis();
        if(now < scalarMetaDataLastRefreshed + SCALAR_META_REFRESH_INTERVAL_MILLIS) {
            return;
        }

        scalarMetaDataLastRefreshed = now;
        scalarMetaData = scalarMetaDataDao.getAll();
    }

    public AOption<ScalarMetaData> get(String name) {
        checkRefresh();
        return AOption.fromNullable(scalarMetaData.get(name));
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
        //TODO move this code to scalar dao

//        final List<String> monitoredApplications = monitoredApplicationDao.getMonitoredApplicationNames();
//        final Collection<ScalarMetaData> scalarMetaDatas = scalarMetaDataDao.getAll().values();
//
//        for(ScalarMetaData scalar: scalarMetaDatas) {
//            for(String appId: monitoredApplications) {
//                final DBCollection coll = getScalarCollection(appId, scalar.name);
//                if(coll.count() == 0) {
//                    final BasicDBObject idx = new BasicDBObject("instance", 1);
//                    idx.append("timestamp", 1);
//                    coll.ensureIndex(idx, INDEX_NAME_SCALAR_INSTANCE_AND_TIME);
//                    //TODO check if an update is necessary, and if so, log at info level
//                }
//                else {
//                    //TODO coll.getIndexInfo() and warn
//                }
//            }
//        }
    }
}

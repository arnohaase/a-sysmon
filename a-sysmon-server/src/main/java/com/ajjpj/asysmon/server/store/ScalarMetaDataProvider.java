package com.ajjpj.asysmon.server.store;


import com.ajjpj.asysmon.server.store.backend.ScalarMetaData;
import com.ajjpj.asysmon.server.store.backend.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.util.AOption;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides cached read access to meta data for scalar measurements
 *
 * @author arno
 */
public class ScalarMetaDataProvider {
    public static final long REFRESH_INTERVAL_MILLIS = 10*1000;

    private final ScalarMetaDataDao dao;

    private volatile Map<String, ScalarMetaData> data = new HashMap<>();
    private volatile long lastRefreshed = 0;

    public ScalarMetaDataProvider(ScalarMetaDataDao dao) {
        this.dao = dao;
        checkRefresh();
    }

    private void checkRefresh() {
        final long now = System.currentTimeMillis();
        if(now < lastRefreshed + REFRESH_INTERVAL_MILLIS) {
            return;
        }

        lastRefreshed = now;
        data = dao.getAll();
    }

    public AOption<ScalarMetaData> get(String name) {
        checkRefresh();
        return AOption.fromNullable(data.get(name));
    }
}

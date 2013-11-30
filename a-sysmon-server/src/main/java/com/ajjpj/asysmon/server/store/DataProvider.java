package com.ajjpj.asysmon.server.store;

import com.ajjpj.asysmon.server.store.backend.ScalarDataPoint;

import java.util.List;

/**
 * @author arno
 */
public interface DataProvider {
    public List<ScalarDataPoint> findAll(String appId, String scalarName, long fromTimestamp, long toTimestamp, String... instanceIds);
}

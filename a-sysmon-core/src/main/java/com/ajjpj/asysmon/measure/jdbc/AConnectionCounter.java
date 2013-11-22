package com.ajjpj.asysmon.measure.jdbc;


import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.global.AScalarMeasurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AConnectionCounter implements AScalarMeasurer {
    public static final AConnectionCounter INSTANCE = new AConnectionCounter(); //TODO make instance management configurable

    private static final String DEFAULT_POOL_IDENTIFIER = " @@##++ ";
    private final Map<String, AtomicInteger> openPerConnectionPool = new ConcurrentHashMap<String, AtomicInteger>();
    private final Map<String, AtomicInteger> activePerConnectionPool = new ConcurrentHashMap<String, AtomicInteger>();

    public void onOpenConnection(String qualifier) {
        getCounter(qualifier, openPerConnectionPool).incrementAndGet();
    }

    public void onActivateConnection(String qualifier) {
        getCounter(qualifier, activePerConnectionPool).incrementAndGet();
    }

    private AtomicInteger getCounter(String qualifier, Map<String, AtomicInteger> map) {
        if(qualifier == null) {
            qualifier = DEFAULT_POOL_IDENTIFIER;
        }
        AtomicInteger result = map.get(qualifier);
        if(result == null) {
            synchronized (map) {
                result = map.get(qualifier);
                if(result == null) {
                    result = new AtomicInteger(0);
                    map.put(qualifier, result);
                }
            }
        }
        return result;
    }

    public void onCloseConnection(String qualifier) {
        getCounter(qualifier, openPerConnectionPool).decrementAndGet();
    }

    public void onPassivateConnection(String qualifier) {
        getCounter(qualifier, activePerConnectionPool).decrementAndGet();
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp) {
        for(String key: openPerConnectionPool.keySet()) {
            final String ident = (DEFAULT_POOL_IDENTIFIER == key) ? "Open JDBC Connections" : ("Open JDBC Connections (" + key + ")");
            data.put(ident, new AScalarDataPoint(timestamp, ident, openPerConnectionPool.get(key).get(), 0));
        }
        for(String key: activePerConnectionPool.keySet()) {
            final String ident = (DEFAULT_POOL_IDENTIFIER == key) ? "Active JDBC Connections" : ("Active JDBC Connections (" + key + ")");
            data.put(ident, new AScalarDataPoint(timestamp, ident, activePerConnectionPool.get(key).get(), 0));
        }
    }

    @Override public void shutdown() {
    }
}

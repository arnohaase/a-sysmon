package com.ajjpj.asysmon.measure.jdbc;


import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.measure.global.AGlobalMeasurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public class AConnectionCounter implements AGlobalMeasurer {
    public static final AConnectionCounter INSTANCE = new AConnectionCounter(); //TODO make instance management configurable

    private static final String DEFAULT_POOL_IDENTIFIER = " @@##++ ";
    private final Map<String, AtomicInteger> perConnectionPool = new ConcurrentHashMap<String, AtomicInteger>();

    public void onOpenConnection(String qualifier) {
        getCounter(qualifier).incrementAndGet();
    }

    private AtomicInteger getCounter(String qualifier) {
        if(qualifier == null) {
            qualifier = DEFAULT_POOL_IDENTIFIER;
        }
        AtomicInteger result = perConnectionPool.get(qualifier);
        if(result == null) {
            synchronized (perConnectionPool) {
                result = perConnectionPool.get(qualifier);
                if(result == null) {
                    result = new AtomicInteger(0);
                    perConnectionPool.put(qualifier, result);
                }
            }
        }
        return result;
    }

    public void onCloseConnection(String qualifier) {
        getCounter(qualifier).decrementAndGet();
    }

    @Override public void contributeMeasurements(Map<String, AGlobalDataPoint> data) {
        for(String key: perConnectionPool.keySet()) {
            final String ident = (DEFAULT_POOL_IDENTIFIER == key) ? "Default Connection Pool" : ("Connection Pool " + key);
            data.put(ident, new AGlobalDataPoint(ident, perConnectionPool.get(key).get(), 0));
        }
    }
}

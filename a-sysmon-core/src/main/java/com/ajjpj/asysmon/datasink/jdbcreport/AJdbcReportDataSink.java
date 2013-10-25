package com.ajjpj.asysmon.datasink.jdbcreport;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.jdbc.ASysMonStatement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author arno
 */
public class AJdbcReportDataSink implements ADataSink {
    private final Map<String, AtomicReference<ATotalAndNum>> entryPoints = new ConcurrentHashMap<String, AtomicReference<ATotalAndNum>>();
    private final Map<String, AJdbcStatementData> byJdbcStatement = new ConcurrentHashMap<String, AJdbcStatementData>();

    private volatile boolean isActive = true; // TODO change default to 'false'

    @Override public void onStartedHierarchicalMeasurement() {
    }

    public Map<String, AtomicReference<ATotalAndNum>> getEntryPoints() {
        return entryPoints;
    }

    public Map<String, AJdbcStatementData> getByJdbcStatement() {
        return byJdbcStatement;
    }

    public void clear() {
        entryPoints.clear();
        byJdbcStatement.clear();
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public boolean isActive() {
        return this.isActive;
    }

    private static ATotalAndNum updateAndGet(Map<String, AtomicReference<ATotalAndNum>> map, String identifier, long newValue) {
        AtomicReference<ATotalAndNum> ref = map.get(identifier);

        if(ref != null) {
            ATotalAndNum prevData;
            do {
                prevData = ref.get();
            }
            while(! ref.compareAndSet(prevData, prevData.withNewValue(newValue)));
            return ref.get();
        }
        else {
            // double checked locking
            synchronized(map) {
                ref = map.get(identifier);
                if(ref == null) {
                    final ATotalAndNum result = new ATotalAndNum(newValue);
                    map.put(identifier, new AtomicReference<ATotalAndNum>(result));
                    return result;
                }
                else {
                    ATotalAndNum prevData;
                    do {
                        prevData = ref.get();
                    }
                    while(! ref.compareAndSet(prevData, prevData.withNewValue(newValue)));
                    return ref.get();
                }
            }
        }
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalData data) {
        if(!isActive) {
            return;
        }

        final ATotalAndNum entryPoint = updateAndGet(entryPoints, data.getIdentifier(), data.getDurationNanos());
        registerJdbcRec(data, data.getIdentifier());
    }

    private boolean isJdbc(AHierarchicalData data) {
        return data.getIdentifier().startsWith(ASysMonStatement.IDENT_PREFIX_JDBC);
    }

    //TODO percentage values for JDBC / entry point (both ways)
    private void doRegisterJdbc(AHierarchicalData jdbcData, String entryPointIdentifier) {
        AJdbcStatementData jdbcStatementData = byJdbcStatement.get(jdbcData.getIdentifier());
        if(jdbcStatementData == null) {
            // double checked locking
            synchronized (byJdbcStatement) {
                jdbcStatementData = byJdbcStatement.get(jdbcData.getIdentifier());
                if(jdbcStatementData == null) {
                    byJdbcStatement.put(jdbcData.getIdentifier(), new AJdbcStatementData(jdbcData.getIdentifier(), jdbcData.getDurationNanos()));
                }
                else {
                    jdbcStatementData.getTotalNanos().addAndGet(jdbcData.getDurationNanos());
                    updateAndGet(jdbcStatementData.getByEntryPoint(), entryPointIdentifier, jdbcData.getDurationNanos());
                }
            }
        }
        else {
            jdbcStatementData.getTotalNanos().addAndGet(jdbcData.getDurationNanos());
            updateAndGet(jdbcStatementData.getByEntryPoint(), entryPointIdentifier, jdbcData.getDurationNanos());
        }
    }

    private void registerJdbcRec(AHierarchicalData curData, String entryPointIdentifier) {
        if(isJdbc(curData)) {
            doRegisterJdbc(curData, entryPointIdentifier);
        }
        else {
            for(AHierarchicalData child: curData.getChildren()) {
                registerJdbcRec(child, entryPointIdentifier);
            }
        }
    }
}

package com.ajjpj.asysmon.server.store;


import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import org.apache.log4j.Logger;

/**
 * @author arno
 */
public class DataStoreImpl implements DataProvider, DataPersister {
    private static final Logger log = Logger.getLogger(DataStoreImpl.class);

    @Override public void storeScalarData(ScalarNode scalarData) {
        log.debug("storing " + scalarData);
    }

    @Override public void storeTraceData(TraceRootNode traceData) {
        log.debug("storing " + traceData);
    }

    @Override public void storeEnvironmentData(EnvironmentNode environmentData) {
        log.debug("storing " + environmentData);
    }
}

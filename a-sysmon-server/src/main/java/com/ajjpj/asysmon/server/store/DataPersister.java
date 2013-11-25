package com.ajjpj.asysmon.server.store;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;


/**
 * @author arno
 */
public interface DataPersister {
    public void storeScalarData(ScalarNode scalarData);
    public void storeTraceData(TraceRootNode traceData);
    public void storeEnvironmentData(EnvironmentNode environmentData);
}

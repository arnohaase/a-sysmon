package com.ajjpj.asysmon.server.processing;

import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;


/**
 * @author arno
 */
public interface NewDataListener {
    void onNewScalarData(ScalarNode scalarData);
    void onNewTrace(TraceRootNode traceData);
    void onNewEnvironmentData(EnvironmentNode environmentData);
}

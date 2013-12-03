package com.ajjpj.asysmon.server.processing;


import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;

/**
 * This event eventbus is the back bone of the A-SysMon server. All the different parts communicate through this eventbus, making
 *  the overall architecture flexible and extensible.<p />
 *
 * Notification of listeners is done <em>synchronously</em>, i.e. it is done in the same thread and before the
 *  corresponding fire... method returns. Listener implementations are expected to return quickly, and if there is
 *  non-trivial work to be done, offload that work to a different thread.<p />
 *
 * NB: listeners will often be called with a large number of concurrent events, making every listener a potential
 *  global bottleneck. Listener implementations must therefore take care to scale very well!
 *
 * //TODO exception model - may listeners throw?
 *
 * @author arno
 */
public interface EventBus {
    void addListener(NewDataListener l);
    void removeListener(NewDataListener l);

    void fireNewScalarData(ScalarNode scalarData);
    void fireNewTrace(TraceRootNode traceData);
    void fireNewEnvironmentData(EnvironmentNode environmentData);
}

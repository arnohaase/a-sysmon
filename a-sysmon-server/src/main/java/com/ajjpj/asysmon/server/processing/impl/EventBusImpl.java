package com.ajjpj.asysmon.server.processing.impl;


import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import com.ajjpj.asysmon.server.processing.NewDataListener;
import com.ajjpj.asysmon.server.processing.EventBus;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author arno
 */
public class EventBusImpl implements EventBus {
    private final Collection<NewDataListener> listeners = new CopyOnWriteArrayList<>();

    @Override public void addListener(NewDataListener l) {
        listeners.add(l);
    }

    @Override public void removeListener(NewDataListener l) {
        listeners.remove(l);
    }

    @Override public void fireNewScalarData(ScalarNode scalarData) {
        for(NewDataListener l: listeners) {
            l.onNewScalarData(scalarData);
        }
    }

    @Override public void fireNewTrace(TraceRootNode traceData) {
        for(NewDataListener l: listeners) {
            l.onNewTrace(traceData);
        }
    }

    @Override public void fireNewEnvironmentData(EnvironmentNode environmentData) {
        for(NewDataListener l: listeners) {
            l.onNewEnvironmentData(environmentData);
        }
    }
}

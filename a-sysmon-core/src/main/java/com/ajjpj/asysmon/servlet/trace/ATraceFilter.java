package com.ajjpj.asysmon.servlet.trace;

import com.ajjpj.asysmon.data.AHierarchicalDataRoot;

/**
 * @author arno
 */
public interface ATraceFilter {
    String getId();
    String getShortLabel();
    String getFullLabel();

    boolean shouldCollect(AHierarchicalDataRoot trace);

    ATraceFilter ALL = new ATraceFilter() {
        @Override public String getId() {
            return "tracesAll";
        }

        @Override public String getShortLabel() {
            return "All Traces";
        }

        @Override public String getFullLabel() {
            return "All Traces";
        }

        @Override public boolean shouldCollect(AHierarchicalDataRoot trace) {
            return true;
        }
    };
}

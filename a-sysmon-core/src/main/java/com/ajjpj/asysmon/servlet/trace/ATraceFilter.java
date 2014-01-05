package com.ajjpj.asysmon.servlet.trace;

import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.measure.http.ASimpleHttpRequestAnalyzer;

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

    ATraceFilter HTTP = new ATraceFilter() {
        @Override public String getId() {
            return "tracesHttp";
        }

        @Override public String getShortLabel() {
            return "HTTP Requests";
        }

        @Override public String getFullLabel() {
            return "Traces for all HTTP requests";
        }

        @Override  public boolean shouldCollect(AHierarchicalDataRoot trace) {
            return trace.getRootNode().getParameters().containsKey(ASimpleHttpRequestAnalyzer.PARAM_FULL_URL);
        }
    };
}

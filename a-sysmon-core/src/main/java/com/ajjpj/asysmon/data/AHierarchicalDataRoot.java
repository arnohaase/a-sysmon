package com.ajjpj.asysmon.data;

import com.ajjpj.afoundation.util.AUUID;

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author arno
 */
public class AHierarchicalDataRoot {
    private final AUUID uuid = AUUID.createRandom();
    private final Collection<ACorrelationId> startedFlows;
    private final Collection<ACorrelationId> joinedFlows;
    private final AHierarchicalData root;

    public AHierarchicalDataRoot(AHierarchicalData root, Collection<ACorrelationId> startedFlows, Collection<ACorrelationId> joinedFlows) {
        this.startedFlows = new ArrayList<ACorrelationId>(startedFlows);
        this.joinedFlows = new ArrayList<ACorrelationId>(joinedFlows);
        this.root = root;
    }

    public AUUID getUuid() {
        return uuid;
    }

    public Collection<ACorrelationId> getStartedFlows() {
        return startedFlows;
    }

    public Collection<ACorrelationId> getJoinedFlows() {
        return joinedFlows;
    }

    public AHierarchicalData getRootNode() {
        return root;
    }

    @Override
    public String toString() {
        return "AHierarchicalDataRoot{" +
                "startedFlows=" + startedFlows +
                ", joinedFlows=" + joinedFlows +
                ", root=" + root +
                '}';
    }
}

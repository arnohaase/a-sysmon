package com.ajjpj.asysmon.servlet.trace;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.servlet.performance.AAbstractAsysmonPerformancePageDef;

import java.util.*;

/**
 * @author arno
 */
public class ATracePageDefinition extends AAbstractAsysmonPerformancePageDef {
    private final ATraceFilter filter;
    private final ATraceCollectingDataSink collector;

    private static final List<ColDef> colDefs = Arrays.asList(
            new ColDef("%", true, 1, ColWidth.Medium),
            new ColDef("total µs", false, 0, ColWidth.Long),
            new ColDef("self µs", false, 0, ColWidth.Long),
            new ColDef("start @", false, 0, ColWidth.Long)
    );


    public ATracePageDefinition(ATraceFilter traceFilter, int bufferSize) {
        this.filter = traceFilter;
        this.collector = new ATraceCollectingDataSink(traceFilter, bufferSize);
    }

    @Override public void init(ASysMon sysMon) {
        super.init(sysMon);
        ASysMonConfigurer.addDataSink(sysMon, collector);
    }

    @Override public String getId() {
        return filter.getId();
    }

    @Override public String getShortLabel() {
        return filter.getShortLabel();
    }

    @Override public String getFullLabel() {
        return filter.getFullLabel();
    }

    @Override protected void doStartMeasurements() {
        collector.isStarted = true;
    }

    @Override protected void doStopMeasurements() {
        collector.isStarted = false;
    }

    @Override protected void doClearMeasurements() {
        collector.clear();
    }

    @Override protected boolean isStarted() {
        return collector.isStarted;
    }

    @Override protected List<ColDef> getColDefs() {
        return colDefs;
    }

    @Override protected List<TreeNode> getData() {
        final List<TreeNode> result = new ArrayList<TreeNode>();

        for(AHierarchicalDataRoot root: collector.getData()) {
            result.add(asTreeNode(root.getRootNode(), root.getUuid().toString(), System.currentTimeMillis(), root.getRootNode().getDurationNanos(), 0));
        }

        Collections.sort(result, new Comparator<TreeNode>() {
            @Override public int compare(TreeNode o1, TreeNode o2) {
                return (int) (o2.colDataRaw[3] - o1.colDataRaw[3]);
            }
        });

        return result;
    }

    private TreeNode asTreeNode(AHierarchicalData node, String id, long now, long parentNanos, int level) {
        final List<TreeNode> children = new ArrayList<TreeNode>();
        long selfNanos = node.getDurationNanos();

        final long childNow = level > 0 ? now : node.getStartTimeMillis();

        int i=0;
        for(AHierarchicalData child: node.getChildren()) {
            if(child.isSerial()) {
                selfNanos -= child.getDurationNanos();
            }
            children.add(asTreeNode(child, String.valueOf(i), childNow, node.getDurationNanos(), level+1));
            i++;
        }

        if(selfNanos < 0) selfNanos = 0;
        if(selfNanos > node.getDurationNanos()) selfNanos = node.getDurationNanos();

        if(selfNanos != 0 && children.size() > 0) {
            children.add(0, new TreeNode("<self>", true, new long[] {selfNanos * 1000 / node.getDurationNanos(), selfNanos / 1000, selfNanos / 1000, node.getStartTimeMillis() - childNow}, Collections.<TreeNode>emptyList()));
        }


        final long[] colDataRaw = new long[] {
                node.getDurationNanos() * 100 * 10 / parentNanos, // 100 for '%', 10 for 1 frac digit
                node.getDurationNanos() / 1000,
                selfNanos / 1000,
                node.getStartTimeMillis() - now
        };

        return new TreeNode(id, node.getIdentifier(), node.isSerial(), colDataRaw, children);
    }
}

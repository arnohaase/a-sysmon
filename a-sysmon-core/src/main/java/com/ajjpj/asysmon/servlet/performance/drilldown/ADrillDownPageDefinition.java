package com.ajjpj.asysmon.servlet.performance.drilldown;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.impl.ASysMonConfigurer;
import com.ajjpj.asysmon.servlet.performance.AAbstractAsysmonPerformancePageDef;
import com.ajjpj.asysmon.servlet.performance.AMinMaxAvgData;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class ADrillDownPageDefinition extends AAbstractAsysmonPerformancePageDef {
    private static final int MILLION = 1000*1000;

    private static final List<ColDef> colDefs = Arrays.asList(
            new ColDef("%", true, 1, ColWidth.Medium),
            new ColDef("#", false, 2, ColWidth.Long),
            new ColDef("total", false, 0, ColWidth.Long),
            new ColDef("avg", false, 0, ColWidth.Medium),
            new ColDef("min", false, 0, ColWidth.Medium),
            new ColDef("max", false, 0, ColWidth.Medium)
    );

    private volatile DrillDownDataSink collector;

    @Override public String getId() {
        return "drilldown";
    }

    @Override public String getShortLabel() {
        return "Hierarchy";
    }

    @Override public String getFullLabel() {
        return "Hierarchical Performance Statistics";
    }

    @Override public void init(ASysMonApi sysMon) {
        super.init(sysMon);

        collector = new DrillDownDataSink();
        ASysMonConfigurer.addDataSink(sysMon, collector);
    }


    @Override protected boolean isStarted() {
        return collector.isActive();
    }

    @Override protected void doStartMeasurements() {
        collector.setActive(true);
    }

    @Override protected void doStopMeasurements() {
        collector.setActive(false);
    }

    @Override protected void doClearMeasurements() {
        collector.clear();
    }

    @Override protected List<ColDef> getColDefs() {
        return colDefs;
    }

    @Override protected List<TreeNode> getData() {
        long totalNanos = 0;
        for(AMinMaxAvgData d: collector.getData().values()) {
            totalNanos += d.getTotalNanos();
        }

        return getDataRec(collector.getData(), 0, totalNanos, 1);
    }

    private List<TreeNode> getDataRec(Map<String, AMinMaxAvgData> map, long parentSelfNanos, double parentTotalNanos, int numParentCalls) {
        final List<TreeNode> result = new ArrayList<TreeNode>();
        for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(map, parentSelfNanos, numParentCalls)) {
            final AMinMaxAvgData inputData = entry.getValue();

            double fractionOfParent = inputData.getTotalNanos() / parentTotalNanos;

            long selfNanos = inputData.getTotalNanos();
            for(AMinMaxAvgData childData: inputData.getChildren().values()) {
                if(childData.isSerial()) {
                    selfNanos -= childData.getTotalNanos();
                }
            }

            final long[] dataRaw = new long[] {
                    (long)(100 * 10 * fractionOfParent),
                    (long)(100 * inputData.getTotalNumInContext() / numParentCalls),
                    inputData.getTotalNanos() / MILLION,
                    inputData.getAvgNanos() / MILLION,
                    inputData.getMinNanos() / MILLION,
                    inputData.getMaxNanos() / MILLION
            };

            result.add(new TreeNode(entry.getKey(), inputData.isSerial(), dataRaw, getDataRec(inputData.getChildren(), selfNanos, inputData.getTotalNanos(), inputData.getTotalNumInContext())));
        }
        return result;
    }

    private List<Map.Entry<String, AMinMaxAvgData>> getSorted(Map<String, AMinMaxAvgData> raw, long selfNanos, int numParent) {
        final List<Map.Entry<String, AMinMaxAvgData>> result = new ArrayList<Map.Entry<String, AMinMaxAvgData>>(raw.entrySet());

        if(selfNanos != 0 && !raw.isEmpty()) {
            final AMinMaxAvgData selfData = new AMinMaxAvgData(true, numParent, 0, 0, selfNanos / numParent, selfNanos, new ConcurrentHashMap<String, AMinMaxAvgData>());
            result.add(new AbstractMap.SimpleEntry<String, AMinMaxAvgData>("<self>", selfData));
        }

        Collections.sort(result, new Comparator<Map.Entry<String, AMinMaxAvgData>>() {
            @Override public int compare(Map.Entry<String, AMinMaxAvgData> o1, Map.Entry<String, AMinMaxAvgData> o2) {
                final long delta = o2.getValue().getTotalNanos() - o1.getValue().getTotalNanos();
                if(delta > 0) {
                    return 1;
                }
                if(delta < 0) {
                    return -1;
                }
                return Collator.getInstance().compare(o1.getKey(), o2.getKey());
            }
        });
        return result;
    }
}

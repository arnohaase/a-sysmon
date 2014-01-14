package com.ajjpj.asysmon.servlet.performance.bottomup;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.impl.ASysMonConfigurer;
import com.ajjpj.asysmon.servlet.performance.AAbstractAsysmonPerformancePageDef;
import com.ajjpj.asysmon.servlet.performance.AMinMaxAvgData;

import java.text.Collator;
import java.util.*;

/**
 * @author arno
 */
public abstract class ABottomUpPageDefinition extends AAbstractAsysmonPerformancePageDef {
    private volatile ABottomUpDataSink collector;

    public static final List<ColDef> COL_DEFS = Arrays.asList(
            new ColDef("%", true, 1, ColWidth.Medium),
            new ColDef("%local", false, 1, ColWidth.Medium),
            new ColDef("#calls", false, 0, ColWidth.Medium),
            new ColDef("avg", false, 0, ColWidth.Medium),
            new ColDef("min", false, 0, ColWidth.Medium),
            new ColDef("max", false, 0, ColWidth.Medium)
    );

    public static final int MILLION = 1000*1000;

    protected abstract ABottomUpLeafFilter createLeafFilter();

    @Override public void init(ASysMonApi sysMon) {
        super.init(sysMon);
        collector = new ABottomUpDataSink(createLeafFilter());
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
        return COL_DEFS;
    }

    @Override protected List<TreeNode> getData() {
        long totalJdbcNanos = 0;
        int totalJdbcCalls = 0;
        for(AMinMaxAvgData d: collector.getData().values()) {
            totalJdbcNanos += d.getTotalNanos();
            totalJdbcCalls += d.getTotalNumInContext();
        }

        return getDataRec(collector.getData(), 0, totalJdbcNanos, totalJdbcNanos, totalJdbcCalls);
    }

    private List<TreeNode> getDataRec(Map<String, AMinMaxAvgData> map, int level, double jdbcTimeInParent, double totalJdbcTime, int totalNumCallsInContext) {
        final List<TreeNode> result = new ArrayList<TreeNode>();

        for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(map, level == 0)) {
            final AMinMaxAvgData inputData = entry.getValue();

            final double jdbcTimeHere = (level == 0) ? inputData.getTotalNanos() : jdbcTimeInParent * inputData.getTotalNumInContext() / totalNumCallsInContext;
            final double timeFracLocal = jdbcTimeHere / jdbcTimeInParent;
            final double timeFracGlobal = jdbcTimeHere / totalJdbcTime;

            final long[] dataRaw = new long[] {
                    (long)(timeFracGlobal * 100 * 10),
                    (long)(timeFracLocal * 100 * 10),
                    inputData.getTotalNumInContext(),
                    inputData.getAvgNanos() / MILLION,
                    inputData.getMinNanos() / MILLION,
                    inputData.getMaxNanos() / MILLION
            };

            int totalChildCalls = 0;
            for(AMinMaxAvgData childData: inputData.getChildren().values()) {
                totalChildCalls += childData.getTotalNumInContext();
            }

            result.add(new TreeNode(entry.getKey(), inputData.isSerial(), dataRaw, getDataRec(inputData.getChildren(), level+1, jdbcTimeHere, totalJdbcTime, totalChildCalls)));
        }

        return result;
    }

    private List<Map.Entry<String, AMinMaxAvgData>> getSorted(Map<String, AMinMaxAvgData> raw, final boolean rootLevel) {
        final List<Map.Entry<String, AMinMaxAvgData>> result = new ArrayList<Map.Entry<String, AMinMaxAvgData>>(raw.entrySet());

        Collections.sort(result, new Comparator<Map.Entry<String, AMinMaxAvgData>>() {
            @Override
            public int compare(Map.Entry<String, AMinMaxAvgData> o1, Map.Entry<String, AMinMaxAvgData> o2) {
                final long delta = rootLevel ? (o2.getValue().getTotalNanos() - o1.getValue().getTotalNanos()) : (o2.getValue().getTotalNumInContext() - o1.getValue().getTotalNumInContext());
                if (delta > 0) {
                    return 1;
                }
                if (delta < 0) {
                    return -1;
                }
                return Collator.getInstance().compare(o1.getKey(), o2.getKey());
            }
        });
        return result;
    }
}

package com.ajjpj.asysmon.datasink.aggregation.minmaxavg;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;
import com.ajjpj.asysmon.datasink.aggregation.AbstractDynamicAsysmonServlet;

import javax.servlet.ServletException;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author arno
 */
public class ADynamicMinMaxAvgServlet extends AbstractDynamicAsysmonServlet {
    private static final int MILLION = 1000*1000;

    private static final List<ColDef> colDefs = Arrays.asList(
            new ColDef("%",     true,  1, ColWidth.Medium),
            new ColDef("#",     false, 2, ColWidth.Medium),
            new ColDef("total", false, 0, ColWidth.Long),
            new ColDef("avg",   false, 0, ColWidth.Medium),
            new ColDef("min",   false, 0, ColWidth.Medium),
            new ColDef("max",   false, 0, ColWidth.Medium)
    );


    private static volatile AMinMaxAvgDataSink collector;



    /**
     * Override to customize initialization (and potentially registration) of the collector.
     */
    @Override public void init() throws ServletException {
        synchronized (AMinMaxAvgServlet.class) {
            if(collector == null) {
                collector = createCollector();
                ASysMonConfigurer.addDataSink(getSysMon(), collector);
            }
        }
    }

    /**
     * Override to customize.
     */
    protected AMinMaxAvgDataSink createCollector() {
        return new AMinMaxAvgDataSink();
    }


    @Override
    protected String getTitle() {
        return "A-SysMon performance min / avg / max";
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

        return getDataRec(collector.getData(), totalNanos, 1);
    }

    private List<TreeNode> getDataRec(Map<String, AMinMaxAvgData> map, double parentTotalNanos, double numParentCalls) {
        final List<TreeNode> result = new ArrayList<TreeNode>();
        for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(map, 0, 0)) {
            final AMinMaxAvgData rawData = entry.getValue();

            double fractionOfParent = rawData.getTotalNanos() / parentTotalNanos;

            long selfNanos = rawData.getTotalNanos();
            for(AMinMaxAvgData childData: rawData.getChildren().values()) {
                if(childData.isSerial()) {
                    selfNanos -= childData.getTotalNanos();
                }
            }

            final long[] dataRaw = new long[] {
                    (long)(100 * 10 * fractionOfParent),
                    (long)(100 * rawData.getTotalNumInContext() / numParentCalls),
                    rawData.getTotalNanos() / MILLION,
                    rawData.getAvgNanos() / MILLION,
                    rawData.getMinNanos() / MILLION,
                    rawData.getMaxNanos() / MILLION
            };

            result.add(new TreeNode(entry.getKey(), rawData.isSerial(), dataRaw, getDataRec(rawData.getChildren(), rawData.getTotalNanos(), rawData.getTotalNumInContext())));
        }
        return result;
    }

    //TODO move sorting to browser?
    private List<Map.Entry<String, AMinMaxAvgData>> getSorted(Map<String, AMinMaxAvgData> raw, long selfNanos, int numParent) {
        final List<Map.Entry<String, AMinMaxAvgData>> result = new ArrayList<Map.Entry<String, AMinMaxAvgData>>(raw.entrySet());

        if(selfNanos != 0) {
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

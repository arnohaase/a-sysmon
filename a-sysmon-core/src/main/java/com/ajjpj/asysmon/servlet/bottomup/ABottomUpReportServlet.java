package com.ajjpj.asysmon.servlet.bottomup;

import com.ajjpj.asysmon.servlet.AMinMaxAvgData;
import com.ajjpj.asysmon.servlet.AbstractAsysmonReportServlet;

import java.text.Collator;
import java.util.*;


/**
 * @author arno
 */
public abstract class ABottomUpReportServlet extends AbstractAsysmonReportServlet {
    public static final List<ColDef> COL_DEFS = Arrays.asList(
            new ColDef("%", true, 1, ColWidth.Medium),
            new ColDef("%local", false, 1, ColWidth.Medium),
            new ColDef("#calls", false, 0, ColWidth.Medium),
            new ColDef("avg", false, 0, ColWidth.Medium),
            new ColDef("min", false, 0, ColWidth.Medium),
            new ColDef("max", false, 0, ColWidth.Medium)
    );

    public static final int MILLION = 1000*1000;

    protected abstract ABottomUpDataSink getCollector();

    @Override protected boolean isStarted() {
        return getCollector().isActive();
    }

    @Override protected void doStartMeasurements() {
        getCollector().setActive(true);
    }

    @Override protected void doStopMeasurements() {
        getCollector().setActive(false);
    }

    @Override protected void doClearMeasurements() {
        getCollector().clear();
    }

    @Override protected List<ColDef> getColDefs() {
        return COL_DEFS;
    }

    @Override protected List<TreeNode> getData() {
        long totalJdbcNanos = 0;
        int totalJdbcCalls = 0;
        for(AMinMaxAvgData d: getCollector().getData().values()) {
            totalJdbcNanos += d.getTotalNanos();
            totalJdbcCalls += d.getTotalNumInContext();
        }

        return getDataRec(getCollector().getData(), 0, totalJdbcNanos, totalJdbcNanos, totalJdbcCalls);
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
            @Override public int compare(Map.Entry<String, AMinMaxAvgData> o1, Map.Entry<String, AMinMaxAvgData> o2) {
                final long delta = rootLevel ? (o2.getValue().getTotalNanos() - o1.getValue().getTotalNanos()) : (o2.getValue().getTotalNumInContext() - o1.getValue().getTotalNumInContext());
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

//
//    @Override protected void writeData(PrintWriter out) {
//        final IdGenerator idGenerator = new IdGenerator();
//
//        long totalJdbcNanos = 0;
//        int totalJdbcCalls = 0;
//        for(AMinMaxAvgData d: getCollector().getData().values()) {
//            totalJdbcNanos += d.getTotalNanos();
//            totalJdbcCalls += d.getTotalNumInContext();
//        }
//
//        for (Map.Entry<String, AMinMaxAvgData> entry: getSorted(getCollector().getData(), true)) {
//            out.println("<div class='table-header'>&nbsp;<div style=\"float: right;\">");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "%");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "%local");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "#calls");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "avg");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "min");
//            writeColumn(out, CSS_COLUMN_MEDIUM, "max");
//            out.println("</div></div>");
//
//            writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, 0, totalJdbcNanos, totalJdbcNanos, totalJdbcCalls);
//        }
//    }
//
//    private void writeNodeRec(PrintWriter out, String ident, AMinMaxAvgData data, IdGenerator idGenerator, int level, double jdbcTimeInParent, double totalJdbcTime, int totalNumCallsInContext) {
//        final boolean hasChildren = data.getChildren().size() > 0;
//        startNodeRow(out, idGenerator, ident, level, hasChildren, !data.isSerial());
//
//        final double jdbcTimeHere = (level == 0) ? data.getTotalNanos() : jdbcTimeInParent * data.getTotalNumInContext() / totalNumCallsInContext;
//        final double timeFracLocal = jdbcTimeHere / jdbcTimeInParent;
//        final double timeFracGlobal = jdbcTimeHere / totalJdbcTime;
//
//        writeColumn(out, CSS_COLUMN_MEDIUM, "background: " + greenToRed(timeFracGlobal), timeFracGlobal * 100, 2);
//        writeColumn(out, CSS_COLUMN_MEDIUM, timeFracLocal * 100, 2);
//        writeColumn(out, CSS_COLUMN_MEDIUM, data.getTotalNumInContext());
//        writeColumn(out, CSS_COLUMN_MEDIUM, data.getAvgNanos() / MILLION);
//        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMinNanos() / MILLION);
//        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMaxNanos() / MILLION);
//
//        nodeRowAfterColumns(out, idGenerator, ident, level, hasChildren, !data.isSerial());
//
//        if(! data.getChildren().isEmpty()) {
//            int totalChildCalls = 0;
//            for(AMinMaxAvgData childData: data.getChildren().values()) {
//                totalChildCalls += childData.getTotalNumInContext();
//            }
//
//            for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(data.getChildren(), false)) {
//                writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level + 1, jdbcTimeHere, totalJdbcTime, totalChildCalls);
////                writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level+1, totalNanos, data.getTotalNumInContext());
//            }
//        }
//        nodeRowAfterChildren(out, hasChildren);
//    }
//
}

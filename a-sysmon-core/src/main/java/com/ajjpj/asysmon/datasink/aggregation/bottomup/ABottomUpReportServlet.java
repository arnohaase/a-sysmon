package com.ajjpj.asysmon.datasink.aggregation.bottomup;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;
import com.ajjpj.asysmon.datasink.aggregation.AbstractAsysmonServlet;
import com.ajjpj.asysmon.util.APair;

import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.*;


/**
 * @author arno
 */
public abstract class ABottomUpReportServlet extends AbstractAsysmonServlet {
    private volatile ABottomUpDataSink collector;

    private static final int MILLION = 1000*1000;
    private ABottomUpLeafFilter leafFilter;

    /**
     * override to customize
     */
    @Override
    public void init() throws ServletException {
        synchronized (ABottomUpReportServlet.class) {
            collector = new ABottomUpDataSink(getLeafFilter());
            ASysMonConfigurer.addDataSink(getSysMon(), collector);
        }
    }

    public abstract ABottomUpLeafFilter getLeafFilter();

    /**
     * override to customize
     */
    protected ABottomUpDataSink getCollector() {
        return collector;
    }

    @SuppressWarnings("unchecked")
    @Override protected List<APair<String, String>> getCommands() {
        return Arrays.asList(
            new APair<String, String> ("Clear", "clear"),
            getCollector().isActive() ? new APair<String, String>("Stop", "stop") : new APair<String, String>("Start", "start")
        );
    }

    @Override protected void handleCommand(String cmd) {
        if("clear".equals(cmd)) {
            getCollector().clear();
        }
        else if("start".equals(cmd)) {
            getCollector().setActive(true);
        }
        else if("stop".equals(cmd)) {
            getCollector().setActive(false);
        }
    }

    @Override protected void writeData(PrintWriter out) {
        final IdGenerator idGenerator = new IdGenerator();

        long totalJdbcNanos = 0;
        int totalJdbcCalls = 0;
        for(AMinMaxAvgData d: getCollector().getData().values()) {
            totalJdbcNanos += d.getTotalNanos();
            totalJdbcCalls += d.getTotalNumInContext();
        }

        for (Map.Entry<String, AMinMaxAvgData> entry: getSorted(getCollector().getData(), true)) {
            out.println("<div class='table-header'>&nbsp;<div style=\"float: right;\">");
            writeColumn(out, CSS_COLUMN_MEDIUM, "%");
            writeColumn(out, CSS_COLUMN_MEDIUM, "%local");
            writeColumn(out, CSS_COLUMN_MEDIUM, "#calls");
            writeColumn(out, CSS_COLUMN_MEDIUM, "min");
            writeColumn(out, CSS_COLUMN_MEDIUM, "avg");
            writeColumn(out, CSS_COLUMN_MEDIUM, "max");
            out.println("</div></div>");

            writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, 0, totalJdbcNanos, totalJdbcNanos, totalJdbcCalls);
        }
    }

    private void writeNodeRec(PrintWriter out, String ident, AMinMaxAvgData data, IdGenerator idGenerator, int level, double jdbcTimeInParent, double totalJdbcTime, int totalNumCallsInContext) {
        final boolean hasChildren = data.getChildren().size() > 0;
        startNodeRow(out, idGenerator, ident, level, hasChildren, !data.isSerial());

        final double jdbcTimeHere = (level == 0) ? data.getTotalNanos() : jdbcTimeInParent * data.getTotalNumInContext() / totalNumCallsInContext;
        final double timeFracLocal = jdbcTimeHere / jdbcTimeInParent;
        final double timeFracGlobal = jdbcTimeHere / totalJdbcTime;

        writeColumn(out, CSS_COLUMN_MEDIUM, "background: " + greenToRed(timeFracGlobal), timeFracGlobal * 100, 2);
        writeColumn(out, CSS_COLUMN_MEDIUM, timeFracLocal * 100, 2);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getTotalNumInContext());
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMinNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getAvgNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMaxNanos() / MILLION);

        nodeRowAfterColumns(out, idGenerator, level, hasChildren);

        if(! data.getChildren().isEmpty()) {
            int totalChildCalls = 0;
            for(AMinMaxAvgData childData: data.getChildren().values()) {
                totalChildCalls += childData.getTotalNumInContext();
            }

            for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(data.getChildren(), false)) {
                writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level + 1, jdbcTimeHere, totalJdbcTime, totalChildCalls);
//                writeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level+1, totalNanos, data.getTotalNumInContext());
            }
        }
        nodeRowAfterChildren(out, hasChildren);
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
}

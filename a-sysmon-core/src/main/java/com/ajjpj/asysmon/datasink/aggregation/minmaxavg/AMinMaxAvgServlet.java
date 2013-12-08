package com.ajjpj.asysmon.datasink.aggregation.minmaxavg;

import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.aggregation.AMinMaxAvgData;
import com.ajjpj.asysmon.datasink.aggregation.AbstractAsysmonServlet;
import com.ajjpj.asysmon.util.APair;

import javax.servlet.ServletException;
import java.io.PrintWriter;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class AMinMaxAvgServlet extends AbstractAsysmonServlet {
    private static volatile AMinMaxAvgDataSink collector;

    private static final int MILLION = 1000*1000;

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

    @Override protected int getNumChildrenLevelsToExpandInitially() {
        return 1;
    }

    @Override protected int getDataColumnWidth() {
        return 5*60 + 1*80;
    }

    /**
     * Override to customize.
     */
    protected AMinMaxAvgDataSink createCollector() {
        return new AMinMaxAvgDataSink();
    }

    @Override protected String getTitle() {
        return "A-SysMon performance min / avg / max";
    }

    @SuppressWarnings("unchecked")
    @Override protected List<APair<String, String>> getCommands() {
        return Arrays.asList(
                new APair<String, String>("Clear", "clear"),
                collector.isActive() ? new APair<String, String>("Stop", "stop") : new APair<String, String>("Start", "start")
                );
    }

    @Override protected void handleCommand(String cmd) {
        if("clear".equals(cmd)) {
            collector.clear();
        }
        else if("start".equals(cmd)) {
            collector.setActive(true);
        }
        else if("stop".equals(cmd)) {
            collector.setActive(false);
        }
    }

    @Override
    protected void writeData(PrintWriter out) {
        final IdGenerator idGenerator = new IdGenerator();

        long totalNanos = 0;
        for(AMinMaxAvgData d: collector.getData().values()) {
            totalNanos += d.getTotalNanos();
        }

        for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(collector.getData(), 0, 0)) {
            final String rootIdent = entry.getKey();

            out.println("<div class='table-header'>&nbsp;<div style=\"float: right;\">");
            writeColumn(out, CSS_COLUMN_MEDIUM, "%");
            writeColumn(out, CSS_COLUMN_MEDIUM, "#");
            writeColumn(out, CSS_COLUMN_LONG, "total");
            writeColumn(out, CSS_COLUMN_MEDIUM, "min");
            writeColumn(out, CSS_COLUMN_MEDIUM, "avg");
            writeColumn(out, CSS_COLUMN_MEDIUM, "max");
            out.println("</div></div>");

            final AMinMaxAvgData data = entry.getValue();
            writeTreeNodeRec(out, rootIdent, data, idGenerator, 0, totalNanos, 1);
        }
    }



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

    //TODO expand / collapse all

    private void writeTreeNodeRec(PrintWriter out, String ident, AMinMaxAvgData data, IdGenerator idGenerator, int level, double parentTotalNanos, double numParentCalls) {
        final boolean hasChildren = data.getChildren().size() > 0;
        startNodeRow(out, idGenerator, ident, level, hasChildren, !data.isSerial());

        long totalNanos = data.getTotalNanos();
        double fractionOfParent = totalNanos / parentTotalNanos;

        long selfNanos = totalNanos;
        for(AMinMaxAvgData childData: data.getChildren().values()) {
            if(childData.isSerial()) {
                selfNanos -= childData.getTotalNanos();
            }
        }

        final String percentStyle = data.isSerial() ? ("background: " + greenToRed(fractionOfParent)) : null;
        writeColumn(out, CSS_COLUMN_MEDIUM, percentStyle, 100.0 * fractionOfParent, 1);
        writeColumn(out, CSS_COLUMN_MEDIUM, (data.getTotalNumInContext() / numParentCalls), 2);
        writeColumn(out, CSS_COLUMN_LONG, data.getTotalNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMinNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getAvgNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMaxNanos() / MILLION);

        nodeRowAfterColumns(out, idGenerator, ident, level, hasChildren, !data.isSerial());

        if(! data.getChildren().isEmpty()) {
            for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(data.getChildren(), selfNanos, data.getTotalNumInContext())) {
                writeTreeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level+1, totalNanos, data.getTotalNumInContext());
            }
        }
        nodeRowAfterChildren(out, hasChildren);
    }
}

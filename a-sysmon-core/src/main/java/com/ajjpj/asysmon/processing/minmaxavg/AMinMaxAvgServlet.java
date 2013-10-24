package com.ajjpj.asysmon.processing.minmaxavg;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.data.AGlobalDataPoint;
import com.ajjpj.asysmon.measure.global.AMemoryMeasurer;
import com.ajjpj.asysmon.measure.global.ASystemLoadMeasurer;
import com.ajjpj.asysmon.processing.minmaxavg.AMinMaxAvgCollector;
import com.ajjpj.asysmon.processing.minmaxavg.AMinMaxAvgData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class AMinMaxAvgServlet extends HttpServlet {
    private static final String CSS_COLUMN_SHORT = "column-short";
    private static final String CSS_COLUMN_MEDIUM = "column-medium";
    private static final String CSS_COLUMN_LONG = "column-long";

    private static volatile AMinMaxAvgCollector collector;

    private static final int MILLION = 1000*1000;

    /**
     * Override to customize initialization (and potentially registration) of the collector.
     */
    @Override public synchronized void init() throws ServletException {
        collector = new AMinMaxAvgCollector();
        AStaticSysMonConfig.addHandler(collector);
    }

    /**
     * All access to the collector is done through this method. Override to customize.
     */
    protected AMinMaxAvgCollector getCollector() {
        return collector;
    }

    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }


    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getParameter("res") != null) {
            final String resName = req.getParameter("res");
            if(resName.contains("..") || resName.contains("/")) {
                throw new IllegalArgumentException();
            }

//            resp.addHeader("Cache-Control", "max-age=3600");
            serveStaticResource(req.getParameter("res"), resp.getOutputStream());
            return;
        }

        if("clear".equals(req.getParameter("cmd"))) {
            getCollector().clear();
        }
        else if("start".equals(req.getParameter("cmd"))) {
            getCollector().setActive(true);
        }
        else if("stop".equals(req.getParameter("cmd"))) {
            getCollector().setActive(false);
        }

        resp.setCharacterEncoding("utf-8");

        writePage(req.getRequestURL().toString(), resp.getWriter());
    }

    private void serveStaticResource(String name, OutputStream out) throws IOException {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("asysmon-res/" + name);

        final byte[] buf = new byte[4096];
        int numRead=0;
        while((numRead = in.read(buf)) > 0) {
            out.write(buf, 0, numRead);
        }
    }

    private void writePage(String url, PrintWriter out) {
        out.println("<html>");
        out.println("<head><title>A-SysMon performance min / avg / max</title>");
        out.println("<meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" />");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "?res=asysmon-minmaxavg.css\">");
        out.println("<script src=\"" + url + "?res=jquery-1.10.2.min.js\"></script>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>A-SysMon performance min / avg / max</h1>");
        writeCommands(url, out);
        writeGlobalMeasurements(out);
        writeTable(out);
        out.println("</body></html>");
    }

    private void writeGlobalMeasurements(PrintWriter out) {
        final Map<String, AGlobalDataPoint> allData = ASysMon.get().getGlobalMeasurements();

        out.println("<table class='global-measurements'>");

        // special handling for system load
        final AGlobalDataPoint load1  = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_1_MIN);
        final AGlobalDataPoint load5  = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_5_MIN);
        final AGlobalDataPoint load15 = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_15_MIN);

        final String sLoad1  = load1  != null ? getDecimalFormat(load1. getNumFracDigits()).format(load1. getValue()) : "N/A";
        final String sLoad5  = load5  != null ? getDecimalFormat(load5. getNumFracDigits()).format(load5. getValue()) : "N/A";
        final String sLoad15 = load15 != null ? getDecimalFormat(load15.getNumFracDigits()).format(load15.getValue()) : "N/A";

        //TODO color code system load

        writeGlobalMeasurement(out, "System Load", sLoad1 + " / " + sLoad5 + " / " + sLoad15);

        // special handling for memory
        final AGlobalDataPoint memUsed = allData.remove(AMemoryMeasurer.IDENT_MEM_USED);
        final AGlobalDataPoint memTotal = allData.remove(AMemoryMeasurer.IDENT_MEM_TOTAL);
        final AGlobalDataPoint memMax = allData.remove(AMemoryMeasurer.IDENT_MEM_MAX);
        allData.remove(AMemoryMeasurer.IDENT_MEM_FREE);

        final int MEGA = 1024*1024;
        final String sMemUsed  = memUsed  != null ? (getDecimalFormat(memUsed. getNumFracDigits()).format(memUsed. getValue() / MEGA) + "M") : "N/A";
        final String sMemTotal = memTotal != null ? (getDecimalFormat(memTotal.getNumFracDigits()).format(memTotal.getValue() / MEGA) + "M") : "N/A";
        final String sMemMax   = memMax   != null ? (getDecimalFormat(memMax.  getNumFracDigits()).format(memMax.  getValue() / MEGA) + "M") : "N/A";

        //TODO color code memory usage

        writeGlobalMeasurement(out, "Memory", sMemUsed + " / " + sMemTotal + " / " + sMemMax);

        // generic handling for other global measurements
        for(AGlobalDataPoint dp: allData.values()) {
            writeGlobalMeasurement(out, dp.getName(), getDecimalFormat(dp.getNumFracDigits()).format(dp.getValue()));
        }

        out.println("</table>");
    }

    private void writeGlobalMeasurement(PrintWriter out, String ident, String value) {
        out.println("<tr class='global-measurements'><td class='global-measurements-key'>" + escapeHtml(ident) + "</td><td class='global-measuremnets-value'>" + escapeHtml(value) + "</td></tr>");
    }

    private DecimalFormat getDecimalFormat(int numFrac) {
        if(numFrac == 0) {
            return new DecimalFormat("#,##0");
        }

        final String formatSuffix = "000000000". substring(0, numFrac);
        return new DecimalFormat("#,##0." + formatSuffix);
    }

    private void writeCommands(String url, PrintWriter out) {
        out.println("<div class='button-box'>");
        out.println("<a class='btn' href='" + url + "'>Refresh</a>");
        out.println("<a class='btn' href='" + url + "?cmd=clear'>Clear</a>");
        if(getCollector().isActive()) {
            out.println("<a class='btn' href='" + url + "?cmd=stop'>Stop</a>");
        }
        else {
            out.println("<a class='btn' href='" + url + "?cmd=start'>Start</a>");
        }

        out.println("</div>");
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

    private static String escapeHtml(String s) {
        final StringBuilder result = new StringBuilder();

        for(int i=0; i<s.length(); i++) {
            final char ch = s.charAt(i);

            if(ch == '"') {
                result.append("&quot;");
            }
            else if(ch == '<') {
                result.append("&lt;");
            }
            else if(ch == '>') {
                result.append("&gt;");
            }
            else if(ch > 127) {
                result.append("&#" + ((int)ch) + ";");
            }
            else result.append(ch);
        }
        return result.toString();
    }

    private void writeTable(PrintWriter out) {
        final IdGenerator idGenerator = new IdGenerator();

        long totalNanos = 0;
        for(AMinMaxAvgData d: getCollector().getData().values()) {
            totalNanos += d.getTotalNanos();
        }

        for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(getCollector().getData(), 0, 0)) {
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

    //TODO expand / collapse all
    //TODO make static resources cacheable

    private void writeTreeNodeRec(PrintWriter out, String ident, AMinMaxAvgData data, IdGenerator idGenerator, int level, double parentTotalNanos, double numParentCalls) {
        out.println("<div class='" + (data.isDisjoint() ? "" :  "data-row-parallel ") + "data-row data-row-" + level + "' onclick=\"$('#" + idGenerator.nextId() + "').slideToggle(50);\">");
        out.println("<div class='node-icon'>" + (data.getChildren().isEmpty() ? "&nbsp;" : "*") + "</div>");

        final String effIdent = data.isDisjoint() ? ident : ("[" + ident + "]");
        out.println(escapeHtml(effIdent));
        out.println("<div style=\"float: right;\">");

        long totalNanos = data.getTotalNanos();
        double fractionOfParent = totalNanos / parentTotalNanos;

        long selfNanos = totalNanos;
        for(AMinMaxAvgData childData: data.getChildren().values()) {
            if(childData.isDisjoint()) {
                selfNanos -= childData.getTotalNanos();
            }
        }

        final String percentStyle = data.isDisjoint() ? ("background: " + blendedColor(200, 255, 200, 255, 120, 120, fractionOfParent)) : null;
        writeColumn(out, CSS_COLUMN_MEDIUM, percentStyle, 100.0 * fractionOfParent, 1);
        writeColumn(out, CSS_COLUMN_MEDIUM, (data.getTotalNumInContext() / numParentCalls), 2);
        writeColumn(out, CSS_COLUMN_LONG, data.getTotalNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMinNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getAvgNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMaxNanos() / MILLION);
        out.println("</div>");

        out.println("</div>");
        if(! data.getChildren().isEmpty()) {
            out.println("<div class='children' style='display:" + (level == 0 ? "block" : "none") +"' id=\"" + idGenerator.curId() + "\">");
            for(Map.Entry<String, AMinMaxAvgData> entry: getSorted(data.getChildren(), selfNanos, data.getTotalNumInContext())) {
                writeTreeNodeRec(out, entry.getKey(), entry.getValue(), idGenerator, level+1, totalNanos, data.getTotalNumInContext());
            }
            out.println("</div>");
        }
    }

    private String blendedColor(int r0, int g0, int b0, int r1, int g1, int b1, double ratio) {
        String r = Integer.toHexString(r0 + (int)((r1-r0)*ratio));
        String g = Integer.toHexString(g0 + (int)((g1-g0)*ratio));
        String b = Integer.toHexString(b0 + (int)((b1-b0)*ratio));

        while(r.length() < 2) {
            r = "0" + r;
        }
        while(g.length() < 2) {
            g = "0" + g;
        }
        while(b.length() < 2) {
            b = "0" + b;
        }

        return "#" + r + g + b;
    }

    private void writeColumn(PrintWriter out, String cssClass, double data, int numFrac) {
        writeColumn(out, cssClass, null, data, numFrac);
    }
    private void writeColumn(PrintWriter out, String cssClass, String style, double data, int numFrac) {
        writeColumn(out, cssClass, style, getDecimalFormat(numFrac).format(data));
    }

    private void writeColumn(PrintWriter out, String cssClass, long data) {
        writeColumn(out, cssClass, new DecimalFormat().format(data));
    }

    private void writeColumn(PrintWriter out, String cssClass, String data) {
        writeColumn(out, cssClass, null, data);
    }

    private void writeColumn(PrintWriter out, String cssClass, String styleRaw, String data) {
        final String style = styleRaw != null ? (" style=\"" + styleRaw + "\"") : "";

        out.println("<div class='" + cssClass + "'" + style + ">" + data + "</div>");

    }

    static class IdGenerator {
        private int counter = 0;

        String nextId() {
            counter += 1;
            return curId();
        }

        String curId() {
            return "id-" + counter;
        }
    }
}

package com.ajjpj.asysmon.datasink.aggregation;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.AGlobalConfig;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.measure.global.AMemoryMeasurer;
import com.ajjpj.asysmon.measure.global.ASystemLoadMeasurer;
import com.ajjpj.asysmon.util.APair;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author arno
 */
public abstract class AbstractAsysmonServlet extends HttpServlet {
    public static final String CSS_COLUMN_SHORT = "column-short";
    public static final String CSS_COLUMN_MEDIUM = "column-medium";
    public static final String CSS_COLUMN_LONG = "column-long";

    protected static final int MILLION = 1000*1000;

    abstract protected int getNumChildrenLevelsToExpandInitially();
    abstract protected int getDataColumnWidth();


    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        final ServletContext ctx = config.getServletContext();
        ctx.addListener(new ServletContextListener() {
            @Override public void contextInitialized(ServletContextEvent sce) { }

            @Override public void contextDestroyed(ServletContextEvent sce) {
                if(AGlobalConfig.getImplicitlyShutDownWithServlet()) {
                    getSysMon().shutdown();
                }
            }
        });
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

        final String cmd = req.getParameter("cmd");
        if(cmd != null) {
            handleCommand(cmd);
        }

        resp.setCharacterEncoding("utf-8");

        writePage(req.getRequestURL().toString(), resp.getWriter());
    }

    protected abstract void handleCommand(String cmd);

    private void serveStaticResource(String name, OutputStream out) throws IOException {
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("asysmon-res/" + name);

        final byte[] buf = new byte[4096];
        int numRead=0;
        while((numRead = in.read(buf)) > 0) {
            out.write(buf, 0, numRead);
        }
    }

    protected abstract String getTitle();

    private void writePage(String url, PrintWriter out) {
        out.println("<html>");
        out.println("<head><title>" + escapeHtml(getTitle()) + "</title>");
        out.println("<meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\" />");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "?res=asysmon-minmaxavg.css\">");
        out.println("<script src=\"" + url + "?res=jquery-1.10.2.min.js\"></script>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + escapeHtml(getTitle()) + "</h1>");
        writeCommands(url, out);
        writeScalarMeasurements(out);
        writeData(out);
        out.println("</body></html>");
    }

    protected abstract void writeData(PrintWriter out);

    private void writeScalarMeasurements(PrintWriter out) {
        final Map<String, AScalarDataPoint> allData = new TreeMap<String, AScalarDataPoint>(getSysMon().getScalarMeasurements());

        out.println("<table class='global-measurements'>");

        // special handling for system load
        final AScalarDataPoint load1  = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_1_MIN);
        final AScalarDataPoint load5  = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_5_MIN);
        final AScalarDataPoint load15 = allData.remove(ASystemLoadMeasurer.IDENT_LOAD_15_MIN);

        final String sLoad1  = load1  != null ? getDecimalFormat(load1. getNumFracDigits()).format(load1. getValue()) : "N/A";
        final String sLoad5  = load5  != null ? getDecimalFormat(load5. getNumFracDigits()).format(load5. getValue()) : "N/A";
        final String sLoad15 = load15 != null ? getDecimalFormat(load15.getNumFracDigits()).format(load15.getValue()) : "N/A";

        //TODO color code system load

        writeScalarMeasurement(out, "System Load", sLoad1 + " / " + sLoad5 + " / " + sLoad15);

        // special handling for memory
        final AScalarDataPoint memUsed = allData.remove(AMemoryMeasurer.IDENT_MEM_USED);
        final AScalarDataPoint memTotal = allData.remove(AMemoryMeasurer.IDENT_MEM_TOTAL);
        final AScalarDataPoint memMax = allData.remove(AMemoryMeasurer.IDENT_MEM_MAX);
        allData.remove(AMemoryMeasurer.IDENT_MEM_FREE);

        final int MEGA = 1024*1024;
        final String sMemUsed  = memUsed  != null ? (getDecimalFormat(memUsed. getNumFracDigits()).format(memUsed. getValue() / MEGA) + "M") : "N/A";
        final String sMemTotal = memTotal != null ? (getDecimalFormat(memTotal.getNumFracDigits()).format(memTotal.getValue() / MEGA) + "M") : "N/A";
        final String sMemMax   = memMax   != null ? (getDecimalFormat(memMax.  getNumFracDigits()).format(memMax.  getValue() / MEGA) + "M") : "N/A";

        //TODO color code memory usage

        writeScalarMeasurement(out, "Memory", sMemUsed + " / " + sMemTotal + " / " + sMemMax);

        // generic handling for other global measurements
        for(AScalarDataPoint dp: allData.values()) {
            writeScalarMeasurement(out, dp.getName(), getDecimalFormat(dp.getNumFracDigits()).format(dp.getValue()));
        }

        out.println("</table>");
    }

    private void writeScalarMeasurement(PrintWriter out, String ident, String value) {
        out.println("<tr class='global-measurements'><td class='global-measurements-key'>" + escapeHtml(ident) + "</td><td class='global-measuremnets-value'>" + escapeHtml(value) + "</td></tr>");
    }

    protected DecimalFormat getDecimalFormat(int numFrac) {
        if(numFrac == 0) {
            return new DecimalFormat("#,##0");
        }

        final String formatSuffix = "000000000". substring(0, numFrac);
        return new DecimalFormat("#,##0." + formatSuffix);
    }

    protected abstract List<APair<String, String>> getCommands();

    private void writeCommands(String url, PrintWriter out) {
        out.println("<div class='button-box'>");
        out.println("<a class='btn' href='" + url + "'>Refresh</a>");

        for(APair<String, String> cmd: getCommands()) {
            out.println("<a class='btn' href='" + url + "?cmd=" + cmd._2 + "'>" + cmd._1 + "</a>");
        }
        out.println("</div>");
    }

    protected static String escapeHtml(String s) {
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

    protected void startNodeRow(PrintWriter out, IdGenerator idGenerator, String ident, int level, boolean hasChildren, boolean isSubdued) {
        out.println("<div class='" + (isSubdued ? "data-row-subdued " : "") + "data-row data-row-" + level + "' onclick=\"$('#" + idGenerator.nextId() + "').slideToggle(50);\">");
        out.println("<div class='node-icon'>" + (hasChildren ? "*" : "&nbsp;") + "</div>");

        out.println("<div style=\"float: right;\">");
    }

    protected void nodeRowAfterColumns(PrintWriter out, IdGenerator idGenerator, String ident, int level, boolean hasChildren, boolean isSubdued) {
        out.println ("</div>");
        final String effIdent = isSubdued ? ("[" + ident + "]") : ident;
        out.println("<div class='node-text' style='margin-right: " + getDataColumnWidth() + "px;'>");
        out.println(escapeHtml(effIdent));
        out.println("</div></div>");
        if(hasChildren) {
            final boolean expand = getNumChildrenLevelsToExpandInitially() - level > 0;
            out.println("<div class='children' style='display:" + (expand ? "block" : "none") +"' id=\"" + idGenerator.curId() + "\">");
        }
    }

    protected void nodeRowAfterChildren(PrintWriter out, boolean hasChildren) {
        if(hasChildren) {
            out.println("</div>");
        }
    }

    //TODO make static resources cacheable

    protected String greenToRed(double ratio) {
        return blendedColor(200, 255, 200, 255, 120, 120, ratio);
    }

    protected String blendedColor(int r0, int g0, int b0, int r1, int g1, int b1, double ratio) {
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

    protected void writeColumn(PrintWriter out, String cssClass, double data, int numFrac) {
        writeColumn(out, cssClass, null, data, numFrac);
    }
    protected void writeColumn(PrintWriter out, String cssClass, String style, double data, int numFrac) {
        writeColumn(out, cssClass, style, getDecimalFormat(numFrac).format(data));
    }

    protected void writeColumn(PrintWriter out, String cssClass, long data) {
        writeColumn(out, cssClass, new DecimalFormat().format(data));
    }

    protected void writeColumn(PrintWriter out, String cssClass, String data) {
        writeColumn(out, cssClass, null, data);
    }

    protected void writeColumn(PrintWriter out, String cssClass, String styleRaw, String data) {
        final String style = styleRaw != null ? (" style=\"" + styleRaw + "\"") : "";

        out.println("<div class='" + cssClass + "'" + style + ">" + data + "</div>");

    }

    public static class IdGenerator {
        private int counter = 0;

        public String nextId() {
            counter += 1;
            return curId();
        }

        public String curId() {
            return "id-" + counter;
        }
    }
}

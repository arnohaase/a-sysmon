package com.ajjpj.asysmon.render.minmaxavgservlet;

import com.ajjpj.asysmon.config.AStaticSysMonConfig;
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
import java.text.DecimalFormat;
import java.util.Map;


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
        out.println("<script src=\"" + url + "?res=asysmon-minmaxavg.js\"></script>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>A-SysMon performance min / avg / max</h1>");
        writeTable(out);
        out.println("</body></html>");
    }

    private void writeTable(PrintWriter out) {
        final IdGenerator idGenerator = new IdGenerator();

        for(String rootIdent: collector.getData().keySet()) {
            out.println("<div class='table-header'>&nbsp;<div style=\"float: right;\">");
            writeColumn(out, CSS_COLUMN_MEDIUM, "%");
            writeColumn(out, CSS_COLUMN_SHORT, "#");
            writeColumn(out, CSS_COLUMN_LONG, "total");
            writeColumn(out, CSS_COLUMN_MEDIUM, "min");
            writeColumn(out, CSS_COLUMN_MEDIUM, "avg");
            writeColumn(out, CSS_COLUMN_MEDIUM, "max");
            out.println("</div></div>");

            final AMinMaxAvgData data = collector.getData().get(rootIdent);
            writeTreeNodeRec(out, rootIdent, data, idGenerator, 0, data.getAvgNanos() * data.getTotalNumInContext(), data.getTotalNumInContext());
        }
    }

    //TODO top-level call sorted by total duration
    //TODO sort by fraction of parent at every level
    //TODO commands
    //TODO make static resources cacheable
    //TODO visualize 'has children'

    private void writeTreeNodeRec(PrintWriter out, String ident, AMinMaxAvgData data, IdGenerator idGenerator, int level, double parentTotalNanos, double numParentCalls) {
        out.println("<div class='data-row data-row-" + level + "' onclick=\"$('#" + idGenerator.nextId() + "').slideToggle(50);\">");
//        out.println("<div style=\"background: " + colorForLevel(level) + "\" onclick=\"$('#" + idGenerator.nextId() + "').slideToggle(50);\">");
        out.println(ident);
        out.println("<div style=\"float: right;\">");

        long totalNanos = data.getAvgNanos() * data.getTotalNumInContext();
        double fractionOfParent = totalNanos / parentTotalNanos;
        writeColumn(out, CSS_COLUMN_MEDIUM, "background: " + blendedColor(200, 255, 200, 255, 120, 120, fractionOfParent), 100.0 * fractionOfParent, 1);
        writeColumn(out, CSS_COLUMN_SHORT, level == 0 ? data.getTotalNumInContext() : (data.getTotalNumInContext() / numParentCalls), 1);
        writeColumn(out, CSS_COLUMN_LONG, (data.getAvgNanos() * data.getTotalNumInContext()) / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMinNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getAvgNanos() / MILLION);
        writeColumn(out, CSS_COLUMN_MEDIUM, data.getMaxNanos() / MILLION);
        out.println("</div>");

        out.println("</div>");
        if(! data.getChildren().isEmpty()) {
            out.println("<div class='children' style='display:" + (level == 0 ? "block" : "none") +"' id=\"" + idGenerator.curId() + "\">");
            for(Map.Entry<String, AMinMaxAvgData> entry: data.getChildren().entrySet()) {
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
        final String formatSuffix = "000000000". substring(0, numFrac);
        writeColumn(out, cssClass, style, new DecimalFormat("#,##0." + formatSuffix).format(data));
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

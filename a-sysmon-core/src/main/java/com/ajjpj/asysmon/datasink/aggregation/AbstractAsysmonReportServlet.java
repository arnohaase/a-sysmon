package com.ajjpj.asysmon.datasink.aggregation;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.AGlobalConfig;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author arno
 */
public abstract class AbstractAsysmonReportServlet extends HttpServlet {
    private static final AtomicBoolean hasShutdownHook = new AtomicBoolean(false);

    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final boolean wasInitialized = hasShutdownHook.getAndSet(true);
        if(!wasInitialized) {
            try {
                final ServletContext ctx = config.getServletContext();
                ctx.addListener(new ServletContextListener() {
                    @Override public void contextInitialized(ServletContextEvent sce) { }

                    @Override public void contextDestroyed(ServletContextEvent sce) {
                        if(AGlobalConfig.getImplicitlyShutDownWithServlet()) {
                            getSysMon().shutdown();
                        }
                    }
                });
            } catch (IllegalStateException e) {
                // ignore - this only works if init() is called during container startup, i.e. with load-on-startup in web.xml
            }
        }
    }

    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(req.getParameter("res") != null) {
            serveStaticResource(req.getParameter("res"), resp);
            return;
        }

        if(req.getRequestURI().endsWith("/getData")) {
            serveData(resp);
            return;
        }
        if(req.getRequestURI().endsWith("/doStart")) {
            doStartMeasurements();
            serveData(resp);
            return;
        }
        if(req.getRequestURI().endsWith("/doStop")) {
            doStopMeasurements();
            serveData(resp);
            return;
        }
        if(req.getRequestURI().endsWith("/doClear")) {
            doClearMeasurements();
            serveData(resp);
            return;
        }

        if(! req.getRequestURL().toString().endsWith("/")) {
            resp.sendRedirect(req.getRequestURL() + "/");
            return;
        }

        serveStaticResource("asysmon-aggregated.html", resp);
    }

    private void serveData(HttpServletResponse resp) throws IOException {
        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());

        json.startObject();

        json.writeKey("title");
        json.writeStringLiteral(getTitle());
        json.writeKey("isStarted");
        json.writeBooleanLiteral(isStarted());

        json.writeKey("scalars");
        json.startObject();
        for(AScalarDataPoint scalar: getSysMon().getScalarMeasurements().values()) {
            writeScalar(json, scalar);
        }
        json.endObject();

        json.writeKey("columnDefs");
        json.startArray();
        for(ColDef colDef: getColDefs()) {
            writeColDef(json, colDef);
        }
        json.endArray();

        json.writeKey("traces");
        json.startArray();
        for(TreeNode n: getData()) {
            writeDataNode(json, n);
        }
        json.endArray();

        json.endObject();
    }

    private void writeColDef(AJsonSerHelper json, ColDef colDef) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(colDef.name);

        json.writeKey("isPercentage");
        json.writeBooleanLiteral(colDef.isPercentage);

        json.writeKey("numFracDigits");
        json.writeNumberLiteral(colDef.numFracDigits, 0);

        json.writeKey("width");
        json.writeStringLiteral(colDef.width.name());

        json.endObject();
    }

    private void writeScalar(AJsonSerHelper json, AScalarDataPoint scalar) throws IOException {
        json.writeKey(scalar.getName());
        json.startObject();

        json.writeKey("value");
        json.writeNumberLiteral(scalar.getValueRaw(), scalar.getNumFracDigits());

        json.writeKey("numFracDigits");
        json.writeNumberLiteral(scalar.getNumFracDigits(), 0);

        json.endObject();
    }

    private void writeDataNode(AJsonSerHelper json, TreeNode node) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(node.identifier);

        json.writeKey("isSerial");
        json.writeBooleanLiteral(node.isSerial);

        json.writeKey("data");
        json.startArray();
        for(int i=0; i<node.colDataRaw.length; i++) {
            json.writeNumberLiteral(node.colDataRaw[i], getColDefs().get(i).numFracDigits);
        }
        json.endArray();

        if(! node.children.isEmpty()) {
            json.writeKey("children");
            json.startArray();
            for(TreeNode child: node.children) {
                writeDataNode(json, child);
            }
            json.endArray();
        }

        json.endObject();
    }

    protected abstract boolean isStarted();
    protected abstract void doStartMeasurements();
    protected abstract void doStopMeasurements();
    protected abstract void doClearMeasurements();

    protected abstract List<ColDef> getColDefs();

    protected abstract List<TreeNode> getData();

    protected enum ColWidth {Short, Medium, Long}
    protected static class ColDef {
        public final String name;
        public final boolean isPercentage;
        public final int numFracDigits;
        public final ColWidth width;

        public ColDef(String name, boolean isPercentage, int numFracDigits, ColWidth width) {
            this.name = name;
            this.isPercentage = isPercentage;
            this.numFracDigits = numFracDigits;
            this.width = width;
        }
    }

    protected static class TreeNode {
        public final String identifier;
        public final boolean isSerial;
        public final long[] colDataRaw;
        public final List<TreeNode> children;

        public TreeNode(String identifier, boolean isSerial, long[] colDataRaw, List<TreeNode> children) {
            this.identifier = identifier;
            this.isSerial = isSerial;
            this.colDataRaw = colDataRaw;
            this.children = children;
        }
    }

    private void serveStaticResource(String resName, HttpServletResponse resp) throws IOException {
        if(resName.contains("..") || resName.contains("/")) {
            throw new IllegalArgumentException();
        }

        resp.addHeader("Cache-Control", "max-age=36000");

        final OutputStream out = resp.getOutputStream();
        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("asysmon-res/" + resName);

        final byte[] buf = new byte[4096];
        int numRead=0;
        while((numRead = in.read(buf)) > 0) {
            out.write(buf, 0, numRead);
        }
    }

    protected abstract String getTitle();

}

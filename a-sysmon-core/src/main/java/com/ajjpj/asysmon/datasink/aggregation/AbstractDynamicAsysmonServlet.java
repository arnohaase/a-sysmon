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

/**
 * @author arno
 */
public abstract class AbstractDynamicAsysmonServlet extends HttpServlet {

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

    protected abstract boolean isStarted();
    protected abstract void doStartMeasurements();
    protected abstract void doStopMeasurements();
    protected abstract void doClearMeasurements();

    protected abstract List<ColDef> getColDefs();

    protected enum ColWidth {Short, Medium, Long}
    protected class ColDef {
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

package com.ajjpj.asysmon.servlet.memgc;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.servlet.AbstractAsysmonServlet;
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
public class AMemoryAndGcServlet extends AbstractAsysmonServlet {
    private GcDataSink gcDataSink;

    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    @Override public void init() throws ServletException {
        gcDataSink = new GcDataSink(1000); //TODO make this configurable
        ASysMonConfigurer.addDataSink(getSysMon(), gcDataSink);
    }


    @Override protected String getDefaultHtmlName() {
        return "memgc.html";
    }

    @Override protected void handleRestCall(String service, HttpServletResponse resp) throws IOException {
        if("getData".equals(service)) {
            serveData(resp);
            return;
        }
    }

//    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        if(req.getParameter("res") != null) {
//            serveStaticResource(req.getParameter("res"), resp);
//            return;
//        }
//
//        if(req.getRequestURI().endsWith("/getData")) {
//            serveData(resp);
//            return;
//        }
//
//        if(! req.getRequestURL().toString().endsWith("/")) {
//            resp.sendRedirect(req.getRequestURL() + "/");
//            return;
//        }
//
//        serveStaticResource("memgc.html", resp);
//    }

    private void serveData(HttpServletResponse resp) throws IOException {
        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());

        json.startObject();

        json.writeKey("gcs");
        json.startArray();
        for(GcDetails gc: gcDataSink.getData()) {
            serveGcDetails(json, gc);
        }
        json.endArray();

        json.endObject();

    }

    private void serveGcDetails(AJsonSerHelper json, GcDetails gc) throws IOException {
        json.startObject();

        json.writeKey("type");
        json.writeStringLiteral(gc.gcType);

        json.writeKey("algorithm");
        json.writeStringLiteral(gc.algorithm);

        json.writeKey("cause");
        json.writeStringLiteral(gc.cause);

        json.writeKey("startMillis");
        json.writeNumberLiteral(gc.startMillis, 0);

        json.writeKey("durationNanos");
        json.writeNumberLiteral(gc.durationNanos, 0);

        json.writeKey("mem");
        serveMemDetails(json, gc.memDetails);

        json.endObject();
    }

    private void serveMemDetails(AJsonSerHelper json, List<GcMemDetails> memDetails) throws IOException {
        json.startObject();

        for(GcMemDetails mem: memDetails) {
            json.writeKey(mem.memKind);
            json.startObject();

            json.writeKey("usedBefore");
            json.writeNumberLiteral(mem.usedBefore, 0);

            json.writeKey("usedAfter");
            json.writeNumberLiteral(mem.usedAfter, 0);

            json.writeKey("committedBefore");
            json.writeNumberLiteral(mem.committedBefore, 0);

            json.writeKey("committedAfter");
            json.writeNumberLiteral(mem.committedAfter, 0);

            json.endObject();
        }

        json.endObject();
    }


    //TODO getMostRecentTimestamp

    //TODO extract AbstractAsysmonServlet
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
}

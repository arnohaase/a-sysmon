package com.ajjpj.asysmon.servlet_.memgc;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.servlet.AbstractASysMonServlet;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * @author arno
 */
public class AMemoryAndGcServlet extends AbstractASysMonServlet {
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

    @Override protected boolean handleRestCall(List<String> restParams, HttpServletResponse resp) throws IOException {
        if("getData".equals(restParams.get(0))) {
            serveData(resp);
            return true;
        }

        return false;
    }

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
}

package com.ajjpj.asysmon.servlet;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.servlet_.threaddump.AThreadDumpServlet;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * @author arno
 */
public class ASysMonServlet extends AbstractASysMonServlet {
    public static final String REST_GET_CONFIG = "getConfig";

    @Override protected String getDefaultHtmlName() {
        return "asysmon.html";
    }

    @Override protected boolean handleRestCall(String service, List<String> restParams, HttpServletResponse resp) throws IOException {
        if(REST_GET_CONFIG.equals(service)) {
            serveConfig(resp);
            return true;
        }

        if("getData".equals(service)) {
            final AThreadDumpServlet s = new AThreadDumpServlet();
            s.appPkg = "com.ajjpj";
            return s.handleRestCall(service, restParams, resp);
        }

        return false;
    }

    //TODO code to shut down ASysMon instance on servlet shutdown

    /**
     * override to customize
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    private void serveConfig(HttpServletResponse resp) throws IOException {
        final ASysMonConfig config = getSysMon().getConfig();

        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());
        json.startObject();

        json.writeKey("applicationId");
        json.writeStringLiteral(config.applicationId);

        json.writeKey("applicationInstanceId");
        json.writeStringLiteral(config.applicationInstanceId);

        json.writeKey("applicationInstanceHtmlColorCode");
        json.writeStringLiteral(config.applicationInstanceHtmlColorCode);

        json.endObject();
    }
}

package com.ajjpj.asysmon.servlet;

import com.ajjpj.asysmon.config.ASysMonConfig;
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
        return null;
    }

    @Override protected boolean handleRestCall(String service, List<String> restParams, HttpServletResponse resp) throws IOException {
        if(REST_GET_CONFIG.equals(service)) {
            serveConfig(resp);
            return true;
        }
        return false;
    }

    protected ASysMonConfig getConfig() {
        return null; //TODO
//        return new ASysMonConfig("demo", "theInstance", "#8090a0"); //TODO make this configurable
    }

    private void serveConfig(HttpServletResponse resp) throws IOException {
        final ASysMonConfig config = getConfig();

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

package com.ajjpj.asysmon.servlet;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.config.presentation.APresentationMenuEntry;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.servlet_.threaddump.AThreadDumpServlet;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class ASysMonServlet extends AbstractASysMonServlet {
    public static final String CONFIG_JS = "config.js";

    private final Map<String, APresentationPageDefinition> pageDefs = new ConcurrentHashMap<String, APresentationPageDefinition>();

    @Override
    public void init() throws ServletException {
        super.init();
        for(APresentationMenuEntry menuEntry: getSysMon().getConfig().presentationMenuEntries) {
            for(APresentationPageDefinition pageDef: menuEntry.pageDefinitions) {
                pageDef.init(getSysMon());
                final Object prev = pageDefs.put(pageDef.getId(), pageDef);
                if(prev != null) {
                    throw new IllegalStateException("more than one page definitions with id '" + pageDef.getId() + "'");
                }
            }
        }
    }

    @Override protected String getDefaultHtmlName() {
        return "asysmon.html";
    }

    @Override protected boolean handleRestCall(List<String> restParams, HttpServletResponse resp) throws IOException {
        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());

        final String pageId = restParams.remove(0);
        final String service = restParams.remove(0);

        final APresentationPageDefinition pageDef = pageDefs.get(pageId);
        if(pageDef == null) {
            throw new IllegalArgumentException("no page def with ID '" + pageId + "'");
        }

        return pageDef.handleRestCall(service, restParams, json);
    }

    @Override protected boolean handleDynamic(List<String> pathSegments, HttpServletResponse resp) throws IOException {
        if(CONFIG_JS.equals(pathSegments.get(0))) {
            serveConfig(resp);
            return true;
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

        final ServletOutputStream out = resp.getOutputStream();

        out.print("angular.module('ASysMonApp').constant('configRaw', ");
        out.flush();

        final AJsonSerHelper json = new AJsonSerHelper(out);
        json.startObject();

        json.writeKey("applicationId");
        json.writeStringLiteral(config.applicationId);

        json.writeKey("applicationInstanceId");
        json.writeStringLiteral(config.applicationInstanceId);

        json.writeKey("applicationInstanceHtmlColorCode");
        json.writeStringLiteral(config.applicationInstanceHtmlColorCode);

        json.writeKey("menuEntries");
        json.startArray();
        for(APresentationMenuEntry menuEntry: config.presentationMenuEntries) {
            writeMenuEntry(menuEntry, json);
        }
        json.endArray();

        json.endObject();

        out.println(");");
    }

    private void writeMenuEntry(APresentationMenuEntry menuEntry, AJsonSerHelper json) throws IOException {
        json.startObject();

        json.writeKey("label");
        json.writeStringLiteral(menuEntry.label);

        json.writeKey("entries");
        json.startArray();

        for(APresentationPageDefinition pageDef: menuEntry.pageDefinitions) {
            json.startObject();

            json.writeKey("id");
            json.writeStringLiteral(pageDef.getId());

            json.writeKey("controller");
            json.writeStringLiteral(pageDef.getControllerName());

            json.writeKey("htmlFileName");
            json.writeStringLiteral(pageDef.getHtmlFileName());

            json.writeKey("shortLabel");
            json.writeStringLiteral(pageDef.getShortLabel());

            json.writeKey("fullLabel");
            json.writeStringLiteral(pageDef.getFullLabel());

            json.endObject();
        }

        json.endArray();

        json.endObject();
    }
}

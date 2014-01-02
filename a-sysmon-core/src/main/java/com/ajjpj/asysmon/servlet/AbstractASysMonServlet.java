package com.ajjpj.asysmon.servlet;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This servlet_ class serves static resources and dispatches RESTful service calls
 *
 * @author arno
 */
public abstract class AbstractASysMonServlet extends HttpServlet {
    public static final String ASYSMON_MARKER_SEGMENT = "/_$_asysmon_$_/";
    public static final String ASYSMON_MARKER_STATIC = ASYSMON_MARKER_SEGMENT + "static/";
    public static final String ASYSMON_MARKER_REST = ASYSMON_MARKER_SEGMENT + "rest/";

    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();

        if(uri.contains(ASYSMON_MARKER_STATIC)) {
            serveStaticResource(substringAfter(uri, ASYSMON_MARKER_STATIC), resp);
            return;
        }

        if(uri.contains(ASYSMON_MARKER_REST)) {
            final String[] restPart = substringAfter(uri, ASYSMON_MARKER_REST).split("/");
            if(!handleRestCall(new ArrayList<String>(Arrays.asList(restPart)), resp)) {
                throw new IllegalArgumentException("unsupported REST call: " + uri);
            }
            return;
        }

        if(uri.contains(ASYSMON_MARKER_SEGMENT)) {
            final String[] dynamicPart = substringAfter(uri, ASYSMON_MARKER_SEGMENT).split("/");
            if(! handleDynamic(new ArrayList<String>(Arrays.asList(dynamicPart)), resp)) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        //TODO handle 'not found'

        if(! uri.endsWith("/")) {
            resp.sendRedirect(uri + "/");
            return;
        }

        serveStaticResource(getDefaultHtmlName(), resp);
    }

    protected abstract String getDefaultHtmlName();
    protected abstract boolean handleRestCall(List<String> pathSegments, HttpServletResponse resp) throws IOException;
    protected boolean handleDynamic(List<String> pathSegments, HttpServletResponse resp) throws IOException {
        return false;
    }

    private static String substringAfter(String s, String sub) {
        final int idx = s.indexOf(sub);
        return s.substring(idx + sub.length());
    }

    private void serveStaticResource(String resName, HttpServletResponse resp) throws IOException {
        if(resName.contains("..") || resName.startsWith("/") || resName.contains("//")) {
            throw new IllegalArgumentException("rejected resource request '"  + resName + "' for security reasons");
        }

        final InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("asysmon-res/" + resName);
        if(in == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        resp.addHeader("Cache-Control", "max-age=36000");

        final OutputStream out = resp.getOutputStream();

        final byte[] buf = new byte[4096];
        int numRead=0;
        while((numRead = in.read(buf)) > 0) {
            out.write(buf, 0, numRead);
        }
    }
}

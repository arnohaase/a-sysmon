package com.ajjpj.asysmon.servlet;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * This servlet class serves static resources and dispatches RESTful service calls
 *
 * @author arno
 */
public abstract class AbstractAsysmonServlet extends HttpServlet {
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
            handleRestCall(substringAfter(uri, ASYSMON_MARKER_REST), resp);
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
    protected abstract void handleRestCall(String service, HttpServletResponse resp) throws IOException;

    private static String substringAfter(String s, String sub) {
        final int idx = s.indexOf(sub);
        return s.substring(idx + sub.length());
    }

    private void serveStaticResource(String resName, HttpServletResponse resp) throws IOException {
        if(resName.contains("..") || resName.startsWith("/") || resName.contains("//")) {
            throw new IllegalArgumentException("rejected resource request '"  + resName + "' for security reasons");
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

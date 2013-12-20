package com.ajjpj.asysmon.servlet.threaddump;

import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ThreadInfo;
import java.util.Collection;


/**
 * @author arno
 */
public class AThreadDumpServlet extends HttpServlet {
    public static final String INIT_PARAM_APP_PACKAGE = "application.package";

    private volatile String appPkg;

    @Override public void init() throws ServletException {
        appPkg = getServletConfig().getInitParameter(INIT_PARAM_APP_PACKAGE);
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

        if(! req.getRequestURL().toString().endsWith("/")) {
            resp.sendRedirect(req.getRequestURL() + "/");
            return;
        }

        serveStaticResource("threads.html", resp);
    }

    private void serveData(HttpServletResponse resp) throws IOException {
        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());

        json.startObject();

        json.writeKey("title");
        json.writeStringLiteral("A-SysMon: Thread Dump");

        json.writeKey("appPkg");
        json.writeStringLiteral(appPkg);

        json.writeKey("threads");
        dumpThreads(json, AThreadDumper.getThreadInfo(), AThreadDumper.getDeadlockedThreads());

        json.endObject();
    }

    private void dumpThreads(AJsonSerHelper json, Collection<ThreadInfo> threads, Collection<Long> deadlockedThreads) throws IOException {
        json.startArray();

        for(ThreadInfo ti: threads) {
            dumpThread(json, ti, deadlockedThreads.contains(ti.getThreadId()));
        }

        json.endArray();
    }

    private void dumpThread(AJsonSerHelper json, ThreadInfo ti, boolean isDeadLocked) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(ti.getThreadName());

        json.writeKey("id");
        json.writeNumberLiteral(ti.getThreadId(), 0);

        json.writeKey("state");
        json.writeStringLiteral(isDeadLocked ? "DEADLOCKED" : ti.getThreadState().name());

        json.writeKey("stacktrace");
        dumpStackTrace(json, ti.getStackTrace());

        json.endObject();
    }

    private void dumpStackTrace(AJsonSerHelper json, StackTraceElement[] stackTrace) throws IOException {
        json.startArray();

        for(StackTraceElement ste: stackTrace) {
            json.startObject();

            json.writeKey("repr");
            json.writeStringLiteral(ste.toString());

            json.writeKey("hasSource");
            json.writeBooleanLiteral(ste.getLineNumber() > 0);

            json.writeKey("isNative");
            json.writeBooleanLiteral(ste.isNativeMethod());

            json.endObject();
        }

        json.endArray();
    }

    //TODO keep 'head' fixed at the top (here and on other pages)

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

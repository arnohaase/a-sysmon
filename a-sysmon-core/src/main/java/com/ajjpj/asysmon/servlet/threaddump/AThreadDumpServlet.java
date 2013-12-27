package com.ajjpj.asysmon.servlet.threaddump;

import com.ajjpj.asysmon.servlet.AbstractAsysmonServlet;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Collection;


/**
 * @author arno
 */
public class AThreadDumpServlet extends AbstractAsysmonServlet {
    public static final String INIT_PARAM_APP_PACKAGE = "application.package";

    private volatile String appPkg;

    @Override public void init() throws ServletException {
        appPkg = getServletConfig().getInitParameter(INIT_PARAM_APP_PACKAGE);
    }

    @Override protected String getDefaultHtmlName() {
        return "threads.html";
    }

    @Override protected boolean handleRestCall(String service, HttpServletResponse resp) throws IOException {
        if("getData".equals(service)) {
            serveData(resp);
            return true;
        }

        return false;
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
}

package com.ajjpj.asysmon.servlet.threaddump;

import com.ajjpj.afoundation.io.AJsonSerHelper;
import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.impl.ASysMonConfigurer;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author arno
 */
public class AThreadDumpPageDefinition implements APresentationPageDefinition {
    private final ARunningThreadTrackingDataSink runningThreadTracker = new ARunningThreadTrackingDataSink();

    private final String applicationPackage;
    private final String idSuffix;
    private final String labelSuffix;

    public AThreadDumpPageDefinition(String applicationPackage) {
        this(applicationPackage, "", "");
    }

    public AThreadDumpPageDefinition(String applicationPackage, String idSuffix, String labelSuffix) {
        this.applicationPackage = applicationPackage;
        this.idSuffix = idSuffix;
        this.labelSuffix = labelSuffix;
    }

    @Override public String getId() {
        return "threaddump" + idSuffix;
    }

    @Override
    public String getShortLabel() {
        return "Thread Dump" + labelSuffix;
    }

    @Override public String getFullLabel() {
        return getShortLabel();
    }

    @Override public String getHtmlFileName() {
        return "threaddump.html";
    }

    @Override public String getControllerName() {
        return "CtrlThreadDump";
    }

    @Override public boolean handleRestCall(String service, List<String> params, AJsonSerHelper json) throws IOException {
        if("getData".equals(service)) {
            serveData(json);
            return true;
        }

        return false;
    }

    private void serveData(AJsonSerHelper json) throws IOException {
        json.startObject();

        json.writeKey("appPkg");
        json.writeStringLiteral(applicationPackage);

        json.writeKey("threads");
        dumpThreads(json, AThreadDumper.getThreadInfo(), AThreadDumper.getDeadlockedThreads(), runningThreadTracker.getStartTimestamps());

        json.endObject();
    }

    private void dumpThreads(AJsonSerHelper json, Collection<ThreadInfo> threads, Collection<Long> deadlockedThreads, Map<String, Long> startTimestamps) throws IOException {
        json.startArray();

        final long now = System.currentTimeMillis();
        for(ThreadInfo ti: threads) {
            dumpThread(json, ti, deadlockedThreads.contains(ti.getThreadId()), now, startTimestamps.get(ti.getThreadName()));
        }

        json.endArray();
    }

    private void dumpThread(AJsonSerHelper json, ThreadInfo ti, boolean isDeadLocked, long now, Long startTimestamp) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(ti.getThreadName());

        json.writeKey("id");
        json.writeNumberLiteral(ti.getThreadId(), 0);

        if(startTimestamp != null) {
            json.writeKey("runningMillis");
            json.writeNumberLiteral(now - startTimestamp, 0);
        }

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

    @Override public void init(ASysMonApi sysMon) {
        ASysMonConfigurer.addDataSink(sysMon, runningThreadTracker);
    }
}

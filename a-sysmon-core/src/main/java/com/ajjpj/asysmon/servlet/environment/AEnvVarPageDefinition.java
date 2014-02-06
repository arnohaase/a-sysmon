package com.ajjpj.asysmon.servlet.environment;

import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.measure.environment.AEnvironmentData;
import com.ajjpj.asysmon.util.AJsonSerHelper;
import com.ajjpj.asysmon.util.AList;

import java.io.IOException;
import java.util.*;


/**
 * @author arno
 */
public class AEnvVarPageDefinition implements APresentationPageDefinition {
    private final List<PathMatcher> unsortedPathMatchers;
    private volatile ASysMonApi sysMon;

    public AEnvVarPageDefinition(String unsortedPaths) {
        unsortedPathMatchers = PathMatcher.create(unsortedPaths);
    }

    @Override public String getId() {
        return "env";
    }

    @Override public String getShortLabel() {
        return "Environment";
    }

    @Override public String getFullLabel() {
        return "System Environment";
    }

    @Override public String getHtmlFileName() {
        return "envvar.html";
    }

    @Override public String getControllerName() {
        return "CtrlEnvVar";
    }

    @Override public boolean handleRestCall(String service, List<String> params, AJsonSerHelper json) throws Exception {
        if("getData".equals(service)) {
            serveData(json);
            return true;
        }
        return false;
    }

    private void serveData(AJsonSerHelper json) throws Exception {
        json.startObject();

        json.writeKey("envTree");
        writeEnvListRec(json, getData());

        json.endObject();
    }

    private void writeEnvListRec(AJsonSerHelper json, Collection<EnvData> coll) throws IOException {
        json.startArray();

        for(EnvData data: coll) {
            json.startObject();

            json.writeKey("name");
            json.writeStringLiteral(data.segment);

            if(data.value != null) {
                json.writeKey("value");
                json.writeStringLiteral(data.value);
            }

            if(! data.children.isEmpty()) {
                json.writeKey("children");
                writeEnvListRec(json, data.children);
            }

            json.endObject();
        }

        json.endArray();
    }

    private Collection<EnvData> getData() throws Exception {
        final List<AEnvironmentData> raw = sysMon.getEnvironmentMeasurements();
        final SortedSet<EnvData> result = new TreeSet<EnvData>();

        for(AEnvironmentData data: raw) {
            mergeIntoExisting(result, data.getName(), data.getValue(), AList.<String>nil());
        }

        return result;
    }

    private void mergeIntoExisting(Collection<EnvData> existing, AList<String> key, String value, AList<String> keyPrefix) {
        if(key.tail().isEmpty()) {
            findOrCreateMatch(existing, key.head(), keyPrefix).value = value;
        }
        else {
            mergeIntoExisting(findOrCreateMatch(existing, key.head(), keyPrefix).children, key.tail(), value, keyPrefix.cons(key.head()));
        }
    }

    private EnvData findOrCreateMatch(Collection<EnvData> existing, String name, AList<String> keyPrefix) {
        for(EnvData candidate: existing) {
            if(name.equals(candidate.segment)) {
                return candidate;
            }
        }
        final EnvData result = new EnvData(name, isSorted(keyPrefix, name));
        existing.add(result);
        return result;
    }

    private boolean isSorted(AList<String> keyPrefix, String name) {
        final AList<String> effectiveKey = keyPrefix.cons(name).reverse();

        for(PathMatcher m: unsortedPathMatchers) {
            if(m.matches(effectiveKey)) {
                return false;
            }
        }
        return true;
    }


    @Override public void init(ASysMonApi sysMon) {
        this.sysMon = sysMon;
    }

    private static class EnvData implements Comparable<EnvData> {
        public final String segment;
        public String value; // may be null
        public final Collection<EnvData> children;

        private EnvData(String segment, boolean sorted) {
            children = sorted ? new TreeSet<EnvData>() : new ArrayList<EnvData>();
            this.segment = segment;
        }

        @Override public int compareTo(EnvData o) {
            return segment.compareTo(o.segment);
        }
    }
}

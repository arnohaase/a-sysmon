package com.ajjpj.asysmon.servlet.mem;

import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.datasink.ADataSink;
import com.ajjpj.asysmon.measure.special.AJmxGcMeasurerer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class remembers the most recent GC details, discarding older data to make room for newer data.
 *
 * @author arno
 */
class GcDataSink implements ADataSink {
    private final RingBuffer<GcDetails> dataBuffer;

    GcDataSink(int maxNumDetails) {
        dataBuffer = new RingBuffer<GcDetails>(GcDetails.class, maxNumDetails);
    }

    @Override public void onFinishedHierarchicalMeasurement(AHierarchicalDataRoot data) {
        if(! AJmxGcMeasurerer.IDENT_GC_TRACE_ROOT.equals(data.getRootNode().getIdentifier())) {
            return;
        }

        final Map<String, String> params = data.getRootNode().getParameters();
        for(AHierarchicalData d: data.getRootNode().getChildren()) {
            final long startMillis = d.getStartTimeMillis();
            final long durationNanos = d.getDurationNanos();
            final String gcType = d.getIdentifier();

            final String cause = params.get(AJmxGcMeasurerer.KEY_CAUSE);
            final String algorithm = params.get(AJmxGcMeasurerer.KEY_ALGORITHM);

            final GcDetails gcDetails = new GcDetails(startMillis, durationNanos, gcType, cause, algorithm);

            for(String memKind: memKinds(params.keySet())) {
                //TODO create a cache with the keys, here and in AJmxGcMeasurer?
                final long usedAfter = Long.valueOf(params.get(AJmxGcMeasurerer.KEY_PREFIX_MEM + memKind + AJmxGcMeasurerer.KEY_SUFFIX_USED));
                final long committedAfter = Long.valueOf(params.get(AJmxGcMeasurerer.KEY_PREFIX_MEM + memKind + AJmxGcMeasurerer.KEY_SUFFIX_COMMITTED));

                final long usedBefore = usedAfter - Long.valueOf(params.get(AJmxGcMeasurerer.KEY_PREFIX_MEM + memKind + AJmxGcMeasurerer.KEY_SUFFIX_USED_DELTA));
                final long committedBefore = committedAfter - Long.valueOf(params.get(AJmxGcMeasurerer.KEY_PREFIX_MEM + memKind + AJmxGcMeasurerer.KEY_SUFFIX_COMMITTED_DELTA));

                gcDetails.memDetails.add(new GcMemDetails(memKind, usedBefore, usedAfter, committedBefore, committedAfter));
            }

            dataBuffer.put(gcDetails);
        }
    }

    private Set<String> memKinds (Collection<String> paramKeys) {
        final Set<String> result = new HashSet<String>();

        for(String key: paramKeys) {
            if(! key.startsWith(AJmxGcMeasurerer.KEY_PREFIX_MEM)) {
                continue;
            }

            if(key.endsWith(AJmxGcMeasurerer.KEY_SUFFIX_USED)) {
                final String withoutPrefix = key.substring(AJmxGcMeasurerer.KEY_PREFIX_MEM.length());
                result.add(withoutPrefix.substring(0, withoutPrefix.length() - AJmxGcMeasurerer.KEY_SUFFIX_USED.length()));
            }
        }

        return result;
    }

    public Iterable<GcDetails> getData() {
        return dataBuffer;
    }

    @Override public void shutdown() throws Exception {
    }

    @Override public void onStartedHierarchicalMeasurement(String identifier) {
    }
}

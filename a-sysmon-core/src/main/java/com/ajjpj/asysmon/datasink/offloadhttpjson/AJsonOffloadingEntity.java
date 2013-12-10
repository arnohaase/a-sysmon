package com.ajjpj.asysmon.datasink.offloadhttpjson;

import com.ajjpj.asysmon.data.ACorrelationId;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.data.AHierarchicalDataRoot;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AJsonSerHelper;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author arno
 */
class AJsonOffloadingEntity extends AbstractHttpEntity {
    private final String sender;
    private final String senderInstance;

    private final List<AHierarchicalDataRoot> traces = new ArrayList<AHierarchicalDataRoot>();
    private final List<AScalarDataPoint> scalarData = new ArrayList<AScalarDataPoint>();

    AJsonOffloadingEntity(List<AHierarchicalDataRoot> traces, Collection<AScalarDataPoint> scalarData, String sender, String senderInstance) {
        setChunked(true);
        //TODO content type, encoding

        this.sender = sender;
        this.senderInstance = senderInstance;

        this.traces.addAll(traces);
        this.scalarData.addAll(scalarData);
    }

    @Override public boolean isRepeatable() {
        return false;
    }

    @Override public long getContentLength() {
        return -1; // streaming sending means chunked HTTP
    }

    @Override public InputStream getContent() throws IOException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override public void writeTo(OutputStream outstream) throws IOException {
        final AJsonSerHelper ser = new AJsonSerHelper(outstream);

        ser.startObject(); // start 'RootNode'

        ser.writeKey("sender");
        ser.writeStringLiteral(sender);

        ser.writeKey("senderInstance");
        ser.writeStringLiteral(senderInstance);

        ser.writeKey("senderTimestamp");
        ser.writeNumberLiteral(System.currentTimeMillis(), 0);

        ser.writeKey("traces");
        ser.startArray();
        for(AHierarchicalDataRoot trace: traces) {
            writeTraceRoot(ser, trace);
        }
        ser.endArray();

        ser.writeKey("scalars");
        ser.startArray();
        for(AScalarDataPoint scalar: scalarData) {
            writeScalar(ser, scalar);
        }
        ser.endArray();

        ser.endObject();
    }

    private void writeTraceRoot(AJsonSerHelper ser, AHierarchicalDataRoot trace) throws IOException {
        ser.startObject(); // start 'TraceRootNode'

        ser.writeKey("uuid");
        ser.writeStringLiteral(trace.getUuid().toString());

        ser.writeKey("startedFlows");
        ser.startArray();
        for(ACorrelationId flow: trace.getStartedFlows()) {
            writeCorrelationId(ser, flow);
        }
        ser.endArray();

        ser.writeKey("joinedFlows");
        ser.startArray();
        for(ACorrelationId flow: trace.getJoinedFlows()) {
            writeCorrelationId(ser, flow);
        }
        ser.endArray();

        ser.writeKey("trace");
        writeTraceRec(ser, trace.getRootNode());

        ser.endObject();
    }

    private void writeCorrelationId(AJsonSerHelper ser, ACorrelationId flow) throws IOException {
        ser.startObject(); // start 'CorrelationId'

        ser.writeKey("kind");
        ser.writeStringLiteral(flow.getQualifier());

        ser.writeKey("ident");
        ser.writeStringLiteral(flow.getId());

        ser.endObject();
    }

    private void writeTraceRec(AJsonSerHelper ser, AHierarchicalData trace) throws IOException {
        ser.startObject(); // start 'TraceNode'

        ser.writeKey("isSerial");
        ser.writeBooleanLiteral(trace.isSerial());

        ser.writeKey("senderStartTimeMillis");
        ser.writeNumberLiteral(trace.getStartTimeMillis(), 0);

        ser.writeKey("durationNanos");
        ser.writeNumberLiteral(trace.getDurationNanos(), 0);

        ser.writeKey("identifier");
        ser.writeStringLiteral(trace.getIdentifier());

        ser.writeKey("parameters");
        ser.startObject();
        for(String key: trace.getParameters().keySet()) {
            ser.writeKey(key);
            ser.writeStringLiteral(trace.getParameters().get(key));
        }
        ser.endObject();

        ser.writeKey("children");
        ser.startArray();
        for(AHierarchicalData child: trace.getChildren()) {
            writeTraceRec(ser, child);
        }
        ser.endArray();

        ser.endObject();
    }

    private void writeScalar(AJsonSerHelper ser, AScalarDataPoint scalar) throws IOException {
        ser.startObject(); // start 'ScalarNode'

        ser.writeKey("uuid");
        ser.writeStringLiteral(scalar.getUuid().toString());

        ser.writeKey("senderTimestamp");
        ser.writeNumberLiteral(scalar.getTimestamp(), 0);

        ser.writeKey("name");
        ser.writeStringLiteral(scalar.getName());

        ser.writeKey("value");
        ser.writeNumberLiteral(scalar.getValueRaw(), scalar.getNumFracDigits());

        ser.endObject();
    }

    @Override public boolean isStreaming() {
        return true;
    }
}

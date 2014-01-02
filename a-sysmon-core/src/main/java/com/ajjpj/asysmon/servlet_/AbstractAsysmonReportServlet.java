package com.ajjpj.asysmon.servlet_;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.servlet.AbstractASysMonServlet;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author arno
 */
public abstract class AbstractAsysmonReportServlet extends AbstractASysMonServlet {
    private static final AtomicBoolean hasShutdownHook = new AtomicBoolean(false);

    /**
     * Default implementations returns the singleton instance. Override to customize.
     */
    protected ASysMon getSysMon() {
        return ASysMon.get();
    }

    @Override protected String getDefaultHtmlName() {
        return "threaddump.html";
    }

    @Override protected boolean handleRestCall(String service, List<String> restParams, HttpServletResponse resp) throws IOException {
        if("getData".equals(service)) {
            serveData(resp);
            return true;
        }
        if("doStart".equals(service)) {
            doStartMeasurements();
            serveData(resp);
            return true;
        }
        if("doStop".equals(service)) {
            doStopMeasurements();
            serveData(resp);
            return true;
        }
        if("doClear".equals(service)) {
            doClearMeasurements();
            serveData(resp);
            return true;
        }

        return false;
    }

    private void serveData(HttpServletResponse resp) throws IOException {
        final AJsonSerHelper json = new AJsonSerHelper(resp.getOutputStream());

        json.startObject();

        json.writeKey("title");
        json.writeStringLiteral(getTitle());
        json.writeKey("isStarted");
        json.writeBooleanLiteral(isStarted());

        json.writeKey("scalars");
        json.startObject();
        for(AScalarDataPoint scalar: getSysMon().getScalarMeasurements().values()) {
            writeScalar(json, scalar);
        }
        json.endObject();

        json.writeKey("columnDefs");
        json.startArray();
        for(ColDef colDef: getColDefs()) {
            writeColDef(json, colDef);
        }
        json.endArray();

        json.writeKey("traces");
        json.startArray();
        for(TreeNode n: getData()) {
            writeDataNode(json, n);
        }
        json.endArray();

        json.endObject();
    }

    private void writeColDef(AJsonSerHelper json, ColDef colDef) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(colDef.name);

        json.writeKey("isPercentage");
        json.writeBooleanLiteral(colDef.isPercentage);

        json.writeKey("numFracDigits");
        json.writeNumberLiteral(colDef.numFracDigits, 0);

        json.writeKey("width");
        json.writeStringLiteral(colDef.width.name());

        json.endObject();
    }

    private void writeScalar(AJsonSerHelper json, AScalarDataPoint scalar) throws IOException {
        json.writeKey(scalar.getName());
        json.startObject();

        json.writeKey("value");
        json.writeNumberLiteral(scalar.getValueRaw(), scalar.getNumFracDigits());

        json.writeKey("numFracDigits");
        json.writeNumberLiteral(scalar.getNumFracDigits(), 0);

        json.endObject();
    }

    private void writeDataNode(AJsonSerHelper json, TreeNode node) throws IOException {
        json.startObject();

        json.writeKey("name");
        json.writeStringLiteral(node.identifier);

        json.writeKey("isSerial");
        json.writeBooleanLiteral(node.isSerial);

        json.writeKey("data");
        json.startArray();
        for(int i=0; i<node.colDataRaw.length; i++) {
            json.writeNumberLiteral(node.colDataRaw[i], getColDefs().get(i).numFracDigits);
        }
        json.endArray();

        if(! node.children.isEmpty()) {
            json.writeKey("children");
            json.startArray();
            for(TreeNode child: node.children) {
                writeDataNode(json, child);
            }
            json.endArray();
        }

        json.endObject();
    }

    protected abstract boolean isStarted();
    protected abstract void doStartMeasurements();
    protected abstract void doStopMeasurements();
    protected abstract void doClearMeasurements();

    protected abstract List<ColDef> getColDefs();

    protected abstract List<TreeNode> getData();

    protected enum ColWidth {Short, Medium, Long}
    protected static class ColDef {
        public final String name;
        public final boolean isPercentage;
        public final int numFracDigits;
        public final ColWidth width;

        public ColDef(String name, boolean isPercentage, int numFracDigits, ColWidth width) {
            this.name = name;
            this.isPercentage = isPercentage;
            this.numFracDigits = numFracDigits;
            this.width = width;
        }
    }

    protected static class TreeNode {
        public final String identifier;
        public final boolean isSerial;
        public final long[] colDataRaw;
        public final List<TreeNode> children;

        public TreeNode(String identifier, boolean isSerial, long[] colDataRaw, List<TreeNode> children) {
            this.identifier = identifier;
            this.isSerial = isSerial;
            this.colDataRaw = colDataRaw;
            this.children = children;
        }
    }

    protected abstract String getTitle();
}

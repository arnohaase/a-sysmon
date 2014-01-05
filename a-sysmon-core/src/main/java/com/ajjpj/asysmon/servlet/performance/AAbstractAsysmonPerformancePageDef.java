package com.ajjpj.asysmon.servlet.performance;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import java.io.IOException;
import java.util.List;

/**
 * @author arno
 */
public abstract class AAbstractAsysmonPerformancePageDef implements APresentationPageDefinition {
    @Override public String getHtmlFileName() {
        return "aggregated.html";
    }

    @Override public String getControllerName() {
        return "CtrlAggregated";
    }

    @Override public void init(ASysMon sysMon) {
    }

    @Override public boolean handleRestCall(String service, List<String> params, AJsonSerHelper json) throws IOException {
        if("getData".equals(service)) {
            serveData(json);
            return true;
        }
        if("doStart".equals(service)) {
            doStartMeasurements();
            serveData(json);
            return true;
        }
        if("doStop".equals(service)) {
            doStopMeasurements();
            serveData(json);
            return true;
        }
        if("doClear".equals(service)) {
            doClearMeasurements();
            serveData(json);
            return true;
        }

        return false;
    }

    protected abstract void doStartMeasurements();
    protected abstract void doStopMeasurements();
    protected abstract void doClearMeasurements();

    protected abstract boolean isStarted();
    protected abstract List<ColDef> getColDefs();
    protected abstract List<TreeNode> getData();

    private void serveData(AJsonSerHelper json) throws IOException {
        json.startObject();

        json.writeKey("isStarted");
        json.writeBooleanLiteral(isStarted());

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

    private void writeDataNode(AJsonSerHelper json, TreeNode node) throws IOException {
        json.startObject();

        if(node.id != null) {
            json.writeKey("id");
            json.writeStringLiteral(node.id);
        }

        json.writeKey("name");
        json.writeStringLiteral(node.label);

        if(node.tooltip != null) {
            json.writeKey("tooltip");
            json.startArray();

            for(List<String> row: node.tooltip) {
                json.startArray();

                for(String cell: row) {
                    json.writeStringLiteral(cell);
                }

                json.endArray();
            }

            json.endArray();
        }

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
        public final String id;
        public final String label;
        public final List<List<String>> tooltip; // list of rows, each row is a list of columns
        public final boolean isSerial;
        public final long[] colDataRaw;
        public final List<TreeNode> children;

        public TreeNode(String label, boolean isSerial, long[] colDataRaw, List<TreeNode> children) {
            this(null, label, null, isSerial, colDataRaw, children);
        }
        public TreeNode(String id, String label, List<List<String>> tooltip, boolean isSerial, long[] colDataRaw, List<TreeNode> children) {
            this.id = id;
            this.label = label;
            this.tooltip = tooltip;
            this.isSerial = isSerial;
            this.colDataRaw = colDataRaw;
            this.children = children;
        }
    }
}

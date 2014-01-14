package com.ajjpj.asysmon.servlet.environment;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.config.presentation.APresentationPageDefinition;
import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AJsonSerHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author arno
 */
public class AScalarPageDefinition implements APresentationPageDefinition {
    private volatile ASysMonApi sysMon;

    @Override public String getId() {
        return "scalars";
    }

    @Override public String getShortLabel() {
        return "Scalars";
    }

    @Override public String getFullLabel() {
        return "Scalar Measurements";
    }

    @Override public String getHtmlFileName() {
        return "scalars.html";
    }

    @Override public String getControllerName() {
        return "CtrlScalars";
    }

    @Override public void init(ASysMonApi sysMon) {
        this.sysMon = sysMon;
    }

    @Override public boolean handleRestCall(String service, List<String> params, AJsonSerHelper json) throws IOException {
        if("getData".equals(service)) {
            serveData(json);
            return true;
        }

        return false;
    }

    private void serveData(AJsonSerHelper json) throws IOException {
        final Map<String, AScalarDataPoint> scalars = sysMon.getScalarMeasurements();

        json.startObject();

        json.writeKey("scalars");
        json.startObject();

        for(AScalarDataPoint scalar: scalars.values()) {
            writeScalar(json, scalar);
        }

        json.endObject();
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

}

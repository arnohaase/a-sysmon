package com.ajjpj.asysmon.server.connector.httpjson;

import com.ajjpj.asysmon.server.Components;
import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.data.json.EnvironmentNode;
import com.ajjpj.asysmon.server.data.json.RootNode;
import com.ajjpj.asysmon.server.data.json.ScalarNode;
import com.ajjpj.asysmon.server.data.json.TraceRootNode;
import com.ajjpj.asysmon.server.processing.InputProcessor;
import org.codehaus.jackson.map.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author arno
 */
public class JsonConnectorServlet extends HttpServlet {
    @Override protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ObjectMapper om = new ObjectMapper();
        final RootNode root = om.readValue(req.getInputStream(), RootNode.class);

        final InputProcessor processor = getProcessor();
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier(root.getSender(), root.getSenderInstance());

        processor.updateSystemClockDiff(instanceIdentifier, root.getSenderTimestamp());

        for(EnvironmentNode envNode: root.getEnvironment()) {
            processor.addEnvironmentEntry(instanceIdentifier, envNode);
        }
        for(ScalarNode scalarNode: root.getScalars()) {
            processor.addScalarEntry(instanceIdentifier, scalarNode);
        }
        for(TraceRootNode traceNode: root.getTraces()) {
            processor.addTraceEntry(instanceIdentifier, traceNode);
        }
    }

    protected InputProcessor getProcessor() {
        return Components.get().getInputProcessor();
    }
}

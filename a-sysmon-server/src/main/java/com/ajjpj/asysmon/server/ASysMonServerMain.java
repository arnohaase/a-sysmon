package com.ajjpj.asysmon.server;

import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.config.ASysMonServerConfigBuilder;
import com.ajjpj.asysmon.server.connector.httpjson.JsonConnectorServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author arno
 */
public class ASysMonServerMain {
    public static void main(String[] args) throws Exception {
        final ASysMonServerConfig config = new ASysMonServerConfigBuilder()
                .build();
        Components.init(config);

        final Server server = new Server(config.getUploadPortNumber());

        final ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath("/");
        server.setHandler(ctx);

        ctx.addServlet(JsonConnectorServlet.class, "/upload");

        server.start();
        server.join();



//        Tomcat tomcat = new Tomcat();
//        tomcat.setPort(config.getUploadPortNumber());
//
//        tomcat.setBaseDir(".");
//
//        final Context ctx = tomcat.addWebapp("/asysmon", "asysmon");
//        Tomcat.addServlet(ctx, "asysmon", "com.ajjpj.asysmon.server.connector.httpjson.JsonConnectorServlet");
//        ctx.addServletMapping("/asysmon", "asysmon");
//
//        tomcat.start();
//        tomcat.getServer().await();
    }
}

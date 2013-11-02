package com.ajjpj.asysmon.demo;


import com.ajjpj.asysmon.config.ADefaultSysMonConfig;
import com.ajjpj.asysmon.datasink.log.AStdOutDataSink;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author arno
 */
public class AsysmonDemoMain {
    public static void main(String[] args) throws Exception {
//        System.setProperty("com.ajjpj.asysmon.globallydisabled", "true");

        ADefaultSysMonConfig.addHandler(new AStdOutDataSink());

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}

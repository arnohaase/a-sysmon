package com.ajjpj.asysmon.demo;


import com.ajjpj.asysmon.config.AStaticSysMonConfig;
import com.ajjpj.asysmon.measure.threadpool.AThreadPoolTrackingDataSink;
import com.ajjpj.asysmon.processing.log.AStdOutDataSink;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ServiceLoader;

/**
 * @author arno
 */
public class AsysmonDemoMain {
    public static void main(String[] args) throws Exception {
        AStaticSysMonConfig.addHandler(new AStdOutDataSink());

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}

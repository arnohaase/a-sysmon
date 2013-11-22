package com.ajjpj.asysmon.demo;


import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.cyclicdump.ALog4JInfoCyclicMeasurementDumper;
import com.ajjpj.asysmon.datasink.log.AStdOutDataSink;
import com.ajjpj.asysmon.datasink.offloadhttpjson.AHttpJsonOffloadingDataSink;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author arno
 */
public class AsysmonDemoMain {
    public static void main(String[] args) throws Exception {
//        System.setProperty("com.ajjpj.asysmon.globallydisabled", "true");

        new ALog4JInfoCyclicMeasurementDumper(ASysMon.get(), 120);

        ASysMonConfigurer.addDataSink(ASysMon.get(), new AStdOutDataSink());
//        ASysMonConfigurer.addDataSink(ASysMon.get(), new ALog4JDataSink());
        ASysMonConfigurer.addThreadCountSupport(ASysMon.get());
        ASysMonConfigurer.addDataSink(ASysMon.get(), new AHttpJsonOffloadingDataSink(ASysMon.get(), "http://localhost:8899/upload", "demo", "the-instance", 100, 1000, 1, 10*1000));

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}



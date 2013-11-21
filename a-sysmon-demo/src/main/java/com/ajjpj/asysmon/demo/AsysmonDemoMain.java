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
        ASysMonConfigurer.addDataSink(ASysMon.get(), new AHttpJsonOffloadingDataSink("http://localhost:8899/upload"));

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}


/*



/opt/jdk1.6/bin/java -Dvisualvm.id=65791258886995 -Didea.launcher.port=7540 -Didea.launcher.bin.path=/home/arno/eclipse/idea-IU-132.1045/bin -Dfile.encoding=UTF-8 -classpath /opt/jdk1.6/jre/lib/charsets.jar:/opt/jdk1.6/jre/lib/rt.jar:/opt/jdk1.6/jre/lib/jce.jar:/opt/jdk1.6/jre/lib/javaws.jar:/opt/jdk1.6/jre/lib/management-agent.jar:/opt/jdk1.6/jre/lib/plugin.jar:/opt/jdk1.6/jre/lib/deploy.jar:/opt/jdk1.6/jre/lib/jsse.jar:/opt/jdk1.6/jre/lib/resources.jar:/opt/jdk1.6/jre/lib/ext/sunpkcs11.jar:/opt/jdk1.6/jre/lib/ext/sunjce_provider.jar:/opt/jdk1.6/jre/lib/ext/dnsns.jar:/opt/jdk1.6/jre/lib/ext/localedata.jar:/home/arno/ws/a-sysmon/a-sysmon-demo/target/classes:/home/arno/ws/a-sysmon/a-sysmon-core/target/classes:/home/arno/.m2/repository/org/eclipse/jetty/jetty-webapp/8.1.11.v20130520/jetty-webapp-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-xml/8.1.11.v20130520/jetty-xml-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-util/8.1.11.v20130520/jetty-util-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-servlet/8.1.11.v20130520/jetty-servlet-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-security/8.1.11.v20130520/jetty-security-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-server/8.1.11.v20130520/jetty-server-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/orbit/javax.servlet/3.0.0.v201112011016/javax.servlet-3.0.0.v201112011016.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-continuation/8.1.11.v20130520/jetty-continuation-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-http/8.1.11.v20130520/jetty-http-8.1.11.v20130520.jar:/home/arno/.m2/repository/org/eclipse/jetty/jetty-io/8.1.11.v20130520/jetty-io-8.1.11.v20130520.jar:/home/arno/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/home/arno/.m2/repository/com/h2database/h2/1.3.172/h2-1.3.172.jar:/home/arno/eclipse/idea-IU-132.1045/lib/idea_rt.jar com.intellij.rt.execution.application.AppMain com.ajjpj.asysmon.demo.AsysmonDemoMain




 */
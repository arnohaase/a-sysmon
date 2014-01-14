package com.ajjpj.asysmon.demo;


import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.measure.scalar.AJmxGcMeasurerer;
import com.ajjpj.asysmon.measure.scalar.AJmxMemMeasurer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * @author arno
 */
public class AsysmonDemoMain {
    public static void main(String[] args) throws Exception {
        new DeadlockThread().start();

//        System.setProperty("com.ajjpj.asysmon.globallydisabled", "true");

//        new ALog4JInfoCyclicMeasurementDumper(ASysMon.get(), 120);

        ASysMonConfigurer.addThreadCountSupport(ASysMon.get()); //TODO unify the mechanism with GC support

//        ASysMonConfigurer.addDataSink(ASysMon.get(), new AHttpJsonOffloadingDataSink(ASysMon.get(), "http://localhost:8899/upload", "demo", "the-instance", 100, 1000, 1, 10*1000));

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        generateArtificialGcs();

        server.start();
        server.join();
    }

    private static void generateArtificialGcs() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                while (true) {
                    System.gc();
                    try {
                        Thread.sleep(10000 + new Random().nextInt(20000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override public void run() {
                final List<Object> l = new ArrayList<>();

                while (true) {
                    l.clear();
                    for(int i=0; i<10000 + new Random().nextInt(10000); i++) {
                        l.add(new GregorianCalendar());
                    }

                    try {
                        Thread.sleep(500 + new Random().nextInt(500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }
}



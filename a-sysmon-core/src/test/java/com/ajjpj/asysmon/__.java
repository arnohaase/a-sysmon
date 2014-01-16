package com.ajjpj.asysmon;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author arno
 */
public class __ {
    public static final int COUNT = 10*1000;

    public static void main(String[] args) {
        final __ o = new __();

        o.loop();

        new Thread() {
            @Override
            public void run() {
                while(true)
                    o.loop();
            }
        }.start();

        thrash();

        final long start = System.nanoTime();
        o.loop();
        final long end = System.nanoTime();

        System.out.println("duration: " + (end - start)/COUNT + "ns");
    }

    private static void thrash() {
        for(int i=0; i<100*1000; i++) {
            new Random().nextGaussian();
        }
    }

    private void loop() {
        for(int i=0; i<COUNT; i++) {
            doIt();
        }
    }

//    public final ARunningThreadTrackingDataSink ds = new ARunningThreadTrackingDataSink();
    public final ConcurrentHashMap<String, Object> m = new ConcurrentHashMap<String, Object>();

    private void doIt() {
//        ds.onStartedHierarchicalMeasurement(null);
//        ds.onFinishedHierarchicalMeasurement(null);
//        m.put(Thread.currentThread().getName(), System.currentTimeMillis());
    }
}

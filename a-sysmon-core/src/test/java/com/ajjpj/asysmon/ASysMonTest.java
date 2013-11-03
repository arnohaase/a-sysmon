package com.ajjpj.asysmon;


import com.ajjpj.asysmon.config.AGlobalConfig;
import com.ajjpj.asysmon.config.ASysMonConfigBuilder;
import com.ajjpj.asysmon.config.log.AStdOutLogger;
import com.ajjpj.asysmon.data.AHierarchicalData;
import com.ajjpj.asysmon.measure.ACollectingMeasurement;
import com.ajjpj.asysmon.measure.AMeasurementHierarchy;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.testutil.CollectingDataSink;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class ASysMonTest {
    @Before
    public void before() {
        AGlobalConfig.setLogger(AStdOutLogger.INSTANCE);
    }

    @Test
    public void testSimpleMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        sysMon.start("a").finish();

        assertEquals(1, dataSink.data.size());
        assertEquals("a", dataSink.data.get(0).getIdentifier());
    }

    @Test
    public void testNestedMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ASimpleMeasurement m = sysMon.start("a");
        sysMon.start("b").finish();
        sysMon.start("c").finish();
        m.finish();

        sysMon.start("d").finish();

        assertEquals(2, dataSink.data.size());
        assertEquals("a", dataSink.data.get(0).getIdentifier());
        assertEquals("b", dataSink.data.get(0).getChildren().get(0).getIdentifier());
        assertEquals("c", dataSink.data.get(0).getChildren().get(1).getIdentifier());

        assertEquals("d", dataSink.data.get(1).getIdentifier());
    }

    @Test
    public void testCollectingMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ASimpleMeasurement m = sysMon.start("a");
        final ACollectingMeasurement coll = sysMon.startCollectingMeasurement("b");

        coll.startDetail("c1");

        sysMon.start("d").finish();

        coll.finishDetail();

        coll.startDetail("c3");
        coll.addDetailMeasurement("c2", 123);
        coll.finishDetail();

        coll.finish();
        m.finish();

        assertEquals(1, dataSink.data.size());

        final AHierarchicalData root = dataSink.data.get(0);
        assertEquals(2, root.getChildren().size());

        assertEquals("d", root.getChildren().get(0).getIdentifier());

        final AHierarchicalData data = root.getChildren().get(1);
        assertEquals("b", data.getIdentifier());
        assertEquals(3, data.getChildren().size());
        assertEquals("c1", data.getChildren().get(0).getIdentifier());
        assertEquals("c2", data.getChildren().get(1).getIdentifier());
        assertEquals("c3", data.getChildren().get(2).getIdentifier());
    }

    @Test
    public void testOverlappingDetails() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ASimpleMeasurement m = sysMon.start("a");
        final ACollectingMeasurement coll = sysMon.startCollectingMeasurement("b");

        coll.startDetail("c1");
        try {
            coll.startDetail("c2");
            fail("exception expected");
        }
        catch(Exception exc) {
            // expected
        }

        m.finish();
    }

    @Test
    public void testParallelMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        // NB: Parallel measurements do not nest. They can have no children, and they can be closed at any point,
        //      regardless of hierarchy (i.e. as long as the root measurement is not finished).

        final ASimpleMeasurement a = sysMon.start("a");
        final ASimpleMeasurement c1 = sysMon.start("c1", false);

        final ASimpleMeasurement b = sysMon.start("b", true);
        final ASimpleMeasurement c2 = sysMon.start("c2", false);
        final ASimpleMeasurement c3 = sysMon.start("c3", false);

        sysMon.start("d", true).finish();

        c3.finish();
        c1.finish();
        b.finish();
        c2.finish();
        a.finish();

        assertEquals(1, dataSink.data.size());
        final AHierarchicalData ma = dataSink.data.get(0);

        // c1 and b were started directly within ma
        assertEquals(2, ma.getChildren().size());

        // c1 was closed first
        assertEquals("c1", ma.getChildren().get(0).getIdentifier());
        assertEquals("b", ma.getChildren().get(1).getIdentifier());

        final AHierarchicalData mb = ma.getChildren().get(1);

        // c2 and c3 were *started* within b, and that's what determines their data's position in the measurement hierarchy
        assertEquals(3, mb.getChildren().size());

        assertEquals("d", mb.getChildren().get(0).getIdentifier());

        // c3 was finished first
        assertEquals("c3", mb.getChildren().get(1).getIdentifier());
        assertEquals("c2", mb.getChildren().get(2).getIdentifier());
    }

    @Test
    public void testFinishRootWithUnfinishedCollectingMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ASimpleMeasurement m = sysMon.start("m");
        final ACollectingMeasurement a = sysMon.startCollectingMeasurement("a");
        m.finish();

        assertEquals(1, dataSink.data.size());
        assertEquals(1, dataSink.data.get(0).getChildren().size());

        try {
            a.finish();
            fail("exception expected");
        }
        catch(IllegalStateException exc) {
            // expected
        }

        assertEquals(1, dataSink.data.size());
        assertEquals(1, dataSink.data.get(0).getChildren().size());
    }

    @Test
    public void testTimerRecording() {
        fail("todo");
    }

    @Test
    public void testTopLevelCollectingMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        // top-level collecting measurement is permitted, and A-SysMon implicitly creates (and finishes) a
        //  simple measurement that serves as a root wrapper
        sysMon.startCollectingMeasurement("a").finish();

        assertEquals(1, dataSink.data.size());

        final AHierarchicalData root = dataSink.data.get(0);
        assertEquals(AMeasurementHierarchy.IDENT_SYNTHETIC_ROOT, root.getIdentifier());

        assertEquals(1, root.getChildren().size());
        assertEquals("a", root.getChildren().get(0).getIdentifier());
    }

    @Test
    public void testTopLevelNestedCollectingMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ACollectingMeasurement a = sysMon.startCollectingMeasurement("a");
        final ACollectingMeasurement b = sysMon.startCollectingMeasurement("b");
        a.finish();
        assertEquals(0, dataSink.data.size());
        b.finish();
        assertEquals(1, dataSink.data.size());

        final AHierarchicalData root = dataSink.data.get(0);
        assertEquals(AMeasurementHierarchy.IDENT_SYNTHETIC_ROOT, root.getIdentifier());

        assertEquals(2, root.getChildren().size());
        assertEquals("a", root.getChildren().get(0).getIdentifier());
        assertEquals("b", root.getChildren().get(1).getIdentifier());
    }

    @Test
    public void testTopLevelCollectingAndSimpleMeasurement() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        // If a top-level collecting measurement is started, and then a simple measurement is started before
        //  that collecting measurement is finished, the implicitly created synthetic root measurement is finished
        //  only after both measurements are finished.
        // That is pretty pathological behavior (and triggers a warning), but it is better than throwing an exception
        //  and interfering with regular functionality of an instrumented application.

        final ACollectingMeasurement coll = sysMon.startCollectingMeasurement("a");
        final ASimpleMeasurement m = sysMon.start("m");
        coll.finish();
        m.finish();

        assertEquals(1, dataSink.data.size());

        final AHierarchicalData root = dataSink.data.get(0);
        assertEquals(AMeasurementHierarchy.IDENT_SYNTHETIC_ROOT, root.getIdentifier());

        assertEquals(2, root.getChildren().size());
        assertEquals("a", root.getChildren().get(0).getIdentifier());
        assertEquals("m", root.getChildren().get(1).getIdentifier());
    }

    @Test
    public void testImplicitCloseChildAndDescendants() {
        final CollectingDataSink dataSink = new CollectingDataSink();
        final ASysMon sysMon = new ASysMonConfigBuilder()
                .withDataSink(dataSink)
                .build();

        final ASimpleMeasurement m = sysMon.start("m");
        sysMon.start("a");
        sysMon.start("b");

        // a and b were not finished explicitly
        m.finish();

        assertEquals(1, dataSink.data.size());
        assertEquals(1, dataSink.data.get(0).getChildren().size());
        assertEquals(1, dataSink.data.get(0).getChildren().get(0).getChildren().size());
    }
}

//TODO test top-level parallel measurement: should be rejected
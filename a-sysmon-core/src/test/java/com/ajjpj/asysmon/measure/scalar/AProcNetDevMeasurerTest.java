package com.ajjpj.asysmon.measure.scalar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * @author arno
 */
public class AProcNetDevMeasurerTest {
    static final String DATA_1 =
            "Inter-|   Receive                                                |  Transmit\n" +
            " face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed\n" +
            "  eth0:       0       0    0    0    0     0          0         0        0       0    0    0    0     1       0          0\n" +
            "    lo:  197331    1907    0    0    0     0          0         0   197331    1907    0    0    0     2       0          0\n" +
            " wlan0: 290141208  199237    0    0    0     0          0         0 11169874  112278    0    0    0     3       0          0\n";

    static final String DATA_2 =
            "Inter-| Receive | Transmit\n" +
            "face |bytes packets errs drop fifo frame compressed multicast|bytes packets errs drop fifo colls carrier compressed\n" +
            "lo:867736240 8499431 0 0 0 0 0 0 867736240 8499431 0 0 0 4 0 0\n" +
            "eth0:1101013679977 1629454605 0 0 0 0 0 7912999 1661278422272 1511248593 0 0 0 5 0 0";

    @Test
    public void test1() throws Exception {
        final AProcNetDevMeasurer.Snapshot snapshot = AProcNetDevMeasurer.createSnapshot(Arrays.asList(DATA_1.split("\n")));
        assertEquals(3, snapshot.bytesReceived.size());

        assertEquals(0, (long) snapshot.bytesReceived.get("eth0"));
        assertEquals(197331, (long) snapshot.bytesReceived.get("lo"));
        assertEquals(290141208, (long) snapshot.bytesReceived.get("wlan0"));

        assertEquals(0, (long) snapshot.packetsReceived.get("eth0"));
        assertEquals(1907, (long) snapshot.packetsReceived.get("lo"));
        assertEquals(199237, (long) snapshot.packetsReceived.get("wlan0"));

        assertEquals(0, (long) snapshot.bytesSent.get("eth0"));
        assertEquals(197331, (long) snapshot.bytesSent.get("lo"));
        assertEquals(11169874, (long) snapshot.bytesSent.get("wlan0"));

        assertEquals(0, (long) snapshot.packetsSent.get("eth0"));
        assertEquals(1907, (long) snapshot.packetsSent.get("lo"));
        assertEquals(112278, (long) snapshot.packetsSent.get("wlan0"));

        assertEquals(1, (long) snapshot.collisions.get("eth0"));
        assertEquals(2, (long) snapshot.collisions.get("lo"));
        assertEquals(3, (long) snapshot.collisions.get("wlan0"));
    }

    @Test
    public void test2() throws Exception {
        final AProcNetDevMeasurer.Snapshot snapshot = AProcNetDevMeasurer.createSnapshot(Arrays.asList(DATA_2.split("\n")));
        assertEquals(2, snapshot.bytesReceived.size());

        assertEquals(867736240, (long) snapshot.bytesReceived.get("lo"));
        assertEquals(1101013679977L, (long) snapshot.bytesReceived.get("eth0"));

        assertEquals(8499431, (long) snapshot.packetsReceived.get("lo"));
        assertEquals(1629454605L, (long) snapshot.packetsReceived.get("eth0"));

        assertEquals(867736240, (long) snapshot.bytesSent.get("lo"));
        assertEquals(1661278422272L, (long) snapshot.bytesSent.get("eth0"));

        assertEquals(8499431, (long) snapshot.packetsSent.get("lo"));
        assertEquals(1511248593L, (long) snapshot.packetsSent.get("eth0"));

        assertEquals(4, (long) snapshot.collisions.get("lo"));
        assertEquals(5, (long) snapshot.collisions.get("eth0"));
    }
}

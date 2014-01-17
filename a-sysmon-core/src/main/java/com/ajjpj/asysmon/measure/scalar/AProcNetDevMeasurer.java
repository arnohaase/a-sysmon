package com.ajjpj.asysmon.measure.scalar;

import com.ajjpj.asysmon.data.AScalarDataPoint;
import com.ajjpj.asysmon.util.AStatement1;
import com.ajjpj.asysmon.util.io.AFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author arno
 */
public class AProcNetDevMeasurer implements AScalarMeasurer {
    public static final AFile PROC_NET_DEV = new AFile("/proc/net/dev");

    public static final String KEY_PREFIX = "net:";
    public static final String KEY_MEMENTO = KEY_PREFIX;
    public static final String KEY_SUFFIX_RECEIVED_BYTES = ":received-bytes";
    public static final String KEY_SUFFIX_RECEIVED_PACKETS = ":received-pkt";
    public static final String KEY_SUFFIX_SENT_BYTES = ":sent-bytes";
    public static final String KEY_SUFFIX_SENT_PACKETS = ":sent-pkt";
    public static final String KEY_SUFFIX_COLLISIONS = ":collisions";

    @Override public void prepareMeasurements(Map<String, Object> mementos) throws Exception {
        mementos.put(KEY_MEMENTO, createSnapshot());
    }

    @Override public void contributeMeasurements(Map<String, AScalarDataPoint> data, long timestamp, Map<String, Object> mementos) throws Exception {
        final Snapshot prev = (Snapshot) mementos.get(KEY_MEMENTO);
        final Snapshot cur = createSnapshot();

        final long diffTime = cur.timestamp - prev.timestamp;

        for(String iface: new TreeSet<String>(cur.packetsReceived.keySet())) {
            final long receivedBytes   = cur.bytesReceived  .get(iface) - prev.bytesReceived.  get(iface);
            final long receivedPackets = cur.packetsReceived.get(iface) - prev.packetsReceived.get(iface);
            final long sentBytes       = cur.bytesSent.      get(iface) - prev.bytesSent.      get(iface);
            final long sentPackets     = cur.packetsSent.    get(iface) - prev.packetsSent.    get(iface);
            final long collisions      = cur.collisions.     get(iface) - prev.collisions.     get(iface);

            {
                final String key = getKeyReceivedBytes(iface);
                data.put(key, new AScalarDataPoint(timestamp, key, receivedBytes * 10*1000 / diffTime, 1));
            }
            {
                final String key = getKeyReceivedPackets(iface);
                data.put(key, new AScalarDataPoint(timestamp, key, receivedPackets * 10*1000 / diffTime, 1));
            }
            {
                final String key = getKeySentBytes(iface);
                data.put(key, new AScalarDataPoint(timestamp, key, sentBytes * 10*1000 / diffTime, 1));
            }
            {
                final String key = getKeySentPackets(iface);
                data.put(key, new AScalarDataPoint(timestamp, key, sentPackets * 10*1000 / diffTime, 1));
            }
            {
                final String key = getKeyCollisions(iface);
                data.put(key, new AScalarDataPoint(timestamp, key, collisions * 10*1000 / diffTime, 1));
            }
        }
    }

    public static String getKeyReceivedBytes(String iface) {
        return KEY_PREFIX + iface + KEY_SUFFIX_RECEIVED_BYTES;
    }
    public static String getKeyReceivedPackets(String iface) {
        return KEY_PREFIX + iface + KEY_SUFFIX_RECEIVED_PACKETS;
    }

    public static String getKeySentBytes(String iface) {
        return KEY_PREFIX + iface + KEY_SUFFIX_SENT_BYTES;
    }
    public static String getKeySentPackets(String iface) {
        return KEY_PREFIX + iface + KEY_SUFFIX_SENT_PACKETS;
    }

    public static String getKeyCollisions(String iface) {
        return KEY_PREFIX + iface + KEY_SUFFIX_COLLISIONS;
    }

    private Snapshot createSnapshot() throws IOException {
        final Snapshot result = new Snapshot();

        PROC_NET_DEV.iterate(Charset.defaultCharset(), new AStatement1<Iterator<String>, IOException>() {
            @Override public void apply(Iterator<String> iter) throws IOException {
                while(iter.hasNext()) {
                    final String line = iter.next();
                    final String[] split = line.trim().split("\\s+");
                    if(split.length < 15) {
                        continue;
                    }
                    if(! split[0].endsWith(":")) {
                        continue;
                    }
                    final String iface = split[0].substring(0, split[0].length() - 1).trim();

                    final long bytesReceived   = Long.valueOf(split[1]);
                    final long packetsReceived = Long.valueOf(split[2]);
                    final long bytesSent       = Long.valueOf(split[9]);
                    final long packetsSent     = Long.valueOf(split[10]);
                    final long collisions      = Long.valueOf(split[14]);

                    result.add(iface, bytesReceived, packetsReceived, bytesSent, packetsSent, collisions);
                }
            }
        });

        return result;
    }

    @Override public void shutdown() throws Exception {
    }

    private static class Snapshot {
        final long timestamp = System.currentTimeMillis();
        final Map<String, Long> bytesReceived = new HashMap<String, Long>();
        final Map<String, Long> packetsReceived = new HashMap<String, Long>();
        final Map<String, Long> bytesSent = new HashMap<String, Long>();
        final Map<String, Long> packetsSent = new HashMap<String, Long>();
        final Map<String, Long> collisions = new HashMap<String, Long>();

        void add(String iface, long bytesReceived, long packetsReceived, long bytesSent, long packetsSent, long collisons) {
            this.bytesReceived.  put(iface, bytesReceived);
            this.packetsReceived.put(iface, packetsReceived);
            this.bytesSent.      put(iface, bytesSent);
            this.packetsSent.    put(iface, packetsSent);
            this.collisions.     put(iface, collisons);
        }
    }
}

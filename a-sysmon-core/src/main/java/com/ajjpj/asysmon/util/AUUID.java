package com.ajjpj.asysmon.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a far more efficient alternative to using java.util.UUID.randomUUID(), which uses a SecureRandom and
 *  uses excessive amounts of time.<p />
 *
 * NB: This class does <em>not</em> produce 'real' UUID (i.e. its results do not conform to RFC 4122). It does however
 *  create 128 Bit identifiers that are pretty much guaranteed to be unique.<p />
 *
 * NB: This implementation assumes that the system time progresses monotonously, i.e. it never moves 'backwards'.
 *
 * @author arno
 */
public class AUUID {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final byte[] jvmIdentifier = createUniquePart();
    private static final AtomicInteger counter = new AtomicInteger(0);

    private final byte[] data;

    public static AUUID createRandom() {
        final int count = counter.incrementAndGet();
        final long timestamp = System.currentTimeMillis();

        final ByteBuffer result = ByteBuffer.allocate(16);
        result.put(jvmIdentifier);
        result.putLong(timestamp);
        result.putShort((short) count);
        return fromBytes(result.array());
    }

    public static AUUID fromBytes(byte[] data) {
        return new AUUID(data);
    }

    public static AUUID fromString(String data) {
        return AUUID.fromBytes(hexStringToByteArray(data));
    }

    private AUUID(byte[] data) {
        this.data = data;
    }

    public String toString() {
        return bytesToHex(data);
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static byte[] createUniquePart() {
        final byte[] result = new byte[6];
        new SecureRandom().nextBytes(result);
        return result;
    }
}

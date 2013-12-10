package com.ajjpj.asysmon.util;

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AUUIDTest {
    @Test
    public void testFromToString() {
        final AUUID uuid = AUUID.createRandom();
        final String s = uuid.toString();
        final AUUID fromString = AUUID.fromString(s);

        assertEquals(uuid, fromString);
    }
}

package com.ajjpj.asysmon;


import org.junit.Test;

/**
 * @author arno
 */
public class ASysMonTest {
    @Test
    public void testSimpleMeasurement() {

        ASysMon.get().start("a").finish();
    }
}

package com.ajjpj.asysmon.nothirdparty;

import com.ajjpj.asysmon.config.ADefaultConfigFactory;
import com.ajjpj.asysmon.config.log.AStdOutLoggerFactory;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author arno
 */
public class NoThirdPartyTest {
    @Test
    public void testDefaultLogger() {
        assertEquals(AStdOutLoggerFactory.class, ADefaultConfigFactory.getConfiguredLogger().getClass());
    }
}

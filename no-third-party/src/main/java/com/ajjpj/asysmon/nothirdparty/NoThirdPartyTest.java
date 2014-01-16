package com.ajjpj.asysmon.nothirdparty;

import com.ajjpj.asysmon.config.ADefaultConfigFactory;
import com.ajjpj.asysmon.config.log.AStdOutLoggerFactory;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * @author arno
 */
public class NoThirdPartyTest {
    public static void main(String[] args) {
        if(AStdOutLoggerFactory.class == ADefaultConfigFactory.getConfiguredLogger().getClass()) {
            throw new Error("FAILED: " + ADefaultConfigFactory.getConfiguredLogger().getClass().getName());
        }

        System.out.println("OK");
    }
}

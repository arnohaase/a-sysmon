package com.ajjpj.asysmon.server.processing;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;

/**
 * @author arno
 */
public class SystemClockCorrectorNullImpl implements SystemClockCorrector {
    @Override public void updateSystemClockDiff(InstanceIdentifier instance, long senderTimestamp) {
    }

    @Override public long correctedTimestamp(InstanceIdentifier instance, long rawTimestamp) {
        return rawTimestamp;
    }
}

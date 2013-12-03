package com.ajjpj.asysmon.server.upload.preprocess.impl;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;
import com.ajjpj.asysmon.server.upload.preprocess.SystemClockCorrector;

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

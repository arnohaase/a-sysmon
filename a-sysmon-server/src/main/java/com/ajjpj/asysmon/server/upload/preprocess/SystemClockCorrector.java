package com.ajjpj.asysmon.server.upload.preprocess;

import com.ajjpj.asysmon.server.data.InstanceIdentifier;

/**
 * The system clocks on different machines can differ significantly. To facilitate comparison of measurements from
 *  different machines, all raw timestamps are passed through an implementation of this interface to convert 'raw'
 *  timestamps into 'adjusted' timestamps. <p />
 *
 * Different system environments may require different implementations, therefore there are different implementations
 *  of this interface. The simplest of these leaves all timestamps unmodified.
 *
 * TODO real adjusting implementations
 *
 * @author arno
 */
public interface SystemClockCorrector {
    void updateSystemClockDiff(InstanceIdentifier instance, long senderTimestamp);
    long correctedTimestamp(InstanceIdentifier instance, long rawTimestamp);
}

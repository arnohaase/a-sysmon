package com.ajjpj.asysmon.measure;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a builder class representing an ongoing thread specific measurement.
 *
 * @author arno
 */
public interface ASimpleMeasurement extends AWithParameters {
    void finish();
}

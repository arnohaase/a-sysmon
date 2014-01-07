package com.ajjpj.asysmon.server.storage;


/**
 * This class represents meta data for a single kind of environment measurement.
 *
 * @author arno
 */
public class ScalarMetaData {
    public final String name;
    public final int numFracDigits;

    //TODO ttl


    public ScalarMetaData(String name, int numFracDigits) {
        this.name = name;
        this.numFracDigits = numFracDigits;
    }
}

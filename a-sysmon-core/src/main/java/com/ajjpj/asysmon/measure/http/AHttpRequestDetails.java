package com.ajjpj.asysmon.measure.http;

import com.ajjpj.abase.collection.immutable.AOption;

import java.util.Map;

/**
 * @author arno
 */
public interface AHttpRequestDetails {
    /**
     * @return the string to be used as an identifier for this measurement, or AOption.none() if this HTTP request
     *  should be ignored for measurement. The result should be the same for all requests
     *  that 'do the same thing, just with different data'. It is perfectly okay to always return the same string if
     *  there is no need to differentiate between different requests for measurement purposes at this level. Or the
     *  the URL can be returned as an identifier.<p />
     *  For RESTful URLs however it can e.g. be useful to strip off the 'parameter' part of the URL and just return
     *  the 'service' part.
     */
    AOption<String> getIdentifier();

    /**
     * @return stuff that varies for 'the same kind of' requests. That is stuff that is irrelevant for statistical
     *  analysis but provides useful insight in a per-request analysis. It can be stuff like the originating IP
     *  address, an HTTP session identifier (or even all cookies), business data provided as parameters to a
     *  RESTful call, or whatever else helps you in your specific system landscape.
     */
    Map<String, String> getParameters();
}

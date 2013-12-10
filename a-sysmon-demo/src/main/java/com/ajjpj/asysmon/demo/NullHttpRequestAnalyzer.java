package com.ajjpj.asysmon.demo;

import com.ajjpj.asysmon.measure.http.AHttpRequestAnalyzer;
import com.ajjpj.asysmon.measure.http.AHttpRequestDetails;
import com.ajjpj.asysmon.measure.http.ASimpleHttpRequestAnalyzer;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This class removes the default behavior of aggregating HTTP request. It is
 *
 *   JUST HELPFUL FOR THIS DEMO - DO NOT DO THIS FOR PRODUCTION CODE!!!
 *
 *
 * @author arno
 */
public class NullHttpRequestAnalyzer extends ASimpleHttpRequestAnalyzer {
    @Override protected String extractIdentifier(String url) {
        return url;
    }
}

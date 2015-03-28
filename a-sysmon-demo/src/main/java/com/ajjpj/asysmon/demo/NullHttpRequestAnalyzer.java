package com.ajjpj.asysmon.demo;

import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.asysmon.measure.http.ASimpleHttpRequestAnalyzer;

/**
 * This class removes the default behavior of aggregating HTTP request. It is
 *
 *   JUST FOR THIS DEMO - DO NOT DO THIS FOR PRODUCTION CODE!!!
 *
 *
 * @author arno
 */
public class NullHttpRequestAnalyzer extends ASimpleHttpRequestAnalyzer {
    @Override protected AOption<String> extractIdentifier(String url) {
        if(url.contains("asysmon")) {
            return AOption.none();
        }

        return AOption.some(url);
    }
}

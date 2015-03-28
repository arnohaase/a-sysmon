package com.ajjpj.asysmon.measure.http;

import com.ajjpj.afoundation.collection.immutable.AOption;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author arno
 */
public class ASimpleHttpRequestAnalyzer implements AHttpRequestAnalyzer {
    public static final String PARAM_FULL_URL = "http-full-url";
    public static final String PARAM_REMOTE_ADDR = "http-remote-addr";
    public static final String PARAM_PREFIX_COOKIE = "http-cookie-";

    @Override public AHttpRequestDetails analyze(HttpServletRequest request) {
        final AOption<String> identifier = extractIdentifier(request.getRequestURL().toString());
        final Map<String, String> parameters = extractParameters(request);

        return new AHttpRequestDetails() {
            @Override public AOption<String> getIdentifier() {
                return identifier;
            }
            @Override public Map<String, String> getParameters() {
                return parameters;
            }
        };
    }

    protected Map<String, String> extractParameters(HttpServletRequest request) {
        final Map<String, String> result = new HashMap<String, String>();

        result.put(PARAM_FULL_URL, request.getRequestURL().toString());
        result.put(PARAM_REMOTE_ADDR, request.getRemoteAddr());

        if(request.getCookies() != null) {
            for(Cookie cookie: request.getCookies()) {
                result.put(PARAM_PREFIX_COOKIE + cookie.getName(), cookie.getValue());
            }
        }

        return result;
    }

    protected AOption<String> extractIdentifier(String url) {
        return AOption.some("<http request>");
    }
}

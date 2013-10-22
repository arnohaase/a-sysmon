package com.ajjpj.asysmon.measure.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author arno
 */
public class ASimpleHttpRequestAnalyzer implements AHttpRequestAnalyzer {
    public static final String PARAM_REMOTE_ADDR = "http-remote-addr";
    public static final String PARAM_PREFIX_COOKIE = "http-cookie-";

    @Override public AHttpRequestDetails analyze(HttpServletRequest request) {
        final String identifier = extractIdentifier(request.getRequestURL().toString());
        final Map<String, String> parameters = extractParameters(request);

        return new AHttpRequestDetails() {
            @Override public String getIdentifier() {
                return identifier;
            }
            @Override public Map<String, String> getParameters() {
                return parameters;
            }
        };
    }

    private Map<String, String> extractParameters(HttpServletRequest request) {
        final Map<String, String> result = new HashMap<String, String>();

        result.put(PARAM_REMOTE_ADDR, request.getRemoteAddr());

        for(Cookie cookie: request.getCookies()) {
            result.put(PARAM_PREFIX_COOKIE + cookie.getName(), cookie.getValue());
        }

        return result;
    }

    private String extractIdentifier(String url) {
        final int idxHash = url.indexOf('#');
        if(idxHash >= 0) {
            url = url.substring(0, idxHash);
        }

        final int idxQuestionMark = url.indexOf('?');
        if(idxQuestionMark >= 0) {
            url = url.substring(0, idxQuestionMark);
        }

        final int idxSemicolon = url.indexOf(';');
        if(idxSemicolon >= 0) {
            url = url.substring(0, idxSemicolon);
        }

        return url;
    }
}

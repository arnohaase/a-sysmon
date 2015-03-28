package com.ajjpj.asysmon.servlet.environment;

import com.ajjpj.afoundation.collection.immutable.AList;

import java.util.ArrayList;
import java.util.List;


/**
 * @author arno
 */
class PathMatcher {
    private static final String WILDCARD_SINGLE_SEGMENT = "$ASYSMON$?";
    private static final String WILDCARD_SUFFIX = "$ASYSMON$*";

    static List<PathMatcher> create(String raw) {
        final List<PathMatcher> result = new ArrayList<PathMatcher>();

        for(String pathRaw: raw.split("\\|\\|")) {
            final String path = pathRaw.trim();

            final String[] segments = path.split("\\.");
            for(int i=0; i<segments.length; i++) {
                if("?".equals(segments[i])) {
                    segments[i] = WILDCARD_SINGLE_SEGMENT;
                }
                if("*".equals(segments[i])) {
                    segments[i] = WILDCARD_SUFFIX;
                }
            }

            result.add(new PathMatcher(segments));
        }

        return result;
    }

    private final String[] segments;

    PathMatcher(String[] segments) {
        this.segments = segments;
    }

    boolean matches(AList<String> path) {
        AList<String> remaining = path;

        for(String seg: segments) {
            if(WILDCARD_SUFFIX.equals(seg)) {
                return true;
            }

            if(remaining.isEmpty()) {
                // path is shorter than pattern
                return false;
            }

            if(! WILDCARD_SINGLE_SEGMENT.equals(seg)) {
                if(! seg.equals(remaining.head())) {
                    return false;
                }
            }

            remaining = remaining.tail();
        }

        // path is longer than pattern, and pattern has no '*' wildcard
        return false;
    }


}

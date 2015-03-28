package com.ajjpj.asysmon.measure.http;

import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonApi;
import com.ajjpj.asysmon.measure.AMeasureCallbackVoid;
import com.ajjpj.asysmon.measure.AMeasureCallbackVoidNoThrow;
import com.ajjpj.asysmon.measure.ASimpleMeasurement;
import com.ajjpj.asysmon.measure.AWithParameters;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.function.AStatement0;
import com.ajjpj.afoundation.util.AUnchecker;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * @author arno
 */
public class AHttpRequestMeasuringFilter implements Filter {
    public static final String PARAM_ANALYZER_CLASS_FQN = "asysmon.http-analyzer";

    private AHttpRequestAnalyzer analyzer;

    @Override public void init(FilterConfig filterConfig) throws ServletException {
        try {
            analyzer = createAnalyzer(filterConfig);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * override to customize
     */
    protected AHttpRequestAnalyzer createAnalyzer(FilterConfig filterConfig) throws Exception {
        final String analyzerFqn = filterConfig.getInitParameter(PARAM_ANALYZER_CLASS_FQN);

        if(analyzerFqn != null) {
            return (AHttpRequestAnalyzer) Thread.currentThread().getContextClassLoader().loadClass(analyzerFqn).newInstance();
        }

        return getSysMon().getConfig().httpRequestAnalyzer;
    }

    /**
     * override to customize
     */
    protected ASysMonApi getSysMon() {
        return ASysMon.get();
    }

    @Override public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
        final AHttpRequestDetails details = analyzer.analyze((HttpServletRequest) servletRequest);

        final AOption<String> optIdentifier = details.getIdentifier();
        if(optIdentifier.isDefined()) {
            getSysMon().measure(optIdentifier.get(), new AMeasureCallbackVoidNoThrow() {
                @Override public void call(AWithParameters m) {
                    for(Map.Entry<String, String> entry: details.getParameters().entrySet()) {
                        m.addParameter(entry.getKey(), entry.getValue());
                    }

                    AUnchecker.executeUnchecked(new AStatement0<Exception>() {
                        @Override
                        public void apply() throws Exception {
                            filterChain.doFilter(servletRequest, servletResponse);
                        }
});

                }
            });
        }
        else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override public void destroy() {
    }
}

package com.ajjpj.asysmon.server;

import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.processing.InputProcessor;
import com.ajjpj.asysmon.server.processing.InputProcessorImpl;
import com.ajjpj.asysmon.server.processing.SystemClockCorrector;
import com.ajjpj.asysmon.server.processing.SystemClockCorrectorNullImpl;


/**
 * @author arno
 */
public class Components {
    private static volatile Components INSTANCE;

    private final InputProcessor inputProcessor;
    private final SystemClockCorrector systemClockCorrector;

    public static void init(ASysMonServerConfig config) {
        if(INSTANCE != null) {
            throw new IllegalStateException("already initialized");
        }

        final SystemClockCorrector clockCorrector = new SystemClockCorrectorNullImpl(); //TODO make clock corrector configurbale
        final InputProcessor inputProc = new InputProcessorImpl(clockCorrector, config);

        INSTANCE = new Components(inputProc, clockCorrector);
    }

    private Components(InputProcessor inputProcessor, SystemClockCorrector systemClockCorrector) {
        this.inputProcessor = inputProcessor;
        this.systemClockCorrector = systemClockCorrector;
    }

    public static Components get() {
        return INSTANCE;
    }

    public InputProcessor getInputProcessor() {
        return inputProcessor;
    }

    public SystemClockCorrector getSystemClockCorrector() {
        return systemClockCorrector;
    }
}

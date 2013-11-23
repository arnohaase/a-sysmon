package com.ajjpj.asysmon.server;

import com.ajjpj.asysmon.server.bus.EventBus;
import com.ajjpj.asysmon.server.bus.EventBusImpl;
import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.processing.InputProcessor;
import com.ajjpj.asysmon.server.processing.InputProcessorImpl;
import com.ajjpj.asysmon.server.processing.SystemClockCorrector;
import com.ajjpj.asysmon.server.processing.SystemClockCorrectorNullImpl;
import com.ajjpj.asysmon.server.store.BufferingPersistenceProcessor;
import com.ajjpj.asysmon.server.store.DataPersister;
import com.ajjpj.asysmon.server.store.DataProvider;
import com.ajjpj.asysmon.server.store.DataStoreImpl;
import com.ajjpj.asysmon.util.AShutdownable;


/**
 * @author arno
 */
public class Components implements AShutdownable {
    private static volatile Components INSTANCE;

    private final InputProcessor inputProcessor;
    private final SystemClockCorrector systemClockCorrector;

    private final EventBus eventBus;

    private final DataPersister dataPersister;
    private final DataProvider dataProvider;
    private final BufferingPersistenceProcessor persistenceProcessor;


    public static void init(ASysMonServerConfig config) {
        if(INSTANCE != null) {
            throw new IllegalStateException("already initialized");
        }

        final EventBus eventBus = new EventBusImpl();
        final SystemClockCorrector clockCorrector = new SystemClockCorrectorNullImpl(); //TODO make clock corrector configurbale
        final InputProcessor inputProc = new InputProcessorImpl(clockCorrector, eventBus);

        final DataStoreImpl dataStore = new DataStoreImpl();
        final BufferingPersistenceProcessor persistenceProcessor = new BufferingPersistenceProcessor(eventBus, dataStore, config);

        INSTANCE = new Components(inputProc, clockCorrector, eventBus, dataStore, dataStore, persistenceProcessor);
    }

    private Components(InputProcessor inputProcessor, SystemClockCorrector systemClockCorrector, EventBus eventBus,
                       DataPersister dataPersister, DataProvider dataProvider, BufferingPersistenceProcessor persistenceProcessor) {
        this.inputProcessor = inputProcessor;
        this.systemClockCorrector = systemClockCorrector;
        this.eventBus = eventBus;

        this.dataPersister = dataPersister;
        this.dataProvider = dataProvider;
        this.persistenceProcessor = persistenceProcessor;
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
    public EventBus getEventBus() {
        return eventBus;
    }


    @Override public void shutdown() throws Exception { //TODO who calls this?
        persistenceProcessor.shutdown();
    }
}
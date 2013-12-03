package com.ajjpj.asysmon.server;

import com.ajjpj.asysmon.server.config.ASysMonServerConfig;
import com.ajjpj.asysmon.server.config.ASysMonServerConfigBuilder;
import com.ajjpj.asysmon.server.eventbus.EventBus;
import com.ajjpj.asysmon.server.eventbus.EventBusImpl;
import com.ajjpj.asysmon.server.services.AdminService;
import com.ajjpj.asysmon.server.services.impl.AdminServiceImpl;
import com.ajjpj.asysmon.server.storage.MonitoredApplicationDao;
import com.ajjpj.asysmon.server.storage.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.storage.impl.MongoDbProvider;
import com.ajjpj.asysmon.server.storage.impl.MonitoredApplicationDaoImpl;
import com.ajjpj.asysmon.server.storage.impl.ScalarMetaDataDaoImpl;
import com.google.inject.*;
import com.google.inject.spi.BindingScopingVisitor;
import com.mongodb.DB;

import java.lang.annotation.Annotation;


/**
 * This is the guice module
 *
 * @author arno
 */
public class ASysMonModule extends AbstractModule {
    public static final Injector INJECTOR = Guice.createInjector(new ASysMonModule());

    @Override protected void configure() {
        bind(DB.class).toProvider(MongoDbProvider.class).in(Singleton.class);

        // DAOs
        bind(MonitoredApplicationDao.class).to(MonitoredApplicationDaoImpl.class);
        bind(ScalarMetaDataDao.class).to(ScalarMetaDataDaoImpl.class);

        // services
        bind(AdminService.class).to(AdminServiceImpl.class);

        // event bus
        bind(EventBus.class).to(EventBusImpl.class).asEagerSingleton();
    }


    @Provides
    public ASysMonServerConfig getConfig() { //TODO move config to the database
        return new ASysMonServerConfigBuilder().build();
    }






    //TODO shutdown

    static {
        for(Binding<?> b: INJECTOR.getAllBindings().values()) {
            final boolean isSingleton = b.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {
                @Override public Boolean visitEagerSingleton() {
                    System.out.println(1);
                    return true;
                }

                @Override public Boolean visitScope(Scope scope) {
                    System.out.println(2);
                    return scope == Scopes.SINGLETON;
                }

                @Override public Boolean visitScopeAnnotation(Class<? extends Annotation> scopeAnnotation) {
                    System.out.println(3);
                    return scopeAnnotation == Singleton.class;
                }

                @Override public Boolean visitNoScoping() {
                    System.out.println(4);
                    return false;
                }
            });
            if(isSingleton) {
                System.out.println("singleton: " + INJECTOR.getInstance(b.getKey()));
            }
        }
    }
}



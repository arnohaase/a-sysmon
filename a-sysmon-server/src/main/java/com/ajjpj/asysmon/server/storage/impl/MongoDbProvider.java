package com.ajjpj.asysmon.server.storage.impl;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import javax.inject.Provider;
import java.net.UnknownHostException;

/**
 * @author arno
 */
public class MongoDbProvider implements Provider<DB> {
    private final DB db;

    public MongoDbProvider() {
        try {
            final MongoClient mongoClient = new MongoClient(); //TODO make this configurable
            this.db = mongoClient.getDB("a-sysmon");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e); //TODO exception handling
        }
    }

    @Override public DB get() {
        return db;
    }
}

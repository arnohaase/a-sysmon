package com.ajjpj.asysmon.server.dummy;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.Random;

/**
 * @author arno
 */
public class MongoDummyMain {
    public static void main(String[] args) throws UnknownHostException {
        dump();
//        dummy();
    }

    private static void dummy() throws UnknownHostException {
        final MongoClient mongoClient = new MongoClient();
        final DB db = mongoClient.getDB("dummy");

        System.out.println(db.getCollectionNames());

        final DBCollection coll1 = db.getCollection("coll1");

        final BasicDBObject doc = new BasicDBObject("name", "MongoDB").
                append("_id", "asdf").
                append("type", "database").
                append("count", new Random().nextInt()).
                append("info", new BasicDBObject("x", new Random().nextInt()).append("y", 102));

        coll1.insert(doc);

        System.out.println(coll1.count());

        final DBCursor cursor = coll1.find();
        while (cursor.hasNext()) {
            final DBObject cur = cursor.next();
            System.out.println(cur);
        }

        System.out.println("----");
        final DBCursor queryResult = coll1.find(new BasicDBObject("count", new BasicDBObject("$gt", 1)));
        while (queryResult.hasNext()) {
            final DBObject cur = queryResult.next();
            System.out.println(cur);
        }
    }

    private static void dump() throws UnknownHostException {
        final MongoClient mongoClient = new MongoClient();
        final DB db = mongoClient.getDB("a-sysmon");

        System.out.println(db.getCollectionNames());

        final DBCollection coll = db.getCollection("demo:memgc-free");
        System.out.println(coll.getIndexInfo());
        System.out.println(coll.getCount());
    }
}

package com.ajjpj.asysmon.server.storage.impl;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author arno
 */
public class MongoDbHelper {

    /**
     * A separator used by a-sysmon in collection and other Mongo DB names to separate several synthetic parts. This
     *  string should not be used in identifiers - if it is, there is an extremely small chance of name clashes.
     */
    public static final String SEPARATOR = "-_-";

    public static int asIntExpected(DBObject o, String fieldName) {
        return ((Number) o.get(fieldName)).intValue();
    }

    public static int asInt(DBObject o, String fieldName, int defaultValue) {
        final Number result = (Number) o.get(fieldName);
        if(result == null) {
            return defaultValue;
        }
        return result.intValue();
    }

    public static long asLongExpected(DBObject o, String fieldName) {
        return ((Number) o.get(fieldName)).longValue();
    }

    public static long asLong(DBObject o, String fieldName, long defaultValue) {
        final Number result = (Number) o.get(fieldName);
        if(result == null) {
            return defaultValue;
        }
        return result.longValue();
    }

    public static double asDoubleExpected(DBObject o, String fieldName) {
        return ((Number) o.get(fieldName)).doubleValue();
    }

    public static double asDouble(DBObject o, String fieldName, double defaultValue) {
        final Number result = (Number) o.get(fieldName);
        if(result == null) {
            return defaultValue;
        }
        return result.doubleValue();
    }

    //-----------------------------------------------

    public static DBObject createQueryTimestampRange(long from, long to) {
        final BasicDBObject result = new BasicDBObject("$gte", from);
        result.append("$lte", to);
        return result;
    }

    public static DBObject createQueryInClause(String... strings) {
        return new BasicDBObject("$in", strings);
    }

    public static DBObject createSortSpec(String... fieldNames) {
        final BasicDBObject result = new BasicDBObject();
        for(String fieldName: fieldNames) {
            result.append(fieldName, 1);
        }
        return result;
    }
}

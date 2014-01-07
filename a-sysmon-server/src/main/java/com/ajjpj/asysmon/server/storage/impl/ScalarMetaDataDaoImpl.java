package com.ajjpj.asysmon.server.storage.impl;

import com.ajjpj.asysmon.server.storage.ScalarMetaData;
import com.ajjpj.asysmon.server.storage.ScalarMetaDataDao;
import com.ajjpj.asysmon.server.util.AOption;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;


/**
 * @author arno
 */
@Singleton
public class ScalarMetaDataDaoImpl implements ScalarMetaDataDao {
    public static final String COLL_SCALAR_META_DATA = "environment-metadata";
    public static final String FIELD_NUM_FRAC_DIGITS = "numFracDigits";

    //TODO ttl
    //TODO pre-aggregations to store__

    final DBCollection coll;

    @Inject
    public ScalarMetaDataDaoImpl(DB db) {
        this.coll = db.getCollection(COLL_SCALAR_META_DATA);
    }


    @Override public Map<String, ScalarMetaData> getAll() {
        final Map<String, ScalarMetaData> result = new HashMap<>();

        for(DBObject o: coll.find()) {
            final ScalarMetaData data = fromDBObject(o);
            result.put(data.name, data);
        }

        return result;
    }

    ScalarMetaData fromDBObject(DBObject o) {
        final String name = (String) o.get("_id");
        final int numFracDigits = MongoDbHelper.asInt(o, FIELD_NUM_FRAC_DIGITS, 1);

        return new ScalarMetaData(name, numFracDigits);
    }

    DBObject toDBObject(ScalarMetaData scalarMetaData) {
        final BasicDBObject result = new BasicDBObject("_id", scalarMetaData.name);
        result.append(FIELD_NUM_FRAC_DIGITS, scalarMetaData.numFracDigits);
        return result;
    }

    @Override public AOption<ScalarMetaData> get(String name) {
        final DBObject raw = coll.findOne(new BasicDBObject("_id", name));
        if(raw == null) {
            return AOption.none();
        }
        return AOption.some(fromDBObject(raw));
    }

    @Override public void store(ScalarMetaData scalarMetaData) {
        final DBObject q = new BasicDBObject("_id", scalarMetaData.name);
        final DBObject o = toDBObject(scalarMetaData);

        coll.update(q, o, true, false);
    }
}

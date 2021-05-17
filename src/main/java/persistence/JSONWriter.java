package persistence;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import events.birthdayEvent.BirthdayLog;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import static persistence.Writable.ACCESS_KEY;

/**
 * Represents a class that can be saved to JSON files
 */

public class JSONWriter {
    public static MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("mongo_uri")));
    private final MongoCollection<Document> collection;
    private final String accessVal;
    private final UpdateOptions updateOptions;

    public JSONWriter(String colName, String saveVal) {
        updateOptions = new UpdateOptions();
        updateOptions.upsert(true);
        this.accessVal = saveVal;
        MongoDatabase db = mongoClient.getDatabase("test");
        collection = db.getCollection(colName);
    }

    public void saveObject(Writable writable) {
        Document document =  Document.parse(writable.toJSON().toString());
        if (collection.find(Filters.or(Filters.eq(ACCESS_KEY, accessVal),
                            Filters.exists(BirthdayLog.SAVE_VAL))).first()== null) {
            collection.insertOne(document);
        } else {
            collection.findOneAndReplace(Filters.eq(ACCESS_KEY, accessVal),document);
        }
    }

    public void saveString(String str) {
        Document document =  Document.parse(str);
        if (collection.find(Filters.or(Filters.eq(ACCESS_KEY, accessVal),
                Filters.exists(BirthdayLog.SAVE_VAL))).first()== null) {
            collection.insertOne(document);
        } else {
            collection.findOneAndReplace(Filters.eq(ACCESS_KEY, accessVal),document);
        }
    }
}

package persistence;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

import static persistence.Writable.ACCESS_KEY;

/**
 * Represents a class that can be saved to JSON files
 */

public class DBWriter {
    public static MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("mongo_uri")));
    private final MongoCollection<Document> collection;
    private final String accessVal;
    private final ReplaceOptions replaceOptions;

    public DBWriter(String colName, String saveVal) {
        replaceOptions = new ReplaceOptions();
        replaceOptions.upsert(true);
        this.accessVal = saveVal;
        MongoDatabase db = mongoClient.getDatabase("test");
        collection = db.getCollection(colName);
    }

    public void saveObject(Writable writable) {
        Document doc = writable.toDoc();
        collection.findOneAndReplace(Filters.eq(ACCESS_KEY, accessVal),doc);
    }
}

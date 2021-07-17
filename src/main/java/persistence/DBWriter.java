package persistence;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import static persistence.Writable.ACCESS_KEY;


public class DBWriter {
    public static MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("mongo_uri")));
    private final MongoCollection<Document> collection;
    private final String accessVal;
    private final FindOneAndReplaceOptions replaceOptions;

    public DBWriter(String colName, String accessVal) {
        replaceOptions = new FindOneAndReplaceOptions();
        replaceOptions.upsert(true);
        this.accessVal = accessVal;
        MongoDatabase db = mongoClient.getDatabase("test");
        collection = db.getCollection(colName);
    }

    public void saveObject(Writable writable, SaveOption option) {
        Document doc = writable.toDoc();
        switch (option) {
            case REPLACE_DUPLICATES_ONLY:
                collection.findOneAndReplace(doc,doc,replaceOptions);
                break;
            case DEFAULT:
                collection.findOneAndReplace(Filters.eq(ACCESS_KEY, accessVal),doc,replaceOptions);
                break;
        }
    }

    public void removeDocuments(Document filter) {
        collection.deleteMany(filter);
    }

    public void saveObject(Writable writable) {
        Document doc = writable.toDoc();
        collection.findOneAndReplace(Filters.eq(ACCESS_KEY, accessVal),doc,replaceOptions);
    }
}

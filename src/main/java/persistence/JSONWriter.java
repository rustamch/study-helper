package persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import events.birthdayEvent.BirthdayLog;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Filters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Represents a class that can be saved to JSON files
 */

public class JSONWriter {
    public static MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("mongo_uri")));
    private String colName;
    private MongoDatabase db;
    private MongoCollection<Document> collection;
    private String userID;

    public JSONWriter(String colName, String userID) {
        this.colName = colName;
        this.userID = userID;
        db = mongoClient.getDatabase("test");
        collection = db.getCollection(colName);
    }

    public void saveObject(Writable writable) {
        Document document =  Document.parse(writable.toJSON().toString());
        if (collection.find(Filters.or(Filters.eq("userID",userID),
                            Filters.exists(BirthdayLog.SAVE_KEY))).first()== null) {
            collection.insertOne(document);
        } else {
            collection.findOneAndReplace(Filters.eq("userID",userID),document);
        }
    }

    public void saveString(String str) throws FileNotFoundException {
        Document document =  Document.parse(str);
        collection.findOneAndReplace(Filters.exists("save_key"),document);
    }
}

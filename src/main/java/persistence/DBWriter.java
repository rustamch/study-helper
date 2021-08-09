package persistence;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.conversions.Bson;

import static persistence.Writable.ACCESS_KEY;


public class DBWriter {
    public static MongoClient mongoClient = new MongoClient(new MongoClientURI(System.getenv("mongo_uri")));
    private final MongoCollection<Document> collection;
    private final FindOneAndReplaceOptions replaceOptions;

    
    public DBWriter(String collectionName) {
        this.collection = mongoClient.getDatabase("test").getCollection(collectionName);
        this.replaceOptions = new FindOneAndReplaceOptions();
        this.replaceOptions.upsert(true);
    }

    /**
     * Saves the document that was derived through method toDoc earlier.
     * ASSUMES THAT ALL THE DOCUMENTS ARE DERIVED THROUGH Writable.toDoc() AND FOLLOW THE CONVENTION
     * @param writeDoc a BSON document
     */
    public void saveDocument(Document writeDoc) {
        assert writeDoc.containsKey(ACCESS_KEY);
        this.collection.findOneAndReplace(Filters.eq(ACCESS_KEY, writeDoc.get(ACCESS_KEY)),writeDoc,this.replaceOptions);
    }

    public void removeDocuments(Bson filter) {
        collection.deleteMany(filter);
    }

    public void saveObject(Writable writable, SaveOption saveOption) {
        Document doc = writable.toDoc();
        switch (saveOption) {
            case REPLACE_DUPLICATES_ONLY:
                collection.findOneAndReplace(doc, doc, replaceOptions);
                break;
            case DEFAULT:
                collection.findOneAndReplace(Filters.eq(ACCESS_KEY, doc.get(ACCESS_KEY)), doc, replaceOptions);
                break;
        }
    }
}

package persistence;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;

import exceptions.InvalidDocumentException;


public class DBReader {
    private String documentName;
    MongoClient mongoClient = DBWriter.mongoClient;
    private MongoCollection<Document> collection;

    public DBReader(String collectionName) {
        this.collection = mongoClient.getDatabase("test").getCollection(collectionName);
    }

    @Deprecated
    public DBReader(String collectionName, String documentName) {
        this.documentName = documentName;
        this.collection = mongoClient.getDatabase("test").getCollection(collectionName);
    }
    
    public FindIterable<Document> loadDocumentsWithFilter(Bson filter) {
        FindIterable<Document> docs = collection.find(filter);
        return docs;
    }

    public FindIterable<Document> loadAllDocuments() {
        FindIterable<Document> docs = collection.find();
        return docs;
    }

    @Deprecated
    /**
     * Loads an object from the database from the given collection
     * @return a document that has specified field
     * @throws InvalidDocumentException
     */
    public Document loadObject() throws InvalidDocumentException {
        Document document = collection.find(new Document(Writable.ACCESS_KEY,documentName)).first();
        if (document == null) {
            throw new InvalidDocumentException();
        }
        return document;
    }

    /**
     * Loads an object from the database from the given collection
     * @return a document that has specified field
     * @throws InvalidDocumentException
     */
    public Document loadObject(Object accessValue) throws InvalidDocumentException {
        Document document = collection.find(Filters.eq(Writable.ACCESS_KEY, accessValue)).first();
        if (document == null) {
            throw new InvalidDocumentException();
        }
        return document;
    }


}

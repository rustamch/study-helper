package persistence;

import java.time.LocalDate;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;

import events.TodoEvent.Todo;
import events.TodoEvent.TodoList;
import exceptions.CompletedPastTodoException;
import exceptions.InvalidDocumentException;


public class DBReader {
    private String documentName;
    private Document readDoc;
    MongoClient mongoClient = DBWriter.mongoClient;
    private MongoCollection<Document> collection;

    public DBReader(String collectionName) {
        this.readDoc = null;
        this.collection = mongoClient.getDatabase("test").getCollection(collectionName);
    }

    @Deprecated
    public DBReader(String collectionName, String documentName) {
        this.readDoc = null;
        this.documentName = documentName;
        this.collection = mongoClient.getDatabase("test").getCollection(collectionName);
    }

    public TodoList getTodos() {
        try {
            loadObject();
            TodoList todos = new TodoList();
            if (!readDoc.containsKey("todos")) {
                return todos;
            }
            List<Document> todoList = (List<Document>) readDoc.get("todos");
            for (Document doc : todoList) {
                try {
                    todos.addTodo(getTodo(doc));
                } catch (CompletedPastTodoException e) {
                    continue;
                }
            }
            return todos;
        } catch (InvalidDocumentException e1) {
            return new TodoList();
        }

    }

    private Todo getTodo(Document doc) throws CompletedPastTodoException {
        String[] dateStr = doc.getString("dueDate").split("-");
        LocalDate date =
                LocalDate.of(Integer.parseInt(dateStr[0]), Integer.parseInt(dateStr[1]), Integer.parseInt(dateStr[2]));
        String course = doc.getString("course").equals("null") ? null : doc.getString("course");
        Todo todo = new Todo(course, doc.getString("description"), date);
        if (!doc.getBoolean("incomplete")) {
            if (date.isBefore(LocalDate.now())) {
                throw new CompletedPastTodoException();
            }
            todo.setComplete();
        }
        return todo;
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
        readDoc = document;
        return document;
    }

    /**
     * Loads an object from the database from the given collection
     * @return a document that has specified field
     * @throws InvalidDocumentException
     */
    public Document loadObject(Object accessValue) throws InvalidDocumentException {
        Document document = collection.find(new Document(Writable.ACCESS_KEY,accessValue)).first();
        if (document == null) {
            throw new InvalidDocumentException();
        }
        return document;
    }


}

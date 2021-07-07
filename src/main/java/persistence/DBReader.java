package persistence;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import events.BirthdayEvent.BirthdayEvent;
import events.TodoEvent.Todo;
import events.TodoEvent.TodoList;
import exceptions.CompletedPastTodoException;
import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;
import exceptions.InvalidDocumentException;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DBReader {
    private String documentName;
    private Document readDoc;
    MongoClient mongoClient = DBWriter.mongoClient;
    private MongoCollection<Document> collection;

    public DBReader(String collectionName, String documentName) {
        readDoc = null;
        this.documentName = documentName;
        MongoDatabase db;
        db = mongoClient.getDatabase("test");
        collection = db.getCollection(collectionName);
    }

    public Map<String, Date> getBDayLog() {
        try {
            loadObject();
            Map<String, Date> map = new HashMap<>();
            if (readDoc.containsKey("bdayLog")) {
                List<Document> entries = (List<Document>) readDoc.get("bdayLog");
                for (Document entry : entries) {
                    map.put(entry.getString("id"), parseDate(entry.getString("date")));
                }
            }
            return map;
        } catch (InvalidDocumentException e) {
            return new HashMap<String, Date>();
        }
    }

    private Date parseDate(String date) {
        try {
            return BirthdayEvent.getDateFromStr(date);
        } catch (InvalidDateFormatException | IllegalDateException invalidDateFormatException) {
            throw new RuntimeException("Saved date data is wrong");
        }
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

    @NotNull
    private LocalDate locDateFromStr(String dateStr) {
        String[] datelst = dateStr.split("-");
        return LocalDate.of(Integer.parseInt(datelst[0]), Integer.parseInt(datelst[1]), Integer.parseInt(datelst[2]));
    }

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
}

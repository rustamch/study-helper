package events.TodoEvent;
import persistence.Writable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bson.Document;

public class TodoList extends Writable {
    private List<Todo> todoList = new ArrayList<>();
    private String userID;

    public void addTodo(Todo todo) {
        todoList.add(todo);
        Collections.sort(todoList);
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public Document toDoc() {
        List<Document> array = new ArrayList<>();
        for (Todo todo : todoList) {
            array.add(todo.toDoc());
        }
        Document doc = new Document();
        doc.put(ACCESS_KEY,userID);
        doc.put("todos", array);
        return doc;
    }

    @Override
    public String toString() {
        StringBuilder msgBuilder = new StringBuilder();
        for (int i = 0; i < todoList.size(); i++) {
            Todo todo = todoList.get(i);
            msgBuilder.append(i + 1).append(". ").append(todo.toString()).append("\n");
        }
        return msgBuilder.toString();
    }

    public void removeTodo(int n) {
        todoList.remove(n);
    }

    public void clear() {
        todoList.clear();
    }

    public void setComplete(int n) {
        todoList.get(n).setComplete();
    }

    public boolean allComplete() {
        for (Todo todo : todoList) {
            if (todo.isIncomplete()) {
                return false;
            }
        }
        return true;
    }
}

package events.TodoEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoList implements Writable {
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
    public JSONObject toJSON() {
        JSONArray array = new JSONArray();
        for (Todo todo : todoList) {
            array.put(todo.toJSON());
        }
        JSONObject jobject = new JSONObject();
        jobject.put("userID",userID);
        jobject.put("todos", array);
        return jobject;
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

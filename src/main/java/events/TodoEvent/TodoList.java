package events.TodoEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoList implements Writable {
    private List<Todo> todoList = new ArrayList<>();

    public void addTodo(Todo todo) {
        todoList.add(todo);
        Collections.sort(todoList);
    }

    @Override
    public JSONObject toJSON() {
        JSONArray array = new JSONArray();
        for (Todo todo : todoList) {
            array.put(todo.toJSON());
        }
        JSONObject jobject = new JSONObject();
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
}

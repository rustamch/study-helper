package events.TodoEvent;

import net.dv8tion.jda.api.entities.User;
import persistence.DBReader;
import persistence.DBWriter;

import java.time.LocalDate;

import exceptions.IllegalDateException;

public class TodoManager {
    private static final String COLLECTION_NAME = "todo_collection";
    private TodoList todos;
    private final User owner;

    public TodoManager(User name) {
        owner = name;
        loadTodosFor();
    }

    public void addTodo(String course, String description, LocalDate date) throws IllegalDateException {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalDateException();
        }
        todos.addTodo(new Todo(course, description, date));
        save();
    }

    private void save() {
        todos.setUserID(owner.getId());
        DBWriter writer = new DBWriter(COLLECTION_NAME,owner.getId());
        writer.saveObject(todos);
    }

    public String getTodoMessage() {
        return "<@" +
                owner.getId() + ">\n" +
                todos;
    }

    private void loadTodosFor() {
        String userID = owner.getId();
        DBReader reader = new DBReader(COLLECTION_NAME,userID);
        todos = reader.getTodos();
    }

    public void removeTodoByNumber(int n) {
        todos.removeTodo(n);
        save();
    }

    public void clearTodo() {
        todos.clear();
        save();
    }

    public void setTodoAsComplete(int n) {
        todos.setComplete(n);
        save();
    }

    public boolean listIsCleared() {
        return todos.allComplete();
    }
}

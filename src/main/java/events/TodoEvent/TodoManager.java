package events.TodoEvent;

import exception.IllegalDateException;
import net.dv8tion.jda.api.entities.User;
import persistence.JSONReader;
import persistence.JSONWriter;

import java.io.FileNotFoundException;
import java.time.LocalDate;

public class TodoManager {
//    private static final String SAVE_FILE_PACKAGE = ".idea/data/todos";

    private TodoList todos;
    private final User owner;
    private final String fileLocation;

    public TodoManager(User name) {
        owner = name;
        fileLocation = "./" + owner.getId() + ".json";
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
        JSONWriter writer = new JSONWriter(fileLocation);
        try {
            writer.saveObject(todos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public String getTodoMessage() {
        return "<@" +
                owner.getId() + ">\n" +
                todos;
    }

    private void loadTodosFor() {
        JSONReader reader = new JSONReader(fileLocation);
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

package events.TodoEvent;

import persistence.JSONReader;
import persistence.JSONWriter;

import java.io.FileNotFoundException;
import java.time.LocalDate;

public class TodoManager {
    private static final String SAVE_FILE_PACKAGE = ".idea/data/todos";

    private TodoList todos;
    private final String owner;
    private final String fileLocation;

    public TodoManager(String name) {
        owner = name;
        fileLocation = SAVE_FILE_PACKAGE + "/" + owner + ".json";
        loadTodosFor(owner);
    }

    public void addTodo(String course, String description, LocalDate date) {
        todos.addTodo(new Todo(course, description, date));
        JSONWriter writer = new JSONWriter(fileLocation);
        try {
            writer.saveObject(todos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public String getTodoMessage() {
        return "**" +
                owner.toUpperCase() + "**\n" +
                todos;
    }

    private void loadTodosFor(String owner) {
        JSONReader reader = new JSONReader(fileLocation);
        todos = reader.getTodos();
    }
}

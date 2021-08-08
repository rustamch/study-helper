package events.TodoEvent;

import persistence.DBReader;
import persistence.DBWriter;

import java.time.LocalDate;
import java.util.List;

import org.bson.Document;
import org.javacord.api.entity.user.User;

import exceptions.CompletedPastTodoException;
import exceptions.IllegalDateException;
import exceptions.InvalidDocumentException;
import persistence.SaveOption;

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
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        writer.saveObject(todos, SaveOption.DEFAULT);
    }

    public String getTodoMessage() {
        return "<@" +
                owner.getId() + ">\n" +
                todos;
    }

    private void loadTodosFor() {
        String userID = owner.getIdAsString();
        DBReader reader = new DBReader(COLLECTION_NAME);
        try {
            Document doc = reader.loadObject(userID);
            TodoList todos = new TodoList();
            if (!doc.containsKey("todos")) {
                this.todos = todos;
                return;
            }
            List<Document> todoList = (List<Document>) doc.get("todos");
            for (Document d : todoList) {
                try {
                    todos.addTodo(getTodo(d));
                } catch (CompletedPastTodoException e) {
                    continue;
                }
            }
            this.todos = todos;
        } catch (InvalidDocumentException e) {
            this.todos = new TodoList();
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

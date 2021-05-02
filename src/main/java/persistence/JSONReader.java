package persistence;

import events.TodoEvent.Todo;
import events.TodoEvent.TodoList;
import events.birthdayEvent.BirthdayEvent;
import exception.IllegalDateException;
import exception.InvalidDateFormatException;
import exception.ObjectMismatchException;
import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;
import exceptions.ObjectMismatchException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JSONReader {
    private String fileLocation;
    private JSONObject jsonObject;

    public JSONReader(String fileLocation) {
        jsonObject = null;
        this.fileLocation = fileLocation;
    }

    public Map<String, Date> getBDayLog() {
        loadObject();
        Map<String, Date> map = new HashMap<>();
        if (jsonObject.has("no data")) {
            return map;
        }
        if (! jsonObject.has("bdayLog")) {
            throw new ObjectMismatchException();
        }
        JSONArray entries = jsonObject.getJSONArray("bdayLog");
        for (Object obj : entries) {
            JSONObject entry = (JSONObject) obj;
            map.put(entry.getString("name"), parseDate(entry.getString("date")));
        }
        return map;
    }

    private Date parseDate(String date) {
        try {
            return BirthdayEvent.getDateFromStr(date);
        } catch (InvalidDateFormatException | IllegalDateException invalidDateFormatException) {
            throw new RuntimeException("Saved date data is wrong");
        }
    }

    public TodoList getTodos() {
        loadObject();
        TodoList todos = new TodoList();
        if (! jsonObject.has("todos")) {
            return todos;
        }
        JSONArray array = (JSONArray) jsonObject.get("todos");
        for (Object obj : array) {
            todos.addTodo(getTodo((JSONObject) obj));
        }
        return todos;
    }

    private Todo getTodo(JSONObject obj) {
        String[] dateStr = obj.getString("dueDate").split("-");
        LocalDate date =
                LocalDate.of(Integer.parseInt(dateStr[0]), Integer.parseInt(dateStr[1]), Integer.parseInt(dateStr[2]));
        String course = obj.getString("course").equals("null") ? null : obj.getString("course");
        Todo todo = new Todo(course, obj.getString("description"), date);
        if (!obj.getBoolean("incomplete")) {
            todo.setComplete();
        }
        return todo;
    }

    @NotNull
    private LocalDate locDateFromStr(String dateStr) {
        String[] datelst = dateStr.split("-");
        return LocalDate.of(Integer.parseInt(datelst[0]), Integer.parseInt(datelst[1]), Integer.parseInt(datelst[2]));
    }

    private String readFile() throws IOException {
        File save = new File(fileLocation);
        if (save.createNewFile()) {
            PrintWriter writer = new PrintWriter(save);
            writer.println("{}");
        }
        Scanner scanner = new Scanner(save);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();

        return builder.toString();
    }

    private void loadObject() {
        try {
            String jsonData = readFile();
            jsonObject = new JSONObject(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

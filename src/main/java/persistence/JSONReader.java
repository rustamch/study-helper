package persistence;

import events.TodoEvent.Todo;
import events.TodoEvent.TodoList;
import events.birthdayEvent.BirthdayEvent;
import exceptions.IllegalDateException;
import exceptions.InvalidDateFormatException;
import exceptions.ObjectMismatchException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private void loadObject() {
        try {
            String jsonData = readFile();
            jsonObject = new JSONObject(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private String readFile() throws IOException {
        File save = new File(fileLocation);
        if (save.createNewFile()) {
            JSONWriter writer = new JSONWriter(fileLocation);
            writer.saveObject(new TodoList());
        }
        Scanner scanner = new Scanner(save);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();

        return builder.toString();
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
        return new Todo(obj.getString("course"), obj.getString("description"), date);
    }
}

package persistence;

import events.birthdayEvent.BirthdayEvent;
import exception.IllegalDateException;
import exception.InvalidDateFormatException;
import exception.ObjectMismatchException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
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

    public void loadObject() {
        try {
            String jsonData = readFile();
            jsonObject = new JSONObject(jsonData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Date> getBDayLog() {
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

    private String readFile() throws FileNotFoundException {
        File save = new File(fileLocation);
        Scanner scanner = new Scanner(save);
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();

        return builder.toString();
    }
}

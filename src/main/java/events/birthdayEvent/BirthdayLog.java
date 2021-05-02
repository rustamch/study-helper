package events.birthdayEvent;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.Date;
import java.util.Map;

/**
 * Represents a birthday manager that manages members' events.birthdays
 */
public class BirthdayLog implements Writable {
    public static final String SAVE_KEY = "bdayLog";
    public static final String BDAYLOG_LOCATION = ".idea/data/birthdays.json";

    private Map<String, Date> bdays;

    public BirthdayLog(Map<String, Date> log) {
        bdays = log;
    }

    public void addMemberBirthday(String name, Date date) {
        bdays.put(name, date);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        JSONArray logArray = new JSONArray();
        for (Map.Entry<String, Date> entry : bdays.entrySet()) {
            JSONObject entryJSON = new JSONObject();
            entryJSON.put("name", entry.getKey());
            entryJSON.put("date", BirthdayEvent.dateToStr(entry.getValue()));
            logArray.put(entryJSON);
        }
        json.put(SAVE_KEY, logArray);
        return json;
    }

    @Override
    public String toString() {
        return "BirthdayLog{" +
                "bdays=" + bdays +
                '}';
    }

    public Date getDateByName(String nickname) {
        return bdays.get(nickname);
    }
}

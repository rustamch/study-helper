package events.BirthdayEvent;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents a birthday manager that manages members' events.birthdays
 */
public class BirthdayLog extends Writable {
    public static final String SAVE_VAL = "bdayLog";
    public static final String BDAYLOG_LOCATION = "birthdays_col";

    private final Map<String, Date> bdays;

    public BirthdayLog(Map<String, Date> log) {
        bdays = log;
    } // maps member's id to birthday

    public void addMemberBirthday(String name, Date date) {
        bdays.put(name, date);
    }

    @Override
    public Document toDoc() {
        Document bdayLog = new Document();
        List<Document> bdayList = new ArrayList<>();
        for (Map.Entry<String, Date> entry : bdays.entrySet()) {
            Document entryDoc = new Document();
            entryDoc.put("id", entry.getKey());
            entryDoc.put("date", BirthdayEvent.dateToStr(entry.getValue()));
            bdayList.add(entryDoc);
        }
        bdayLog.put("bdays", bdayList);
        bdayLog.put(ACCESS_KEY, SAVE_VAL);
        return bdayLog;
    }

    @Override
    public String toString() {
        return "BirthdayLog{" +
                "bdays=" + bdays +
                '}';
    }

    public Date getDateById(String id) {
        return bdays.get(id);
    }
}

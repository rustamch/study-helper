package events.BirthdayEvent;

import org.bson.Document;

import persistence.DBReader;
import persistence.Writable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
/**
 * Represents a birthday manager that manages members' events.birthdays
 */
public class BirthdayLog extends Writable {
    public static final String COLLECTION_NAME = "bdayLog";
    public static final String MONTH_KEY = "month";
    public static final String DAY_KEY = "day";
    private final Map<String, LocalDate> bdays;
    private final LocalDate bday;

    public BirthdayLog(String memberID) {
        bday = getDateById(memberID);
    } 
    public void addMemberBirthday(String name, LocalDate date) {
        bdays.put(name, date);
    }

    @Override
    public Document toDoc() {
        Document bdayLog = new Document();
        List<Document> bdayList = new ArrayList<>();
        for (Map.Entry<String, LocalDate> entry : bdays.entrySet()) {
            Document entryDoc = new Document();
            entryDoc.put("id", entry.getKey());
            entryDoc.put("date", BirthdayEvent.dateToStr(entry.getValue())); // TODO: use month + day instead
            bdayList.add(entryDoc);
        }
        bdayLog.put("bdays", bdayList);
        bdayLog.put(ACCESS_KEY, COLLECTION_NAME);
        return bdayLog;
    }

    /** 
     * Returns the the birthday of the user given user's id
     * @param id user's id 
     * @return a birthday of the user
    */
    public LocalDate getDateById(String id) {
        return bdays.get(id);
    }

    private static Map<String, LocalDate> findMembersWithBdayToday() {
        DBReader reader = new DBReader(COLLECTION_NAME, "");
        FindIterable<Document> docs = reader.loadDocumentsWithFilter(Filters.eq(MONTH_KEY, LocalDateTime.now().getMonth()), Filter.eq());
    }
}

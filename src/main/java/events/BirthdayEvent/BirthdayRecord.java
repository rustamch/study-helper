package events.BirthdayEvent;

import org.bson.Document;

import exceptions.InvalidDocumentException;
import persistence.DBReader;
import persistence.Writable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

/**
 * Represents a birthday manager that manages members' events.birthdays
 */
public class BirthdayRecord extends Writable {
    public static final String COLLECTION_NAME = "bdayLog";
    public static final String YEAR_KEY = "year";
    public static final String MONTH_KEY = "month";
    public static final String DAY_KEY = "day";
    public static final StRing ID_KEY = "id";

    private String memberID;
    private LocalDate date;

    public BirthdayRecord(String id, LocalDate date) {
        memberID = id;
        this.date = date;
    }

    @Override
    public Document toDoc() {
        Document doc = new Document();
        doc.put(ID_KEY, memberID);
        doc.put(YEAR_KEY, date.getYear());
        doc.put(MONTH_KEY, date.getMonthValue());
        doc.put(DAY_KEY, date.getDayOfMonth());
        bdayLog.put(ACCESS_KEY, COLLECTION_NAME);
        return doc;
    }

    /** 
     * Returns the the birthday of the user given user's id
     * @param id user's id 
     * @return a birthday of the user
    */
    public static LocalDate getDateById(String id) {
        DBReader reader = new DBReader(COLLECTION_NAME, id);
        try {
            Document doc = reader.loadObject();
            return(LocalDate.of(doc.getInteger(YEAR_KEY), doc.getInteger(MONTH_KEY), doc.getInteger(DAY_KEY)));
        } catch (InvalidDocumentException e) {
            return null;
        }
        
    }

    public static Set<String> findMembersWithBdayToday() {
        DBReader reader = new DBReader(COLLECTION_NAME, "");
        FindIterable<Document> docs = reader.loadDocumentsWithFilter(Filters.eq(MONTH_KEY, LocalDateTime.now().getMonthValue()), Filter.eq(DAY_KEY, LocalDateTime.now().getDayOfMonth()));
        HashSet<String> set = new HashSet<>();
        for (Document doc : docs) {
            String userID = doc.getString(ID_KEY);
            set.add(userID);
        }
        return set;
    }
}

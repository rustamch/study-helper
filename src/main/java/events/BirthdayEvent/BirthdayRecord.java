package events.BirthdayEvent;

import org.bson.Document;
import org.bson.conversions.Bson;

import exceptions.InvalidDateFormatException;
import exceptions.InvalidDocumentException;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

/**
 * Represents a birthday manager that manages members' events.birthdays
 */
public class BirthdayRecord implements Writable {
    public static final String COLLECTION_NAME = "bdayLog";
    public static final String YEAR_KEY = "year";
    public static final String MONTH_KEY = "month";
    public static final String DAY_KEY = "day";
    public static final String ID_KEY = "id";
    public static final String GUILD_ID_KEY = "guild_id";
    private static final  DBReader READER = new DBReader(COLLECTION_NAME);
    private static final  DBWriter WRITER = new DBWriter(COLLECTION_NAME);

    private String memberID;
    private LocalDate date;

    /**
     * Constructs a new birthday record.
     * 
     * @param id      user's id.
     * @param date    the date of the birthday.
     */
    public BirthdayRecord(String id, LocalDate date) {
        this.memberID = id;
        this.date = date;
    }

    public static Set<String> findMembersWithBdayOnGivenMonth(int monthValue) {
        return findMembersWithFilters(Filters.eq(MONTH_KEY, monthValue));
    }

    @Override
    public Document toDoc() {
        Document doc = new Document();
        doc.put(ACCESS_KEY, memberID);
        doc.put(YEAR_KEY, date.getYear());
        doc.put(MONTH_KEY, date.getMonthValue());
        doc.put(DAY_KEY, date.getDayOfMonth());
        return doc;
    }

    /**
     * Returns the the birthday of the user given user's id
     * 
     * @param id user's id
     * @return a birthday of the user
     */
    public static LocalDate getDateById(String id) {
        DBReader reader = new DBReader(COLLECTION_NAME, id);
        try {
            Document doc = reader.loadObject();
            return (LocalDate.of(doc.getInteger(YEAR_KEY), doc.getInteger(MONTH_KEY), doc.getInteger(DAY_KEY)));
        } catch (InvalidDocumentException e) {
            return null;
        }
    }

    public static Set<String> findMembersWithBdayOnGivenDay(LocalDate date) {

        return findMembersWithFilters(Filters.eq(MONTH_KEY, date.getMonthValue()),
                Filters.eq(DAY_KEY, date.getDayOfMonth()));
    }

    public static Set<String> findAllMembersWithBdayOnGivenMonth(int monthVal) {
        return findMembersWithFilters(Filters.eq(MONTH_KEY, monthVal));
    }

    private static Set<String> findMembersWithFilters(Bson ... filters) {
        Bson filter = Filters.and(filters);
        FindIterable<Document> docs = READER.loadDocumentsWithFilter(filter);
        HashSet<String> set = new HashSet<>();
        for (Document doc : docs) {
            String userID = doc.getString(ACCESS_KEY);
            set.add(userID);
        }
        return set;
    }

    /**
     * Records the birthday and saves it to DataBase
     * 
     * @param id id of the member
     * @throws InvalidDateFormatException when given date format is unrecognized
     */
    public static void recordBDay(String id, LocalDate bdayDate) {
        BirthdayRecord bday = new BirthdayRecord(id, bdayDate);
        WRITER.saveObject(bday, SaveOption.DEFAULT);
    }
}

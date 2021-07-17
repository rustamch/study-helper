package events.ReminderEvent;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.Writable;

import java.util.ArrayList;
import java.util.List;

public class Reminder extends Writable {
    private final static String COLLECTION_NAME = "reminders";
    private long epoch;
    private int nanos;
    private String userID;

    /**
     * Load all the reminders for the given EPOCH
     * @param epoch long
     * @param nanos int
     * @return list with all reminders that are scheduled for this epoch
     */
    public static List<Reminder> loadReminders(long epoch, int nanos) {
        DBReader reader = new DBReader(COLLECTION_NAME,"reminder");
        Document filter = new Document();
        filter.put("epoch",epoch);
        filter.put("nanos",nanos);
        FindIterable<Document> docs = reader.loadDocumentsWithFilter(filter);
        List<Reminder> reminders = new ArrayList<>();
        for (Document doc : docs) {
            String userID = doc.getString(ACCESS_KEY);
            Reminder rem = new Reminder(epoch,nanos,userID);
            reminders.add(rem);
        }
        DBWriter writer = new DBWriter(COLLECTION_NAME,"reminder");
        writer.removeDocuments(filter);
        return reminders;
    }

    /**
     * Constructs a new reminder
     * @param epoch long
     * @param nanos int
     * @param userID the Discord ID of the user to whom this reminder belongs to
     */
    public Reminder(long epoch, int nanos, String userID) {
        this.epoch = epoch;
        this.nanos = nanos;
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put("nanos",nanos);
        retDoc.put("epoch",epoch);
        retDoc.put(ACCESS_KEY,userID);
        return retDoc;
    }
}

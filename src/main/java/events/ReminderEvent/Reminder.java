package events.ReminderEvent;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.Writable;

import java.util.ArrayList;
import java.util.List;

public class Reminder implements Writable {
    private final static String COLLECTION_NAME = "reminders";
    private final static String EPOCH_KEY = "epoch";
    private final static DBReader reader = new DBReader(COLLECTION_NAME);
    private final static DBWriter writer = new DBWriter(COLLECTION_NAME);
    private long epoch;
    private long userID;

    /**
     * Load all the reminders for the given EPOCH
     * @param epoch long
     * @return list with all reminders that are scheduled for this epoch
     */
    public static List<Reminder> loadReminders(long epoch) {
        FindIterable<Document> docs = reader.loadDocumentsWithFilter(Filters.lte(EPOCH_KEY,epoch));
        List<Reminder> reminders = new ArrayList<>();
        for (Document doc : docs) {
            long userID = doc.getLong(ACCESS_KEY);
            Reminder rem = new Reminder(epoch,userID);
            reminders.add(rem);
        }
        writer.removeDocuments(Filters.lte(EPOCH_KEY,epoch));
        return reminders;
    }

    /**
     * Constructs a new reminder
     * @param epoch long
     * @param userID the Discord ID of the user to whom this reminder belongs to
     */
    public Reminder(long epoch, long userID) {
        this.epoch = epoch;
        this.userID = userID;
    }

    public long getUserID() {
        return userID;
    }

    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put(EPOCH_KEY,epoch);
        retDoc.put(ACCESS_KEY,userID);
        return retDoc;
    }
}

import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;
import persistence.*;
import org.bson.Document;
import org.elasticsearch.action.ActionListener;
import java.awt.event.ActionListener;

import exceptions.InvalidDocumentException;

public class BirthdayReminder extends Writable implements ActionListener {
    private Timer timer;

    private final String COLLECTION_NAME = "bday_reminder";
    private final String DOCUMENT_NAME = "all";
    private final String YEAR_KEY = "year";
    private final String DAY_KEY = "day";

    public BirthdayReminder() {
        LocalDate lastTimeChecked = loadDate();
        LocalDate now = LocalDate.now();
        if (lastTimeChecked.isBefore(now) {
            checkBirthdays(now);
        }
    }

    public Document toDoc() {
        Document saveFile = new Document();
        saveFile.put(YEAR_KEY, LocalDate.now().getYear());
        saveFile.put(DAY_KEY, LocalDate.now().getDayOfYear());
        saveFile.put(Writable.ACCESS_KEY, DOCUMENT_NAME);
        return saveFile;
    }

    private void checkBirthdays(LocalDate now) {
            BirthdayLog bdayLog = new BirthdayLog(log)
    }

    private LocalDate loadDate() {
        DBReader reader = new DBReader(COLLECTION_NAME, DOCUMENT_NAME);
        try {
            Document doc = reader.loadObject();
            return LocalDate.ofYearDay((int) doc.get(YEAR_KEY), (int) doc.get(DAY_KEY));
        } catch (InvalidDocumentException e) {
            return LocalDate.now().minusDays(1);
        }
    }
}
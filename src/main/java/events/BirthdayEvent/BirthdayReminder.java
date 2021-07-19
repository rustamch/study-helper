import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.swing.*;
import java.util.TimerTask;
import persistence.*;
import org.bson.Document;
import org.elasticsearch.action.ActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import exceptions.InvalidDocumentException;

public class BirthdayReminder extends Writable implements ActionListener {
    private Timer timer;

    private final String COLLECTION_NAME = "bday_reminder";
    public static final String DOCUMENT_NAME = "all";
    public static final String YEAR_KEY = "year";
    public static final String MONTH_KEY = "month";
    public static final String DAY_KEY = "day";
    public static final String HOUR_KEY = "hour";
    public static final String MIN_KEY = "minute";

    public BirthdayReminder() {
        LocalDateTime lastTimeChecked = loadDate();
        timer = new Timer((int) lastTimeChecked.until(lastTimeChecked.plusDays(1), ChronoUnit.MILLIS), this);
    }

    public Document toDoc() {
        Document saveFile = new Document();
        LocalDateTime now = LocalDateTime.now();
        saveFile.put(YEAR_KEY, now.getYear());
        saveFile.put(MONTH_KEY, now.getMonth());
        saveFile.put(DAY_KEY, now.getDayOfMonth());
        saveFile.put(HOUR_KEY, now.getHour());
        saveFile.put(MIN_KEY, now.getMinute());
        saveFile.put(Writable.ACCESS_KEY, DOCUMENT_NAME);
        return saveFile;
    }

    private void checkBirthdays(LocalDate now) {
            BirthdayLog bdayLog = new BirthdayLog(log);
    }

    private LocalDateTime loadDate() {
        DBReader reader = new DBReader(COLLECTION_NAME, DOCUMENT_NAME);
        try {
            Document doc = reader.loadObject();
            return LocalDateTime.of((int) doc.get(YEAR_KEY), (int) doc.get(DAY_KEY), (int) doc.get(HOUR_KEY), (int) doc.get(MIN_KEY), 0);
        } catch (InvalidDocumentException e) {
            return LocalDateTime.now().minusDays(1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        checkBirthdays();
        storeNewTime();
    }

    private void checkBirthdays() {
        BirthdayLog log = new BirthdayLog();
    }

    private void storeNewTime() {

    }
}
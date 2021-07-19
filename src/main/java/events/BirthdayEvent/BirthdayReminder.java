import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.swing.*;

import java.util.Set;
import java.util.TimerTask;
import persistence.*;
import org.bson.Document;
import org.elasticsearch.action.ActionListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import exceptions.InvalidDocumentException;
import events.BirthdayEvent.BirthdayRecord.YEAR_KEY;
import events.BirthdayEvent.BirthdayRecord.MONTH_KEY;
import events.BirthdayEvent.BirthdayRecord.DAY_KEY;

public class BirthdayReminder extends Writable implements ActionListener {
    private Timer timer;
    private BirthdayEvent bdayEvent;

    private final String COLLECTION_NAME = "bdayReminder";
    public static final String DOCUMENT_NAME = "all";
    public static final String HOUR_KEY = "hour";
    public static final String MIN_KEY = "minute";

    public BirthdayReminder(BirthdayEvent e) {
        bdayEvent = e;
        setNewTimer();
    }

    @Override
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

    private Set<String> findMembersWithBday(LocalDate date) {
        DBReader reader = new DBReader(COLLECTION_NAME, "");
        FindIterable<Document> docs = reader.loadDocumentsWithFilter(Filters.eq(MONTH_KEY, date.getMonthValue()), Filter.eq(DAY_KEY, date.getDayOfMonth()));
        HashSet<String> set = new HashSet<>();
        for (Document doc : docs) {
            String userID = doc.getString(ID_KEY);
            set.add(userID);
        }
        return set;
    }

    private LocalDateTime loadDate() {
        DBReader reader = new DBReader(COLLECTION_NAME, DOCUMENT_NAME);
        try {
            Document doc = reader.loadObject();
            return LocalDateTime.of(doc.getInteger(YEAR_KEY), doc.getInteger(DAY_KEY), doc.getInteger(HOUR_KEY), doc.getInteger(MIN_KEY), 0);
        } catch (InvalidDocumentException e) {
            return LocalDateTime.now().minusDays(1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        checkBirthdays();
        storeTime();
        setNewTimer();
    }

    private void checkBirthdays() {
        Set<String> ids = findMembersWithBday(LocalDate.now());
        if (!ids.isEmpty()) {
            bdayEvent.congratulateBday(set);
        }
    }

    private void storeTime() {
        DBWriter writer = new DBWriter(COLLECTION_NAME, DOCUMENT_NAME);
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    private void setNewTimer() {
        LocalDateTime lastTimeChecked = loadDate();
        timer = new Timer((int) lastTimeChecked.until(lastTimeChecked.plusDays(1), ChronoUnit.MILLIS), this);
    }
}
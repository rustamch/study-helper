package model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.Document;

import events.BirthdayEvent.BirthdayReminder;
import events.StudyTimeEvent.StudyTimeLeaderboard;
import exceptions.InvalidDocumentException;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;

public class DailyExecutor extends TimerTask implements Writable {
    private static String COLLECTION_NAME = "daily_exucutor_col";
    private static String EPOCH_KEY = "epoch";
    private static String ACCESS_VALUE = "access";
    private static DBReader reader = new DBReader(COLLECTION_NAME);
    private static DBWriter writer = new DBWriter(COLLECTION_NAME);
    private Timer timer;
    private Instant nextExecutionTime;
    private List<DailyTask> tasksToExecute;

    /**
     * Executes the daily tasks.
     */
    public DailyExecutor() {
        this.timer = new Timer();
        this.tasksToExecute = new ArrayList<>();
        this.tasksToExecute.add(new BirthdayReminder());
        this.tasksToExecute.add(new StudyTimeLeaderboard());
        System.out.println("DailyExecutor initialized");
        loadNextExecutionTime();
        scheduleNextExecution();
    }

    /**
     * Loads the next execution time.
     */
    private void loadNextExecutionTime() {
        Document timeDoc;
        try {
            timeDoc = reader.loadObject(ACCESS_VALUE);
            this.nextExecutionTime = Instant.ofEpochSecond(timeDoc.getLong(EPOCH_KEY));
        } catch (InvalidDocumentException e) {
            nextExecutionTime = Instant.now().plus(24, ChronoUnit.HOURS);
            saveNextExecutionTime();
        }
    }

    /**
     * Saves the next execution time.
     */
    private void saveNextExecutionTime() {
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    /**
     * Schedules the next execution.
     */
    private void scheduleNextExecution() {
        long timeUntilNextExecution = getTimeUntilNextExecution();
        timer.schedule(this, timeUntilNextExecution);
        System.out.println("The next execution is scheduled in " + timeUntilNextExecution + " ms");
    }

    /**
     * Gets the time until the next execution.
     * @return the time until the next execution
     */
    private long getTimeUntilNextExecution() {
        Instant now = Instant.now();
        return now.until(nextExecutionTime, ChronoUnit.MILLIS);
    }

    @Override
    public void run() {
        loadNextExecutionTime();
        tasksToExecute.forEach(task -> task.execute());
        scheduleNextExecution();
        saveNextExecutionTime();
    }

    @Override
    public Document toDoc() {
        long nextExecutionTime = Instant.now().plus(24,ChronoUnit.HOURS).getEpochSecond();
        Document retDoc = new Document();
        retDoc.put(ACCESS_KEY, ACCESS_VALUE);
        retDoc.put(EPOCH_KEY, nextExecutionTime);
        return retDoc;
    }

    
}

package model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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
    private static final String COLLECTION_NAME = "daily_executor_col";
    private static final String EPOCH_KEY = "epoch";
    private static final String ACCESS_VALUE = "access";
    private static final DBReader reader = new DBReader(COLLECTION_NAME);
    private static final DBWriter writer = new DBWriter(COLLECTION_NAME);
    private final Timer timer;
    private Instant nextExecutionTime;
    private final List<Runnable> tasksToExecute;

    /**
     * Executes the daily tasks.
     */
    public DailyExecutor() {
        this.timer = new Timer();
        this.tasksToExecute = new ArrayList<>();
        this.tasksToExecute.add(BirthdayReminder::dailyExecutorSchedule);
        this.tasksToExecute.add(StudyTimeLeaderboard::dailyExecutorSchedule);
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
            nextExecutionTime = Instant.now();
        }
    }


    /**
     * Schedules the next execution.
     */
    private void scheduleNextExecution() {
        long timeUntilNextExecution = getTimeUntilNextExecution();
        if (timeUntilNextExecution == 0) {
            run();
        } else {
            timer.schedule(this, timeUntilNextExecution);
        }
    }

    private void save() {
        writer.saveObject(this,SaveOption.DEFAULT);
    }

    /**
     * Gets the time until the next execution.
     * @return the time until the next execution
     */
    private long getTimeUntilNextExecution() {
        Instant now = Instant.now();
        if (now.isAfter(nextExecutionTime)) {
            return 0;
        } else {
            return now.until(nextExecutionTime, ChronoUnit.MILLIS);
        }
    }

    @Override
    public void run() {
        tasksToExecute.forEach(Runnable::run);
        this.nextExecutionTime = Instant.now().plus(24, ChronoUnit.HOURS);
        save();
        scheduleNextExecution();
    }


    @Override
    public Document toDoc() {
        Document retDoc = new Document();
        retDoc.put(ACCESS_KEY, ACCESS_VALUE);
        retDoc.put(EPOCH_KEY, nextExecutionTime.getEpochSecond());
        return retDoc;
    }


}

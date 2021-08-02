package model;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import events.BirthdayEvent.BirthdayReminder;
import persistence.DBReader;

public class DailyExecutor extends TimerTask{
    private Timer timer;
    private Instant nextExecutionTime;
    private List<DailyTask> tasksToExecute;

    /**
     * 
     */
    public DailyExecutor() {
        this.timer = new Timer();
        loadNextExecutionTime();
        scheduleNextExecution();
    }

    private void loadNextExecutionTime() {
        reader.
    }

    private void scheduleNextExecution() {
        long timeUntilNextExecution = getTimeUntilNextExecution();
        timer.schedule(this, timeUntilNextExecution);
    }

    private long getTimeUntilNextExecution() {
        return 0;
    }

    @Override
    public void run() {

        loadNextExecutionTime();
        scheduleNextExecution();
    }

    
}

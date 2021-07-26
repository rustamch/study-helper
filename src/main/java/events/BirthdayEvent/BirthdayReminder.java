package events.BirthdayEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bson.Document;

import exceptions.InvalidDocumentException;
import model.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import persistence.DBReader;
import persistence.DBWriter;
import persistence.SaveOption;
import persistence.Writable;


public class BirthdayReminder extends Writable  {
    private Timer timer;
    private final String EPOCH_KEY = "epoch";
    private final String COLLECTION_NAME = "bdayReminder";

    /**
     * Constructs a new BirthdayReminder.
     */
    public BirthdayReminder() {
        this.timer = new Timer();
        setNewTimer();
    }

    @Override
    public Document toDoc() {
        Document saveFile = new Document();
        Instant now = Instant.now();
        saveFile.put(ACCESS_KEY,"reminder_time");
        saveFile.put(EPOCH_KEY,now.getEpochSecond());
        return saveFile;
    }

    /**
     * Loads the time of the next birthday reminder.
     * @return The time of the next birthday reminder.
     */
    private Instant loadNextTimer() {
        DBReader reader = new DBReader(COLLECTION_NAME);
        try {
            Document doc = reader.loadObject();
            return Instant.ofEpochSecond(doc.getLong(EPOCH_KEY));
        } catch (InvalidDocumentException e) {
            return Instant.now();
        }
    }

    /**
     * Checks if the users have a birthday today. Stores the time of the next birthday reminder to the database.
     * Sets a timer to the next birthday reminder.
     */
    public void onTimer() {
        checkBirthdays();
        storeNextReminderTime();
        setNewTimer();
    }

    /**
     * Checks if the users have a birthday today.
     */
    private void checkBirthdays() {
        Set<String> ids = BirthdayRecord.findMembersWithBdayOnGivenDay(LocalDate.now());
        if (!ids.isEmpty()) {
            congratulateBday(ids);
        }
    }

    /**
     * Sends a congradulations message to the users with a birthday today.
     * @param memberIDs ids of the users with a birthday today.
     */
    public void congratulateBday(Set<String> memberIDs) {
        for (String id : memberIDs) {
            try {
                Bot.BOT_JDA.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            User user = Bot.BOT_JDA.retrieveUserById(id).complete();
            user.openPrivateChannel().queue(chan -> chan.sendMessage(String.format("Happy Birthday %s!",user.getName())).queue());
            List<Guild> guilds = user.getMutualGuilds();
            for (Guild g : guilds) {
                g.getTextChannelsByName("general", true).get(0).sendMessage("Happy birthday " + g.getMemberById(id).getEffectiveName() + "!").queue();
            }
        }
        // StringBuilder builder = new StringBuilder();
        // for (String id : memberIDs) {
        //     builder.append("<@");
        //     builder.append(id);
        //     builder.append("> ");
        // }
        // Bot.BOT_JDA.retrieveUserById(id)
        // for (GuildChannel channel : studeyHall.getChannels()) {
        //     if (channel.getName().equals("general")) {
        //         channel.sendMessage("Happy Birthday! " + builder.toString()).queue();
        //         break;
        //     }
        // }
    }

    /** 
     * Store the time of the next birthday reminder to the database.
     */
    private void storeNextReminderTime() {
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        writer.saveObject(this, SaveOption.DEFAULT);
    }

    /**
     * Sets a timer for the next birthday reminder. If the time record on the databse
     * points to the point of time before now - execute the birthday reminder routine.
     */
    private void setNewTimer() {
        Instant nextTimer = loadNextTimer();
        Instant now = Instant.now();
        if (now.until(nextTimer, ChronoUnit.MILLIS) <= 0) {
            checkBirthdays();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onTimer();
                }
            }, now.until(now.plus(1, ChronoUnit.DAYS), ChronoUnit.MILLIS));
            storeNextReminderTime();
        } else {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onTimer();
            }
        }, now.until(nextTimer, ChronoUnit.MILLIS));
        }
    }
}
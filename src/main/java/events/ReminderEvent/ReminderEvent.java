package events.ReminderEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;

import javax.swing.Timer;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import exceptions.InvalidReminderException;
import exceptions.InvalidTimeInHoursException;
import exceptions.InvalidTimeInMinutesException;

public class ReminderEvent implements ActionListener, BotMessageEvent {
    private final ReminderManager reminderManager;

    /**
     * Constructs a new reminder and sets the timer to check time very minute.
     */
    public ReminderEvent() {
        reminderManager = new ReminderManager();
        Timer timer = new Timer(60000, this);
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        TextChannel channel = event.getChannel();
        try {
            reminderManager.addReminder(event);
            channel.sendMessage("Reminder was added!");
        } catch (InvalidTimeInMinutesException e) {
            channel.sendMessage("Invalid reminder format. " +
                    "Please try again with a valid format e.g. **!reminder 15 min**");
        } catch (InvalidTimeInHoursException e) {
            channel.sendMessage("Invalid reminder format. " +
                    "Please try again with a valid format e.g. **!reminder 3 hr**");
        } catch (InvalidReminderException e) {
            channel.sendMessage("Invalid reminder format. Please try **!reminder YYYY.MM.DD**");
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Instant now = Instant.now();
        reminderManager.notifyUsers(now);
    }
}

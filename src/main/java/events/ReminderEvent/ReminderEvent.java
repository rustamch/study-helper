package events.ReminderEvent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.swing.Timer;

import events.BotEvent;
import exceptions.InvalidReminderException;
import exceptions.InvalidTimeInHoursException;
import exceptions.InvalidTimeInMinutesException;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ReminderEvent implements ActionListener, BotEvent {
    private final ReminderManager reminderManager;
    private final Timer timer;

    /**
     * Constructs a new reminder and sets the timer to check time very minute.
     */
    public ReminderEvent() {
        reminderManager = new ReminderManager();
        timer = new Timer(60000, this);
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public void invoke(MessageReceivedEvent event, String[] content) {
        MessageChannel channel = event.getChannel();
        try {
            reminderManager.addReminder(event);
            channel.sendMessage("Reminder was added!").queue();
        } catch (InvalidTimeInMinutesException e) {
            channel.sendMessage("Invalid reminder format. " +
                    "Please try again with a valid format e.g. **!reminder 15min**").queue();
        } catch (InvalidTimeInHoursException e) {
            channel.sendMessage("Invalid reminder format. " +
                    "Please try again with a valid format e.g. **!reminder 3hr**").queue();
        } catch (InvalidReminderException e) {
            channel.sendMessage("Invalid reminder format. Please try **!reminder YYYY.MM.DD**").queue();
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Instant currMin = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        reminderManager.notifyUsers(currMin);
    }
}

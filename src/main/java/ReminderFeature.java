import com.sun.media.sound.InvalidFormatException;
import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import model.ReminderManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

public class ReminderFeature extends ListenerAdapter implements ActionListener {
    private ReminderManager reminderManager;
    private Timer timer;

    public ReminderFeature() {
        reminderManager = new ReminderManager();
        timer = new Timer(1000, this);
        timer.setRepeats(true);
        timer.start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        String messageStringRaw = message.getContentRaw();

        if (Pattern.matches("!reminder\\s.*", messageStringRaw)) {
            try {
                reminderManager.addReminder(event);
                System.out.println("This should not be printed if exception thrown!"); //todo
                channel.sendMessage("Reminder was added!").queue();
            } catch (DuplicateReminderException e) {
                System.out.println("This prints only when duplicated.");     //todo
                channel.sendMessage("Duplicated reminder.").queue();
            } catch (InvalidReminderFormatException e) {
                System.out.println("This is printed if error with object!");  //todo
                channel.sendMessage("Invalid reminder format.").queue();
            }
        } else if (Pattern.matches("!reminders.*", messageStringRaw)) {
            channel.sendMessage(reminderManager.getAllReminders()).queue();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalDateTime dateAndTimeNow = LocalDateTime.now(ZoneId.of("Canada/Pacific"));
        dateAndTimeNow = dateAndTimeNow.withSecond(0).withNano(0);

        if (reminderManager.containsReminder(dateAndTimeNow)) {
            MessageChannel channel = reminderManager.getChannel(dateAndTimeNow);
            String message = reminderManager.getMessage(dateAndTimeNow);

            channel.sendMessage(message).queue();
        }
    }
}

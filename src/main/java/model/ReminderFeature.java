package model;

import model.ReminderManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.swing.*;

import exceptions.DuplicateReminderException;
import exceptions.InvalidReminderException;
import exceptions.InvalidTimeInHoursException;
import exceptions.InvalidTimeInMinutesException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ReminderFeature extends ListenerAdapter implements ActionListener {
    private final ReminderManager reminderManager;
    private final Timer timer;

    public ReminderFeature() {
        reminderManager = new ReminderManager();
        timer = new Timer(60000, this);
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
                channel.sendMessage("Reminder was added!").queue();
            } catch (DuplicateReminderException e) {
                channel.sendMessage("Duplicated reminder.").queue();
            } catch (InvalidTimeInMinutesException e) {
                channel.sendMessage("Invalid reminder format. " +
                        "Please try again with a valid format e.g. **!reminder 15min**").queue();
            } catch (InvalidTimeInHoursException e) {
                channel.sendMessage("Invalid reminder format. " +
                        "Please try again with a valid format e.g. **!reminder 3hr**").queue();
            } catch (InvalidReminderException e) {
                channel.sendMessage("Invalid reminder format. Please try **!reminder YYYY.MM.DD**").queue();
            }
        } else if (Pattern.matches("!reminders\\s*", messageStringRaw)) {
            channel.sendMessage(reminderManager.getAllReminders()).queue();
        } else if (Pattern.matches("!reminders.*", messageStringRaw)) {
            channel.sendMessage("Did you mean **!reminders**?").queue();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LocalDateTime dateAndTimeNow = LocalDateTime.now();
        dateAndTimeNow = dateAndTimeNow.withSecond(0).withNano(0);

        if (reminderManager.containsReminder(dateAndTimeNow)) {
            MessageChannel channel = reminderManager.getChannel(dateAndTimeNow);
            String message = reminderManager.getMessage(dateAndTimeNow);

            channel.sendMessage(message).queue();
            reminderManager.removeReminder(dateAndTimeNow);
        }
    }
}

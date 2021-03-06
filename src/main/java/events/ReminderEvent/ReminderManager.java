package events.ReminderEvent;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;


import org.javacord.api.event.message.MessageCreateEvent;

import exceptions.InvalidReminderException;
import exceptions.InvalidTimeInHoursException;
import exceptions.InvalidTimeInMinutesException;
import model.Bot;
import persistence.DBWriter;
import persistence.SaveOption;

// A manager for reminders
public class ReminderManager {

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageCreateEvent event)
            throws InvalidReminderException {
        Instant adjustedInstant = parseEventToInstant(event);
        long epoch = adjustedInstant.getEpochSecond();
        long memberID = event.getMessageAuthor().getId();
        Reminder rem = new Reminder(epoch, memberID);
        String COLLECTION_NAME = "reminders";
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        writer.saveObject(rem, SaveOption.REPLACE_DUPLICATES_ONLY);
    }


    /**
     * Parses the given message into instant of time that is offset by time specified in the message
     * @param event JDA event that carries the message and other user information
     * @return Instant of time that is offset by time specified in the message
     * @throws InvalidReminderException
     */
    private Instant parseEventToInstant(MessageCreateEvent event)
            throws InvalidReminderException {
        String eventMessageRaw = event.getMessageContent();
        Instant truncatedInstant = null;
        if (eventMessageRaw.matches("!reminder\\s+\\d+\\s+min.*|minutes.*|minute.*")) {
            truncatedInstant = parseStringTimeInMinutesToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("!reminder\\s+\\d+\\s+hr.*|hours.*|hour.*")) {
            truncatedInstant = parseStringTimeInHoursToInstance(eventMessageRaw);
        } else {
            throw new InvalidReminderException();
        }
        assert truncatedInstant != null;
        return truncatedInstant;
    }

    /**
     * Parses user message and returns the Instance that is set to current time plus number of minutes
     * specified in the message
     * @param message the message that contains info about reminder
     * @return  Instance that is set to current time plus number of hours
     * @throws InvalidTimeInHoursException
     */
    private Instant parseStringTimeInMinutesToLocalDateTime(String message)
            throws InvalidTimeInMinutesException {
        Instant curr = Instant.now();
        int minutes = 0;
        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("min|minute|minutes")[0];
        try {
            minutes = Integer.parseInt(minuteString);
            curr = curr.plus(minutes,ChronoUnit.MINUTES);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInMinutesException();
        }
        return curr;
    }

    /**
     * Parses user message and returns the Instance that is set to current time plus number of hours
     * specified in the message
     * @param message the message that contains info about reminder
     * @return  Instance that is set to current time plus number of hours
     * @throws InvalidTimeInHoursException
     */
    private Instant parseStringTimeInHoursToInstance(String message)
            throws InvalidTimeInHoursException {
        Instant curr = Instant.now();
        int hours = 0;
        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("hr|hours|hour")[0];
        try {
            hours = Integer.parseInt(minuteString);
            curr = curr.plus(hours,ChronoUnit.HOURS);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInHoursException();
        }
        return curr;
    }

    /**
     * Sends the message to all users that have asked to be reminded
     * @param currMin the instance that contains current time truncated to closest minute
     */
    public void notifyUsers(Instant currMin) {
        List<Reminder> reminders = Reminder.loadReminders(currMin.getEpochSecond());
        for (Reminder rm : reminders) {
            notifyUser(rm);
        }
    }

    /**
     * Sends a message to the user that he asked to be notified about something 
     * @param rm a reminder that contains the id of the user
     */
    private void notifyUser(Reminder rm) {
        Bot.API.getUserById(rm.getUserID()).thenAccept(user ->
            user.sendMessage("You have been asked to be notified about something."));
    }
}
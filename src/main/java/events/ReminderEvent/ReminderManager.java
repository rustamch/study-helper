package events.ReminderEvent;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import exceptions.DuplicateReminderException;
import exceptions.InvalidReminderException;
import exceptions.InvalidTimeInHoursException;
import exceptions.InvalidTimeInMinutesException;
import model.Bot;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import persistence.DBWriter;
import persistence.SaveOption;

// A manager for reminders
public class ReminderManager {
    private final String DEFAULT_TIME = "9:00";
    private final String COLLECTION_NAME = "reminders";

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageReceivedEvent event)
            throws DuplicateReminderException, InvalidReminderException {
        Instant adjustedInstant = parseEventToInstant(event);
        long epoch = adjustedInstant.getEpochSecond();
        String memberID = event.getAuthor().getId();
        Reminder rem = new Reminder(epoch, memberID);
        DBWriter writer = new DBWriter(COLLECTION_NAME, memberID);
        writer.saveObject(rem, SaveOption.REPLACE_DUPLICATES_ONLY);
    }


    // parses an event to a LocalDateTime
    private Instant parseEventToInstant(MessageReceivedEvent event)
            throws InvalidReminderException {
        String eventMessageRaw = event.getMessage().getContentRaw();
        Instant truncatedInstant = null;
//        // message format: "!reminder YYYY.MM.DD HH:MM" or "!reminder YYYY.MM.DD"
//        if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}\\s+\\d{2}:\\d{2}.*")) {
//            truncatedInstant = parseStringDateAndTimeToLocalDateTime(eventMessageRaw);
//        } else if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}.*")) {
//            truncatedInstant = parseStringDateToLocalDateTime(eventMessageRaw);
//        } else
        if (eventMessageRaw.matches("!reminder\\s+\\d+min.*")) {
            truncatedInstant = parseStringTimeInMinutesToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("!reminder\\s+\\d+hr.*")) {
            truncatedInstant = parseStringTimeInHoursToLocalDateTime(eventMessageRaw);
        } else {
            throw new InvalidReminderException();
        }
        assert truncatedInstant != null;
        return truncatedInstant;
    }

    // parses time in minutes from string
    private Instant parseStringTimeInMinutesToLocalDateTime(String message)
            throws InvalidTimeInMinutesException {
        Instant curr = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        int minutes = 0;
        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("min")[0];
        try {
            minutes = Integer.parseInt(minuteString);
            curr = curr.plus(minutes,ChronoUnit.MINUTES);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInMinutesException();
        }
        return curr;
    }

    // parses time in hours from string
    private Instant parseStringTimeInHoursToLocalDateTime(String message)
            throws InvalidTimeInHoursException {
        Instant curr = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        int hours = 0;

        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("hr")[0];
        try {
            hours = Integer.parseInt(minuteString);
            curr = curr.plus(hours,ChronoUnit.HOURS);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInHoursException();
        }
        return curr;
    }
//
//    // parses date and time from string
//    private Instant parseStringDateAndTimeToLocalDateTime(String message)
//            throws InvalidReminderException {
//        LocalDateTime localDateTime = null;
//
//        // message format: "!reminder YYYY.MM.DD hh:mm"
//        String[] messages = message.split("\\s+");
//        String dateAndTimeString = messages[1] + " " + messages[2];
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");
//
//        try {
//            localDateTime = LocalDateTime.parse(dateAndTimeString, formatter);
//        } catch (DateTimeParseException e) {
//            throw new InvalidReminderException();
//        }
//        assert localDateTime != null;
//
//        return localDateTime;
//    }
//
//    // parses date from string
//    private Instant parseStringDateToLocalDateTime(String message)
//            throws InvalidReminderException {
//        LocalDateTime localDateTime = null;
//
//        // message format: "!reminder YYYY.MM.DD hh:mm"
//        String[] messages = message.split("\\s+");
//        String dateString = messages[1] + " " + DEFAULT_TIME;
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");
//        try {
//            localDateTime = LocalDateTime.parse(dateString, formatter);
//        } catch (DateTimeParseException e) {
//            throw new InvalidReminderException();
//        }
//        assert localDateTime != null;
//        return localDateTime;
//    }

    public void notifyUsers(Instant currMin) {
        List<Reminder> reminders = Reminder.loadReminders(currMin.getEpochSecond());
        for (Reminder rm : reminders) {
            notifyUser(rm);
        }
    }

    private void notifyUser(Reminder rm) {
        Bot.BOT_JDA.getUserById(rm.getUserID()).openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage("Reminding you of something!").queue();
        });
    }
}
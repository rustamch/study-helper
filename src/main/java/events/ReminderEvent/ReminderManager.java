package events.ReminderEvent;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        LocalDateTime reminder = parseEventToLocalDateTime(event);
        reminder = reminder.withSecond(0).withNano(0);
        long epoch = reminder.toEpochSecond(ZoneOffset.UTC);
        int nanos = reminder.getNano();
        String memberID = event.getAuthor().getId();
        Reminder rem = new Reminder(epoch, nanos, memberID);
        DBWriter writer = new DBWriter(COLLECTION_NAME, memberID);
        writer.saveObject(rem, SaveOption.REPLACE_DUPLICATES_ONLY);
    }


    // parses an event to a LocalDateTime
    private LocalDateTime parseEventToLocalDateTime(MessageReceivedEvent event)
            throws InvalidReminderException {
        String eventMessageRaw = event.getMessage().getContentRaw();
        LocalDateTime localDateTime = null;
        // message format: "!reminder YYYY.MM.DD HH:MM" or "!reminder YYYY.MM.DD"
        if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}\\s+\\d{2}:\\d{2}.*")) {
            localDateTime = parseStringDateAndTimeToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}.*")) {
            localDateTime = parseStringDateToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("!reminder\\s+\\d+min.*")) {
            localDateTime = parseStringTimeInMinutesToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("!reminder\\s+\\d+hr.*")) {
            localDateTime = parseStringTimeInHoursToLocalDateTime(eventMessageRaw);
        } else {
            throw new InvalidReminderException();
        }
        assert localDateTime != null;
        return localDateTime;
    }

    // parses time in minutes from string
    private LocalDateTime parseStringTimeInMinutesToLocalDateTime(String message)
            throws InvalidTimeInMinutesException {
        LocalDateTime localDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        int minutes = 0;

        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("min")[0];
        try {
            minutes = Integer.parseInt(minuteString);
            localDateTime = localDateTime.plusMinutes(minutes);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInMinutesException();
        }

        return localDateTime;
    }

    // parses time in hours from string
    private LocalDateTime parseStringTimeInHoursToLocalDateTime(String message)
            throws InvalidTimeInHoursException {
        LocalDateTime localDateTime = LocalDateTime.now().withSecond(0).withNano(0);
        int hours = 0;

        String[] messages = message.split("\\s+");
        String minuteString = messages[1].split("hr")[0];
        try {
            hours = Integer.parseInt(minuteString);
            localDateTime = localDateTime.plusHours(hours);
        } catch (NumberFormatException e) {
            throw new InvalidTimeInHoursException();
        }

        return localDateTime;
    }

    // parses date and time from string
    private LocalDateTime parseStringDateAndTimeToLocalDateTime(String message)
            throws InvalidReminderException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD hh:mm"
        String[] messages = message.split("\\s+");
        String dateAndTimeString = messages[1] + " " + messages[2];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");

        try {
            localDateTime = LocalDateTime.parse(dateAndTimeString, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderException();
        }
        assert localDateTime != null;

        return localDateTime;
    }

    // parses date from string
    private LocalDateTime parseStringDateToLocalDateTime(String message)
            throws InvalidReminderException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD hh:mm"
        String[] messages = message.split("\\s+");
        String dateString = messages[1] + " " + DEFAULT_TIME;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");
        try {
            localDateTime = LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderException();
        }
        assert localDateTime != null;
        return localDateTime;
    }

    public void notifyUsers(LocalDateTime dateAndTimeNow) {
        List<Reminder> reminders = Reminder.loadReminders(dateAndTimeNow.toEpochSecond(ZoneOffset.UTC),
                dateAndTimeNow.getNano());
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
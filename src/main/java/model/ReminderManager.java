package model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class ReminderManager {
    private Map<LocalDateTime, User> reminders;


    // constructs ReminderManager object
    public ReminderManager() {
        reminders = new HashMap<>();
    }

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageReceivedEvent event)
            throws DuplicateReminderException, InvalidReminderFormatException {
        LocalDateTime reminder = parseEventToLocalDateTime(event);
        User user = event.getAuthor();

        if (contains(reminder, user)) {
            throw new DuplicateReminderException();
        }

        reminders.put(reminder, user);
    }

    public String buildReminder() {
        return null;
    }


    // parses an event to a LocalDateTime
    private LocalDateTime parseEventToLocalDateTime(MessageReceivedEvent event)
            throws InvalidReminderFormatException {
        String eventMessageRaw = event.getMessage().getContentRaw();
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD"
        if (eventMessageRaw.matches("\\!reminder \\d{4}\\.\\d{2}.\\d{2}\\.*\\d{1,2}")) {
            localDateTime = parseStringDateAndTimeToLocalDateTime(eventMessageRaw);
        } else if (eventMessageRaw.matches("\\!reminder \\d{4}\\.\\d{2}.\\d{2}\\.*")) {
            localDateTime = parseStringDateToLocalDateTime(eventMessageRaw);
        }

        return localDateTime;
    }

    // parses date and time from string
    private LocalDateTime parseStringDateAndTimeToLocalDateTime(String message)
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD HH:MM"
        String[] messages = message.split("\\s*");
        String dateAndTimeString = messages[1] + " " + messages[2];
        DateTimeFormatter format = DateTimeFormatter.ofPattern("uuuu.M.d H:mm");
        try {
            localDateTime = LocalDateTime.parse(dateAndTimeString, format);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        return localDateTime;
    }

    // parses date from string
    private LocalDateTime parseStringDateToLocalDateTime(String message)
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD"
        String[] messages = message.split("\\.");
        try {
            String dateString = messages[1] + "-" + messages[2] + "-" + messages[3];
            localDateTime = LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        return localDateTime;
    }

    // checks if reminder for user contained in map
    private boolean contains(LocalDateTime reminder, User user) {
        return reminders.containsKey(reminder) && reminders.containsValue(user);
    }
}

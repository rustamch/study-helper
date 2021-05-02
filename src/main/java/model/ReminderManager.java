package model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;

import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import exception.InvalidTimeInHoursException;
import exception.InvalidTimeInMinutesException;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class ReminderManager {
    private final Map<LocalDateTime, MessageReceivedEvent> reminders;
    private final String DEFAULT_TIME = "9:00";

    // constructs ReminderManager object
    public ReminderManager() {
        reminders = new TreeMap<>();
    }

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageReceivedEvent event)
            throws DuplicateReminderException, InvalidReminderFormatException {
        LocalDateTime reminder = parseEventToLocalDateTime(event);
        reminder = reminder.withSecond(0).withNano(0);
        User user = event.getAuthor();

        if (containsDateTimeAndUser(reminder, user)) {
            throw new DuplicateReminderException();
        }

        reminders.put(reminder, event);
    }

    // returns true if reminder is in reminders
    public boolean containsReminder(LocalDateTime reminder) {
        return reminders.containsKey(reminder);
    }

    // returns a message for the given reminder
    public String getMessage(LocalDateTime reminder) {
        MessageReceivedEvent event = reminders.get(reminder);
        User user = event.getAuthor();
        // returns the message for the user with a ping
        return "Reminding <@" + user.getId() + ">! You have something planned right now!";
    }

    // returns all reminders as a message
    public String getAllReminders() {
        String message = "Reminders:\n";
        for (Map.Entry<LocalDateTime, MessageReceivedEvent> entry : reminders.entrySet()) {
            String dateString = entry.getKey().toString();
            String nameString = entry.getValue().getAuthor().getName();
            message = message + dateString + " " + nameString + "\n";
        }
        return message;
    }

    // returns the message channel for the reminder
    public MessageChannel getChannel(LocalDateTime reminder) {
        MessageReceivedEvent event = reminders.get(reminder);
        return event.getChannel();
    }

    // parses an event to a LocalDateTime
    private LocalDateTime parseEventToLocalDateTime(MessageReceivedEvent event)
            throws InvalidReminderFormatException {
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
            throw new InvalidReminderFormatException();
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
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD hh:mm"
        String[] messages = message.split("\\s+");
        String dateAndTimeString = messages[1] + " " + messages[2];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");

        try {
            localDateTime = LocalDateTime.parse(dateAndTimeString, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        assert localDateTime != null;

        return localDateTime;
    }

    // parses date from string
    private LocalDateTime parseStringDateToLocalDateTime(String message)
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD hh:mm"
        String[] messages = message.split("\\s+");
        String dateString = messages[1] + " " + DEFAULT_TIME;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("u.M.d H:m");
        try {
            localDateTime = LocalDateTime.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        assert localDateTime != null;

        return localDateTime;
    }

    // checks if reminder for user contained in map
    private boolean containsDateTimeAndUser(LocalDateTime reminder, User user) {
        return reminders.containsKey(reminder)
                && reminders.get(reminder).getAuthor().equals(user);
    }
}

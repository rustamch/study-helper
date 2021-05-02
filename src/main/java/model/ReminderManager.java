package model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class ReminderManager {
    private Map<LocalDateTime, MessageReceivedEvent> reminders;

    // constructs ReminderManager object
    public ReminderManager() {
        reminders = new HashMap<>();
    }

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageReceivedEvent event)
            throws DuplicateReminderException, InvalidReminderFormatException {
        LocalDateTime reminder = parseEventToLocalDateTime(event);
        reminder = reminder.withSecond(0).withNano(0);
        User user = event.getAuthor();

        if (contains(reminder, user)) {
            throw new DuplicateReminderException();
        }

        reminders.put(reminder, event);
    }

    // returns true if reminder is in reminders
    public boolean containsReminder(LocalDateTime reminder) {
        return reminders.containsKey(reminder);
    }

    public String getMessage(LocalDateTime reminder) {
        MessageReceivedEvent event = reminders.get(reminder);
        User user = event.getAuthor();
        String message = "Reminding " + user.getName() + "!";
        return message;
    }

    public String getAllReminders() {
        String message = "Reminders:\n";
        for (Map.Entry<LocalDateTime, MessageReceivedEvent> entry : reminders.entrySet()) {
            String dateString = entry.getKey().toString();
            String nameString = entry.getValue().getAuthor().getName();
            message = message + dateString + " " + nameString + "\n";
        }
        return message;
    }

    public MessageChannel getChannel(LocalDateTime reminder) {
        MessageReceivedEvent event = reminders.get(reminder);
        MessageChannel channel = event.getChannel();
        return channel;
    }


    // parses an event to a LocalDateTime
    private LocalDateTime parseEventToLocalDateTime(MessageReceivedEvent event)
            throws InvalidReminderFormatException {
        String eventMessageRaw = event.getMessage().getContentRaw();
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD HH:MM" or "!reminder YYYY.MM.DD"
        if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}\\s+\\d{2}:\\d{2}.*")) {
            localDateTime = parseStringDateAndTimeToLocalDateTime(eventMessageRaw);
        }
        if (eventMessageRaw.matches("!reminder\\s\\d{4}\\.\\d{2}.\\d{2}.*")) {
            localDateTime = parseStringDateToLocalDateTime(eventMessageRaw);
        }

        return localDateTime;
    }

    // parses date and time from string
    private LocalDateTime parseStringDateAndTimeToLocalDateTime(String message)
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD HH:MM"
        String[] messages = message.split("\\s+");
        String dateAndTimeString = messages[1] + " " + messages[2];
        DateTimeFormatter format = DateTimeFormatter.ofPattern("u.M.d H:m");

        try {
            localDateTime = LocalDateTime.parse(dateAndTimeString, format);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        if (localDateTime == null) {
            System.out.println("Date and Time Error"); //todo
            throw new InvalidReminderFormatException();
        }

        return localDateTime;
    }

    // parses date from string
    private LocalDateTime parseStringDateToLocalDateTime(String message)
            throws InvalidReminderFormatException {
        LocalDateTime localDateTime = null;

        // message format: "!reminder YYYY.MM.DD"
        String[] messages = message.split("\\s+");
        String dateString = messages[1].replace('.', '-');
        try {
            localDateTime = LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new InvalidReminderFormatException();
        }

        if (localDateTime == null) {
            System.out.println("Date Only Error"); //todo
            throw new InvalidReminderFormatException();
        }

        return localDateTime;
    }

    // checks if reminder for user contained in map
    private boolean contains(LocalDateTime reminder, User user) {
        return reminders.containsKey(reminder) && reminders.containsValue(user);
    }
}

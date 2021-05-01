package model;

import exception.DuplicateReminderException;
import exception.InvalidReminderFormatException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class ReminderManager {
    private Map<Calendar, User> reminders;

    // constructs ReminderManager object
    public ReminderManager() {
        reminders = new HashMap<>();
    }

    // checks for duplicates and adds reminder to reminders
    public void addReminder(MessageReceivedEvent event)
            throws DuplicateReminderException, InvalidReminderFormatException {
        Calendar reminder = parseEventToCalendar(event);
        User user = event.getAuthor();

        if (contains(reminder, user)) {
            throw new DuplicateReminderException();
        }

        reminders.put(reminder, user);
    }

    // parses an event to a calendar
    private Calendar parseEventToCalendar(MessageReceivedEvent event)
            throws InvalidReminderFormatException {
        String eventMessageRaw = event.getMessage().getContentRaw();

        eventMessageRaw[]
    }

    // checks if reminder for user contained in map
    private boolean contains(Calendar reminder, User user) {
        return reminders.containsKey(reminder) && reminders.containsValue(user);
    }

}

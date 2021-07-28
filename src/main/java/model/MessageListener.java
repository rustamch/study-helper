package model;

import java.util.Arrays;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import events.BotEvent;
import events.BirthdayEvent.BirthdayEvent;
import events.ReminderEvent.ReminderEvent;
import events.SimpleEvents.AboutEvent;
import events.SimpleEvents.DoraListener;
import events.TodoEvent.TodoEvent;

public class MessageListener implements MessageCreateListener {
    private BotEvent bdayEvent;
    private BotEvent todoEvent;
    private BotEvent reminderEvent;
    private BotEvent oOEvent;
    private BotEvent abtEvent;

    public MessageListener() {
        bdayEvent = new BirthdayEvent();
        todoEvent = new TodoEvent();
        reminderEvent = new ReminderEvent();
        oOEvent = new DoraListener();
        abtEvent = new AboutEvent();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        String[] msgArr = event.getMessageContent().split("\\s");
        String command = msgArr[0];
        String[] content = Arrays.copyOfRange(msgArr, 1, msgArr.length);
        if (command.charAt(0) == '!') {
            switch (command) {
                case "!bday":
                    bdayEvent.invoke(event, content);
                    break;
                case "!about":
                    abtEvent.invoke(event, content);
                    break;
                case "!reminder":
                    reminderEvent.invoke(event, content);
                    break;
                case "!o_O":
                    oOEvent.invoke(event, content);
                    break;
                case "!todo":
                    todoEvent.invoke(event, content);
                    break;
            }
        }
    }
}
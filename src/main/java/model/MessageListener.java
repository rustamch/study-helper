package model;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

import events.BotEvent;
import events.BirthdayEvent.BirthdayEvent;
import events.ReminderEvent.ReminderEvent;
import events.SimpleEvents.AboutEvent;
import events.SimpleEvents.DoraListener;
import events.TodoEvent.TodoEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageListener extends ListenerAdapter{
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
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String[] msgArr = msg.getContentRaw().split("\\s");
        String command = msgArr[0];
        String[] content = Arrays.copyOfRange(msgArr, 1, msgArr.length);
        switch (command) {
            case "!bday" :
                bdayEvent.invoke(event, content);
                break;
            case "!about" :
                abtEvent.invoke(event, content);
                break;
            case "!reminder" :
                reminderEvent.invoke(event, content);
                break;
            case "o_O":
                oOEvent.invoke(event, content);
                break;
            case "!todo" :
                todoEvent.invoke(event, content);
                break;
        }
    }
}
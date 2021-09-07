package model;

import java.util.Arrays;

import events.ConfigCommand;
import events.ReactionEvent.ReactRoleMessageEvent;
import events.studysession.StudySessionEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import events.BotMessageEvent;
import events.BirthdayEvent.BirthdayEvent;
import events.PurgeEvent.PurgeEvent;
import events.ReminderEvent.ReminderEvent;
import events.SimpleEvents.AboutEvent;
import events.SimpleEvents.DoraListener;
import events.StudyTimeEvent.StudyTimeEvent;
import events.TodoEvent.TodoEvent;

public class MessageListener implements MessageCreateListener {
    private final BotMessageEvent bdayEvent;
    private final BotMessageEvent todoEvent;
    private final BotMessageEvent reminderEvent;
    private final BotMessageEvent oOEvent;
    private final BotMessageEvent abtEvent;
    private final BotMessageEvent studyTimeEvent;
    private final BotMessageEvent reactRoleMesageEvent;
    private final BotMessageEvent purgeEvent;
    private final BotMessageEvent configCommand;
    private final BotMessageEvent studySessionCommand;

    public MessageListener() {
        bdayEvent = new BirthdayEvent();
        todoEvent = new TodoEvent();
        reminderEvent = new ReminderEvent();
        oOEvent = new DoraListener();
        abtEvent = new AboutEvent();
        studyTimeEvent = new StudyTimeEvent();
        reactRoleMesageEvent = new ReactRoleMessageEvent();
        purgeEvent = new PurgeEvent();
        configCommand = new ConfigCommand();
        studySessionCommand = new StudySessionEvent();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        String message = event.getMessageContent();
        if (message.charAt(0) == '!') {
            String[] msgArr = event.getMessageContent().split("\\s");
            String command = msgArr[0];
            String[] content = Arrays.copyOfRange(msgArr, 1, msgArr.length);
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
                case "!studytime":
                    studyTimeEvent.invoke(event, content);
                    break;
                case "!rr":
                    reactRoleMesageEvent.invoke(event,content);
                    break;
                case "!del":
                    purgeEvent.invoke(event, content);
                    break;
                case "!config":
                    configCommand.invoke(event,content);
                    break;
                case "!studysession":
                    studySessionCommand.invoke(event,content);
                    break;
            }
        }
    }
}
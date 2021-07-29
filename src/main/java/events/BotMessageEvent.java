package events;

import org.javacord.api.event.message.MessageCreateEvent;

public interface BotMessageEvent {
    public void invoke(MessageCreateEvent event, String[] content);
}

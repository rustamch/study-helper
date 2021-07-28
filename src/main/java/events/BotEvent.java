package events;

import org.javacord.api.event.message.MessageCreateEvent;

public interface BotEvent {
    public void invoke(MessageCreateEvent event, String[] content);
}

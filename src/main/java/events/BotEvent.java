package events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface BotEvent {
    public void invoke(MessageReceivedEvent event, String[] content);
}

package events.PurgeEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;

public class PurgeEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (content.length > 0) {
            int num = Integer.parseInt(content[0]);
            TextChannel channel = event.getChannel();
            event.getMessage().getUserAuthor().ifPresent(user -> {
                if(channel.canManageMessages(user)) {
                    channel.getMessages(num).thenAccept(messages -> {
                        channel.bulkDelete(messages);
                    });
                }
            });
        }
    }
}
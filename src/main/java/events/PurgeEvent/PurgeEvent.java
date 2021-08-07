package events.PurgeEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;

public class PurgeEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (content.length == 0) {
            return;
        } else {
            int num = content[0].equalsIgnoreCase("all") ? Integer.MAX_VALUE : Integer.parseInt(content[0]);
            TextChannel channel = event.getChannel();
            event.getMessage().getUserAuthor().ifPresent(user -> {
                if(channel.canManageMessages(user)) {
                    channel.sendMessage("Deleting " + num + " messages...").thenAccept(msg -> {
                        try {
                            Thread.currentThread();
                            Thread.sleep(1000);
                            channel.getMessages(num + 1).thenAccept(msgs -> {
                                channel.bulkDelete(msgs);
                            });
                        } catch (InterruptedException e) {
                            // do nothing
                        } 
                    });
                }
            });
        }
    }
}
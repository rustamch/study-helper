package events.SimpleEvents;


import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;

public class DoraListener implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        TextChannel channel = event.getChannel();
        long time = System.currentTimeMillis();
        channel.sendMessage("O_o").thenAccept(response /* => Message */ -> {
                    response.edit("O_o: " + (System.currentTimeMillis() - time) + " ms");
                });
    }
}

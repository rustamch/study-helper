package events.SimpleEvents;


import events.BotEvent;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DoraListener implements BotEvent {

    @Override
    public void invoke(MessageReceivedEvent event, String[] content) {
        MessageChannel channel = event.getChannel();
        long time = System.currentTimeMillis();
        channel.sendMessage("O_o") /* => RestAction<Message> */
                .queue(response /* => Message */ -> {
                    response.editMessageFormat("O_o: %d ms", System.currentTimeMillis() - time).queue();
                });
    }
}

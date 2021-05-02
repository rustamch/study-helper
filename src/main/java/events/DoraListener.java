package events;


import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DoraListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        Message msg = event.getMessage();
        String rawMsg = msg.getContentRaw();
        if (rawMsg.equals("o_O")) {
            long time = System.currentTimeMillis();
            channel.sendMessage("O_o") /* => RestAction<Message> */
                    .queue(response /* => Message */ -> {
                        response.editMessageFormat("O_o: %d ms", System.currentTimeMillis() - time).queue();
                    });
        }
    }
}

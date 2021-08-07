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
            handleSubCommand(event, content);
        }
    }

    private void handleSubCommand(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        TextChannel channel = event.getChannel();
        if (subCommand.equalsIgnoreCase("all"))  {
            channel.sendMessage("You have asked to delete all the messages!");
            channel.getMessagesWhile(message -> {
                return true;
            }).thenAccept(messages -> channel.deleteMessages(messages));
        } else if (subCommand.matches("(\\d+)")) {
            int num = Integer.parseInt(subCommand);
            channel.getMessages(num + 2).thenAccept(msgs -> {
                channel.deleteMessages(msgs);
            });
        }
    }
}
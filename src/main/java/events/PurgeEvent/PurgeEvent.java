package events.PurgeEvent;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;

public class PurgeEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        if (event.getMessageAuthor().isServerAdmin()) {
            if (content.length == 0) {
                event.getChannel().sendMessage("Please specify number of messages you want to delete!");
            } else {
                handleSubCommand(event, content);
            }
        } else {
            event.getChannel().sendMessage("You need to be admin to use this command!");
        }
    }

    private void handleSubCommand(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        TextChannel channel = event.getChannel();
        if (subCommand.equalsIgnoreCase("all"))  {
            channel.sendMessage("You have asked to delete all the messages!");
            channel.getMessagesWhile(message -> true).thenAccept(channel::deleteMessages);
        } else if (subCommand.matches("(\\d+)")) {
            int num = Integer.parseInt(subCommand);
            channel.getMessages(num + 2).thenAccept(channel::deleteMessages);
        }
    }
}
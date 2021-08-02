package events.ReactionEvent;

import java.util.concurrent.TimeUnit;

import org.javacord.api.event.message.MessageCreateEvent;

import events.BotMessageEvent;
import model.Bot;

public class ReactRoleMesageEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        String messageLink = content[1];
        Bot.API.getMessageByLink(messageLink).ifPresentOrElse(
                messageIncompl -> messageIncompl.thenAccept(message -> message.getServer().ifPresent(msgServer -> {
                    if (msgServer.getOwnerId() == event.getMessageAuthor().getId()) {
                        switch (subCommand) {
                            case "addRRMsg":
                                ReactRoleMessage rrMsg = new ReactRoleMessage(message.getId());
                                rrMsg.trackMsg(msgServer.getId());
                                
                            case "addRR":
                                event.getChannel()
                                        .sendMessage(
                                                "React to this message with emoji that you want to assign to the role!")
                                        .thenAccept(reactMsg -> reactMsg.addReactionAddListener(reactEvent -> {
                                            if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                                                reactEvent.getEmoji();
                                                reactEvent.deleteMessage().join();
                                            }
                                        }).removeAfter(5, TimeUnit.MINUTES));
                                break;
                            case "rmRR":
                                event.getChannel()
                                        .sendMessage(
                                                "React to this message with emoji that you want to remove!")
                                        .thenAccept(reactMsg -> reactMsg.addReactionAddListener(reactEvent -> {
                                            if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                                                reactEvent.getEmoji();
                                                reactEvent.deleteMessage().join();
                                            }
                                        }).removeAfter(5, TimeUnit.MINUTES));
                            default:
                                break;
                        }
                    } else {
                        event.getChannel()
                                .sendMessage("You cannot setup a new reation role message on the server you don't own");
                    }
                })).exceptionally(fn -> {
                    event.getChannel().sendMessage("The link doesn't point to a valid message!");
                    return null;
                }), () -> event.getChannel().sendMessage("The link format is invalid!"));
    }
}

package events.ReactionEvent;

import events.BotMessageEvent;
import exceptions.InvalidEmojiException;
import model.Bot;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class ReactRoleMesageEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        String subCommand = content[0];
        String messageLink = content[1];
        String roleName = content[2];
        Bot.API.getMessageByLink(messageLink).ifPresentOrElse(messageIncompl ->
                messageIncompl.thenAccept(message -> message.getServer().ifPresent(msgServer -> {
                    handleUserCommand(event, message, msgServer, subCommand, roleName);
                })).exceptionally(fn -> {
                    event.getChannel().sendMessage("The link doesn't point to a valid message!");
                    return null;
                }), () -> event.getChannel().sendMessage("The link format is invalid!"));
    }

    /**
     * Handles the user command.
     *
     * @param event      A MessageCreateEvent.
     * @param message    A Message.
     * @param msgServer  A Server.
     * @param subCommand The sub command.
     * @param roleId     The snowflake id of the role.
     */
    private void handleUserCommand(MessageCreateEvent event, Message message, Server msgServer, String subCommand,
                                   String roleId) {
        event.getMessageAuthor().asUser().ifPresent(user -> {
            if (msgServer.getPermissions(user).getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
                msgServer.getRoleById(roleId).ifPresent(role -> {
                    switch (subCommand) {
                        case "add":
                            addRoleToRrMsg(event, message, role);
                            break;
                        case "rm":
                            removeRoleFromRrMsg(event, message);
                            break;
                        default:
                            event.getChannel().sendMessage("Invalid sub command!");
                            break;
                    }
                });
            } else {
                event.getChannel().sendMessage("You don't have a permission to do this!");
            }
        });
    }

    private void removeRoleFromRrMsg(MessageCreateEvent event, Message message) {
        event.getChannel().sendMessage("React to this message with emoji that you want to add!")
                .thenAccept(reactMsg -> reactMsg.addReactionAddListener(reactEvent -> {
                    if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                        try {
                            ReactRoleMessage.rmRoleFromMsg(message.getId(), reactEvent.getEmoji());
                            reactEvent.deleteMessage();
                        } catch (InvalidEmojiException e) {
                            event.getChannel().sendMessage("Emoji isn't associated with any role, try again!");
                            reactEvent.deleteMessage();
                        } catch (NoSuchElementException e) {
                            event.getChannel().sendMessage("You haven't added any reaction roles to this message");
                            reactEvent.deleteMessage();
                        }
                    }
                }).removeAfter(2, TimeUnit.MINUTES)).thenAccept(inst ->
                        event.getChannel().sendMessage("You took too long bye!"));
    }

    private void addRoleToRrMsg(MessageCreateEvent event, Message message, Role role) {
        event.getChannel().sendMessage("React to this message with emoji that you want to add!")
                .thenAccept(reactMsg -> reactMsg.addReactionAddListener(reactEvent -> {
                    if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                        try {
                            Emoji userReaction = reactEvent.getEmoji();
                            ReactRoleMessage.addRoleToMsg(message.getId(), userReaction, role.getId());
                            message.addReaction(userReaction);
                            message.removeReactionByEmoji(reactEvent.getEmoji());
                            reactEvent.deleteMessage();
                        } catch (InvalidEmojiException e) {
                            event.getChannel().sendMessage("This emote is already used, please " +
                                    "react with a different emote.");
                            reactEvent.removeReaction();
                        }
                    }
                }).removeAfter(2, TimeUnit.MINUTES)).thenAccept(inst ->
                        event.getChannel().sendMessage("You took too long bye!"));
    }

}

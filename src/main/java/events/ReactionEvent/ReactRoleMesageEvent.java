package events.ReactionEvent;

import events.BotMessageEvent;
import exceptions.InvalidEmojiException;
import model.Bot;

import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class ReactRoleMesageEvent implements BotMessageEvent {

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        Message msg = event.getMessage();
        msg.getReferencedMessage().ifPresentOrElse(refMsg -> {
            List<Role> roles = msg.getMentionedRoles();
            msg.getServer().ifPresent(msgServer -> handleUserCommand(event, refMsg, msgServer, content[0], roles));
        }, () -> event.getChannel().sendMessage("Please reply to the message you wish to attach reaction roles to!"));
    }


    /**
     * Handles the user command.
     *
     * @param event      A MessageCreateEvent.
     * @param message    A Message.
     * @param msgServer  A Server.
     * @param subCommand The sub command.
     * @param role       A Role
     */
    private void handleUserCommand(MessageCreateEvent event, Message message, Server msgServer, String subCommand,
                                   List<Role> roles) {
        event.getMessageAuthor().asUser().ifPresent(user -> {
            if (msgServer.getPermissions(user).getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
                switch (subCommand) {
                    case "add":
                        roles.forEach(role -> addRoleToRrMsg(event, message, role));
                        break;
                    case "rm":
                        removeRoleFromRrMsg(event, message);
                        break;
                    case "bulkAdd":
                        bulkAddRolesToMsg(event, message, roles, message.getCustomEmojis());
                    default:
                        event.getChannel().sendMessage("Invalid sub command!");
                        break;
                }
            } else {
                event.getChannel().sendMessage("You don't have a permission to do this!");
            }
        });
    }

    private void bulkAddRolesToMsg(MessageCreateEvent event, Message message, List<Role> roles, List<CustomEmoji> emojis) {
        for(int i = 0; i < roles.size(); i++) {
            Role role = roles.get(i);
            Emoji emoji = emojis.get(i);
            message.addReactionAddListener(reactEvent -> {
                if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                    try {
                        ReactRoleMessage.addRoleToMsg(message.getId(), emoji, role.getId());
                        message.addReaction(emoji);
                        message.removeReactionByEmoji(emoji);
                    } catch (InvalidEmojiException e) {
                        event.getChannel().sendMessage(emoji.getMentionTag() + " is already used, please " +
                                "react with a different emote.");
                        reactEvent.removeReaction();
                    }
                }
            });
        }
    }

    private void removeRoleFromRrMsg(MessageCreateEvent event, Message message) {
        event.getChannel().sendMessage("React to this message with emoji that you want to remove!")
                .thenAccept(reactMsg -> reactMsg.addReactionAddListener(reactEvent -> {
                    if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                        try {
                            ReactRoleMessage.rmRoleFromMsg(message.getId(), reactEvent.getEmoji());
                            message.removeReactionByEmoji(reactEvent.getEmoji());
                            reactEvent.deleteMessage();
                        } catch (InvalidEmojiException e) {
                            event.getChannel().sendMessage("Emoji isn't associated with any role, try again!");
                            reactEvent.deleteMessage();
                        } catch (NoSuchElementException e) {
                            event.getChannel().sendMessage("You haven't added any reaction roles to this message");
                            reactEvent.deleteMessage();
                        }
                    }
                }).removeAfter(2, TimeUnit.MINUTES).addRemoveHandler(() ->
                        event.getChannel().sendMessage("You took too long bye!")));
    }

    private void addRoleToRrMsg(MessageCreateEvent event, Message message, Role role) {
        event.getChannel().sendMessage(role.getName() + ": React to this message with emoji that you want to add!")
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
                }).removeAfter(2, TimeUnit.MINUTES).addRemoveHandler(() ->
                        event.getChannel().sendMessage("You took too long bye!")));
    }
}

package events.ReactionEvent;

import events.BotMessageEvent;
import exceptions.InvalidEmojiException;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReactRoleMessageEvent implements BotMessageEvent {

    private static final long TIMEOUT_SECONDS = 120;
    private static final long PER_ROLE_BONUS = 30;

    @Override
    public void invoke(MessageCreateEvent event, String[] content) {
        Message msg = event.getMessage();
        msg.getReferencedMessage().ifPresentOrElse(refMsg -> {
            List<Role> roles = msg.getMentionedRoles();
            msg.getServer().ifPresent(msgServer -> handleUserCommand(event, refMsg, msgServer, content[0], roles));
        }, () -> event.getChannel()
                .sendMessage("Please reply to the message you wish to attach reaction roles to!"));
    }


    /**
     * Handles the user command.
     *
     * @param event      A MessageCreateEvent.
     * @param message    A Message.
     * @param msgServer  A Server.
     * @param subCommand The sub command.
     * @param roles      A list of roles mentioned in the message.
     */
    private void handleUserCommand(MessageCreateEvent event, Message message, Server msgServer, String subCommand,
                                   List<Role> roles) {
        event.getMessageAuthor().asUser().ifPresent(user -> {
            if (msgServer.getPermissions(user).getState(PermissionType.ADMINISTRATOR) == PermissionState.ALLOWED) {
                switch (subCommand) {
                    case "add":
                        addRolesToRrMsg(event, message, roles);
                        break;
                    case "rm":
                        removeRoleFromRrMsg(event, message);
                        break;
                    default:
                        event.getChannel().sendMessage("Invalid sub command!");
                        break;
                }
            } else {
                event.getChannel().sendMessage("You don't have a permission to do this!");
            }
        });
    }

    /**
     * Removes a role reaction from ReactRoleMessage.
     *
     * @param event   A MessageCreateEvent that contains all information about the message that contains the RR request.
     * @param message The message that ReactRole needs to be added to
     */
    private void removeRoleFromRrMsg(MessageCreateEvent event, Message message) {
        event.getChannel().sendMessage("React to this message with emoji that you want to remove!")
                .thenAccept(reactMsg -> {
                    AtomicBoolean completed = new AtomicBoolean(false);
                    reactMsg.addReactionAddListener(reactEvent -> {
                        if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                            try {
                                ReactRoleMessage.rmRoleFromMsg(message.getId(), reactEvent.getEmoji());
                                message.removeReactionByEmoji(reactEvent.getEmoji());
                            } catch (InvalidEmojiException e) {
                                event.getChannel()
                                        .sendMessage("Emoji isn't associated with any role, try again!");
                                reactEvent.deleteMessage();
                            } catch (NoSuchElementException e) {
                                event.getChannel()
                                        .sendMessage("You haven't added any reaction roles to this message");
                            } finally {
                                completed.set(true);
                            }
                        }
                    }).removeAfter(2, TimeUnit.MINUTES).addRemoveHandler(() -> {
                        if (!completed.get()) {
                            event.getChannel().sendMessage("You took too long bye!");
                        }
                    });
                });
    }

    /**
     * Adds role(s) to ReactRole message.
     *
     * @param event   A MessageCreateEvent that contains all information about the message that contains the RR request.
     * @param message The message that ReactRole needs to be added to.
     * @param roles   A list of roles that need to be added to the ReactRoleMessage.
     */
    private void addRolesToRrMsg(MessageCreateEvent event, Message message, @NotNull List<Role> roles) {
        roles.forEach(role -> {
            long listenerTimeout = TIMEOUT_SECONDS + (roles.size() - 1) * PER_ROLE_BONUS;
            event.getChannel()
                    .sendMessage(role.getName() +
                            ": React to this message with emoji that you want to associate with role!")
                    .thenAccept(reactMsg -> {
                        AtomicBoolean completed = new AtomicBoolean(false);
                        reactMsg.addReactionAddListener(reactEvent -> {
                            if (reactEvent.getUserId() == event.getMessageAuthor().getId()) {
                                Emoji userReaction = reactEvent.getEmoji();
                                    message.addReaction(userReaction).thenAccept(cmpl ->  {
                                        try {
                                            ReactRoleMessage.addRoleToMsg(message.getId(), userReaction, role.getId());
                                            completed.set(true);
                                            reactEvent.deleteMessage();
                                        } catch (InvalidEmojiException e) {
                                            event.getChannel()
                                                    .sendMessage("This emoji is already used please react " +
                                                            "with a different emoji!");
                                            reactEvent.removeReaction();
                                        }
                                    }).exceptionally(e -> {
                                        event.getChannel().sendMessage("This emoji ain’t familia >:( " +
                                                "Please react with emoji that is either a standard discord emoji or " +
                                                "belongs to this server!");
                                        reactEvent.removeReaction();
                                        return null;
                                    });
                            }
                        }).removeAfter(listenerTimeout, TimeUnit.SECONDS).addRemoveHandler(() -> {
                            if (!completed.get()) {
                                event.getChannel().sendMessage("You took too long bye!");
                            }
                        });
                    });
        });
    }
}

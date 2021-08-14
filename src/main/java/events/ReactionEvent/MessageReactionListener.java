package events.ReactionEvent;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

public class MessageReactionListener implements ReactionAddListener, ReactionRemoveListener {

    @Override
    public void onReactionRemove(ReactionRemoveEvent event) {
        event.getServer().ifPresent(server -> event.getUser().ifPresent(user -> {
            Emoji userReaction = event.getEmoji();
            long messageID = event.getMessageId();
            ReactRoleMessage.loadReactRoleMessage(messageID).flatMap(rrMsg ->
                    rrMsg.getRoleIdByEmoji(userReaction)).flatMap(server::getRoleById).ifPresent(role ->
                    server.removeRoleFromUser(user, role));
        }));
    }

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        event.getServer().ifPresent(server -> event.getUser().ifPresent(user -> {
            Emoji userReaction = event.getEmoji();
            long messageID = event.getMessageId();
            ReactRoleMessage.loadReactRoleMessage(messageID).ifPresent(rrMsg ->
                     rrMsg.getRoleIdByEmoji(userReaction).ifPresentOrElse(roleId ->
                            server.getRoleById(roleId).ifPresent(role ->
                                    server.addRoleToUser(user, role)), event::removeReaction));
        }));
    }
}

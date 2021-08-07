package events.ReactionEvent;

import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
  
public class MessageReactionEvent implements ReactionAddListener, ReactionRemoveListener {

    @Override
    public void onReactionRemove(ReactionRemoveEvent event) {
        event.getServer().ifPresent(server -> {
            Emoji userReaction = event.getEmoji();
            long messageID = event.getMessageId();
            long userID = event.getUserId();
            ReactRoleMessage.checkAndRemoveRole(messageID, userReaction, server, userID);
        });
    }

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        event.getServer().ifPresent(server -> {
            Emoji userReaction = event.getEmoji();
            long messageID = event.getMessageId();
            long userID = event.getUserId();
            if (ReactRoleMessage.checkAndAddRole(messageID, userReaction, server, userID)) {


            }
        });
    }
}

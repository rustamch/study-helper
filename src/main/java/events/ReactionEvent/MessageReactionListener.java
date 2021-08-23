package events.ReactionEvent;

import events.ServerConfig;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.user.User;
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
                            server.getRoleById(roleId).ifPresent(role -> {
                                server.addRoleToUser(user, role);
                                if (ServerConfig.isStudyRole(role, server)) {
                                    switchUserToStudyMode(user);
                                }
                            }), event::removeReaction));
        }));
    }

    /**
     * Adds the StudyRole to the user on every server where the bot is present.
     *
     * @param user user that needs to be put into StudyMode.
     */
    private void switchUserToStudyMode(User user) {
        user.getMutualServers().forEach(server ->
                ServerConfig.getStudyRoleForServer(server).ifPresent(studyRole ->
                        server.addRoleToUser(user, studyRole)));
    }
}

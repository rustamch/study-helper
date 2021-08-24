package events.ReactionEvent;

import events.ServerConfig;
import model.Bot;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

import java.util.Collection;

public class MessageReactionListener implements ReactionAddListener, ReactionRemoveListener {

    @Override
    public void onReactionRemove(ReactionRemoveEvent event) {
        event.getServer().ifPresent(server -> event.getUser().ifPresent(user -> {
            Emoji userReaction = event.getEmoji();
            long messageID = event.getMessageId();
            ReactRoleMessage.loadReactRoleMessage(messageID).flatMap(rrMsg ->
                    rrMsg.getRoleIdByEmoji(userReaction)).flatMap(server::getRoleById).ifPresent(role -> {
                if (ServerConfig.isStudyRole(role.getId(), server.getId())) {
                    kickUserOutOfStudyMode(user,server);
                } else {
                    server.removeRoleFromUser(user, role);
                }
            });
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
                                if (ServerConfig.isStudyRole(role.getId(),server.getId())) {
                                    switchUserToStudyMode(user,server);
                                } else {
                                    server.addRoleToUser(user, role);
                                }
                            }), event::removeReaction));
        }));
    }

    private void kickUserOutOfStudyMode(User user, Server currServer) {
        Collection<Server> servers = user.getMutualServers();
        servers.remove(currServer);
        Bot.API.getServers().forEach(server ->
                server.getMemberById(user.getId()).ifPresent(member ->
                        ServerConfig.getStudyRoleForServer(server).ifPresent(studyRole ->
                                server.removeRoleFromUser(member, studyRole))));
    }

    /**
     * Adds the StudyRole to the user on every server where the bot is present.
     *
     * @param user user that needs to be put into StudyMode.
     */
    private void switchUserToStudyMode(User user, Server currServer) {
        Collection<Server> servers = user.getMutualServers();
        servers.remove(currServer);
        Bot.API.getServers().forEach(server ->
                server.getMemberById(user.getId()).ifPresent(member ->
                        ServerConfig.getStudyRoleForServer(server).ifPresent(studyRole ->
                                server.addRoleToUser(member, studyRole))));
    }
}

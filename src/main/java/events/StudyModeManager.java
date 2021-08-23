package events;

import model.Bot;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import java.util.Collection;

public class StudyModeManager implements UserRoleAddListener, UserRoleRemoveListener {

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        Server server = event.getServer();
        long roleId = event.getRole().getId();
        long serverId = server.getId();
        if (ServerConfig.isStudyRole(roleId, serverId)) {
            Bot.API.getOwner().thenAccept(owner ->
                    owner.sendMessage("Putting user " + event.getUser().getName() + " into study mode!"));
            switchUserToStudyMode(event.getUser(),server);
        }
    }

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        Server server = event.getServer();
        long roleId = event.getRole().getId();
        long serverId = server.getId();
        if (ServerConfig.isStudyRole(roleId, serverId)) {
            kickUserOutOfStudyMode(event.getUser(),server);
        }
    }

    private void kickUserOutOfStudyMode(User user, Server currServer) {
        Collection<Server> servers = user.getMutualServers();
        servers.remove(currServer);
        servers.forEach(server ->
                ServerConfig.getStudyRoleForServer(server).ifPresent(studyRole ->
                        server.removeRoleFromUser(user, studyRole)));
    }

    /**
     * Adds the StudyRole to the user on every server where the bot is present.
     *
     * @param user user that needs to be put into StudyMode.
     */
    private void switchUserToStudyMode(User user,Server currServer) {
        Collection<Server> servers = user.getMutualServers();
        servers.remove(currServer);
        servers.forEach(server ->
                ServerConfig.getStudyRoleForServer(server).ifPresent(studyRole ->
                        server.addRoleToUser(user, studyRole)));
    }
}
